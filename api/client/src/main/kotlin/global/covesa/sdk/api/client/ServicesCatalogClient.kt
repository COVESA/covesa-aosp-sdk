package global.covesa.sdk.api.client

import android.content.Context
import android.content.Intent
import android.os.IBinder
import global.covesa.sdk.api.ICovesaCatalogRemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

class ServicesCatalogClient(
    context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CovesaServiceClient<ICovesaCatalogRemoteService>(context, coroutineScope, TAG) {
    override fun interfaceFromBinder(service: IBinder): ICovesaCatalogRemoteService =
        ICovesaCatalogRemoteService.Stub.asInterface(service)

    override fun serviceIntent(): Intent = ServiceIntentBuilder.buildServicesCatalogBindIntent()

    suspend fun getInstalledServices(): List<String> = remoteService.first()?.installedServices ?: emptyList()

    suspend fun getServiceApiVersion(): Int? = remoteService.first()?.apiVersion

    companion object {
        const val TAG = "CovesaServicesCatalogClient"
    }
}
