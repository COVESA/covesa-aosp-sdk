package global.covesa.sdk.client

import android.app.Activity
import global.covesa.sdk.api.client.push.PushManager

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
            Type.SendNotification -> MockApplicationServer(activity).MockApi().sendNotification()
        }
    }

    private fun registerPush(activity: Activity) {
        PushManager.tryUseCurrentOrDefaultDistributor(
            activity
        ) {
            PushManager.register(
                activity,
                vapid = MockApplicationServer(activity).MockApi().getVapidPubKey()
            )
        }
    }
}