package global.covesa.sdk.client.ui

import android.content.Context
import global.covesa.sdk.api.client.push.PushManager

data class PushUiState(
    val registered: Boolean = false,
    val pushDistributor: String = "",
) {
    constructor(context: Context) : this(
        registered = PushManager.getAckDistributor(context) != null,
        pushDistributor =  PushManager.getAckDistributor(context).toString(),
    )
}