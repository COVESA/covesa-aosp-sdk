package global.covesa.sdk.api.client.internal

import android.content.Context
import android.content.Intent
import android.os.IBinder
import global.covesa.sdk.api.client.CovesaServiceClient
import global.covesa.sdk.api.client.push.PushService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * This is used to connect to the service implementing [PushService]
 */
internal class InternalPushServiceClient(
    context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CovesaServiceClient<PushService.PushBinder>(context, coroutineScope, TAG) {

    class ServiceNotFoundException(e: String = "Service not found") : Exception(e)

    private val packageName = context.packageName

    override fun interfaceFromBinder(service: IBinder): PushService.PushBinder {
        return service as PushService.PushBinder
    }

    override fun serviceIntent(): Intent {
        return Intent().apply {
            action = PushService.ACTION_PUSH_EVENT
            `package` = packageName
        }
    }

    private suspend fun getService(): PushService {
        return remoteService.first()?.getService() ?: throw ServiceNotFoundException()
    }

    companion object {
        private const val TAG = "InternalPushServiceClient"
        private var service : PushService? = null
        internal suspend fun getService(context: Context) : PushService {
            return service ?: InternalPushServiceClient(context.applicationContext).getService().also {
                service = it
            }
        }
    }
}