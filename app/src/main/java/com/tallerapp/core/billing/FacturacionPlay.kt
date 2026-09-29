package com.tallerapp.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Una opción de compra de Pro, con el precio ya formateado por Play en la moneda del usuario. */
data class OfertaPro(
    val idPlanBase: String,
    val etiqueta: String,
    val precio: String,
    val periodo: String,
    val diasPrueba: Int,
    /** Duración del período pagado en días (30 el mensual, 365 el anual). */
    val diasPeriodo: Int,
    internal val offerToken: String,
)

/**
 * Cobro de la suscripción Pro con Google Play Billing.
 *
 * Quien decide si el usuario es Pro es Play, no la app: al conectar y ante cada compra se
 * consultan las compras activas y se escribe el resultado en [EstadoPlan]. Nada se guarda
 * en preferencias porque sería trivial de falsear.
 *
 * Los precios **nunca** se escriben en el código: los devuelve Play ya localizados.
 */
class FacturacionPlay(context: Context) {

    companion object {
        private const val TAG = "FacturacionPlay"

        /** Id del producto de suscripción creado en Play Console. */
        const val PRODUCTO_PRO = "pro"

        const val PLAN_MENSUAL = "mensual"
        const val PLAN_ANUAL = "anual"

        private const val CLAVE_PLAN_COMPRADO = "plan_base_comprado"
        private const val CLAVE_DIAS_PERIODO = "plan_dias_periodo"
        private const val CLAVE_DIAS_PRUEBA = "plan_dias_prueba"

        /** Si no se sabe qué plan compró, se asume el mensual: es el más corto y el más común. */
        private const val DIAS_PERIODO_POR_DEFECTO = 30
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Guarda **cuál** plan compró el usuario (mensual o anual), no si es Pro.
     *
     * Play no dice a qué plan base corresponde una compra, y sin eso no se sabe si el
     * período dura 30 o 365 días. Falsear este valor solo desajusta la barra de días: el
     * acceso a Pro lo sigue decidiendo Play (ver [EstadoPlan]).
     */
    private val prefs = context.applicationContext
        .getSharedPreferences("mis_finanzas_prefs", Context.MODE_PRIVATE)

    private val _ofertas = MutableStateFlow<List<OfertaPro>>(emptyList())
    val ofertas: StateFlow<List<OfertaPro>> = _ofertas.asStateFlow()

    /** True mientras hay una compra aprobada pero todavía no confirmada por Play. */
    private val _compraPendiente = MutableStateFlow(false)
    val compraPendiente: StateFlow<Boolean> = _compraPendiente.asStateFlow()

    private val listener = PurchasesUpdatedListener { resultado, compras ->
        when (resultado.responseCode) {
            BillingClient.BillingResponseCode.OK -> procesar(compras.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> Log.w(TAG, "Compra fallida: ${resultado.debugMessage}")
        }
    }

    private val cliente = BillingClient.newBuilder(context)
        .setListener(listener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    /** Conecta con Play y sincroniza el estado. Llamar al iniciar la app. */
    fun conectar() {
        if (cliente.isReady) {
            scope.launch { sincronizar() }
            return
        }
        cliente.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(resultado: BillingResult) {
                if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        sincronizar()
                        cargarOfertas()
                    }
                } else {
                    Log.w(TAG, "No se pudo conectar con Play: ${resultado.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Play reconecta en el próximo intento; no se degrada el plan por esto.
                Log.w(TAG, "Servicio de Play desconectado")
            }
        })
    }

    /**
     * Consulta las compras activas y actualiza [EstadoPlan].
     * Esto es también el "restaurar compras": corre al iniciar, así una reinstalación o un
     * teléfono nuevo recuperan Pro sin que el usuario haga nada.
     */
    private suspend fun sincronizar() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val resultado = cliente.queryPurchasesAsync(params)
        procesar(resultado.purchasesList)
    }

