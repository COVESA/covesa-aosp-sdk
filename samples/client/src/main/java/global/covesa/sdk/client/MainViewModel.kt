package global.covesa.sdk.client

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import global.covesa.sdk.api.client.LightsServiceClient
import global.covesa.sdk.api.client.ServicesCatalogClient
import global.covesa.sdk.api.lights.LightState
import global.covesa.sdk.client.ui.InstalledService
import global.covesa.sdk.client.ui.InstalledServicesUiState
import global.covesa.sdk.client.ui.LightServiceVersion
import global.covesa.sdk.client.ui.LightsUiState
import global.covesa.sdk.client.ui.PushUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.UnifiedPush

class MainViewModel(
    private val context: Context,
    private val lightsServiceClient: LightsServiceClient,
    private val servicesCatalogClient: ServicesCatalogClient
) : ViewModel() {
    var lightsUiState by mutableStateOf(LightsUiState())
        private set

    var installedServicesUiState by mutableStateOf(InstalledServicesUiState())
        private set

    var pushUiState by mutableStateOf(PushUiState())
        private set

    init {
        lightsServiceClient.lightsStates
            .onEach { updatedLightsList ->
                lightsUiState = lightsUiState.copy(lights = updatedLightsList.toImmutableList())
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            installedServicesUiState = installedServicesUiState.copy(
                installedServices = servicesCatalogClient.getInstalledServices()
                    .map { action -> InstalledService(action) }
                    .toImmutableList()
            )
        }

        viewModelScope.launch {
            val serviceApiVersion = lightsServiceClient.getServiceApiVersion()
            lightsUiState = lightsUiState.copy(
                installedServiceVersion = if (serviceApiVersion == null) {
                    LightServiceVersion.NotInstalled
                } else {
                    LightServiceVersion.Installed(serviceApiVersion)
                }
            )
        }

        refreshPushRegistration()
    }

    fun setInternalLight(lightState: LightState) {
        viewModelScope.launch {
            lightsServiceClient.setInternalLight(lightState)
        }
    }

    fun registerPushService() {
        viewModelScope.launch {
            UnifiedPush.tryUseDefaultDistributor(context) {
                UnifiedPush.registerApp(context)
            }
        }
    }

    fun unregisterPushService() {
        viewModelScope.launch {
            UnifiedPush.unregisterApp(context)
            pushUiState = pushUiState.copy(registered = false)
        }
    }

    fun refreshPushRegistration() {
        viewModelScope.launch {
            val registered = UnifiedPush.getAckDistributor(context) != null
            pushUiState = pushUiState.copy(registered = registered)
        }
    }
}
