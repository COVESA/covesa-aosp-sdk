package global.covesa.sdk.api.client

import android.content.Intent
import global.covesa.sdk.api.ICovesaCatalogRemoteService

object ServiceIntentBuilder {

    fun buildServicesCatalogBindIntent(): Intent {
        return Intent(ICovesaCatalogRemoteService.CATALOG_SERVICE_ACTION)
            .setPackage("global.covesa.sdk.server")
    }

    fun buildLightsServiceBindIntent(): Intent {
        // Same action as defined in the service intent filter in manifest
        return Intent(ICovesaCatalogRemoteService.LIGHT_SERVICE_ACTION)
            .setPackage("global.covesa.sdk.server")
    }

}
