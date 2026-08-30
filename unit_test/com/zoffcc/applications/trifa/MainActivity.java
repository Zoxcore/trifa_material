package com.zoffcc.applications.trifa;

import java.nio.ByteBuffer;

public class MainActivity {

    // =========================================================================
    // 1. INITIALIZATION & CORE
    // =========================================================================
    public native void init(String data_dir, int udp_enabled, int local_discovery_enabled,
                            int orbot_enabled, String orbot_host, long orbot_port,
                            String tox_encrypt_passphrase_hash, int enable_ipv6,
                            int force_udp_only_mode, int ngc_video_bitrate, int max_quantizer,
                            int ngc_audio_bitrate, int ngc_audio_sampling_rate, int ngc_audio_channel_count);

    public native String getNativeLibAPI();
    public static native String getNativeLibGITHASH();
    public static native String getNativeLibTOXGITHASH();
    public static native void update_savedata_file(String tox_encrypt_passphrase_hash);
    public static native void export_savedata_file_unsecure(String tox_encrypt_passphrase_hash, String export_full_path_of_file);
    public static native String get_my_toxid();
    public static native String tox_get_all_tcp_relays();
    public static native String tox_get_all_udp_connections();
    public static native int add_tcp_relay_single(String ip, String key_hex, long port);
    public static native int bootstrap_single(String ip, String key_hex, long port);
    public static native int tox_self_get_connection_status();
    public static native void init_tox_callbacks();
    public static native long tox_iteration_interval();
    public static native long tox_iterate();
    public static native long tox_kill();
    public static native void exit();

    // =========================================================================
    // 2. SELF & FRIEND MANAGEMENT
    // =========================================================================
    public static native long tox_friend_add(String toxid_str, String message);
    public static native long tox_friend_add_norequest(String public_key_str);
    public static native long tox_self_get_friend_list_size();
    public static native void tox_self_set_nospam(long nospam);
    public static native long tox_self_get_nospam();
    public static native long tox_friend_by_public_key(String friend_public_key_string);
    public static native String tox_friend_get_name(long friend_number);
    public static native String tox_friend_get_public_key(long friend_number);
    public static native long tox_friend_get_capabilities(long friend_number);
    public static native long[] tox_self_get_friend_list();
    public static native int tox_self_set_name(String name);
    public static native int tox_self_set_status_message(String status_message);
    public static native void tox_self_set_status(int a_TOX_USER_STATUS);
    public static native int tox_self_set_typing(long friend_number, int typing);
    public static native int tox_friend_get_connection_status(long friend_number);
    public static native String tox_friend_get_connection_ip(long friend_number);
    public static native int tox_friend_delete(long friend_number);
    public static native String tox_self_get_name();
    public static native long tox_self_get_name_size();
    public static native long tox_self_get_status_message_size();
    public static native String tox_self_get_status_message();
    public static native long tox_self_get_capabilities();

