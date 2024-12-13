package global.covesa.sdk.api.client.push

import android.content.Context
import android.util.AndroidException
import org.unifiedpush.android.connector.UnifiedPush

/**
 * Object containing functions to interact with the distributor (= push service)
 *
 * ### Use user's default distributor
 *
 * Users are allowed to define a default distributor on their system, because UnifiedPush distributors
 * have to be able to process a deeplink.
 *
 * When you set UnifiedPush for the first time on your application, you will want to use the default user's
 * distributor.
 *
 * From time to time, like every time you starts your application, you should register your application in case the
 * user have uninstalled the previous distributor.
 * If the previous distributor is uninstalled, you can fallback to the default one again.
 *
 * Therefore, you can use [tryUseCurrentOrDefaultDistributor]
 * to select the saved distributor or the default one when your application starts (when your main activity is created for instance).
 *
 * When the distributor is saved, you can call [register] to request a new registration.
 * It has optional parameters, the following example uses `messageForDistributor` and `vapid`.
 * You can use `instance` to bring multiple-registration support to your application.
 *
 * ```kotlin
 * import global.covesa.sdk.api.client.push.PushManager
 * [...]
 *
 * PushManager.tryUseCurrentOrDefaultDistributor(context) { success ->
 *     if (success) {
 *         // We have a distributor
 *         // Register your app to the distributor
 *         PushManager.register(context, messageForDistributor, vapid)
 *     }
 * }
 * ```
 *
 * Be aware that [tryUseDefaultDistributor] starts a new translucent activity in order to get the
 * result of the distributor activity. You may prefer to use [LinkActivityHelper] directly in your
 * own activity instead.
 *
 * ### Use another distributor
 *
 * You will probably want to allow the users to use another distributor but their default one.
 *
 * For this, you can get the list of available distributors with [getDistributors].
 *
 * Once the user has chosen the distributor, you have to save it with [saveDistributor]
 * This function must be called before [register].
 *
 * When the distributor is saved, you can call [register] to request a new registration.
 * It has optional parameters, the following example uses `messageForDistributor` and `vapid`.
 * You can use `instance` to bring multiple-registration support to your application.
 *
 * ```kotlin
 * import global.covesa.sdk.api.client.push.PushManager
 * [...]
 *
 * // Get a list of distributors that are available
 * val distributors = PushManager.getDistributors(context)
 * // select one or ask the user which distributor to use, eg. with a dialog
 * val userDistrib = yourFunc(distributors)
 * // save the distributor
 * PushManager.saveDistributor(context, userDistrib)
 * // register your app to the distributor
 * PushManager.register(context, messageForDistributor, vapid)
 * ```
 *
 * ### Unsubscribe
 *
 * To unsubscribe, simply call [unregister].
 * Set the instance you want to unsubscribed from if you used one during registration.
 *
 * It removes the distributor if this is the last instance to unregister.
 */
object PushManager {
    const val INSTANCE_DEFAULT = "default"

    /**
     * Request a new registration for the [instance] to the saved distributor.
     *
     * [saveDistributor] must be called before this function.
     *
     * If there was a distributor but it has been removed, [PushService.onUnregistered] will be called for all subscribed instances.
     *
     * @param [context] To interact with the shared preferences and send broadcast intents.
     * @param [instance] Registration instance. Can be used to get multiple registrations, eg. for multi-account support.
     * @param [messageForDistributor] May be shown by the distributor UI to identify this registration.
     * @param [vapid] VAPID public key ([RFC8292](https://www.rfc-editor.org/rfc/rfc8292)) base64url encoded of the uncompressed form (87 chars long).
     *
     * @throws [VapidNotValidException] if [vapid] is not in the in the uncompressed form and base64url encoded.
     */
    @Throws(VapidNotValidException::class)
    fun register(
        context: Context,
        instance: String = INSTANCE_DEFAULT,
        messageForDistributor: String? = null,
        vapid: String? = null,
    ) {
        try {
            UnifiedPush.registerApp(context, instance, messageForDistributor, vapid)
        } catch (e: UnifiedPush.VapidNotValidException) {
            throw VapidNotValidException()
        }
    }

