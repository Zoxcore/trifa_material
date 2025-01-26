/* SPDX-License-Identifier: GPL-3.0-or-later
 * [sorma2], Java part of sorma2
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
 */

package com.zoffcc.applications.sorm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.zoffcc.applications.sorm.Log;

public class OrmaDatabase
{
    private static final String TAG = "sorm.OrmaDatabase";
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean ORMA_LONG_RUNNING_TRACE = true; // set "false" for release builds
    final static long ORMA_LONG_RUNNING_MS = 180;

    private static final String CREATE_DB_FILE_SHA256SUM = "LvrHIP4y43BVnVTsd6Y1kAZXqaqKQPnRk3+0HKFP0xA=";
    private static final String CREATE_DB_FILE_ON_WINDOWS_SHA256SUM = "5QKQ8Ga1SXdvsiEbf6Ps99KdIJVTNldtI42C3UMI9DM=";
    public static Connection sqldb = null;
    static int current_db_version = 0;
    //
    static Semaphore orma_semaphore_lastrowid_on_insert = new Semaphore(1);
    //
    static ReentrantReadWriteLock orma_global_readwritelock = new ReentrantReadWriteLock(true);
    //
    static final Lock orma_global_readLock = orma_global_readwritelock.readLock();
    public static final Lock orma_global_writeLock = orma_global_readwritelock.writeLock();
    // --- read locks ---
    static final Lock orma_global_sqlcount_lock = orma_global_readLock;
    static final Lock orma_global_sqltolist_lock = orma_global_readLock;
    // static final Lock orma_global_sqlgetlastrowid_lock = orma_global_readLock;
    static final Lock orma_global_sqlexecute_lock = orma_global_readLock;
    static final Lock orma_global_sqlinsert_lock = orma_global_readLock;
    // --- read locks ---
    //
    // --- write locks ---
    static final Lock orma_global_sqlfreehand_lock = orma_global_writeLock;
    // --- write locks ---
    //

    private static String db_file_path = null;
    private static String secrect_key = null;
    private static boolean wal_mode = false; // default mode is WAL off!

    public OrmaDatabase(final String db_file_path, final String secrect_key, boolean wal_mode)
    {
        OrmaDatabase.db_file_path = db_file_path;
        OrmaDatabase.secrect_key = secrect_key;
        OrmaDatabase.wal_mode = wal_mode;
    }

    public static Connection getSqldb()
    {
        return sqldb;
    }

