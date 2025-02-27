package global.covesa.sdk.client.ui

import android.content.Context
import global.covesa.sdk.api.client.push.PushManager

data class PushUiState(
    val registered: Boolean = false,
    val pushDistributor: String = "",
    val availableDistributors: List<String> = emptyList(),
    val selectedDistributor: String = "",
    val savedDistributor: String = "",
) {
    constructor(context: Context) : this(
        registered = PushManager.getAckDistributor(context) != null,
        pushDistributor = PushManager.getAckDistributor(context).toString(),
        availableDistributors = PushManager.getDistributors(context),
        selectedDistributor = if (PushManager.getDistributors(context).size > 1) PushManager.getDistributors(
            context
        ).first() else "",
        savedDistributor = PushManager.getSavedDistributor(context).toString(),
    )
}