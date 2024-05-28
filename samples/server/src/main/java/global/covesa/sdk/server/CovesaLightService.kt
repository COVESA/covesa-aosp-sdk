package global.covesa.sdk.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import global.covesa.sdk.api.lights.ICovesaLightsRemoteService
import global.covesa.sdk.api.lights.ILightsStateListener
import global.covesa.sdk.api.lights.LightState

class CovesaLightService : Service() {

    private val lightsListeners = mutableListOf<ILightsStateListener>()

    private var currentLights = mutableMapOf<Int, LightState>()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = object : ICovesaLightsRemoteService.Stub() {

        override fun getApiVersion(): Int = ICovesaLightsRemoteService.API_VERSION

        override fun setInternalLight(lightState: LightState?) {
            if (lightState == null) {
                return
            } else {
                // this is just for debug / demo purposes
                // in a production service here we should communicate with VHAL
                logReceivedState(lightState)

                if (lightState.zone == LightState.ALL_ZONES) {
                    // if the client sets a value for all zones, then we just keep that one
                    currentLights.clear()
                }

                currentLights[lightState.zone] = lightState

                lightsListeners.forEach {
                    it.onLightsStateUpdate(
                        currentLights.values.toList()
                    )
                }
            }
        }

        override fun registerLightsStateListener(listener: ILightsStateListener?) {
            if (listener == null) {
                return
            } else {
                lightsListeners.add(listener)

                // Immediately notify the new listener about the current lights states
                listener.onLightsStateUpdate(currentLights.values.toList())
            }
        }

        override fun unregisterLightsStateListener(listener: ILightsStateListener?) {
            if (listener == null) {
                return
            } else {
                lightsListeners.remove(listener)
            }
        }
    }

    private fun logReceivedState(lightState: LightState) {
        val colorString = lightState.color?.let { "r:${it.r},g:${it.g},b:${it.b}," }
        val message = "Received light. Zone: ${lightState.zone} Color: $colorString Brightness: ${lightState.brightness}"

        Log.d(TAG, message)
        val handler = Handler(applicationContext.mainLooper)
        handler.post(Runnable {
            Toast.makeText(
                this@CovesaLightService,
                message,
                Toast.LENGTH_LONG
            ).show()
        })
    }

    companion object{
        private const val TAG = "CovesaLightService"
    }
}
