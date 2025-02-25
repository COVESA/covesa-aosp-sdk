package global.covesa.sdk.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import global.covesa.sdk.api.client.LightsServiceClient
import global.covesa.sdk.api.client.ServicesCatalogClient
import global.covesa.sdk.client.push.ActionEvent
import global.covesa.sdk.client.ui.MainUi
import global.covesa.sdk.client.ui.PushUiState
import global.covesa.sdk.client.ui.theme.CovesaSDKTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private var job : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel {
                MainViewModel(
                    pushUiState = PushUiState(this@MainActivity),
                    lightsServiceClient = LightsServiceClient(this@MainActivity),
                    servicesCatalogClient = ServicesCatalogClient(this@MainActivity)
                )
            }

            CovesaSDKTheme {
                MainUi(viewModel)
            }
        }
        subscribeActions()
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        super.onDestroy()
    }

    private fun subscribeActions() {
        job = CoroutineScope(Dispatchers.IO).launch {
            ActionEvent.events.collect {
                it.handleAction(this@MainActivity)
            }
        }
    }
}
