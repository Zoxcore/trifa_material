package com.zoffcc.applications.trifa

import ImageloaderDispatcher
import MessageAction
import SnackBarToast
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
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import com.zoffcc.applications.sorm.Filetransfer
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
import com.zoffcc.applications.trifa.HelperMessage.tox_friend_send_message_wrapper
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count
import com.zoffcc.applications.trifa.MainActivity.Companion.modify_message_with_ft
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_file_control
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_delete
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_capabilities
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_leave
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_messagev3_friend_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.update_savedata_file
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE
import com.zoffcc.applications.trifa.ToxVars.TOX_MSGV3_MAX_MESSAGE_LENGTH
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import kotlinx.coroutines.withContext
import messagestore
import myUser
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

object HelperGeneric {
    private const val TAG = "trifa.Hlp.Generic"
    @JvmStatic val hexArray = "0123456789ABCDEF".toCharArray()
    const val PUBKEY_SHORT_LEN = 6

    fun PubkeyShort(pubkey: String) : String {
        return pubkey.take(PUBKEY_SHORT_LEN)
    }
    fun bytebuffer_to_hexstring(`in`: ByteBuffer, upper_case: Boolean): String?
    {
        return try
        {
            `in`.rewind()
            val sb = StringBuilder("")
            while (`in`.hasRemaining())
            {
                if (upper_case)
                {
                    sb.append(String.format("%02X", `in`.get()))
                } else
                {
                    sb.append(String.format("%02x", `in`.get()))
                }
            }
            `in`.rewind()
            sb.toString()
        } catch (e: java.lang.Exception)
        {
            null
        }
    }

    fun cancel_ft_from_ui(uimessage: UIMessage)
    {
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
                msg.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value
                modify_message_with_ft(msg, Filetransfer())
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
        SnackBarToast("Group removed")
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
        SnackBarToast("Friend removed")
    }

