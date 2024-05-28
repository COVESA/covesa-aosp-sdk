package global.covesa.sdk.client.ui

import global.covesa.sdk.api.lights.LightState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LightsUiState(
    val installedServiceVersion: LightServiceVersion = LightServiceVersion.NotInstalled,
    val lights: ImmutableList<LightState> = persistentListOf()
)

sealed class LightServiceVersion {
    data object NotInstalled : LightServiceVersion()
    data class Installed(val version: Int) : LightServiceVersion()
}
