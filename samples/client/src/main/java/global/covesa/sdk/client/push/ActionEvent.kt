package global.covesa.sdk.client.push

import android.app.Activity
import android.util.Log
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
        Log.w(PushManager.TAG, "Handling action event $type")
        when(type) {
            Type.RegisterPush -> registerPush(activity)
            Type.UnregisterPush -> PushManager.unregister(activity)
            Type.SendNotification -> FakeApplicationServer(activity).MockApi().sendNotification()
        }
    }

    private fun registerPush(activity: Activity) {
        Log.w(TAG,"Registering push on $activity")
        PushManager.tryUseCurrentOrDefaultDistributor(
            activity
        ) { success ->
            Log.d(TAG, "Using")
            if (success) {
                val vapidPubKey = FakeApplicationServer(activity).MockApi().getVapidPubKey()
                try {
                    PushManager.register(
                        activity,
                        vapid = vapidPubKey
                    )
                    Log.w(TAG, "UnifiedPush registered successfully.")
                } catch (e: PushManager.VapidNotValidException) {
                    Log.w(TAG, "UnifiedPush failed to register with exception $e")
                }
            }
        }
    }

    companion object {
        private const val TAG = "ActionEvent"
        private val _events = MutableSharedFlow<ActionEvent>()
        val events = _events.asSharedFlow()
        suspend fun emit(event: ActionEvent) {
            _events.emit(event)
        }
    }
}