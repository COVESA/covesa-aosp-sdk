package global.covesa.sdk.client

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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val lightsServiceClient: LightsServiceClient,
    private val servicesCatalogClient: ServicesCatalogClient
) : ViewModel() {
    var lightsUiState by mutableStateOf(LightsUiState())
        private set

    var installedServicesUiState by mutableStateOf(InstalledServicesUiState())
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
    }

    fun setInternalLight(lightState: LightState) {
        viewModelScope.launch {
            lightsServiceClient.setInternalLight(lightState)
        }
    }
}
