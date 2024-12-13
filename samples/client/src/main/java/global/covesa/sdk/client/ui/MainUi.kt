package global.covesa.sdk.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import global.covesa.sdk.api.lights.LightColor
import global.covesa.sdk.api.lights.LightState
import global.covesa.sdk.client.MainViewModel
import kotlin.random.Random

@Composable
fun MainUi(viewModel: MainViewModel) {
    val lightsUiState = viewModel.lightsUiState
    val installedServicesUiState = viewModel.installedServicesUiState
    val pushUiState = viewModel.pushUiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = installedServicesUiState.installedServices
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n", prefix = "Installed services:\n") {
                    "• ${it.intentAction}"
                }
                ?: "No COVESA services installed",
            modifier = Modifier.padding(top = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        Text(
            text = when (lightsUiState.installedServiceVersion) {
                is LightServiceVersion.Installed -> "Lights service version ${lightsUiState.installedServiceVersion.version}"
                LightServiceVersion.NotInstalled -> "Lights service not installed"
            },
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            enabled = lightsUiState.installedServiceVersion is LightServiceVersion.Installed,
            modifier = Modifier.padding(top = 32.dp),
            onClick = {
                viewModel.setInternalLight(
                    LightState().apply {
                        zone = LightState.ALL_ZONES
                        color = LightColor().apply {
                            r = Random.nextInt(255)
                            g = Random.nextInt(255)
                            b = Random.nextInt(255)
                        }
                        brightness = 255
                    }
                )
            }
        ) {
            Text(text = "Set internal lights")
        }
        if (lightsUiState.installedServiceVersion is LightServiceVersion.Installed) {
            LightStates(lightsUiState.lights)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        if (pushUiState.registered) {
            Text(
                text = "Registered to push service.",
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    viewModel.unregisterPushService()
                }
            ) {
                Text(text = "Unregister")
            }
            Button(
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    viewModel.sendPushNotification()
                }
            ) {
                Text(text = "Notify")
            }
        } else {
            Text(
                text = "Not registered to push service.",
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    viewModel.registerPushService()
                }
            ) {
                Text(text = "Register")
            }
        }
    }
}


@Composable
private fun LightStates(
    allLights: List<LightState>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        item {
            Text(
                text = "Current lights:\n",
                style = MaterialTheme.typography.headlineSmall,
                modifier = modifier
            )
        }
        items(
            items = allLights,
            key = { it.zone },
            contentType = { it }
        ) {
            LightStateRow(light = it)
        }
    }
}

@Composable
private fun LightStateRow(
    light: LightState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Zone: ${zoneName(light.zone)}\n" +
                    "Brightness: ${light.brightness}",
            modifier = modifier
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(light.color.r, light.color.g, light.color.b))
        )
    }
}

private fun zoneName(zoneId: Int) = when (zoneId) {
    LightState.ALL_ZONES -> "All zones"
    LightState.ZONE_DRIVER -> "Driver"
    LightState.ZONE_PASSENGER -> "Passenger"
    LightState.ZONE_REAR_LEFT -> "Rear left"
    LightState.ZONE_REAR_CENTER -> "Rear center"
    LightState.ZONE_REAR_RIGHT -> "Rear right"
    else -> "Unknown zone $zoneId"
}
