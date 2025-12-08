@file:Suppress("FunctionName", "VerboseNullabilityAndEmptiness", "MemberVisibilityCanBePrivate", "LocalVariableName", "ConvertToStringTemplate", "SpellCheckingInspection", "UNUSED_VARIABLE", "ReplaceCallWithBinaryOperator", "USELESS_CAST", "ReplaceGetOrSet", "ReplaceSizeZeroCheckWithIsEmpty", "RedundantIf", "ReplaceWithOperatorAssignment", "ReplaceSizeCheckWithIsNotEmpty", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_VALUE", "ReplacePutWithAssignment", "LiftReturnOrAssignment", "ReplaceRangeToWithRangeUntil")

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
import avstatestore
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import com.vanniktech.emoji.search.SearchEmojiManager
import com.zoffcc.applications.sorm.Filetransfer
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.HelperFiletransfer.get_filetransfer_filenum_from_id
import com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_state_from_id
import com.zoffcc.applications.trifa.HelperFriend.delete_friend
import com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_filetransfers
import com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_messages
import com.zoffcc.applications.trifa.HelperGroup.YUV420rotateMinus90
import com.zoffcc.applications.trifa.HelperGroup.delete_group
import com.zoffcc.applications.trifa.HelperGroup.delete_group_all_messages
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id
import com.zoffcc.applications.trifa.HelperMessage.set_message_state_from_id
import com.zoffcc.applications.trifa.HelperMessage.tox_friend_send_message_wrapper
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count
import com.zoffcc.applications.trifa.MainActivity.Companion.ROTATE_INCOMING_NGC_VIDEO
import com.zoffcc.applications.trifa.MainActivity.Companion.audio_queue_full_trigger
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibTOXGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.modify_message_with_ft
import com.zoffcc.applications.trifa.MainActivity.Companion.ngc_audio_in_queue
import com.zoffcc.applications.trifa.MainActivity.Companion.ngc_audio_in_queue_max_capacity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_file_control
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_delete
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_capabilities
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_leave
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_messagev3_friend_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_nospam
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_ngc_audio_decode
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_ngc_video_decode
import com.zoffcc.applications.trifa.MainActivity.Companion.update_savedata_file
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE
import com.zoffcc.applications.trifa.ToxVars.TOX_MSGV3_MAX_MESSAGE_LENGTH
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import globalstore
import grouppeerstore
import groupstore
import kotlinx.coroutines.withContext
import messagestore
import myUser
import org.briarproject.briar.desktop.contact.GroupItem
import org.briarproject.briar.desktop.contact.GroupPeerItem
import org.jetbrains.skia.Bitmap
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object HelperGeneric {
    private const val TAG = "trifa.Hlp.Generic"
    @JvmStatic val hexArray = "0123456789ABCDEF".toCharArray()
    const val PUBKEY_SHORT_LEN = 6

    var ngc_video_showing_video_from_peer_pubkey = "-1"
    var ngc_video_frame_last_incoming_ts = -1L
    var ngc_video_packet_last_incoming_ts = -1L
    var ngc_audio_packet_last_incoming_ts = -1L
    var ngc_video_frame_image: Bitmap? = null
    var ngc_own_video_frame_image: Bitmap? = null
    var lookup_ngc_incoming_video_peer_list: MutableMap<String, Long> = HashMap()
    var flush_decoder = 0
    var last_video_seq_num: Long = -1
    @JvmStatic val df_date_time_long = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

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
                .get(0)
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

            try
            {
                orma!!.deleteFromGroupDB().group_identifierEq(group_id.lowercase())
            } catch (_: Exception)
            {
            }
        }
        SnackBarToast("Group removed")
    }

    @JvmStatic fun delete_friend_wrapper(friend_pubkey: String, toast_message: String?)
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
        if (!toast_message.isNullOrEmpty())
        {
            SnackBarToast(toast_message)
        }
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

    @OptIn(ExperimentalStdlibApi::class)
    fun long_to_hex(l: Long): String
    {
        try
        {
            val out: String = l.toHexString(format = HexFormat {
                number {
                    prefix = ""
                    upperCase = true
                    suffix = ""
                    minLength = 8
                    removeLeadingZeros = true
                }
            }
            )
            return out
        }
        catch(_: Exception)
        {
            return ""
        }
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

    @JvmStatic fun update_savedata_file_wrapper() {
        synchronized(this) {
            var callerMethodName = ""
            try {
                val stacktrace = Thread.currentThread().stackTrace
                val e = stacktrace[2]
                callerMethodName = " called from:" + e.methodName
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try
            {
                MainActivity.semaphore_tox_savedata!!.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                val password_hash_2 = MainActivity.password_hash
                val start_timestamp = System.currentTimeMillis()
                update_savedata_file(password_hash_2)
                val end_timestamp = System.currentTimeMillis()
                MainActivity.semaphore_tox_savedata!!.release()
                // DEBUG// Log.i(TAG, "update_savedata_file()" + callerMethodName + " took:" + (end_timestamp - start_timestamp).toFloat() / 1000f + "s")
            } catch (e: InterruptedException)
            {
                MainActivity.semaphore_tox_savedata!!.release()
                e.printStackTrace()
            }
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
                Log.i(TAG, "shrink_image_file:ff1a=" + ff1.absolutePath + " ff1c=" + ff1.canonicalPath +  " ff2c=" + ff2.canonicalPath)
                ImmutableImage.loader().fromFile(ff1).scaleToWidth(max_width).
                    output(WebpWriter().withQ(quality), ff2.canonicalPath)
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
            e.printStackTrace()
        }
        Log.i(TAG, "trim_to_utf8_length_bytes: returning NULL")
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

            if (msg == null)
            {
                Log.i(TAG, "send_message_onclick:trimmed message is null: orig: " + msg2 + " trimmed: " + msg)
                return false
            }

            val m = Message()
            m.tox_friendpubkey = friendPubkey.uppercase()
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

            val result: MainActivity.Companion.send_message_result? =
                tox_friend_send_message_wrapper(friendPubkey.uppercase(),
                    0, msg,
                    m.sent_timestamp / 1000)
            if (result == null)
            {
                Log.i(TAG, "send_message_onclick:tox_friend_send_message_wrapper returned null")
                return false
            }

            val res: Long = result.msg_num
            if (res > -1)
            {
                m.resend_count = 1 // we sent the message successfully
                m.message_id = res
                Log.i(TAG, "send_message_onclick:1: message_id=" + res)
            } else
            {
                m.resend_count = 0 // sending was NOT successfull
                m.message_id = -1
                Log.i(TAG, "send_message_onclick:2: message_id=" + "-1")
            }
            if (result.msg_v2)
            {
                Log.i(TAG, "send_message_onclick:2: msg_v2=" + m.msg_version)
                m.msg_version = 1
            } else
            {
                Log.i(TAG, "send_message_onclick:2: msg_v2=" + m.msg_version)
                m.msg_version = 0
            }
            if (result.msg_hash_hex != null && !result.msg_hash_hex.equals("", true))
            {
                // msgV2 message -----------
                m.msg_id_hash = result.msg_hash_hex
                Log.i(TAG, "send_message_onclick:2: msg_id_hash=" + m.msg_id_hash)
                // msgV2 message -----------
            }
            if (result.msg_hash_v3_hex != null && !result.msg_hash_v3_hex.equals("", true))
            {
                // msgV3 message -----------
                m.msg_idv3_hash = result.msg_hash_v3_hex
                Log.i(TAG, "send_message_onclick:2: msg_idv3_hash=" + m.msg_idv3_hash)
                // msgV3 message -----------
            }
            if (result.raw_message_buf_hex != null && !result.raw_message_buf_hex.equals("", true))
            {
                // save raw message bytes of this v2 msg into the database
                // we need it if we want to resend it later
                m.raw_msgv2_bytes = result.raw_message_buf_hex
                Log.i(TAG, "send_message_onclick:2: raw_msgv2_bytes=" + m.raw_msgv2_bytes)
            }
            // TODO: typing indicator **// stop_self_typing_indicator_s()
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoMessage(m)
            } catch (_: Exception)
            {
            }
            m.id = row_id
            messagestore.send(MessageAction.SendMessage(UIMessage(
                direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value,
                user = myUser,
                timeMs = timestamp,
                recvTimeMs = 0L,
                read = m.read,
                sent_push = m.sent_push,
                msg_id_hash = m.msg_id_hash,
                msg_idv3_hash = m.msg_idv3_hash,
                msg_version = m.msg_version,
                sentTimeMs = timestamp,
                text = msg!!, toxpk = friendPubkey.uppercase(),
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
            if ((fcap and ToxVars.TOX_CAPABILITY_MSGV3) != 0.toLong())
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
            Log.i(TAG, "tox_friend_resend_msgv3_wrapper:1: msg_idv3_hash=" + m.msg_idv3_hash)
            return false
        }
        if (m.msg_idv3_hash.length < TOX_HASH_LENGTH)
        {
            m.resend_count++
            Log.i(TAG, "tox_friend_resend_msgv3_wrapper:2: msg_idv3_hash=" + m.msg_idv3_hash)
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

    @JvmStatic
    fun set_new_random_nospam_value()
    {
        val random = Random()
        val new_nospam = random.nextInt().toLong() + (1L shl 31)
        tox_self_set_nospam(new_nospam)
        update_savedata_file_wrapper()
    }

    fun play_ngc_incoming_audio_frame(group_number: Long,
                                      peer_id: Long,
                                      encoded_audio_and_header: ByteArray,
                                      length: Long)
    {
        val group_id = HelperGroup.tox_group_by_groupnum__wrapper(group_number).lowercase()
        if ((globalstore.isMinimized()) || (avstatestore.state.calling_state_get() == AVState.CALL_STATUS.CALL_STATUS_CALLING)
            || (!groupstore.state.visible) || (groupstore.stateFlow.value.selectedGroupId != group_id))
        {
            // Log.i(TAG, "play_ngc_incoming_audio_frame: audio frame not from selected group chat")
            return
        }

        ngc_video_packet_last_incoming_ts = System.currentTimeMillis()
        val ngc_incoming_video_from_peer: String = tox_group_peer_get_public_key(group_number, peer_id)!!
        ngc_update_video_incoming_peer_list(ngc_incoming_video_from_peer)

        if (ngc_video_showing_video_from_peer_pubkey.equals("-1"))
        {
            Log.i(TAG, "play_ngc_incoming_audio_frame: ngc_video_showing_video_from_peer_pubkey:2a:" + ngc_video_showing_video_from_peer_pubkey)
            ngc_video_showing_video_from_peer_pubkey = ngc_incoming_video_from_peer
            Log.i(TAG, "play_ngc_incoming_audio_frame: ngc_video_showing_video_from_peer_pubkey:2b:" + ngc_video_showing_video_from_peer_pubkey)
        }
        else if (!ngc_video_showing_video_from_peer_pubkey.equals(ngc_incoming_video_from_peer, true))
        {
            // we are already showing the video of a different peer in the group
            // Log.i(TAG, "play_ngc_incoming_audio_frame: we are already playing the audio of a different peer in the group")
            return
        }
        // remove header from data (10 bytes)
        // remove header from data (10 bytes)
        val pcm_encoded_length = (length - 10).toInt()
        val pcm_encoded_buf = ByteArray(pcm_encoded_length * 10)
        val pcm_decoded_buf = ByteArray(20000)
        val bytes_in_40ms = 1920
        val pcm_decoded_buf_delta_1 = ByteArray(bytes_in_40ms * 2)
        val pcm_decoded_buf_delta_2 = ByteArray(bytes_in_40ms * 2)
        val pcm_decoded_buf_delta_3 = ByteArray(bytes_in_40ms * 2)
        try
        {
            System.arraycopy(encoded_audio_and_header, 10, pcm_encoded_buf, 0, pcm_encoded_length)
            //
            // Log.i(TAG, "play_ngc_incoming_audio_frame:toxav_ngc_audio_decode:"
            //        + pcm_encoded_buf
            //        + " " + pcm_encoded_length
            //        + " " + pcm_decoded_buf)
            val decoded_samples: Int = toxav_ngc_audio_decode(
                pcm_encoded_buf,
                pcm_encoded_length,
                pcm_decoded_buf)
            // Log.i(TAG, "play_ngc_incoming_audio_frame:toxav_ngc_audio_decode:decoded_samples="
            //        + decoded_samples)
            // put pcm data into a FIFO
            if ((ngc_audio_in_queue.remainingCapacity() < 1) && (!audio_queue_full_trigger))
            {
                Log.i(TAG, "play_ngc_incoming_audio_frame:--- DROP:1 !! FULL !! :trigger:" + ngc_audio_in_queue.size)
                audio_queue_full_trigger = true
            }
            else
            {
                if (audio_queue_full_trigger)
                {
                    if (ngc_audio_in_queue.remainingCapacity() >= ngc_audio_in_queue_max_capacity - 2)
                    {
                        audio_queue_full_trigger = false
                        System.arraycopy(pcm_decoded_buf, 0, pcm_decoded_buf_delta_1, 0, bytes_in_40ms * 2)
                        ngc_audio_in_queue.offer(pcm_decoded_buf_delta_1)
                        System.arraycopy(pcm_decoded_buf, bytes_in_40ms * 2, pcm_decoded_buf_delta_2, 0, bytes_in_40ms * 2)
                        ngc_audio_in_queue.offer(pcm_decoded_buf_delta_2)
                        System.arraycopy(pcm_decoded_buf, bytes_in_40ms * 2 * 2, pcm_decoded_buf_delta_3, 0, bytes_in_40ms * 2)
                        ngc_audio_in_queue.offer(pcm_decoded_buf_delta_3)
                        // Log.i(TAG, "play_ngc_incoming_audio_frame:release:")
                    }
                    else
                    {
                        Log.i(TAG, "play_ngc_incoming_audio_frame:--- DROP:2 ----:" +
                                audio_queue_full_trigger)
                    }
                }
                else
                {
                    // Log.i(TAG, "play_ngc_incoming_audio_frame:push:" + ngc_audio_in_queue.size + " " + ngc_audio_in_queue.remainingCapacity())
                    System.arraycopy(pcm_decoded_buf, 0, pcm_decoded_buf_delta_1, 0, bytes_in_40ms * 2)
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_1)
                    System.arraycopy(pcm_decoded_buf, bytes_in_40ms * 2, pcm_decoded_buf_delta_2, 0, bytes_in_40ms * 2)
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_2)
                    System.arraycopy(pcm_decoded_buf, bytes_in_40ms * 2 * 2, pcm_decoded_buf_delta_3, 0, bytes_in_40ms * 2)
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_3)
                }
            }
            ngc_video_frame_last_incoming_ts = System.currentTimeMillis()
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
    }

    fun show_ngc_incoming_video_frame_v2(group_number: Long,
                                         peer_id: Long,
                                         encoded_video_and_header: ByteArray,
                                         length: Long) {
        val group_id = HelperGroup.tox_group_by_groupnum__wrapper(group_number).lowercase()
        if ((globalstore.isMinimized()) || (avstatestore.state.calling_state_get() == AVState.CALL_STATUS.CALL_STATUS_CALLING)
            || (!groupstore.state.visible) || (groupstore.stateFlow.value.selectedGroupId != group_id))
        {
            // Log.i(TAG, "show_ngc_incoming_video_frame_v2: video frame not from selected group chat")
            return
        }

        ngc_video_packet_last_incoming_ts = System.currentTimeMillis()
        val ngc_incoming_video_from_peer: String = tox_group_peer_get_public_key(group_number, peer_id)!!
        // Log.i(TAG, "show_ngc_incoming_video_frame_v2: ngc_update_video_incoming_peer_list:" + ngc_incoming_video_from_peer)
        ngc_update_video_incoming_peer_list(ngc_incoming_video_from_peer)

        if (ngc_video_showing_video_from_peer_pubkey.equals("-1"))
        {
            Log.i(TAG, "show_ngc_incoming_video_frame_v2: ngc_video_showing_video_from_peer_pubkey:1a:" + ngc_video_showing_video_from_peer_pubkey)
            ngc_video_showing_video_from_peer_pubkey = ngc_incoming_video_from_peer
            Log.i(TAG, "show_ngc_incoming_video_frame_v2: ngc_video_showing_video_from_peer_pubkey:1b:" + ngc_video_showing_video_from_peer_pubkey)
        }
        else if (!ngc_video_showing_video_from_peer_pubkey.equals(ngc_incoming_video_from_peer, true))
        {
            // we are already showing the video of a different peer in the group
            // Log.i(TAG, "show_ngc_incoming_video_frame_v2: we are already showing the video of a different peer in the group")
            return
        }

        // remove header from data (14 bytes)
        val yuv_frame_encoded_bytes = (length - 14).toInt()
        if ((yuv_frame_encoded_bytes > 0) && (yuv_frame_encoded_bytes < 40000)) {
            // TODO: make faster and better. this is not optimized.
            val yuv_frame_encoded_buf = ByteArray(yuv_frame_encoded_bytes)
            val w2 = 480 + 32 // 240 + 16; // encoder stride added
            val h2 = 640 // 320;
            val y_bytes2 = w2 * h2
            val u_bytes2 = (w2 * h2) / 4
            val v_bytes2 = (w2 * h2) / 4
            val y_buf2 = ByteArray(y_bytes2)
            val u_buf2 = ByteArray(u_bytes2)
            val v_buf2 = ByteArray(v_bytes2)
            var ystride = -1
            val chkskum = ByteArray(1)
            val low_seqnum = ByteArray(1)
            val high_seqnum = ByteArray(1)
            try {
                System.arraycopy(encoded_video_and_header, 14, yuv_frame_encoded_buf, 0, yuv_frame_encoded_bytes)
                System.arraycopy(encoded_video_and_header, 11, low_seqnum, 0, 1)
                System.arraycopy(encoded_video_and_header, 12, high_seqnum, 0, 1)
                System.arraycopy(encoded_video_and_header, 13, chkskum, 0, 1)
                val seqnum = java.lang.Byte.toUnsignedInt(low_seqnum[0]) + Integer.toUnsignedLong((high_seqnum[0].toInt() shl 8))
                // if (seqnum != (last_video_seq_num + 1)) {
                //    Log.i(TAG, "!!!!!!!seqnumber_missing!!!!! " + seqnum + " -> " + (last_video_seq_num + 1));
                //}
                last_video_seq_num = seqnum
                // val crc_8 = Integer.toUnsignedLong(calc_crc_8(yuv_frame_encoded_buf))
                // if (java.lang.Byte.toUnsignedInt(chkskum[0]).toLong() != crc_8) {
                //    Log.i(TAG, "checksum=" + java.lang.Byte.toUnsignedInt(chkskum[0]).toLong()
                //          + " crc8=" + crc_8 + " seqnum=" + seqnum
                //          + " yuv_frame_encoded_bytes=" + (yuv_frame_encoded_bytes + 14));
                //}
                //
                ystride = toxav_ngc_video_decode(yuv_frame_encoded_buf, yuv_frame_encoded_bytes,
                    w2, h2, y_buf2, u_buf2, v_buf2, flush_decoder)
                // if (ystride != -1)
                //{
                //    Log.i(TAG, "toxav_ngc_video_decode:ystride=" + ystride);
                //}
                flush_decoder = 0
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return
            }
            val ystride_ = ystride
            //
            try  {
                if (ystride_ == -1)
                {
                    // TODO // ngc_video_view.setImageResource(R.drawable.round_loading_animation)
                }
                else
                {
                    var w2_decoder = ystride_ // encoder stride
                    val w2_decoder_uv = ystride_ / 2 // encoder stride
                    var h2_decoder = 640 // 320;
                    val h2_decoder_uv = h2_decoder / 2
                    val y_bytes2_decoder = h2_decoder * w2_decoder
                    val u_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv)
                    val v_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv)
                    val yuv_frame_data_buf: ByteBuffer?

                    if (ROTATE_INCOMING_NGC_VIDEO)
                    {
                        // - rotate yuv (not optimized!!) -
                        // - rotate yuv (not optimized!!) -
                        val tmp_yuv = ByteArray(y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder)
                        var tmp_yuv_rotated = ByteArray(y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder)
                        System.arraycopy(y_buf2, 0, tmp_yuv, 0, y_bytes2_decoder)
                        System.arraycopy(u_buf2, 0, tmp_yuv, y_bytes2_decoder, u_bytes2_decoder)
                        System.arraycopy(v_buf2, 0, tmp_yuv, y_bytes2_decoder + u_bytes2_decoder, v_bytes2_decoder)
                        tmp_yuv_rotated = YUV420rotateMinus90(tmp_yuv, tmp_yuv_rotated, w2_decoder, h2_decoder)
                        val tmp = w2_decoder
                        w2_decoder = h2_decoder
                        h2_decoder = tmp
                        //
                        yuv_frame_data_buf = ByteBuffer.allocateDirect(
                            y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder)
                        yuv_frame_data_buf.rewind()
                        //
                        yuv_frame_data_buf.put(tmp_yuv_rotated, 0, y_bytes2_decoder)
                        yuv_frame_data_buf.put(tmp_yuv_rotated, y_bytes2_decoder, u_bytes2_decoder)
                        yuv_frame_data_buf.put(tmp_yuv_rotated, y_bytes2_decoder + u_bytes2_decoder, v_bytes2_decoder)
                        // - rotate yuv (not optimized!!) -
                        // - rotate yuv (not optimized!!) -
                    }
                    else
                    {
                        yuv_frame_data_buf = ByteBuffer.allocateDirect(
                            y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder)
                        yuv_frame_data_buf.rewind()
                        //
                        yuv_frame_data_buf.put(y_buf2, 0, y_bytes2_decoder)
                        yuv_frame_data_buf.put(u_buf2, 0, u_bytes2_decoder)
                        yuv_frame_data_buf.put(v_buf2, 0, v_bytes2_decoder)
                        //
                    }
                    yuv_frame_data_buf.rewind()
                    if ((VideoInFrame.width != w2_decoder || VideoInFrame.height != h2_decoder) || (VideoInFrame.imageInByte == null))
                    {
                        VideoInFrame.setup_video_in_resolution(w2_decoder, h2_decoder, (y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder))
                    }
                    VideoInFrame.new_video_in_frame(yuv_frame_data_buf, w2_decoder, h2_decoder)
                }
                ngc_video_frame_last_incoming_ts = System.currentTimeMillis()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calc_crc_8(yuv_frame_encoded_buf: ByteArray): Int
    {
        val CRC_POLYNOM = 0x9c
        val CRC_PRESET = 0xFF
        var crc_U = CRC_PRESET
        for (i in yuv_frame_encoded_buf.indices)
        {
            crc_U = crc_U xor java.lang.Byte.toUnsignedInt(yuv_frame_encoded_buf[i])
            for (j in 0..7)
            {
                crc_U = if (crc_U and 0x01 != 0)
                {
                    crc_U ushr 1 xor CRC_POLYNOM
                } else
                {
                    crc_U ushr 1
                }
            }
        }
        return crc_U
    }

    fun ngc_update_video_incoming_peer_list(peer_pubkey: String)
    {
        lookup_ngc_incoming_video_peer_list.put(peer_pubkey, System.currentTimeMillis())
        ngc_update_video_incoming_peer_list_ts()
        // Log.i(TAG, "ngc_update_video_incoming_peer_list entries=" + lookup_ngc_incoming_video_peer_list.size)
    }

    fun ngc_update_video_incoming_peer_list_ts()
    {
        if (lookup_ngc_incoming_video_peer_list.isEmpty())
        {
            return
        }
        // remove all peers that have not sent video in the last 5 seconds
        val iterator: MutableIterator<MutableMap.MutableEntry<String, Long>> = lookup_ngc_incoming_video_peer_list.iterator() as MutableIterator
        while (iterator.hasNext())
        {
            val item = iterator.next()
            if (item.value < System.currentTimeMillis() - (5 * 1000))
            {
                // Log.i(TAG, "ngc_update_video_incoming_peer_list: iterator.remove: " + item.key)
                if (item.key.equals(ngc_video_showing_video_from_peer_pubkey, true))
                {
                    // HINT: we are removing the peer from which we are showing the video currently
                    // set pubkey to "-1". this will switch to the next peer sending video
                    ngc_video_showing_video_from_peer_pubkey = "-1"
                }
                iterator.remove()
            }
            else
            {
                // Log.i(TAG, "ngc_update_video_incoming_peer_list: KEEP")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun friend_get_avatar(friend_pubkey: String): ByteArray?
    {
        try
        {
            val f = orma!!.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).get(0)
            if ((f.avatar_hex == null) || (f.avatar_hex.length < 1))
            {
                return null
            }
            return f.avatar_hex.hexToByteArray()
        }
        catch(_: Exception)
        {
            return null
        }
    }

    fun friend_has_avatar(friend_pubkey: String): Boolean
    {
        try
        {
            val f = orma!!.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).get(0)
            if ((f.avatar_hex == null) || (f.avatar_hex.length < 1))
            {
                return false
            }
            return true
        }
        catch(_: Exception)
        {
            return false
        }
    }

    @JvmStatic fun long_date_time_format(timestamp_in_millis: Long): String
    {
        return try
        {
            df_date_time_long.format(Date(timestamp_in_millis))
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
            "_Datetime_ERROR_"
        }
    }

    @JvmStatic
    fun JavaSnackBarToast(text: String?)
    {
        if (!text.isNullOrEmpty())
        {
            SnackBarToast(text)
        }
    }

    // !! unused now !!
    fun replace_emojis_in_text(input: String): String
    {
        var output = input
        val strings = arrayOf<String>("smile", "sad", "thumbsup", "thumbsdown", "heart")
        val emojis = arrayOf<String>("slightly_smiling_face", "slightly_frowning_face", "thumbsup", "thumbsdown", "heart")
        for (i in 0..(strings.size - 1))
        {
            try
            {
                // Log.i(TAG, "i=" + i + " s=" + strings[i])
                if (strings[i].equals("thumbsup", ignoreCase = false))
                {
                    output = output.replace(":" + strings[i] + ":", SearchEmojiManager().search(query = emojis[i]).first().emoji.variants.get(1).unicode, ignoreCase = true)
                }
                else
                {
                    output = output.replace(":" + strings[i] + ":", SearchEmojiManager().search(query = emojis[i]).first().emoji.unicode, ignoreCase = true)
                }
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        return output
    }

    fun get_java_os_name(): String
    {
        try
        {
            return System.getProperty("os.name")
        } catch (e: java.lang.Exception)
        {
            return "??????????".take(4)
        }
    }

    fun get_java_os_version(): String
    {
        try
        {
            return System.getProperty("os.version")
        } catch (e: java.lang.Exception)
        {
            return "??????????".take(4)
        }
    }

    fun get_trifa_build_str(): String
    {
        var build_str = ""

        try
        {
            build_str = build_str + BuildConfig.GIT_COMMIT_HASH.take(4)
        } catch (e: java.lang.Exception)
        {
            build_str = build_str + "??????????".take(4)
        }

        try
        {
            build_str = build_str + "-" + getNativeLibTOXGITHASH()!!.take(3)
        } catch (e: java.lang.Exception)
        {
            build_str = build_str + "??????????".take(3)
        }

        try
        {
            build_str = build_str + "-" + getNativeLibGITHASH()!!.take(3)
        } catch (e: java.lang.Exception)
        {
            build_str = build_str + "??????????".take(3)
        }

        try
        {
            build_str = build_str + "-" + System.getProperty("os.arch")
        } catch (e: java.lang.Exception)
        {
            build_str = build_str + "??????????".take(3)
        }

        try
        {
            var tox_jni_asan_append = ""
            if (MainActivity.jnictoxcore_version().contains("-asan", true))
            {
                tox_jni_asan_append = "-ASAN"
            }

            build_str = build_str + tox_jni_asan_append
        } catch (_: Exception)
        {
        }

        return build_str
    }

    fun get_self_group_role(group_num: Long): Int
    {
        var peer_role = ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value
        try
        {
            val self_peer_role = MainActivity.tox_group_self_get_role(group_num)
            if (self_peer_role >= 0)
            {
                peer_role = self_peer_role
            }
        } catch (_: Exception)
        {
        }

        return peer_role
    }

    fun get_self_group_role(group_id: String): Int
    {
        val group_num_temp: Long = tox_group_by_groupid__wrapper(group_id)
        if (group_num_temp == -1L)
        {
            return ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value
        }

        return get_self_group_role(group_num_temp)
    }

    fun is_self_group_role_admin(group_role: Int): Boolean
    {
        if (group_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_FOUNDER.value)
        {
            return true
        }
        else if (group_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
        {
            return true
        }
        return false
    }

    fun is_peer_self(group_id: String, peer_pubkey: String): Boolean
    {
        try
        {
            val self_group_pubkey = tox_group_self_get_public_key(tox_group_by_groupid__wrapper(group_id))
            return (self_group_pubkey == peer_pubkey)
        }
        catch(_: Exception)
        {
        }
        return false
    }

    fun force_update_group_peerlist_ui(group_id: String)
    {
        try
        {
            // HINT: this is not pretty but it does the job for now :-(
            grouppeerstore.update(GroupPeerItem(ip_addr = "127.0.0.1", groupID = group_id, name = "x", connectionStatus = 0, pubkey = "x", peerRole = 0))
        } catch (_: Exception)
        {
        }
    }
}

/*
private fun BufferedInputStream.use(block: KFunction1<File, ImageBitmap>): ImageBitmap
{
    TODO("Not yet implemented")
}
*/
