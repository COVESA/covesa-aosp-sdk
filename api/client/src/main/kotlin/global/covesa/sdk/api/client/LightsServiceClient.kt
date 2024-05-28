package global.covesa.sdk.api.client

import android.content.Context
import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import global.covesa.sdk.api.lights.ICovesaLightsRemoteService
import global.covesa.sdk.api.lights.ILightsStateListener
import global.covesa.sdk.api.lights.LightState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

class LightsServiceClient(
    context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CovesaServiceClient<ICovesaLightsRemoteService>(context, coroutineScope, TAG) {
    override fun interfaceFromBinder(service: IBinder): ICovesaLightsRemoteService =
        ICovesaLightsRemoteService.Stub.asInterface(service)

    override fun serviceIntent(): Intent = ServiceIntentBuilder.buildLightsServiceBindIntent()

    val lightsStates: Flow<List<LightState>> = remoteService.flatMapLatest { service ->
        if (service != null) {
            callbackFlow {
                Log.d(TAG, "getMessages() called")
                val listener = object : ILightsStateListener.Stub() {
                    override fun onLightsStateUpdate(states: MutableList<LightState>?) {
                        if (states != null) {
                            trySend(states)
                        }
                    }
                }
                Log.d(TAG, "register messages callback")
                service.registerLightsStateListener(listener)
                awaitClose {
                    Log.d(TAG, "unregister messages callback")
                    try {
                        service.unregisterLightsStateListener(listener)
                    } catch (e: DeadObjectException) {
                        Log.w(TAG, "service died before unregistering lights listener")
                    }
                }
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setInternalLight(lightState: LightState) {
        remoteService.first()?.setInternalLight(lightState)
    }

    suspend fun getServiceApiVersion(): Int? = remoteService.first()?.apiVersion

    companion object {
        const val TAG = "CovesaLightsServiceClient"
    }
}
