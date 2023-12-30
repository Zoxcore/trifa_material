/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.sorm;

import com.zoffcc.applications.trifa.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class Message
{
    private static final String TAG = "DB.Message";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long message_id = -1; // ID given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_friendpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    public int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    public int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "1", helpers = Column.Helpers.ALL)
    public int state = TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_started = false;

    @Column(indexed = true, defaultExpr = "-1")
    public long filedb_id; // f_key -> FileDB.id

    @Column(indexed = true, defaultExpr = "-1")
    public long filetransfer_id; // f_key -> Filetransfer.id

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public long sent_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public long sent_timestamp_ms = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    public long rcvd_timestamp = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    public long rcvd_timestamp_ms = 0L;

    @Column(helpers = Column.Helpers.ALL)
    public boolean read = false;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int send_retries = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new = true;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_id_hash = null; // 32byte hash, used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String raw_msgv2_bytes = null; // used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, defaultExpr = "0")
    public int msg_version; // 0 -> old Message, 1 -> for MessageV2 Message

    @Column(indexed = true, defaultExpr = "2")
    public int resend_count; // for msgV2 "> 1" -> do not resend msg anymore, 0 or 1 -> resend count

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_queued = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean msg_at_relay = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_idv3_hash = null; // 32byte hash, used for MessageV3 Messages! and otherwise NULL

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public int sent_push = 0;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public int filetransfer_kind = TOX_FILE_KIND_DATA.value;

    // ------- SWING UI elements ------- //
    JButton _swing_ok = null;
    JButton _swing_cancel = null;
    // ------- SWING UI elements ------- //

    static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id = in.message_id;
        out.tox_friendpubkey = in.tox_friendpubkey;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.filedb_id = in.filedb_id;
        out.filetransfer_id = in.filetransfer_id;
        out.sent_timestamp = in.sent_timestamp;
        out.sent_timestamp_ms = in.sent_timestamp_ms;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.rcvd_timestamp_ms = in.rcvd_timestamp_ms;
        out.read = in.read;
        out.send_retries = in.send_retries;
        out.is_new = in.is_new;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;
        out.msg_id_hash = in.msg_id_hash;
        out.msg_version = in.msg_version;
        out.raw_msgv2_bytes = in.raw_msgv2_bytes;
        out.resend_count = in.resend_count;
        out.ft_outgoing_queued = in.ft_outgoing_queued;
        out.msg_at_relay = in.msg_at_relay;
        out.msg_idv3_hash = in.msg_idv3_hash;
        out.sent_push = in.sent_push;
        out.filetransfer_kind = in.filetransfer_kind;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id=" + message_id + ", filetransfer_id=" + filetransfer_id + ", filedb_id=" +
                filedb_id + ", tox_friendpubkey=" + "*pubkey*" + ", direction=" + direction + ", state=" + state +
                ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
                ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
                ", send_retries=" + send_retries + ", text=" + "xxxxxx" + ", filename_fullpath=" + filename_fullpath +
                ", is_new=" + is_new + ", msg_id_hash=" + msg_id_hash + ", msg_version=" + msg_version +
                ", resend_count=" + resend_count + ", raw_msgv2_bytes=" + "xxxxxx" + ", ft_outgoing_queued=" +
                ft_outgoing_queued + ", msg_at_relay=" + msg_at_relay + ", sent_push=" + sent_push +
                ", filetransfer_kind=" + filetransfer_kind;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit
    Object[] sql_bind_vars = new Object[]{};

    public List<Message> toList()
    {
        List<Message> list = new ArrayList<>();

        try
        {
            Statement statement = sqldb.createStatement();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }
            final long t1 = System.currentTimeMillis();
            ResultSet rs = statement.executeQuery(sql);
            final long t2 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t2 - t1) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "long running (" + (t2 - t1)+ " ms) sql=" + sql);
                }
            }
            final long t3 = System.currentTimeMillis();
            while (rs.next())
            {
                Message out = new Message();

                out.id = rs.getLong("id");
                out.message_id = rs.getLong("message_id");
                out.tox_friendpubkey = rs.getString("tox_friendpubkey");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.state = rs.getInt("state");
                out.ft_accepted = rs.getBoolean("ft_accepted");
                out.ft_outgoing_started = rs.getBoolean("ft_outgoing_started");
                out.filedb_id = rs.getLong("filedb_id");
                out.filetransfer_id = rs.getLong("filetransfer_id");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.sent_timestamp_ms = rs.getLong("sent_timestamp_ms");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.rcvd_timestamp_ms = rs.getLong("rcvd_timestamp_ms");
                out.read = rs.getBoolean("read");
                out.send_retries = rs.getInt("send_retries");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.filename_fullpath = rs.getString("filename_fullpath");
                out.msg_id_hash = rs.getString("msg_id_hash");
                out.msg_version = rs.getInt("msg_version");
                out.raw_msgv2_bytes = rs.getString("raw_msgv2_bytes");
                out.resend_count = rs.getInt("resend_count");
                out.ft_outgoing_queued = rs.getBoolean("ft_outgoing_queued");
                out.msg_at_relay = rs.getBoolean("msg_at_relay");
                out.msg_idv3_hash = rs.getString("msg_idv3_hash");
                out.sent_push = rs.getInt("sent_push");
                out.filetransfer_kind = rs.getInt("filetransfer_kind");

                list.add(out);
            }
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "long running (" + (t4 - t3)+ " ms) fetch=" + sql);
                }
            }


            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return list;
    }

    public long insert()
    {
        long ret = -1;

        try
        {
            String insert_pstmt_sql = null;
            PreparedStatement insert_pstmt = null;

            // @formatter:off
            insert_pstmt_sql ="insert into " + this.getClass().getSimpleName() +
                    "(" +
                    "message_id," +
                    "tox_friendpubkey," +
                    "direction," +
                    "TOX_MESSAGE_TYPE," +
                    "TRIFA_MESSAGE_TYPE," +
                    "state," +
                    "ft_accepted," +
                    "ft_outgoing_started," +
                    "filedb_id," +
                    "filetransfer_id," +
                    "sent_timestamp," +
                    "sent_timestamp_ms," +
                    "rcvd_timestamp," +
                    "rcvd_timestamp_ms," +
                    "read," +
                    "send_retries," +
                    "is_new," +
                    "text," +
                    "filename_fullpath," +
                    "msg_id_hash," +
                    "msg_version," +
                    "raw_msgv2_bytes," +
                    "resend_count," +
                    "ft_outgoing_queued," +
                    "msg_at_relay," +
                    "msg_idv3_hash," +
                    "filetransfer_kind," +
                    "sent_push" +
                    ")" +
                    "values" +
                    "(" +
                    "?1," +
                    "?2," +
                    "?3," +
                    "?4," +
                    "?5," +
                    "?6," +
                    "?7," +
                    "?8," +
                    "?9," +
                    "?10," +
                    "?11," +
                    "?12," +
                    "?13," +
                    "?14," +
                    "?15," +
                    "?16," +
                    "?17," +
                    "?18," +
                    "?19," +
                    "?20," +
                    "?21," +
                    "?22," +
                    "?23," +
                    "?24," +
                    "?25," +
                    "?26," +
                    "?27," +
                    "?28" +
                    ")";
            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);

            insert_pstmt.clearParameters();

            insert_pstmt.setLong(1, this.message_id);
            insert_pstmt.setString(2, this.tox_friendpubkey);
            insert_pstmt.setInt(3, this.direction);
            insert_pstmt.setInt(4, this.TOX_MESSAGE_TYPE);
            insert_pstmt.setInt(5, this.TRIFA_MESSAGE_TYPE);
            insert_pstmt.setInt(6, this.state);
            insert_pstmt.setBoolean(7, this.ft_accepted);
            insert_pstmt.setBoolean(8, this.ft_outgoing_started);
            insert_pstmt.setLong(9, this.filedb_id);
            insert_pstmt.setLong(10, this.filetransfer_id);
            insert_pstmt.setLong(11, this.sent_timestamp);
            insert_pstmt.setLong(12, this.sent_timestamp_ms);
            insert_pstmt.setLong(13, this.rcvd_timestamp);
            insert_pstmt.setLong(14, this.rcvd_timestamp_ms);
            insert_pstmt.setBoolean(15, this.read);
            insert_pstmt.setInt(16, this.send_retries);
            insert_pstmt.setBoolean(17, this.is_new);
            insert_pstmt.setString(18, this.text);
            insert_pstmt.setString(19, this.filename_fullpath);
            insert_pstmt.setString(20, this.msg_id_hash);
            insert_pstmt.setInt(21, this.msg_version);
            insert_pstmt.setString(22, this.raw_msgv2_bytes);
            insert_pstmt.setInt(23, this.resend_count);
            insert_pstmt.setBoolean(24, this.ft_outgoing_queued);
            insert_pstmt.setBoolean(25, this.msg_at_relay);
            insert_pstmt.setString(26, this.msg_idv3_hash);
            insert_pstmt.setInt(27, this.filetransfer_kind);
            insert_pstmt.setInt(28, this.sent_push);
            // @formatter:on

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + insert_pstmt);
            }

            final long t1 = System.currentTimeMillis();
            orma_semaphore_lastrowid_on_insert.acquire();
            final long t2 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t2 - t1) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertIntoMessage acquire running long (" + (t2 - t1)+ " ms)");
                }
            }

            final long t3 = System.currentTimeMillis();
            insert_pstmt.executeUpdate();
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertIntoMessage sql running long (" + (t4 - t3)+ " ms)");
                }
            }

            final long t5 = System.currentTimeMillis();
            insert_pstmt.close();
            final long t6 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t6 - t5) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertIntoMessage statement close running long (" + (t6 - t5)+ " ms)");
                }
            }

            final long t7 = System.currentTimeMillis();
            ret = get_last_rowid_pstmt();
            final long t8 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t8 - t7) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertIntoMessage getLastRowId running long (" + (t8 - t7)+ " ms)");
                }
            }

            orma_semaphore_lastrowid_on_insert.release();
        }
        catch (Exception e)
        {
            orma_semaphore_lastrowid_on_insert.release();
            throw new RuntimeException(e);
        }

        return ret;
    }

    public Message get(int i)
    {
        this.sql_limit = " limit " + i + ",1 ";
        return this.toList().get(0);
    }

    public void execute()
    {
        try
        {
            Statement statement = sqldb.createStatement();
            final String sql = this.sql_start + " " + this.sql_set + " " + this.sql_where;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }
            statement.executeUpdate(sql);

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "EE1:" + e2.getMessage());
        }
    }

    public int count()
    {
        int ret = 0;

        try
        {
            Statement statement = sqldb.createStatement();
            this.sql_start = "SELECT count(*) as count FROM " + this.getClass().getSimpleName();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next())
            {
                ret = rs.getInt("count");
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public Message limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public Message limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public Message tox_friendpubkeyEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey='" + s(tox_friendpubkey) + "' ";
        return this;
    }

    public Message orderBySent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp ASC ";
        return this;
    }

    public Message orderBySent_timestamp_msAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp_ms ASC ";
        return this;
    }

    public Message directionEq(int i)
    {
        this.sql_where = this.sql_where + " and direction='" + s(i) + "' ";
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEEq(int value)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE='" + s(value) + "' ";
        return this;
    }

    public Message resend_countEq(int i)
    {
        this.sql_where = this.sql_where + " and resend_count='" + s(i) + "' ";
        return this;
    }

    public Message readEq(boolean b)
    {
        this.sql_where = this.sql_where + " and read='" + b(b) + "' ";
        return this;
    }

    public Message idEq(long id)
    {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }

    public Message message_id(long message_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " message_id='" + s(message_id) + "' ";
        return this;
    }

    public Message text(String text)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " text='" + s(text) + "' ";
        return this;
    }

    public Message sent_timestamp(long sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " sent_timestamp='" + s(sent_timestamp) + "' ";
        return this;
    }

    public Message msg_version(int msg_version)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_version='" + s(msg_version) + "' ";
        return this;
    }

    public Message filename_fullpath(String filename_fullpath)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filename_fullpath='" + s(filename_fullpath) + "' ";
        return this;
    }

    public Message raw_msgv2_bytes(String raw_msgv2_bytes)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " raw_msgv2_bytes='" + s(raw_msgv2_bytes) + "' ";
        return this;
    }

    public Message msg_id_hash(String msg_id_hash)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_id_hash='" + s(msg_id_hash) + "' ";
        return this;
    }

    public Message resend_count(int resend_count)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " resend_count='" + s(resend_count) + "' ";
        return this;
    }

    public Message msg_versionEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and  msg_version='" + s(msg_version) + "' ";
        return this;
    }

    public Message message_idEq(long message_id)
    {
        this.sql_where = this.sql_where + " and  message_id='" + s(message_id) + "' ";
        return this;
    }

    public Message orderByIdDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id DESC ";
        return this;
    }

    public Message read(boolean read)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " read='" + b(read) + "' ";
        return this;
    }

    public Message rcvd_timestamp(long rcvd_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " rcvd_timestamp='" + s(rcvd_timestamp) + "' ";
        return this;
    }

    public Message msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and  msg_id_hash='" + s(msg_id_hash) + "' ";
        return this;
    }

    public Message ft_accepted(boolean ft_accepted)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_accepted='" + b(ft_accepted) + "' ";
        return this;
    }

    public Message state(int state)
    {
        Log.i(TAG, "DB:state="+state);
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " state='" + s(state) + "' ";
        return this;
    }

    public Message filetransfer_idEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and  filetransfer_id='" + s(filetransfer_id) + "' ";
        return this;
    }

    public Message filedb_id(long filedb_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filedb_id='" + s(filedb_id) + "' ";
        return this;
    }

    public Message filetransfer_id(long filetransfer_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filetransfer_id='" + s(filetransfer_id) + "' ";
        return this;
    }

    public Message ft_outgoing_started(boolean ft_outgoing_started)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_outgoing_started='" + b(ft_outgoing_started) + "' ";
        return this;
    }

    public Message is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and  is_new='" + b(is_new) + "' ";
        return this;
    }

    public Message is_new(boolean is_new)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " is_new='" + b(is_new) + "' ";
        return this;
    }

    public Message ft_outgoing_queued(boolean ft_outgoing_queued)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_outgoing_queued='" + b(ft_outgoing_queued) + "' ";
        return this;
    }

    public Message ft_outgoing_queuedEq(boolean ft_outgoing_queued)
    {
        this.sql_where = this.sql_where + " and  ft_outgoing_queued='" + b(ft_outgoing_queued) + "' ";
        return this;
    }

    public Message stateNotEq(int state)
    {
        this.sql_where = this.sql_where + " and  state != '" + s(state) + "' ";
        return this;
    }

    public Message msg_at_relay(boolean msg_at_relay)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_at_relay='" + b(msg_at_relay) + "' ";
        return this;
    }

    public Message orderBySent_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp DESC ";
        return this;
    }

    public Message msg_at_relayEq(boolean msg_at_relay)
    {
        this.sql_where = this.sql_where + " and  msg_at_relay='" + b(msg_at_relay) + "' ";
        return this;
    }

    public Message filedb_idEq(long filedb_id)
    {
        this.sql_where = this.sql_where + " and  filedb_id='" + s(filedb_id) + "' ";
        return this;
    }

    public Message msg_idv3_hashEq(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and  msg_idv3_hash='" + s(msg_idv3_hash) + "' ";
        return this;
    }

    public Message msg_idv3_hash(String msg_idv3_hash)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_idv3_hash='" + s(msg_idv3_hash) + "' ";
        return this;
    }

    public Message resend_countLt(int resend_count)
    {
        this.sql_where = this.sql_where + " and  resend_count<'" + s(resend_count) + "' ";
        return this;
    }

    public Message sent_pushEq(int sent_push)
    {
        this.sql_where = this.sql_where + " and  sent_push='" + s(sent_push) + "' ";
        return this;
    }

    public Message sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and  sent_timestamp<'" + s(sent_timestamp) + "' ";
        return this;
    }

    public Message sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and  sent_timestamp>'" + s(sent_timestamp1) + "' and sent_timestamp<'" +
                s(sent_timestamp2) + "' ";
        return this;
    }

    public Message sent_push(int sent_push)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " sent_push='" + s(sent_push) + "' ";
        return this;
    }

    public Message filetransfer_kind(int filetransfer_kind)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filetransfer_kind='" + s(filetransfer_kind) + "' ";
        return this;
    }
}
