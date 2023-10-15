package com.zoffcc.applications.trifa

import com.zoffcc.applications.trifa.MainActivity.Companion.update_savedata_file
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object HelperGeneric {
    private const val TAG = "trifa.Hlp.Generic"
    val hexArray = "0123456789ABCDEF".toCharArray()
    const val PUBKEY_SHORT_LEN = 6

    fun PubkeyShort(pubkey: String) : String {
        return pubkey.take(PUBKEY_SHORT_LEN)
    }

    fun bytesToHex(bytes: ByteArray, start: Int, len: Int): String {
        val hexChars = CharArray(len * 2)
        // System.out.println("blen=" + (len));
        for (j in start until start + len) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[(j - start) * 2] = hexArray[v ushr 4]
            hexChars[(j - start) * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun two_hex_bytes_to_dec_int(b1: Byte, b2: Byte): Int {
        var res = 0
        // ascii:0 .. 9 -> byte:48 ..  57
        // ascii:A .. F -> byte:65 ..  70
        // ascii:a .. f -> byte:97 .. 102
        if (48 <= b1 && b1 <= 57) {
            res = b1 - 48
        } else if ('A'.code.toByte() <= b1 && b1 <= 'F'.code.toByte()) {
            res = b1 - 65 + 10
        } else if ('a'.code.toByte() <= b1 && b1 <= 'f'.code.toByte()) {
            res = b1 - 97 + 10
        }
        if (48 <= b2 && b2 <= 57) {
            res = res + (b2 - 48) * 16
        } else if ('A'.code.toByte() <= b2 && b2 <= 'F'.code.toByte()) {
            res = res + (b2 - 65 + 10) * 16
        } else if ('a'.code.toByte() <= b2 && b2 <= 'f'.code.toByte()) {
            res = res + (b2 - 97 + 10) * 16
        }
        return res
    }

    @JvmStatic
    fun hexstring_to_bytebuffer(input: String): ByteBuffer? {
        try {
            val in_bytes: ByteArray = input.toByteArray(StandardCharsets.US_ASCII)
            val ret: ByteBuffer = ByteBuffer.allocateDirect(in_bytes.size / 2)
            var i = 0
            while (i < in_bytes.size) {

                // Log.i(TAG, "hexstring_to_bytebuffer:i=" + i + " byte=" + in_bytes[i] + " byte2=" + in_bytes[i + 1]);
                val b1 = in_bytes[i + 1]
                val b2 = in_bytes[i]
                // Log.i(TAG, "hexstring_to_bytebuffer:res=" + two_hex_bytes_to_dec_int(b1, b2));
                ret.put(two_hex_bytes_to_dec_int(b1, b2) as Byte)
                i = i + 2
            }
            ret.rewind()
            return ret
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun update_savedata_file_wrapper() {
        try {
            MainActivity.semaphore_tox_savedata!!.acquire()
            val password_hash_2 = MainActivity.password_hash
            val start_timestamp = System.currentTimeMillis()
            update_savedata_file(password_hash_2)
            val end_timestamp = System.currentTimeMillis()
            MainActivity.semaphore_tox_savedata!!.release()
            Log.i(TAG, "update_savedata_file() took:" + (end_timestamp - start_timestamp).toFloat() / 1000f + "s")
        } catch (e: InterruptedException) {
            MainActivity.semaphore_tox_savedata!!.release()
            e.printStackTrace()
        }
    }
}
