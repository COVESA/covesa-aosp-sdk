package global.covesa.sdk.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import global.covesa.sdk.api.client.LightsServiceClient
import global.covesa.sdk.api.client.ServicesCatalogClient
import global.covesa.sdk.client.ui.MainUi
import global.covesa.sdk.client.ui.theme.CovesaSDKTheme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : ComponentActivity() {

    private var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            viewModel = viewModel {
                MainViewModel(
                    context = this@MainActivity,
                    lightsServiceClient = LightsServiceClient(this@MainActivity),
                    servicesCatalogClient = ServicesCatalogClient(this@MainActivity)
                )
            }

            CovesaSDKTheme {
                MainUi(viewModel!!)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: PushSubscriptionEvent?) {
        viewModel?.refreshPushRegistration()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }
}
