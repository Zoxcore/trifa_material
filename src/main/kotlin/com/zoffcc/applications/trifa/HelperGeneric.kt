package com.zoffcc.applications.trifa

import ImageloaderDispatcher
import UIMessage
import androidx.compose.foundation.Image
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.HelperFiletransfer.get_filetransfer_filenum_from_id
import com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_state_from_id
import com.zoffcc.applications.trifa.HelperFriend.delete_friend
import com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_filetransfers
import com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_messages
import com.zoffcc.applications.trifa.HelperGroup.delete_group
import com.zoffcc.applications.trifa.HelperGroup.delete_group_all_messages
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id
import com.zoffcc.applications.trifa.HelperMessage.set_message_state_from_id
import com.zoffcc.applications.trifa.MainActivity.Companion.modify_message_with_ft
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_file_control
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_delete
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_by_chat_id
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_leave
import com.zoffcc.applications.trifa.MainActivity.Companion.update_savedata_file
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import kotlinx.coroutines.withContext
import org.xml.sax.InputSource
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object HelperGeneric {
    private const val TAG = "trifa.Hlp.Generic"
    val hexArray = "0123456789ABCDEF".toCharArray()
    const val PUBKEY_SHORT_LEN = 6

    fun PubkeyShort(pubkey: String) : String {
        return pubkey.take(PUBKEY_SHORT_LEN)
    }

    fun cancel_ft_from_ui(uimessage: UIMessage)
    {
        Log.i(TAG, "cancel_ft_from_ui:001")
        try
        {
            val msg: Message? = orma!!.selectFromMessage().idEq(uimessage.msgDatabaseId)
                .tox_friendpubkeyEq(uimessage.toxpk)
                .toList().get(0)
            if (msg != null)
            {
                set_message_queueing_from_id(msg.id, false) // cancel FT
                // cancel FT
                tox_file_control(tox_friend_by_public_key(msg.tox_friendpubkey), get_filetransfer_filenum_from_id(msg.filetransfer_id), ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                set_filetransfer_state_from_id(msg.filetransfer_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                set_message_state_from_id(msg.id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value) // update message view
                // update message view
                modify_message_with_ft(msg, null)
            }
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun delete_group_wrapper(group_id: String)
    {
        val group_num_temp: Long = tox_group_by_groupid__wrapper(group_id)
        delete_group_all_messages(group_id)
        delete_group(group_id)
        if (group_num_temp > -1)
        {
            tox_group_leave(group_num_temp, "quit")
            update_savedata_file_wrapper()
        }
    }

    fun delete_friend_wrapper(friend_pubkey: String)
    {
        val friend_num_temp: Long = tox_friend_by_public_key(friend_pubkey)
        delete_friend_all_filetransfers(friend_pubkey)
        delete_friend_all_messages(friend_pubkey)
        delete_friend(friend_pubkey)
        if (friend_num_temp > -1)
        {
            tox_friend_delete(friend_num_temp)
            update_savedata_file_wrapper()
        }
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
            // e.printStackTrace()
        }
        return null
    }

    fun update_savedata_file_wrapper() {
        var callerMethodName = ""
        try {
            val stacktrace = Thread.currentThread().stackTrace
            val e = stacktrace[2]
            callerMethodName = " called from:" + e.methodName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            MainActivity.semaphore_tox_savedata!!.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            val password_hash_2 = MainActivity.password_hash
            val start_timestamp = System.currentTimeMillis()
            update_savedata_file(password_hash_2)
            val end_timestamp = System.currentTimeMillis()
            MainActivity.semaphore_tox_savedata!!.release()
            //DEBUG// Log.i(TAG, "update_savedata_file()" + callerMethodName + " took:" + (end_timestamp - start_timestamp).toFloat() / 1000f + "s")
        } catch (e: InterruptedException) {
            MainActivity.semaphore_tox_savedata!!.release()
            e.printStackTrace()
        }
    }

    @Composable
    fun <T> AsyncImage(
        load: suspend () -> T,
        painterFor: @Composable (T) -> Painter,
        contentDescription: String,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit,
    ) {
        val image: T? by produceState<T?>(null) {
            value = withContext(ImageloaderDispatcher) {
                try {
                    // DEBUG -> to test lazy loading of images // Thread.sleep(1000)
                    load()
                } catch (e: Exception) {
                    // e.printStackTrace()
                    null
                }
            }
        }

        if (image != null) {
            Image(
                painter = painterFor(image!!),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        else
        {
            Icon(
                modifier = modifier,
                imageVector = Icons.Default.Downloading,
                contentDescription = "loading ...",
                tint = MaterialTheme.colors.primary
            )
        }
    }

    /* Loading from file with java.io API */

    fun loadImageBitmap(file: File): ImageBitmap =
        file.inputStream().buffered().use(::loadImageBitmap)

    fun loadSvgPainter(file: File, density: Density): Painter =
        file.inputStream().buffered().use { loadSvgPainter(it, density) }

    fun loadXmlImageVector(file: File, density: Density): ImageVector =
        file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }
}

/*
private fun BufferedInputStream.use(block: KFunction1<File, ImageBitmap>): ImageBitmap
{
    TODO("Not yet implemented")
}
*/