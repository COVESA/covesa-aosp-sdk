package global.covesa.sdk.api.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn

abstract class CovesaServiceClient<T>(
    context: Context,
    coroutineScope: CoroutineScope,
    TAG: String
) {
    protected abstract fun interfaceFromBinder(service: IBinder): T
    protected abstract fun serviceIntent(): Intent

    private var serviceConnection: ServiceConnection? = null

    protected var remoteService = callbackFlow<T?> {
        try {
            check(serviceConnection == null) { "service is already connected" }

            Log.i(TAG, "connecting to remote service")
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    trySend(interfaceFromBinder(service))
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    cancel("connection to service lost")
                }

                override fun onNullBinding(name: ComponentName?) {
                    Log.i(TAG, "received null binding from remote service")
                    cancel(
                        "connection to remote service failed",
                        NullPointerException("received null binding from remote service")
                    )
                }

                override fun onBindingDied(name: ComponentName?) {
                    Log.i(TAG, "connection died from remote service")
                    cancel("connection to remote service lost")
                }
            }

            val serviceBound = context.bindService(
                serviceIntent(),
                serviceConnection!!,
                Context.BIND_AUTO_CREATE
            )
            check(serviceBound) { "Service was not found" }
        } catch (e: SecurityException) {
            cancel("connection to remote service failed", e)
            Log.w(TAG, "could not connect to remote service", e)
            serviceConnection = null
        } catch (e: IllegalStateException) {
            cancel("connection to remote service failed", e)
            Log.w(TAG, e.message, e)
        } catch (e: Exception) {
            Log.w(TAG, e.message, e)
        }

        awaitClose {
            Log.d(TAG, "disconnecting from remote service")
            if (serviceConnection != null) {
                context.unbindService(serviceConnection!!)
                serviceConnection = null
            } else {
                Log.w(TAG, "not yet connected to remote service")
            }
        }
    }.retry(3) {
        Log.d(TAG, "retry: will retry connection after delay")
        delay(1_000L)
        return@retry true
    }
        .catch { emit(null) }
        .shareIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(1_000L, 0),
            1
        )

}