    public static String bytesToString(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String sha256sum_of_file(String filename_with_path)
    {
        try
        {
            byte[] buffer = new byte[8192];
            int count;
            long bytes_read_total = 0;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename_with_path));
            while ((count = bis.read(buffer)) > 0)
            {
                digest.update(buffer, 0, count);
                bytes_read_total = bytes_read_total + count;
            }
            bis.close();
            Log.i(TAG, "sha256sum_of_file:bytes_read_total=" + bytes_read_total);
            byte[] hash = digest.digest();
            return (bytesToString(hash));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static final int BINDVAR_TYPE_Int = 0;
    static final int BINDVAR_TYPE_Long = 1;
    static final int BINDVAR_TYPE_String = 2;
    static final int BINDVAR_TYPE_Boolean = 3;
    static final int BINDVAR_OFFSET_WHERE = 400;
    static final int BINDVAR_OFFSET_SET = 600;

    public static class OrmaBindvar
    {
        int type;
        Object value;

        OrmaBindvar(final int type, final Object value)
        {
            this.type = type;
            this.value = value;
        }
    }

    /*
     * repair or finally replace a string that is not correct UTF-8
     */
    @Deprecated
    static String safe_string_sql(String in)
    {
        if (in == null)
        {
            return null;
        }

        if (in.equals(""))
        {
            return "";
        }

        try
        {
            byte[] bytes = in.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++)
            {
                if (bytes[i] == 0)
                {
                    bytes[i] = '_';
                }
            }
            return (new String(bytes, StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            Log.i(TAG, "safe_string_sql:EE:" + e.getMessage());
            e.printStackTrace();
        }
        return "__ERROR_IN_STRING__";
    }

    public static long get_last_rowid_pstmt()
    {
        // orma_global_sqlgetlastrowid_lock.lock();
        try
        {
            long ret = -1;
            PreparedStatement lastrowid_pstmt = sqldb.prepareStatement("select last_insert_rowid() as lastrowid");
            try
            {
                ResultSet rs = lastrowid_pstmt.executeQuery();
                if (rs.next())
                {
                    ret = rs.getLong("lastrowid");
                }
                rs.close();
                lastrowid_pstmt.close();
                // Log.i(TAG, "get_last_rowid_pstmt:ret=" + ret);
            }
            catch(Exception e3)
            {
                Log.i(TAG, "ERR:GLRI:001:" + e3.getMessage());
                try
                {
                    lastrowid_pstmt.close();
                }
                catch(Exception e4)
                {
                }
            }
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "ERR:GLRI:002:" + e.getMessage());
            return -1;
        }
        finally
        {
            // orma_global_sqlgetlastrowid_lock.unlock();
        }
    }

    /*
     * escape to prevent SQL injection, very basic and bad!
     * TODO: make me better (and later use prepared statements)
     */
    @Deprecated
    public static String s(String str)
    {
        // TODO: bad!! use prepared statements
        String data = "";

        str = safe_string_sql(str);

        if (str == null || str.length() == 0)
        {
            return "";
        }

        if (str != null && str.length() > 0)
        {
            str = str.
                    // replace("\\", "\\\\"). // \ -> \\
                    // replace("%", "\\%"). // % -> \%
                    // replace("_", "\\_"). // _ -> \_
                            replace("'", "''"). // ' -> ''
                            replace("\\x1a", "\\Z"); // \\x1a --> EOF char
            data = str;
        }

        return data;
    }

    public static String s(int i)
    {
        return "" + i;
    }

    public static String s(long l)
    {
        return "" + l;
    }

    public static int b(boolean in)
    {
        if (in == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static String readSQLFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while ((line = reader.readLine()) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }

    public static String get_current_sqlite_version()
    {
        String ret = "unknown";

        orma_global_sqlfreehand_lock.lock();
        Statement statement = null;
        try
        {
            statement = sqldb.createStatement();
            final ResultSet rs = statement.executeQuery("SELECT sqlite_version()");
            if (rs.next())
            {
                ret = rs.getString(1);
            }
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CSQLV:001:" + e.getMessage());
                e.printStackTrace();
            }
            return ret;
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:CSQLV:002:" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            orma_global_sqlfreehand_lock.unlock();
        }

        return ret;
    }

    public static int get_current_db_version()
    {
        int ret = 0;

        orma_global_sqlfreehand_lock.lock();
        Statement statement = null;
        try
        {
            statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select db_version from orma_schema order by db_version desc limit 1");
            if (rs.next())
            {
                ret = rs.getInt("db_version");
            }
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CDBV:001:" + e.getMessage());
                e.printStackTrace();
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CDBV:002:" + e.getMessage());
            }

            return ret;
        }
        catch (Exception e)
        {
            try
            {
                statement.close();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            ret = 0;

            try
            {
                final String update_001 = "CREATE TABLE orma_schema (db_version INTEGER NOT NULL);";
                run_multi_sql(update_001);
                final String update_002 = "insert into orma_schema values ('0');";
                run_multi_sql(update_002);
            }
            catch (Exception e2)
            {
                Log.i(TAG, "ERR:CDBV:003:" + e2.getMessage());
                e2.printStackTrace();
            }
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            orma_global_sqlfreehand_lock.unlock();
        }

        return ret;
    }

    public static void set_new_db_version(int new_version)
    {
        orma_global_sqlfreehand_lock.lock();
        try
        {
            final String update_001 = "update orma_schema set db_version='" + new_version + "';";
            run_multi_sql(update_001);
        }
        catch (Exception e2)
        {
            Log.i(TAG, "ERR:SNDBV:001:" + e2.getMessage());
            e2.printStackTrace();
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }
    }

    public static int update_db(final int current_db_version)
    {
        //noinspection StatementWithEmptyBody
        if (current_db_version < 1)
        {
            // dummy. sadly now it has to stay.
        }

        if (current_db_version < 2)
        {
            try
            {
                final String update_001 =
                        "alter table Message add ft_outgoing_queued BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                                "CREATE INDEX index_ft_outgoing_queued_on_Message ON Message (ft_outgoing_queued);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 3)
        {
            try
            {
                final String update_001 =
                        "alter table Message add msg_at_relay BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                                "CREATE INDEX index_msg_at_relay_on_Message ON Message (msg_at_relay);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 4)
        {
            try
            {
                final String update_001 = "alter table FriendList add push_url TEXT DEFAULT NULL;" + "\n" +
                        "CREATE INDEX index_push_url_on_FriendList ON FriendList (push_url);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 5)
        {
            try
            {
                final String update_001 = "alter table Message add msg_idv3_hash TEXT DEFAULT NULL;" + "\n" +
                        "CREATE INDEX index_msg_idv3_hash_on_Message ON Message (msg_idv3_hash);";
                run_multi_sql(update_001);
                final String update_002 = "alter table Message add sent_push INTEGER DEFAULT '0';" + "\n" +
                        "CREATE INDEX index_sent_push_on_Message ON Message (sent_push);";
                run_multi_sql(update_002);

                final String update_003 = "alter table FriendList add capabilities INTEGER DEFAULT '0';" + "\n" +
                        "CREATE INDEX index_capabilities_on_FriendList ON FriendList (capabilities);";
                run_multi_sql(update_003);
                final String update_004 = "alter table FriendList add msgv3_capability INTEGER DEFAULT '0';" + "\n" +
                        "CREATE INDEX index_msgv3_capability_on_FriendList ON FriendList (msgv3_capability);";
                run_multi_sql(update_004);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 6)
        {
            try
            {
                // @formatter:off
                final String update_001 = "CREATE TABLE IF NOT EXISTS GroupDB ( " +
                        "who_invited__tox_public_key_string  TEXT, "+
                        "name TEXT, "+
                        "topic TEXT, "+
                        "peer_count  INTEGER NOT NULL DEFAULT -1, "+
                        "own_peer_number  INTEGER NOT NULL DEFAULT -1, "+
                        "privacy_state INTEGER NOT NULL DEFAULT 1, "+
                        "tox_group_number  INTEGER NOT NULL DEFAULT -1, "+
                        "group_active BOOLEAN DEFAULT false, "+
                        "notification_silent BOOLEAN DEFAULT false, "+
                        "group_identifier TEXT, "+
                        "PRIMARY KEY(\"group_identifier\") "+
                        ");";
                // @formatter:on
                run_multi_sql(update_001);

                // @formatter:off
                final String update_002 = "CREATE TABLE IF NOT EXISTS GroupMessage ( " +
                        "message_id_tox  TEXT , "+
                        "group_identifier  TEXT NOT NULL DEFAULT \"-1\", "+
                        "tox_group_peer_pubkey  TEXT NOT NULL, "+
                        "private_message  INTEGER NOT NULL DEFAULT 0, "+
                        "tox_group_peername  TEXT, "+
                        "direction  INTEGER NOT NULL , "+
                        "TOX_MESSAGE_TYPE  INTEGER NOT NULL , "+
                        "TRIFA_MESSAGE_TYPE  INTEGER NOT NULL DEFAULT 0 , "+
                        "sent_timestamp  INTEGER, "+
                        "rcvd_timestamp  INTEGER, "+
                        "read   BOOLEAN NOT NULL DEFAULT 0 , "+
                        "is_new   BOOLEAN NOT NULL DEFAULT 1 , "+
                        "text  TEXT, "+
                        "was_synced   BOOLEAN NOT NULL DEFAULT 0 , "+
                        "msg_id_hash   TEXT, "+
                        "id INTEGER, "+
                        "PRIMARY KEY(\"id\") "+
                        ");";
                // @formatter:on
                run_multi_sql(update_002);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 7)
        {
            try
            {
                // @formatter:off
                final String update_001 = "CREATE INDEX IF NOT EXISTS index_message_id_tox_on_GroupMessage ON GroupMessage (message_id_tox);\n" +
                        "CREATE INDEX IF NOT EXISTS index_group_identifier_tox_on_GroupMessage ON GroupMessage (group_identifier);\n" +
                        "CREATE INDEX IF NOT EXISTS index_tox_group_peer_pubkey_on_GroupMessage ON GroupMessage (tox_group_peer_pubkey);\n" +
                        "CREATE INDEX IF NOT EXISTS index_direction_on_GroupMessage ON GroupMessage (direction);\n" +
                        "CREATE INDEX IF NOT EXISTS index_TOX_MESSAGE_TYPE_on_GroupMessage ON GroupMessage (TOX_MESSAGE_TYPE);\n" +
                        "CREATE INDEX IF NOT EXISTS index_TRIFA_MESSAGE_TYPE_on_GroupMessage ON GroupMessage (TRIFA_MESSAGE_TYPE);\n" +
                        "CREATE INDEX IF NOT EXISTS index_rcvd_timestamp_on_GroupMessage ON GroupMessage (rcvd_timestamp);\n" +
                        "CREATE INDEX IF NOT EXISTS index_sent_timestamp_on_GroupMessage ON GroupMessage (sent_timestamp);\n" +
                        "CREATE INDEX IF NOT EXISTS index_private_message_on_GroupMessage ON GroupMessage (private_message);\n" +
                        "CREATE INDEX IF NOT EXISTS index_tox_group_peername_on_GroupMessage ON GroupMessage (tox_group_peername);\n" +
                        "CREATE INDEX IF NOT EXISTS index_was_synced_on_GroupMessage ON GroupMessage (was_synced);\n" +
                        "CREATE INDEX IF NOT EXISTS index_is_new_on_GroupMessage ON GroupMessage (is_new);\n" +
                        "CREATE INDEX IF NOT EXISTS index_msg_id_hash_on_GroupMessage ON GroupMessage (msg_id_hash);";
                run_multi_sql(update_001);
                // @formatter:on
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 8)
        {
            try
            {
                // @formatter:off
                final String update_001 = "CREATE INDEX IF NOT EXISTS index_who_invited__tox_public_key_string_on_GroupDB ON GroupDB (who_invited__tox_public_key_string);\n" +
                        "CREATE INDEX IF NOT EXISTS index_name_on_GroupDB ON GroupDB (name);\n" +
                        "CREATE INDEX IF NOT EXISTS index_topic_on_GroupDB ON GroupDB (topic);\n" +
                        "CREATE INDEX IF NOT EXISTS index_peer_count_on_GroupDB ON GroupDB (peer_count);\n" +
                        "CREATE INDEX IF NOT EXISTS index_own_peer_number_on_GroupDB ON GroupDB (own_peer_number);\n" +
                        "CREATE INDEX IF NOT EXISTS index_privacy_state_on_GroupDB ON GroupDB (privacy_state);\n" +
                        "CREATE INDEX IF NOT EXISTS index_tox_group_number_on_GroupDB ON GroupDB (tox_group_number);\n" +
                        "CREATE INDEX IF NOT EXISTS index_group_active_on_GroupDB ON GroupDB (group_active);\n" +
                        "CREATE INDEX IF NOT EXISTS index_notification_silent_on_GroupDB ON GroupDB (notification_silent);\n" +
                        "CREATE INDEX IF NOT EXISTS index_group_identifier_on_GroupDB ON GroupDB (group_identifier);";

                run_multi_sql(update_001);
                // @formatter:on
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 9)
        {
            try
            {
                final String update_001 = "alter table Filetransfer add tox_file_id_hex TEXT DEFAULT NULL;" + "\n" +
                        "CREATE INDEX index_tox_file_id_hex_on_Filetransfer ON Filetransfer (tox_file_id_hex);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 10)
        {
            try
            {
                final String update_001 = "alter table Message add filetransfer_kind INTEGER NOT NULL DEFAULT 0;" + "\n" +
                        "CREATE INDEX index_filetransfer_kind_on_Message ON Message (filetransfer_kind);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 11)
        {
            try
            {
                final String update_001 = "alter table GroupMessage add path_name TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_path_name_on_GroupMessage ON GroupMessage (path_name);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
            final String update_002 = "alter table GroupMessage add file_name TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_file_name_on_GroupMessage ON GroupMessage (file_name);";
                run_multi_sql(update_002);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                final String update_003 = "alter table GroupMessage add filename_fullpath TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_filename_fullpath_on_GroupMessage ON GroupMessage (filename_fullpath);";
                run_multi_sql(update_003);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                final String update_004 = "alter table GroupMessage add filesize INTEGER NOT NULL DEFAULT 0;" + "\n" +
                "CREATE INDEX index_filesize_on_GroupMessage ON GroupMessage (filesize);";
                run_multi_sql(update_004);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 12)
        {
            try
            {
                final String update_001 = "alter table FriendList add avatar_hex TEXT DEFAULT NULL;" + "\n" +
                        "alter table FriendList add avatar_hash_hex TEXT DEFAULT NULL;";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 13)
        {
            try
            {
                final String update_004 = "alter table GroupMessage add tox_group_peer_role INTEGER NOT NULL DEFAULT '-1';" + "\n" +
                        "CREATE INDEX index_tox_group_peer_role_on_GroupMessage ON GroupMessage (tox_group_peer_role);";
                run_multi_sql(update_004);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        final int new_db_version = 13;
        set_new_db_version(new_db_version);
        // return the updated DB VERSION
        return new_db_version;
    }

    public static void shutdown()
    {
        Log.i(TAG, "SHUTDOWN:start");
        try
        {
            sqldb.close();
        }
        catch (Exception e2)
        {
            Log.i(TAG, "ERR:SHUTDOWN:001:" + e2.getMessage());
            e2.printStackTrace();
        }
        Log.i(TAG, "SHUTDOWN:finished");
    }

    public static void init()
    {
        Log.i(TAG, "INIT:start");
        // create a database connection
        try
        {
            // Class.forName("org.sqlite.JDBC");
            sqldb = DriverManager.getConnection("jdbc:sqlite:" + OrmaDatabase.db_file_path);
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:INIT:001:" + e.getMessage());
            e.printStackTrace();
        }

        if (OrmaDatabase.wal_mode)
        {
            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));

            // set WAL mode
            final String set_wal_mode = "PRAGMA journal_mode = WAL;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:setting WAL mode");

            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));

            // set journal and wal size limit to 10 MB
            final String set_journal_size_limit = "PRAGMA journal_size_limit = " + (10 * 1024 * 1024) + ";";
            run_multi_sql(set_journal_size_limit);
            Log.i(TAG, "INIT:setting journal_size_limit");

            // set wal_autocheckpoint
            final String set_wal_autocheckpoint = "PRAGMA wal_autocheckpoint = 1000;";
            run_multi_sql(set_wal_autocheckpoint);
            Log.i(TAG, "INIT:setting wal_autocheckpoint");


            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));
        } else {
            // turn off WAL mode (since this setting will persist inside the database even after a restart)
            final String set_wal_mode = "PRAGMA journal_mode = DELETE;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:turning OFF WAL mode");
        }

        Log.i(TAG, "loaded:sqlite:" + get_current_sqlite_version());

        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        current_db_version = get_current_db_version();
        Log.i(TAG, "trifa:current_db_version=" + current_db_version);
        create_db(current_db_version);
        current_db_version = update_db(current_db_version);
        Log.i(TAG, "trifa:new_db_version=" + current_db_version);
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        Log.i(TAG, "INIT:finished");
    }

    public static void create_db(int current_db_version)
    {
        try
        {
            final File resources_dir = new File(System.getProperty("compose.application.resources.dir"));
            Log.i(TAG, "resources dir: " + resources_dir);
            String asset_filename = resources_dir.getCanonicalPath() + File.separator + "main.db.txt";
            Log.i(TAG, "loading asset file: " + asset_filename);
            String sha256sum_of_create_db_file = sha256sum_of_file(asset_filename);
            Log.i(TAG, "create_db:sha256sum_of_create_db_file=" + sha256sum_of_create_db_file);
            // TODO: on windows systems the checksum does not seem to match?
            // it must be "\r\n" in the sql textfile the file has more bytes also?
            if ((sha256sum_of_create_db_file.equals(CREATE_DB_FILE_SHA256SUM))
                || (sha256sum_of_create_db_file.equals(CREATE_DB_FILE_ON_WINDOWS_SHA256SUM)))
            {
                String create_db_sqls = readSQLFileAsString(asset_filename);
                if (current_db_version == 0)
                {
                    run_multi_sql(create_db_sqls);
                }
            }
            else
            {
                Log.i(TAG, "expected:"+ CREATE_DB_FILE_SHA256SUM);
                Log.i(TAG, "     git:" + sha256sum_of_create_db_file);
                Log.i(TAG, "create_db:input file sha256 hash does not match!");
                System.exit(5);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:CRDB:001:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Runs SQL statements that are seperated by ";" character
     */
    public static void run_multi_sql(String sql_multi)
    {
        orma_global_sqlfreehand_lock.lock();
        try
        {
            Statement statement = null;

            String[] queries = sql_multi.split(";");
            for (String query : queries)
            {
                try
                {
                    statement = sqldb.createStatement();
                    statement.setQueryTimeout(10);  // set timeout to x sec.
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:001:" + e.getMessage());
                }

                try
                {
                    if (ORMA_TRACE)
                    {
                        Log.i(TAG, "sql=" + query);
                    }
                    statement.executeUpdate(query);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:002:" + e.getMessage());
                }

                try
                {
                    statement.close();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:003:" + e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:MS:004:" + e.getMessage());
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }
    }

    public static String run_query_for_single_result(String sql_multi)
    {
        String text_result = null;

        orma_global_sqlfreehand_lock.lock();
        try
        {
            Statement statement = null;

            String[] queries = sql_multi.split(";");
            for (String query : queries)
            {
                try
                {
                    statement = sqldb.createStatement();
                    statement.setQueryTimeout(10);  // set timeout to x sec.
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:QSL:001:" + e.getMessage());
                }

                try
                {
                    if (ORMA_TRACE)
                    {
                        Log.i(TAG, "sql=" + query);
                    }
                    ResultSet rs = statement.executeQuery(query);
                    if (rs.next())
                    {
                        text_result = rs.getObject(1).toString();
                    }
                    rs.close();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:QSL:002:" + e.getMessage());
                }

                try
                {
                    statement.close();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:QSL:003:" + e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:QSL:004:" + e.getMessage());
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }

        return text_result;
    }

    public static boolean set_bindvars_where(final PreparedStatement statement,
                                             final int bind_where_count,
                                             final List<OrmaBindvar> bind_where_vars)
    {
        try {
            statement.clearParameters();
            if (bind_where_count > 0) {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE), (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE), (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE), (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE), (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBV:001:" + e.getMessage());
                }
            }
        }
        catch(Exception e1)
        {
            Log.i(TAG, "ERR:SBV:002:" + e1.getMessage());
            return false;
        }
        return true;
    }

    public static boolean set_bindvars_where_and_set(final PreparedStatement statement,
                                                     final int bind_where_count,
                                                     final List<OrmaBindvar> bind_where_vars,
                                                     final int bind_set_count,
                                                     final List<OrmaBindvar> bind_set_vars)
    {
        try {
            statement.clearParameters();
            if (bind_set_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_set_count; jj++) {
                        int type = bind_set_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_SET),
                                    (int) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_SET),
                                    (long) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_SET),
                                    (String) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_SET),
                                    (boolean) bind_set_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBVWS:001:" + e.getMessage());
                }
            }
            if (bind_where_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE),
                                    (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE),
                                    (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE),
                                    (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE),
                                    (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBVWS:002:" + e.getMessage());
                }
            }
        }
        catch(Exception e1)
        {
            Log.i(TAG, "ERR:SBVWS:003:" + e1.getMessage());
            return false;
        }
        return true;
    }

    public static void log_bindvars_where(final String sql, final int bind_where_count, final List<OrmaBindvar> bind_where_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + bind_where_count);
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }

    public static void log_bindvars_where_and_set(final String sql, final int bind_where_count,
                                                  final List<OrmaBindvar> bind_where_vars,
                                                  final int bind_set_count,
                                                  final List<OrmaBindvar> bind_set_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + (bind_set_count + bind_where_count));
            if (bind_set_count > 0)
            {
                for(int jj=0;jj<bind_set_count;jj++) {
                    Log.i(TAG, "bindvar set ?" + (jj + BINDVAR_OFFSET_SET) +
                            " = " + bind_set_vars.get(jj).value);
                }
            }
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar where ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }


    public ConferenceDB selectFromConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "SELECT * FROM \"ConferenceDB\"";
        return ret;
    }

    public long insertIntoConferenceDB(ConferenceDB obj)
    {
        return obj.insert();
    }

    public ConferenceDB updateConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "UPDATE \"ConferenceDB\"";
        return ret;
    }

    public ConferenceDB deleteFromConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "DELETE FROM \"ConferenceDB\"";
        return ret;
    }


    public BootstrapNodeEntryDB selectFromBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "SELECT * FROM \"BootstrapNodeEntryDB\"";
        return ret;
    }

    public long insertIntoBootstrapNodeEntryDB(BootstrapNodeEntryDB obj)
    {
        return obj.insert();
    }

    public BootstrapNodeEntryDB updateBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "UPDATE \"BootstrapNodeEntryDB\"";
        return ret;
    }

    public BootstrapNodeEntryDB deleteFromBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "DELETE FROM \"BootstrapNodeEntryDB\"";
        return ret;
    }