    // =========================================================================
    // 3. MESSAGING & FILES
    // =========================================================================
    public static native long tox_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, String message);
    public static native int tox_friend_send_lossless_packet(long friend_number, byte[] data, int data_length);
    public static native int tox_file_control(long friend_number, long file_number, int a_TOX_FILE_CONTROL);
    public static native int tox_hash(ByteBuffer hash_buffer, ByteBuffer data_buffer, long data_length);
    public static native int tox_file_seek(long friend_number, long file_number, long position);
    public static native int tox_file_get_file_id(long friend_number, long file_number, ByteBuffer file_id_buffer);
    public static native long tox_file_send(long friend_number, long kind, long file_size, ByteBuffer file_id_buffer, String file_name, long filename_length);
    public static native int tox_file_send_chunk(long friend_number, long file_number, long position, ByteBuffer data_buffer, long data_length);
    public static native long tox_max_filename_length();
    public static native long tox_file_id_length();
    public static native long tox_max_message_length();

    // =========================================================================
    // 4. MESSAGE V2 & V3
    // =========================================================================
    public static native long tox_messagev2_size(long text_length, long type, long alter_type);
    public static native int tox_messagev2_wrap(long text_length, long type, long alter_type, ByteBuffer message_text_buffer, long ts_sec, long ts_ms, ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);
    public static native int tox_messagev2_get_message_id(ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);
    public static native long tox_messagev2_get_ts_sec(ByteBuffer raw_message_buffer);
    public static native long tox_messagev2_get_ts_ms(ByteBuffer raw_message_buffer);
    public static native long tox_messagev2_get_message_text(ByteBuffer raw_message_buffer, long raw_message_len, int is_alter_msg, long alter_type, ByteBuffer message_text_buffer);
    public static native String tox_messagev2_get_sync_message_pubkey(ByteBuffer raw_message_buffer);
    public static native long tox_messagev2_get_sync_message_type(ByteBuffer raw_message_buffer);
    public static native int tox_util_friend_send_msg_receipt_v2(long friend_number, long ts_sec, ByteBuffer msgid_buffer);
    public static native long tox_util_friend_send_message_v2(long friend_number, int type, long ts_sec, String message, long length, ByteBuffer raw_message_back_buffer, ByteBuffer raw_message_back_buffer_length, ByteBuffer msgid_back_buffer);
    public static native int tox_util_friend_resend_message_v2(long friend_number, ByteBuffer raw_message_buffer, long raw_msg_len);

    public static native int tox_messagev3_get_new_message_id(ByteBuffer hash_buffer);
    public static native long tox_messagev3_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, String message, ByteBuffer mag_hash, long timestamp);

    // =========================================================================
    // 5. CONFERENCES & GROUPS
    // =========================================================================
    public static native long tox_conference_join(long friend_number, ByteBuffer cookie_buffer, long cookie_length);
    public static native long tox_conference_peer_count(long conference_number);
    public static native long tox_conference_peer_get_name_size(long conference_number, long peer_number);
    public static native String tox_conference_peer_get_name(long conference_number, long peer_number);
    public static native String tox_conference_peer_get_public_key(long conference_number, long peer_number);
    public static native long tox_conference_offline_peer_count(long conference_number);
    public static native long tox_conference_offline_peer_get_name_size(long conference_number, long offline_peer_number);
    public static native String tox_conference_offline_peer_get_name(long conference_number, long offline_peer_number);
    public static native String tox_conference_offline_peer_get_public_key(long conference_number, long offline_peer_number);
    public static native long tox_conference_offline_peer_get_last_active(long conference_number, long offline_peer_number);
    public static native int tox_conference_peer_number_is_ours(long conference_number, long peer_number);
    public static native long tox_conference_get_title_size(long conference_number);
    public static native String tox_conference_get_title(long conference_number);
    public static native int tox_conference_get_type(long conference_number);
    public static native int tox_conference_send_message(long conference_number, int a_TOX_MESSAGE_TYPE, String message);
    public static native int tox_conference_delete(long conference_number);
    public static native long tox_conference_get_chatlist_size();
    public static native long[] tox_conference_get_chatlist();
    public static native int tox_conference_get_id(long conference_number, ByteBuffer cookie_buffer);
    public static native int tox_conference_new();
    public static native int tox_conference_invite(long friend_number, long conference_number);
    public static native int tox_conference_set_title(long conference_number, String title);

    public static native long tox_group_new(int a_TOX_GROUP_PRIVACY_STATE, String group_name, String my_peer_name);
    public static native long tox_group_join(ByteBuffer chat_id_buffer, long chat_id_length, String my_peer_name, String password);
    public static native int tox_group_leave(long group_number, String part_message);
    public static native int tox_group_disconnect(long group_number);
    public static native long tox_group_self_get_peer_id(long group_number);
    public static native int tox_group_self_set_name(long group_number, String my_peer_name);
    public static native String tox_group_self_get_public_key(long group_number);
    public static native int tox_group_self_get_role(long group_number);
    public static native int tox_group_peer_get_role(long group_number, long peer_id);
    public static native int tox_group_get_chat_id(long group_number, ByteBuffer chat_id_buffer);
    public static native long tox_group_get_number_groups();
    public static native long[] tox_group_get_grouplist();
    public static native long tox_group_peer_count(long group_number);
    public static native int tox_group_get_peer_limit(long group_number);
    public static native int tox_group_founder_set_peer_limit(long group_number, int max_peers);
    public static native long tox_group_offline_peer_count(long group_number);
    public static native long[] tox_group_get_peerlist(long group_number);
    public static native long tox_group_by_chat_id(ByteBuffer chat_id_buffer);
    public static native int tox_group_get_privacy_state(long group_number);
    public static native int tox_group_mod_kick_peer(long group_number, long peer_id);
    public static native int tox_group_mod_set_role(long group_number, long peer_id, int a_Tox_Group_Role);
    public static native int tox_group_founder_set_voice_state(long group_number, int a_Tox_Group_Voice_State);
    public static native int tox_group_get_voice_state(long group_number);
    public static native String tox_group_peer_get_public_key(long group_number, long peer_id);
    public static native String tox_group_savedpeer_get_public_key(long group_number, long slot_num);
    public static native long tox_group_peer_by_public_key(long group_number, String peer_public_key_string);
    public static native String tox_group_peer_get_name(long group_number, long peer_id);
    public static native String tox_group_get_name(long group_number);
    public static native String tox_group_get_topic(long group_number);
    public static native int tox_group_peer_get_connection_status(long group_number, long peer_id);
    public static native int tox_group_invite_friend(long group_number, long friend_number);
    public static native int tox_group_is_connected(long group_number);
    public static native int tox_group_reconnect(long group_number);
    public static native int tox_group_send_custom_packet(long group_number, int lossless, byte[] data, int data_length);
    public static native int tox_group_send_custom_private_packet(long group_number, long peer_id, int lossless, byte[] data, int data_length);
    public static native long tox_group_send_message(long group_number, int a_TOX_MESSAGE_TYPE, String message);
    public static native long tox_group_send_private_message(long group_number, long peer_id, int a_TOX_MESSAGE_TYPE, String message);
    public static native long tox_group_send_private_message_by_peerpubkey(long group_number, String peer_public_key_string, int a_TOX_MESSAGE_TYPE, String message);
    public static native long tox_group_invite_accept(long friend_number, ByteBuffer invite_data_buffer, long invite_data_length, String my_peer_name, String password);

    // =========================================================================
    // 6. AUDIO/VIDEO (AV) & NGC
    // =========================================================================
    public static native long toxav_join_av_groupchat(long friend_number, ByteBuffer cookie_buffer, long cookie_length);
    public static native long toxav_add_av_groupchat();
    public static native long toxav_groupchat_enable_av(long conference_number);
    public static native long toxav_groupchat_disable_av(long conference_number);
    public static native int toxav_groupchat_av_enabled(long conference_number);
    public static native int toxav_group_send_audio(long groupnumber, long sample_count, int channels, long sampling_rate);
    public static native int toxav_answer(long friendnum, long audio_bit_rate, long video_bit_rate);
    public static native long toxav_iteration_interval();
    public static native int toxav_call(long friendnum, long audio_bit_rate, long video_bit_rate);
    public static native int toxav_bit_rate_set(long friendnum, long audio_bit_rate, long video_bit_rate);
    public static native int toxav_call_control(long friendnum, int a_TOXAV_CALL_CONTROL);
    public static native int toxav_video_send_frame_uv_reversed(long friendnum, int frame_width_px, int frame_height_px);
    public static native int toxav_video_send_frame(long friendnum, int frame_width_px, int frame_height_px);
    public static native int toxav_video_send_frame_h264(long friendnum, int frame_width_px, int frame_height_px, long data_len);
    public static native int toxav_video_send_frame_h264_age(long friendnum, int frame_width_px, int frame_height_px, long data_len, int age_ms);
    public static native int toxav_option_set(long friendnum, long a_TOXAV_OPTIONS_OPTION, long value);
    public static native void set_av_call_status(int status);
    public static native void set_audio_play_volume_percent(int volume_percent);
    public static native long set_JNI_video_buffer(ByteBuffer buffer, int frame_width_px, int frame_height_px);
    public static native void set_JNI_video_buffer2(ByteBuffer buffer2, int frame_width_px, int frame_height_px);
    public static native void set_JNI_audio_buffer(ByteBuffer audio_buffer);
    public static native void set_JNI_audio_buffer2(ByteBuffer audio_buffer2);
    public static native int toxav_audio_send_frame(long friend_number, long sample_count, int channels, long sampling_rate);
    public static native int toxav_ngc_video_encode(int vbitrate, int max_quantizer, int width, int height, byte[] y, int y_bytes, byte[] u, int u_bytes, byte[] v, int v_bytes, byte[] encoded_frame_bytes);
    public static native int toxav_ngc_video_decode(byte[] encoded_frame_bytes, int encoded_frame_size_bytes, int width, int height, byte[] y, byte[] u, byte[] v, int flush_decoder);
    public static native int toxav_ngc_audio_encode(byte[] pcm, int sample_count_per_frame, byte[] encoded_frame_bytes);
    public static native int toxav_ngc_audio_decode(byte[] encoded_frame_bytes, int encoded_frame_size_bytes, byte[] pcm_decoded);

    // =========================================================================
    // 7. TRIfA INTERNAL HELPERS
    // =========================================================================
    public static native int jni_iterate_group_audio(int delta_new, int want_ms_output);
    public static native int jni_iterate_videocall_audio(int delta_new, int want_ms_output, int channels, int sample_rate, int send_emtpy_buffer);
    public static native void tox_set_do_not_sync_av(int do_not_sync_av);
    public static native void tox_set_onion_active(int active);

    // =========================================================================
    // 8. STATIC CALLBACK METHODS (Must EXACTLY match C GetStaticMethodID signatures)
    // =========================================================================
    
    // Core callbacks
    public static void android_tox_callback_self_connection_status_cb_method(int connection_status) {}
    public static void android_tox_callback_friend_name_cb_method(long friend_number, String name, long length) {}
    public static void android_tox_callback_friend_status_message_cb_method(long friend_number, String message, long length) {}
    public static void android_tox_callback_friend_lossless_packet_cb_method(long friend_number, byte[] data, long length) {}
    public static void android_tox_callback_friend_status_cb_method(long friend_number, int status) {}
    public static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int connection_status) {}
    public static void android_tox_callback_friend_typing_cb_method(long friend_number, int is_typing) {}
    public static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id) {}
    public static void android_tox_callback_friend_request_cb_method(String public_key_hex, String message, long length) {}
    public static void android_tox_callback_friend_message_cb_method(long friend_number, int type, String message, long length, byte[] raw_message, long msgid) {}
    public static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length) {}
    public static void android_tox_callback_friend_sync_message_v2_cb_method(long friend_number, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length, byte[] raw_data, long raw_data_length) {}
    public static void android_tox_callback_friend_read_receipt_message_v2_cb_method(long friend_number, long ts_sec, byte[] msgid) {}
    
    // File callbacks
    public static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int control) {}
    public static void android_tox_callback_file_chunk_request_cb_method(long friend_number, long file_number, long position, long length) {}  
    public static void android_tox_callback_file_recv_cb_method(long friend_number, long file_number, int kind, long file_size, String filename, long filename_length) {}
    public static void android_tox_callback_file_recv_chunk_cb_method(long friend_number, long file_number, long position, byte[] data, long length) {}
    
    // Conference callbacks
    public static void android_tox_callback_conference_invite_cb_method(long friend_number, int type, byte[] cookie, long length) {}
    public static void android_tox_callback_conference_connected_cb_method(long conference_number) {}
    public static void android_tox_callback_conference_message_cb_method(long conference_number, long peer_number, int type, String message, long length) {}
    public static void android_tox_callback_conference_title_cb_method(long conference_number, long peer_number, String title, long length) {}
    public static void android_tox_callback_conference_peer_name_cb_method(long conference_number, long peer_number, String name, long length) {}
    public static void android_tox_callback_conference_peer_list_changed_cb_method(long conference_number) {}
    public static void android_tox_callback_conference_namelist_change_cb_method(long conference_number, long peer_number, int change) {}
    
    // Group callbacks
    public static void android_tox_callback_group_message_cb_method(long group_number, long peer_id, int type, String message, long length, long message_id) {}
    public static void android_tox_callback_group_private_message_cb_method(long group_number, long peer_id, int type, String message, long length, long message_id) {}
    public static void android_tox_callback_group_invite_cb_method(long friend_number, byte[] invite_data, long length, String group_name) {}
    public static void android_tox_callback_group_peer_join_cb_method(long group_number, long peer_id) {}
    public static void android_tox_callback_group_peer_exit_cb_method(long group_number, long peer_id, int exit_type) {}
    public static void android_tox_callback_group_peer_name_cb_method(long group_number, long peer_id) {}
    public static void android_tox_callback_group_moderation_cb_method(long group_number, long source_peer_id, long target_peer_id, int mod_type) {}
    public static void android_tox_callback_group_connection_status_cb_method(long group_number, int status) {}
    public static void android_tox_callback_group_join_fail_cb_method(long group_number, int fail_type) {}
    public static void android_tox_callback_group_self_join_cb_method(long group_number) {}
    public static void android_tox_callback_group_topic_cb_method(long group_number, long peer_id, String topic, long length) {}
    public static void android_tox_callback_group_privacy_state_cb_method(long group_number, int privacy_state) {}
    public static void android_tox_callback_group_custom_packet_cb_method(long group_number, long peer_id, byte[] data, long length) {}
    public static void android_tox_callback_group_custom_private_packet_cb_method(long group_number, long peer_id, byte[] data, long length) {}
    
    // AV callbacks
    public static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled) {}
    public static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride) {}
    public static void android_toxav_callback_video_receive_frame_pts_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride, long pts) {}
    public static void android_toxav_callback_video_receive_frame_h264_cb_method(long friend_number, long buf_size) {}
    public static void android_toxav_callback_call_state_cb_method(long friend_number, int call_state) {}
    public static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate) {}
    public static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate) {}
    public static void android_toxav_callback_audio_receive_frame_pts_cb_method(long friend_number, long sample_count, int channels, long sampling_rate, long pts) {}
    public static void android_toxav_callback_group_audio_receive_frame_cb_method(long group_number, long peer_id, long sample_count, int channels, long sampling_rate) {}
    public static void android_toxav_callback_call_comm_cb_method(long friend_number, long comm_info, long comm_number) {}
    
    // Logging callback (Matches: "(ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;)V")
    public static void android_tox_log_cb_method(int level, String file, long line, String function, String message)
    {
         System.out.println("C-TOXCORE:" + level + ":file=" + file + ":linenum=" +
                       line + ":func=" + function + ":msg=" + message);
    }
}
