package global.covesa.sdk.client.push

import android.util.Base64
import com.google.crypto.tink.subtle.EllipticCurves
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec

/**
 * Decode [ECPublicKey] from [String]
 */
fun String.decodePubKey(): ECPublicKey {
    val point = EllipticCurves.pointDecode(
        EllipticCurves.CurveType.NIST_P256,
        EllipticCurves.PointFormatType.UNCOMPRESSED, this.b64decode())
    val spec = EllipticCurves.getCurveSpec(EllipticCurves.CurveType.NIST_P256)
    return KeyFactory.getInstance("EC").generatePublic(ECPublicKeySpec(point, spec)) as ECPublicKey
}

/**
 * Encode [ECPublicKey] to [String]
 */
fun ECPublicKey.encode(): String {
    val points = EllipticCurves.pointEncode(
        EllipticCurves.CurveType.NIST_P256,
        EllipticCurves.PointFormatType.UNCOMPRESSED,
        this.w
    )
    return points.b64encode()
}

/**
 * Base64 decode, url safe, no padding
 */
fun String.b64decode(): ByteArray {
    return Base64.decode(
        this,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    )
}

/**
 * Base64 encode, url safe, no padding
 */
fun ByteArray.b64encode(): String {
    return Base64.encode(
        this,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    ).toString(Charsets.UTF_8)
}

/**
 * Pads the byte array to 32 bytes.
 */
fun ByteArray.padTo32Bytes(): ByteArray {
    return when {
        size == 32 -> this // Correct size, return as is
        size > 32 -> copyOfRange(size - 32, size) // Trim excess bytes (keep right-most 32)
        else -> ByteArray(32 - size) { 0 } + this // Left-pad with 0s
    }
}

fun ByteArray.convertSignatureDerToRawRS(): ByteArray {
    val asn1 = ASN1InputStream(this)
    val sequence = asn1.readObject() as ASN1Sequence
    val r = (sequence.getObjectAt(0) as ASN1Integer).positiveValue.toByteArray()
    val s = (sequence.getObjectAt(1) as ASN1Integer).positiveValue.toByteArray()
    return r.padTo32Bytes() + s.padTo32Bytes()
}
