package global.covesa.sdk.client.push

import android.app.Activity
import global.covesa.sdk.api.client.push.PushManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ActionEvent(private val type: Type) {
    enum class Type {
        RegisterPush,
        UnregisterPush,
        SendNotification,
    }

    fun handleAction(activity: Activity) {
        when(type) {
            Type.RegisterPush -> registerPush(activity)
            Type.UnregisterPush -> PushManager.unregister(activity)
            Type.SendNotification -> FakeApplicationServer(activity).MockApi().sendNotification()
        }
    }

    private fun registerPush(activity: Activity) {
        PushManager.tryUseCurrentOrDefaultDistributor(
            activity
        ) {
            PushManager.register(
                activity,
                vapid = FakeApplicationServer(activity).MockApi().getVapidPubKey()
            )
        }
    }

    companion object {
        private val _events = MutableSharedFlow<ActionEvent>()
        val events = _events.asSharedFlow()
        suspend fun emit(event: ActionEvent) {
            _events.emit(event)
        }
    }
}