    @JvmStatic fun bytesToHex(bytes: ByteArray, start: Int, len: Int): String {
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
                ret.put(two_hex_bytes_to_dec_int(b1, b2).toByte())
                i = i + 2
            }
            ret.rewind()
            return ret
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun read_chunk_from_SD_file(file_name_with_path: String?, position: Long, file_chunk_length: Long): ByteArray?
    {
        if (file_name_with_path == null)
        {
            return null
        }

        val out = ByteArray(file_chunk_length.toInt())
        try
        {
            val fis = FileInputStream(File(file_name_with_path))
            fis.channel.position(position)
            val actually_read = fis.read(out, 0, file_chunk_length.toInt())
            try
            {
                fis.close()
            } catch (_: java.lang.Exception)
            {
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        return out
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
            // DEBUG// Log.i(TAG, "update_savedata_file()" + callerMethodName + " took:" + (end_timestamp - start_timestamp).toFloat() / 1000f + "s")
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

    fun shrink_image_file(ofw: MainActivity.Companion.outgoing_file_wrapped, groupid: String?): MainActivity.Companion.outgoing_file_wrapped
    {
        if (groupid == null)
        {
            return ofw
        }

        val ret = MainActivity.Companion.outgoing_file_wrapped()
        ret.filename_wrapped = ofw.filename_wrapped
        ret.filepath_wrapped = ofw.filepath_wrapped
        ret.file_size_wrapped = ofw.file_size_wrapped

        try
        {
            val ff1 = File(ofw.filepath_wrapped + File.separator + ofw.filename_wrapped)
            Log.i(TAG, "shrink_image_file:file_before=" + ff1.canonicalPath)
            Log.i(TAG, "shrink_image_file:fsize_before=" + ff1.length())
            var new_len = ff1.length()
            var max_width = 800
            val filename_out = HelperFiletransfer.
                get_incoming_filetransfer_local_filename(
                    ofw.filename_wrapped, groupid.lowercase()
                )
            val ff2 = File(VFS_FILE_DIR + File.separator + groupid.lowercase(), filename_out)
            File(VFS_FILE_DIR + File.separator + groupid.lowercase()).mkdirs()
            val qualityies = intArrayOf(70, 50, 30, 10, 4, 2, 1, 0)
            var count = 0
            var quality = qualityies[count]
            while (new_len > TOX_MAX_NGC_FILESIZE)
            {
                ImmutableImage.loader().fromFile(ff1).scaleToWidth(max_width).
                    output(WebpWriter().withQ(quality),ff2.canonicalPath);
                new_len = ff2.length()
                Log.i(TAG, "shrink_image_file:fsize_after=" +
                        new_len + " " + quality + " " + max_width + " " + ff2.absolutePath)
                count++
                if (count < qualityies.size)
                {
                    quality = qualityies[count]
                    Log.i(TAG, "shrink_image_file:A:count=" + count + " qualityies.length=" + qualityies.size + " quality=" + quality)
                } else
                {
                    Log.i(TAG, "shrink_image_file:B:count=" + count + " qualityies.length=" + qualityies.size + " quality=" + quality)
                }
                if (quality > 0)
                {
                    max_width = max_width - 20
                } else
                {
                    max_width = max_width / 2
                    if (max_width < 30)
                    {
                        max_width = 30
                    }
                }
                if (max_width <= 30)
                {
                    ret.filename_wrapped = ff2.name
                    ret.filepath_wrapped = ff2.parent
                    ret.file_size_wrapped = new_len
                    Log.i(TAG, "shrink_image_file:done:1:" +
                            ret.filename_wrapped + " " + ret.filepath_wrapped + " " + ret.file_size_wrapped)
                    return(ret)
                }
                if (new_len <= TOX_MAX_NGC_FILESIZE)
                {
                    ret.filename_wrapped = ff2.name
                    ret.filepath_wrapped = ff2.parent
                    ret.file_size_wrapped = new_len
                    Log.i(TAG, "shrink_image_file:done:1:" +
                            ret.filename_wrapped + " " + ret.filepath_wrapped + " " + ret.file_size_wrapped)
                    return(ret)
                } else
                {
                    try
                    {
                        ff2.delete()
                        Log.i(TAG, "shrink_image_file:temp file deleted:002")
                    } catch (ignored: java.lang.Exception)
                    {
                    }
                }
            }
            Log.i(TAG, "shrink_image_file:fsize_after:END=" + ff1.length() + " " + ff1.absolutePath)
        } catch (e: java.lang.Exception)
        {
            Log.i(TAG, "shrink_image_file:compressToFile:EE003:" + e.message)
            e.printStackTrace()
        }
        return(ret)
    }
    @Throws(IOException::class)
    fun io_file_copy(src: File?, dst: File?)
    {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0)
                {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    fun trim_to_utf8_length_bytes(input_string_2: String, max_length_in_bytes: Int): String?
    {
        var input_string = input_string_2
        try
        {
            do
            {
                var valueInBytes: ByteArray? = null
                valueInBytes = input_string.toByteArray(StandardCharsets.UTF_8)
                input_string = if (valueInBytes.size > max_length_in_bytes)
                {
                    input_string.substring(0, input_string.length - 1)
                } else
                {
                    return input_string
                }
            } while (input_string.length > 0)
        } catch (e: java.lang.Exception)
        {
        }
        return null
    }

    /* HINT: send a message to a friend */
    fun send_message_onclick(msg2: String?, friendPubkey: String?): Boolean
    {
        if ((friendPubkey == null) || (friendPubkey.isEmpty()))
        {
            Log.i(TAG, "send_message_onclick:ret:01")
            return false
        }

        if ((msg2 == null) || (msg2.isEmpty()))
        {
            Log.i(TAG, "send_message_onclick:ret:02")
            return false
        }

        val friendnum = tox_friend_by_public_key(friendPubkey)
        val timestamp = System.currentTimeMillis()

        if (friendnum == -1L)
        {
            Log.i(TAG, "send_message_onclick:ret:03")
            return false
        }
        var msg: String? = ""
        try
        {
            // send typed message to friend
            msg = trim_to_utf8_length_bytes(msg2, TOX_MSGV3_MAX_MESSAGE_LENGTH)
            val m = Message()
            m.tox_friendpubkey = friendPubkey.toUpperCase()
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value
            m.TOX_MESSAGE_TYPE = 0
            m.TRIFA_MESSAGE_TYPE = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.rcvd_timestamp = 0L
            m.sent_timestamp = timestamp
            m.read = false
            m.is_new = false // own messages are always "not new"
            m.text = msg
            m.msg_version = 0
            m.resend_count = 0 // we have tried to resend this message "0" times
            m.sent_push = 0
            m.msg_idv3_hash = ""
            m.msg_id_hash = ""
            m.raw_msgv2_bytes = ""

            val result: MainActivity.Companion.send_message_result =
                tox_friend_send_message_wrapper(friendPubkey.toUpperCase(),
                    0, msg,
                    m.sent_timestamp / 1000)

            val res: Long = result.msg_num
            if (res > -1)
            {
                m.resend_count = 1 // we sent the message successfully
                m.message_id = res
            } else
            {
                m.resend_count = 0 // sending was NOT successfull
                m.message_id = -1
            }
            if (result.msg_v2)
            {
                m.msg_version = 1
            } else
            {
                m.msg_version = 0
            }
            if (result.msg_hash_hex != null && !result.msg_hash_hex.equals("", true))
            {
                // msgV2 message -----------
                m.msg_id_hash = result.msg_hash_hex
                // msgV2 message -----------
            }
            if (result.msg_hash_v3_hex != null && !result.msg_hash_v3_hex.equals("", true))
            {
                // msgV3 message -----------
                m.msg_idv3_hash = result.msg_hash_v3_hex
                // msgV3 message -----------
            }
            if (result.raw_message_buf_hex != null && !result.raw_message_buf_hex.equals("", true))
            {
                // save raw message bytes of this v2 msg into the database
                // we need it if we want to resend it later
                m.raw_msgv2_bytes = result.raw_message_buf_hex
            }
            // TODO: typing indicator **// stop_self_typing_indicator_s()
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoMessage(m)
            } catch (e: Exception)
            {
            }
            m.id = row_id
            messagestore.send(MessageAction.SendMessage(UIMessage(
                direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value,
                user = myUser,
                timeMs = timestamp,
                text = msg!!, toxpk = friendPubkey.toUpperCase(),
                trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value,
                msgDatabaseId = row_id,
                filename_fullpath = null)))

        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        return true
    }

    fun is_friend_online_real(friendnum: Long): Int
    {
        return try {
            tox_friend_get_connection_status(friendnum)
        } catch (e: java.lang.Exception) {
            0
        }
    }

    fun get_friend_msgv3_capability(friend_public_key_string: String?): Long
    {
        var ret: Long = 0
        return try {
            val fcap = tox_friend_get_capabilities(tox_friend_by_public_key(friend_public_key_string))
            if ((fcap and ToxVars.TOX_CAPABILITY_MSGV2) != 0.toLong())
            {
                1
            }
            else
            {
                0
            }
        } catch (e: java.lang.Exception) {
            0
        }
    }

    fun tox_friend_resend_msgv3_wrapper(m: Message): Boolean
    {
        if (m.msg_idv3_hash == null)
        {
            m.resend_count++
            update_message_in_db_resend_count(m)
            return false
        }
        if (m.msg_idv3_hash.length < TOX_HASH_LENGTH)
        {
            m.resend_count++
            update_message_in_db_resend_count(m)
            return false
        }
        val hash_bytes = hexstring_to_bytebuffer(m.msg_idv3_hash)
        val res = tox_messagev3_friend_send_message(tox_friend_by_public_key(m.tox_friendpubkey),
            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, m.text, hash_bytes,
            m.sent_timestamp / 1000)
        m.resend_count++
        m.message_id = res
        update_message_in_db_resend_count(m)
        update_message_in_db_messageid(m)
        // TODO: // update_single_message(m, true)
        return true
    }
}

/*
private fun BufferedInputStream.use(block: KFunction1<File, ImageBitmap>): ImageBitmap
{
    TODO("Not yet implemented")
}
*/