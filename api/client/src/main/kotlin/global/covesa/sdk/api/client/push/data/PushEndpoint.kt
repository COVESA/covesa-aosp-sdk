package global.covesa.sdk.api.client.push.data

import android.os.Parcel
import android.os.Parcelable
import org.unifiedpush.android.connector.data.PushEndpoint as UPushEndpoint

/**
 * Contains the push endpoint and the associated [PublicKeySet].
 */
class PushEndpoint(
    /** URL to push notifications to. */
    val url: String,
    /** Web Push public key set. */
    val pubKeySet: PublicKeySet?,
) : Parcelable {

    internal constructor(uPushEndpoint: UPushEndpoint) : this(
        uPushEndpoint.url,
        uPushEndpoint.pubKeySet?.let { PublicKeySet(it) }
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(url)
        parcel.writeInt(pubKeySet?.let { 1 } ?: 0)
        pubKeySet?.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PushEndpoint> {
        override fun createFromParcel(parcel: Parcel): PushEndpoint? {
            val url = parcel.readString()
            val pubKeySet =
                if (parcel.readInt() == 1) {
                    PublicKeySet.createFromParcel(parcel)
                } else {
                    null
                }
            return PushEndpoint(
                url ?: return null,
                pubKeySet,
            )
        }

        override fun newArray(size: Int): Array<PushEndpoint?> {
            return arrayOfNulls(size)
        }
    }
}