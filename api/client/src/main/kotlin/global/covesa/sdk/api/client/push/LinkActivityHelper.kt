package global.covesa.sdk.api.client.push

import android.app.Activity
import android.content.Intent
import org.unifiedpush.android.connector.LinkActivityHelper as ULinkActivityHelper


/**
 * Helper with functions to request the distributor's link activity for result and process the result
 *
 * ## Usage
 *
 * In your activity, define a new LinkActivityHelper, override onActivityResult to use
 * [onLinkActivityResult] then use [startLinkActivityForResult] to start activity on the
 * distributor.
 *
 * ```
 * class MyActivity: Activity() {
 *     [...]
 *     private val helper = LinkActivityHelper(this)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         if (!helper.startLinkActivityForResult()) {
 *             // No distributor found
 *         }
 *     }
 *
 *     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
 *         if (helper.onLinkActivityResult(requestCode, resultCode, data)) {
 *             // The distributor is saved, you can request registrations with UnifiedPush.registerApp now
 *         } else {
 *            // An error occurred, consider no distributor found for the moment
 *         }
 *     }
 * [...]
 * }
 * ```
 */
class LinkActivityHelper(activity: Activity) {
    private val uLinkActivityHelper = ULinkActivityHelper(activity)

    /**
     * Start distributor's link activity for result.
     *
     * @return `true` if the activity has been requested else no distributor can handle the request
     */
    fun startLinkActivityForResult(): Boolean = uLinkActivityHelper.startLinkActivityForResult()

    /**
     * Process result from the distributor's activity
     *
     * You have to call [PushManager.register] for all your registrations if this returns `true`.
     *
     * @return `true` if the [requestCode] matches the one of the request and the [resultCode]
     *  is OK and the [data] contains the PendingIntent to identify the distributor packageName.
     */
    fun onLinkActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ): Boolean = uLinkActivityHelper.onLinkActivityResult(requestCode, resultCode, data)
}