package hashing

import java.security.MessageDigest

/**
 * Created by stephen on 11/14/15.
 */
object HashingUtilities {
    val SHA256 = "SHA-256"

    fun getMessageDigest() : MessageDigest {
        return MessageDigest.getInstance(SHA256)
    }

    fun getPreppedMessageDigest(prepString : String) : MessageDigest {
        val md = getMessageDigest()
        md.update(prepString.toByteArray()) // scratch: my need UTF_16

        return md
    }
}