    /**
     * Send an unregistration request for the [instance] to the saved distributor and remove the registration. Remove the distributor if this is the last instance registered.
     *
     * [PushService.onUnregistered] won't be called after that request.
     *
     * @param [context] To interact with the shared preferences and send broadcast intents.
     * @param [instance] Registration instance. Can be used to get multiple registrations, eg. for multi-account support.
     */
    fun unregister(
        context: Context,
        instance: String = INSTANCE_DEFAULT,
    ) {
        UnifiedPush.unregisterApp(context, instance)
    }

    /**
     * Get a list of available distributors installed on the system
     *
     * @return The list of distributor's package name
     */
    fun getDistributors(context: Context): List<String> {
        return UnifiedPush.getDistributors(context)
    }

    /**
     * Try to use the distributor opening the deeplink "unifiedpush://link"
     *
     * It allows users to define a default distributor for all their applications
     *
     * **External distributors will be favored over embedded distributors.**
     *
     * Be aware that this function starts a new translucent activity in order to
     * get the result of the distributor activity. You may prefer to use [LinkActivityHelper]
     * directly in your own activity instead.
     *
     * ## Usage
     *
     * ```kotlin
     * tryUseDefaultDistributor(context) { success ->
     *     if (success) {
     *         //TODO: registerApp
     *     }
     * }
     * ```
     *
     * @param [context] Must be an activity or it will fail and the callback will be called with `false`
     * @param [callback] is a function taking a Boolean as parameter. This boolean is
     * true if the registration using the deeplink succeeded.
     */
    fun tryUseDefaultDistributor(
        context: Context,
        callback: (Boolean) -> Unit,
    ) {
        UnifiedPush.tryUseDefaultDistributor(context, callback)
    }

    /**
     * Try to use the saved distributor else, use the default distributor opening the deeplink "unifiedpush://link"
     *
     * It can be used on application startup to register to the distributor.
     * If you had already registered to a distributor, this ensure the connection is working.
     * If the previous distributor has been uninstalled, it will fallback to the user's default.
     * If you register for the first time, it will use the user's default Distributor or the OS will
     * ask what it should use.
     *
     * **External distributors will be favored over embedded distributors.**
     *
     * Be aware that this function may start a new translucent activity in order to
     * get the result of the distributor activity. You may prefer to use [LinkActivityHelper]
     * directly in your own activity instead.
     *
     * ## Usage
     *
     * ```kotlin
     * tryUseCurrentOrDefaultDistributor(context) { success ->
     *     if (success) {
     *         //TODO: registerApp
     *     }
     * }
     * ```
     *
     * @param [context] Must be an activity or it will fail if there is no current distributor and the callback will be called with `false`
     * @param [callback] is a function taking a Boolean as parameter. This boolean is
     * true if the registration using the deeplink succeeded.
     */
    fun tryUseCurrentOrDefaultDistributor(context: Context, callback: (Boolean) -> Unit) {
        return UnifiedPush.tryUseCurrentOrDefaultDistributor(context, callback)
    }

    /**
     * Save [distributor] as the new distributor to use
     *
     * @param [context] To interact with the shared preferences.
     * @param [distributor] The distributor package name
     */
    fun saveDistributor(context: Context, distributor: String) {
        UnifiedPush.saveDistributor(context, distributor)
    }

    /**
     * Get the distributor registered by the user, but the
     * distributor may not have respond yet to our requests. Most of the time [getAckDistributor] is preferred.
     *
     * Will call [PushService.onUnregistered] for all instances if the distributor
     * is not installed anymore.
     *
     * @return The distributor package name if any, else null
     */
    fun getSavedDistributor(context: Context): String? = UnifiedPush.getSavedDistributor(context)

    /**
     * Get the distributor registered by the user, and the
     * distributor has already respond to our requests
     *
     * Will call [PushService.onUnregistered] for all instances if the distributor
     * is not installed anymore.
     *
     * @return The distributor package name if any, else null
     */
    fun getAckDistributor(context: Context): String? = UnifiedPush.getAckDistributor(context)


    /**
     * Unregister all instances and remove the distributor
     *
     * @param [context] To interact with the shared preferences and send broadcast intents.
     */
    fun removeDistributor(context: Context) = UnifiedPush.forceRemoveDistributor(context)

    /**
     * The VAPID public key is not in the right format.
     *
     * It should be in the uncompressed form, and base64url encoded (87 chars long)
     */
    class VapidNotValidException : AndroidException()
}