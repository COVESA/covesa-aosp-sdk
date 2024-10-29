package global.covesa.sdk.client.ui

import android.content.Context
import org.unifiedpush.android.connector.UnifiedPush

data class PushUiState (
    val registered: Boolean = false
) {
    constructor(context: Context) : this(
        registered = UnifiedPush.getAckDistributor(context) != null
    )
}