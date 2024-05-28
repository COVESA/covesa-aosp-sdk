package global.covesa.sdk.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import global.covesa.sdk.api.client.LightsServiceClient
import global.covesa.sdk.api.client.ServicesCatalogClient
import global.covesa.sdk.client.ui.MainUi
import global.covesa.sdk.client.ui.theme.CovesaSDKTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel {
                MainViewModel(
                    lightsServiceClient = LightsServiceClient(this@MainActivity),
                    servicesCatalogClient = ServicesCatalogClient(this@MainActivity)
                )
            }

            CovesaSDKTheme {
                MainUi(viewModel)
            }
        }
    }
}