    private fun procesar(compras: List<Purchase>) {
        val activas = compras.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        _compraPendiente.value = compras.any { it.purchaseState == Purchase.PurchaseState.PENDING }

        // Solo PURCHASED habilita Pro. PENDING no: en Argentina el pago en efectivo puede
        // tardar días y entregar antes sería regalar la suscripción.
        val vigente = activas.firstOrNull()
        EstadoPlan.actualizar(
            if (vigente != null) Plan.PRO else Plan.GRATIS,
            calcularSuscripcion(vigente),
        )

        // Play reembolsa automáticamente toda compra no confirmada en 3 días.
        activas.filterNot { it.isAcknowledged }.forEach { compra ->
            scope.launch {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(compra.purchaseToken)
                    .build()
                val r = cliente.acknowledgePurchase(params)
                if (r.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "No se pudo confirmar la compra: ${r.debugMessage}")
                }
            }
        }
    }

    private suspend fun cargarOfertas() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCTO_PRO)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                ),
            )
            .build()

        val resultado = cliente.queryProductDetails(params)
        val producto = resultado.productDetailsList?.firstOrNull() ?: return
        // Play devuelve una entrada por cada oferta del plan base y otra por el plan base
        // pelado, así que un plan con prueba gratuita llega dos veces. Se agrupa y se elige
        // la de prueba: mostrar las dos daba un botón que compraba sin los días gratis.
        _ofertas.value = producto.subscriptionOfferDetails.orEmpty()
            .groupBy { it.basePlanId }
            .mapNotNull { (idPlanBase, delPlan) ->
                val oferta = delPlan.firstOrNull { o ->
                    o.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
                } ?: delPlan.firstOrNull() ?: return@mapNotNull null

                val fase = oferta.pricingPhases.pricingPhaseList
                    .lastOrNull() ?: return@mapNotNull null
                val prueba = oferta.pricingPhases.pricingPhaseList
                    .firstOrNull { it.priceAmountMicros == 0L }
                val anual = idPlanBase == PLAN_ANUAL

                OfertaPro(
                    idPlanBase = idPlanBase,
                    etiqueta = if (anual) "Anual" else "Mensual",
                    precio = fase.formattedPrice,
                    periodo = if (anual) "por año" else "por mes",
                    diasPrueba = prueba?.billingPeriod?.let(::diasDe) ?: 0,
                    diasPeriodo = diasDe(fase.billingPeriod).takeIf { it > 0 }
                        ?: if (anual) 365 else 30,
                    offerToken = oferta.offerToken,
                )
            }
            // El orden en que Play devuelve los planes no está garantizado.
            .sortedBy { if (it.idPlanBase == PLAN_ANUAL) 0 else 1 }
            .also { if (it.isEmpty()) Log.w(TAG, "Producto $PRODUCTO_PRO sin ofertas") }
    }

    /** Abre la pantalla de pago de Google. */
    fun comprar(activity: Activity, oferta: OfertaPro) {
        // Se anota antes de abrir el flujo: si el usuario compra, la compra que devuelve Play
        // no dice de qué plan base es y la barra de días necesita saber cuánto dura.
        prefs.edit()
            .putString(CLAVE_PLAN_COMPRADO, oferta.idPlanBase)
            .putInt(CLAVE_DIAS_PERIODO, oferta.diasPeriodo)
            .putInt(CLAVE_DIAS_PRUEBA, oferta.diasPrueba)
            .apply()
        scope.launch {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCTO_PRO)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    ),
                )
                .build()
            val producto: ProductDetails =
                cliente.queryProductDetails(params).productDetailsList?.firstOrNull() ?: return@launch

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(producto)
                            .setOfferToken(oferta.offerToken)
                            .build(),
                    ),
                )
                .build()
            cliente.launchBillingFlow(activity, flowParams)
        }
    }

    /**
     * Deduce cuánto le queda al período en curso.
     *
     * Play Billing no informa el vencimiento de una suscripción: solo `purchaseTime` y si
     * la renovación automática sigue activa. Así que se arranca de la compra, se suman los
     * días de prueba (si todavía está corriendo) y después se avanza de período en período
     * hasta pasar el día de hoy. Eso vale igual si Play actualizó `purchaseTime` en la
     * última renovación o si dejó el de la compra original.
     *
     * Es una estimación declarada como tal en [Suscripcion]; el acceso lo decide Play.
     */
    private fun calcularSuscripcion(compra: Purchase?): Suscripcion {
        if (compra == null) return Suscripcion.GRATIS

        val ahora = System.currentTimeMillis()
        val diasPeriodo = prefs.getInt(CLAVE_DIAS_PERIODO, DIAS_PERIODO_POR_DEFECTO)
            .coerceAtLeast(1)
        val diasPrueba = prefs.getInt(CLAVE_DIAS_PRUEBA, 0).coerceAtLeast(0)
        val inicio = compra.purchaseTime

        val finPrueba = inicio + TimeUnit.DAYS.toMillis(diasPrueba.toLong())
        if (diasPrueba > 0 && ahora < finPrueba) {
            return Suscripcion(
                estado = EstadoSuscripcion.PRUEBA,
                diasRestantes = Suscripcion.diasHasta(finPrueba, ahora),
                diasTotales = diasPrueba,
                finMillis = finPrueba,
            )
        }

        val periodoMillis = TimeUnit.DAYS.toMillis(diasPeriodo.toLong())
        var fin = if (diasPrueba > 0) finPrueba else inicio
        // Cota: el bucle avanza períodos completos, así que termina siempre.
        while (fin <= ahora) fin += periodoMillis

        return Suscripcion(
            estado = if (compra.isAutoRenewing) EstadoSuscripcion.ACTIVA else EstadoSuscripcion.CANCELADA,
            diasRestantes = Suscripcion.diasHasta(fin, ahora),
            diasTotales = diasPeriodo,
            finMillis = fin,
        )
    }

    /** Convierte un período ISO-8601 de Play ("P2W", "P14D", "P1M") a días. */
    private fun diasDe(periodoIso: String): Int {
        val n = periodoIso.filter { it.isDigit() }.toIntOrNull() ?: return 0
        return when {
            periodoIso.endsWith("D") -> n
            periodoIso.endsWith("W") -> n * 7
            periodoIso.endsWith("M") -> n * 30
            periodoIso.endsWith("Y") -> n * 365
            else -> 0
        }
    }
}
