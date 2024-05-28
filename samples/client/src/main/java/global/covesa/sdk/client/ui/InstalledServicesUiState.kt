package global.covesa.sdk.client.ui

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class InstalledServicesUiState(
    val installedServices: ImmutableList<InstalledService> = persistentListOf()
)

data class InstalledService(val intentAction: String)
