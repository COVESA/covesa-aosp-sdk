package global.covesa.sdk.api.client.push

import org.unifiedpush.android.connector.FailedReason as UFailedReason

/**
 * A [registration request][TODO()] may fail for different reasons.
 */
enum class FailedReason {
    /**
     * This is a generic error type, you can try to register again directly.
     */
    INTERNAL_ERROR,

    /**
     * The registration failed because of missing network connection, try again when network is back.
     */
    NETWORK,

    /**
     * The distributor requires a user action to work. For instance, the distributor may be log out of the push server and requires the user to log in. The user must interact with the distributor or sending a new registration will fail again.
     */
    ACTION_REQUIRED,

    /**
     * The distributor requires a VAPID key and you didn't provide one during [registration][UnifiedPush.registerApp].
     */
    VAPID_REQUIRED, ;

    companion object {
        internal fun fromUp(reason: UFailedReason): FailedReason {
            return when (reason) {
                UFailedReason.NETWORK -> NETWORK
                UFailedReason.ACTION_REQUIRED -> ACTION_REQUIRED
                UFailedReason.VAPID_REQUIRED -> VAPID_REQUIRED
                else -> INTERNAL_ERROR
            }
        }
    }
}