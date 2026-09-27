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

/** Una opción de compra de Pro, con el precio ya formateado por Play en la moneda del usuario. */
data class OfertaPro(
    val idPlanBase: String,
    val etiqueta: String,
    val precio: String,
    val periodo: String,
    val diasPrueba: Int,
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
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        EstadoPlan.actualizar(if (activas.isNotEmpty()) Plan.PRO else Plan.GRATIS)

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
        _ofertas.value = producto.subscriptionOfferDetails.orEmpty().mapNotNull { oferta ->
            val fase = oferta.pricingPhases.pricingPhaseList.lastOrNull() ?: return@mapNotNull null
            val prueba = oferta.pricingPhases.pricingPhaseList
                .firstOrNull { it.priceAmountMicros == 0L }
            OfertaPro(
                idPlanBase = oferta.basePlanId,
                etiqueta = if (oferta.basePlanId == PLAN_ANUAL) "Anual" else "Mensual",
                precio = fase.formattedPrice,
                periodo = if (oferta.basePlanId == PLAN_ANUAL) "por año" else "por mes",
                diasPrueba = prueba?.billingPeriod?.let(::diasDe) ?: 0,
                offerToken = oferta.offerToken,
            )
        }.also { if (it.isEmpty()) Log.w(TAG, "Producto $PRODUCTO_PRO sin ofertas") }
    }

    /** Abre la pantalla de pago de Google. */
    fun comprar(activity: Activity, oferta: OfertaPro) {
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
