package hashing

import java.security.MessageDigest

/**
 * Created by stephen on 11/14/15.
 */
object HashingUtilities {
    val SHA256 = "SHA-256"

    fun getMessageDigest(): MessageDigest {
        return MessageDigest.getInstance(SHA256)
    }

    fun getPreppedMessageDigest(prepString: String): MessageDigest {
        val md = getMessageDigest()
        md.update(prepString.toByteArray()) // scratch: may need UTF_16

        return md
    }

    fun stringBuilderFromMessageDigest(someHashGuy: MessageDigest): StringBuilder {
        val builder = StringBuilder()
        for (_byte in someHashGuy.digest()) {
            val maskedByte = _byte.toInt() and 0xFF
            builder.append(Integer.toString(maskedByte + 0x100, 16).substring(1))
        }
        return builder
    }
}