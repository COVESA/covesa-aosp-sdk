package global.covesa.sdk.client

import android.app.Activity
import org.unifiedpush.android.connector.UnifiedPush

class ActionEvent(private val type: Type) {
    enum class Type {
        RegisterPush,
        UnregisterPush,
        SendNotification,
    }

    fun handleAction(activity: Activity) {
        when(type) {
            Type.RegisterPush -> registerPush(activity)
            Type.UnregisterPush -> UnifiedPush.unregisterApp(activity)
            Type.SendNotification -> MockApplicationServer(activity).MockApi().sendNotification()
        }
    }

    private fun registerPush(activity: Activity) {
        UnifiedPush.tryUseCurrentOrDefaultDistributor(
            activity
        ) {
            UnifiedPush.registerApp(
                activity,
                vapid = MockApplicationServer(activity).MockApi().getVapidPubKey()
            )
        }
    }
}