package com.tallerapp.core.license

import android.content.Context
import com.licensemanager.sdk.android.AndroidDeviceIdProvider
import com.licensemanager.sdk.android.LicenseSDK
import com.licensemanager.sdk.core.ProductConfig
import com.tallerapp.BuildConfig

/**
 * Punto único de configuración e inicialización del SDK de licencias.
 * Los valores sensibles/de producto vienen de BuildConfig (app/licensing.properties, fuera de git).
 */
object Licencia {

    /** true si la app NO exige licencia (modo desarrollo). */
    val bypass: Boolean = BuildConfig.LICENSE_DEV_BYPASS

    /** true si hay clave pública configurada (sin ella la validación siempre falla). */
    val configurada: Boolean get() = BuildConfig.LICENSE_PUBLIC_KEY.isNotBlank()

    private val productConfig = ProductConfig(
        productId = BuildConfig.LICENSE_PRODUCT_ID,
        name = "Mis Finanzas",
        publicKeyHex = BuildConfig.LICENSE_PUBLIC_KEY,
        premiumFeatures = emptySet(),
        plans = setOf("Free", "Pro", "Premium"),
        gracePeriodDays = 0,
    )

    @Volatile
    private var instancia: LicenseSDK? = null

    fun sdk(context: Context): LicenseSDK =
        instancia ?: synchronized(this) {
            instancia ?: LicenseSDK.init(context, productConfig).also { instancia = it }
        }

    fun deviceId(context: Context): String = AndroidDeviceIdProvider(context).deviceId()
}