    public ConferenceMessage selectFromConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "SELECT * FROM \"ConferenceMessage\"";
        return ret;
    }

    public long insertIntoConferenceMessage(ConferenceMessage obj)
    {
        return obj.insert();
    }

    public ConferenceMessage updateConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "UPDATE \"ConferenceMessage\"";
        return ret;
    }

    public ConferenceMessage deleteFromConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "DELETE FROM \"ConferenceMessage\"";
        return ret;
    }


    public FileDB selectFromFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "SELECT * FROM \"FileDB\"";
        return ret;
    }

    public long insertIntoFileDB(FileDB obj)
    {
        return obj.insert();
    }

    public FileDB updateFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "UPDATE \"FileDB\"";
        return ret;
    }

    public FileDB deleteFromFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "DELETE FROM \"FileDB\"";
        return ret;
    }


    public Filetransfer selectFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "SELECT * FROM \"Filetransfer\"";
        return ret;
    }

    public long insertIntoFiletransfer(Filetransfer obj)
    {
        return obj.insert();
    }

    public Filetransfer updateFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "UPDATE \"Filetransfer\"";
        return ret;
    }

    public Filetransfer deleteFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "DELETE FROM \"Filetransfer\"";
        return ret;
    }


    public FriendList selectFromFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "SELECT * FROM \"FriendList\"";
        return ret;
    }

    public long insertIntoFriendList(FriendList obj)
    {
        return obj.insert();
    }

    public FriendList updateFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "UPDATE \"FriendList\"";
        return ret;
    }

    public FriendList deleteFromFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "DELETE FROM \"FriendList\"";
        return ret;
    }


    public GroupMessage selectFromGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "SELECT * FROM \"GroupMessage\"";
        return ret;
    }

    public long insertIntoGroupMessage(GroupMessage obj)
    {
        return obj.insert();
    }

    public GroupMessage updateGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "UPDATE \"GroupMessage\"";
        return ret;
    }

    public GroupMessage deleteFromGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "DELETE FROM \"GroupMessage\"";
        return ret;
    }


    public GroupDB selectFromGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "SELECT * FROM \"GroupDB\"";
        return ret;
    }

    public long insertIntoGroupDB(GroupDB obj)
    {
        return obj.insert();
    }

    public GroupDB updateGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "UPDATE \"GroupDB\"";
        return ret;
    }

    public GroupDB deleteFromGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "DELETE FROM \"GroupDB\"";
        return ret;
    }


    public RelayListDB selectFromRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "SELECT * FROM \"RelayListDB\"";
        return ret;
    }

    public long insertIntoRelayListDB(RelayListDB obj)
    {
        return obj.insert();
    }

    public RelayListDB updateRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "UPDATE \"RelayListDB\"";
        return ret;
    }

    public RelayListDB deleteFromRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "DELETE FROM \"RelayListDB\"";
        return ret;
    }


    public TRIFADatabaseGlobalsNew selectFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "SELECT * FROM \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }

    public long insertIntoTRIFADatabaseGlobalsNew(TRIFADatabaseGlobalsNew obj)
    {
        return obj.insert();
    }

    public TRIFADatabaseGlobalsNew updateTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "UPDATE \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }

    public TRIFADatabaseGlobalsNew deleteFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "DELETE FROM \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }


    public Message selectFromMessage()
    {
        Message ret = new Message();
        ret.sql_start = "SELECT * FROM \"Message\"";
        return ret;
    }

    public long insertIntoMessage(Message obj)
    {
        return obj.insert();
    }

    public Message updateMessage()
    {
        Message ret = new Message();
        ret.sql_start = "UPDATE \"Message\"";
        return ret;
    }

    public Message deleteFromMessage()
    {
        Message ret = new Message();
        ret.sql_start = "DELETE FROM \"Message\"";
        return ret;
    }

}

