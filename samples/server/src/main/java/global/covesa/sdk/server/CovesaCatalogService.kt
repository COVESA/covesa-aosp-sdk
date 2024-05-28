package global.covesa.sdk.server

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import android.os.IBinder
import android.util.Log
import global.covesa.sdk.api.ICovesaCatalogRemoteService

class CovesaCatalogService : Service() {

    private lateinit var packageManager :PackageManager

    override fun onCreate() {
        super.onCreate()
        packageManager = application.packageManager
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = object : ICovesaCatalogRemoteService.Stub() {

        override fun getApiVersion(): Int = ICovesaCatalogRemoteService.API_VERSION

        override fun getInstalledServices(): List<String> {
            return listOf(
                ICovesaCatalogRemoteService.LIGHT_SERVICE_ACTION
            ).filter { action ->
                val resolveService = packageManager.resolveService(Intent(action), MATCH_ALL)
                Log.d(TAG, "Query for $action: $resolveService")
                resolveService?.serviceInfo?.isEnabled ?: false
            }
        }

    }
    companion object{
        private const val TAG = "CovesaCatalogService"
    }
}
