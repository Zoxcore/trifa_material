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

    // ______@@SORMA_END@@______

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
        out.raw_msgv2_bytes = in.raw_msgv2_bytes;
        out.msg_version = in.msg_version;
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
    List<OrmaBindvar> bind_where_vars = new ArrayList<>();
    int bind_where_count = 0;
    List<OrmaBindvar> bind_set_vars = new ArrayList<>();
    int bind_set_count = 0;

    public List<Message> toList()
    {
        List<Message> list = new ArrayList<>();
        try
        {
            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            final long t1 = System.currentTimeMillis();
            PreparedStatement statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where(statement, bind_where_count, bind_where_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return null;
            }
            ResultSet rs = statement.executeQuery();
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
                out.raw_msgv2_bytes = rs.getString("raw_msgv2_bytes");
                out.msg_version = rs.getInt("msg_version");
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
                    "("
                    + "message_id"
                    + ",tox_friendpubkey"
                    + ",direction"
                    + ",TOX_MESSAGE_TYPE"
                    + ",TRIFA_MESSAGE_TYPE"
                    + ",state"
                    + ",ft_accepted"
                    + ",ft_outgoing_started"
                    + ",filedb_id"
                    + ",filetransfer_id"
                    + ",sent_timestamp"
                    + ",sent_timestamp_ms"
                    + ",rcvd_timestamp"
                    + ",rcvd_timestamp_ms"
                    + ",read"
                    + ",send_retries"
                    + ",is_new"
                    + ",text"
                    + ",filename_fullpath"
                    + ",msg_id_hash"
                    + ",raw_msgv2_bytes"
                    + ",msg_version"
                    + ",resend_count"
                    + ",ft_outgoing_queued"
                    + ",msg_at_relay"
                    + ",msg_idv3_hash"
                    + ",sent_push"
                    + ",filetransfer_kind"
                    + ")" +
                    "values" +
                    "("
                    + "?1"
                    + ",?2"
                    + ",?3"
                    + ",?4"
                    + ",?5"
                    + ",?6"
                    + ",?7"
                    + ",?8"
                    + ",?9"
                    + ",?10"
                    + ",?11"
                    + ",?12"
                    + ",?13"
                    + ",?14"
                    + ",?15"
                    + ",?16"
                    + ",?17"
                    + ",?18"
                    + ",?19"
                    + ",?20"
                    + ",?21"
                    + ",?22"
                    + ",?23"
                    + ",?24"
                    + ",?25"
                    + ",?26"
                    + ",?27"
                    + ",?28"
                    + ")";

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
            insert_pstmt.setString(21, this.raw_msgv2_bytes);
            insert_pstmt.setInt(22, this.msg_version);
            insert_pstmt.setInt(23, this.resend_count);
            insert_pstmt.setBoolean(24, this.ft_outgoing_queued);
            insert_pstmt.setBoolean(25, this.msg_at_relay);
            insert_pstmt.setString(26, this.msg_idv3_hash);
            insert_pstmt.setInt(27, this.sent_push);
            insert_pstmt.setInt(28, this.filetransfer_kind);
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
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" acquire running long (" + (t2 - t1)+ " ms)");
                }
            }

            final long t3 = System.currentTimeMillis();
            insert_pstmt.executeUpdate();
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" sql running long (" + (t4 - t3)+ " ms)");
                }
            }

            final long t5 = System.currentTimeMillis();
            insert_pstmt.close();
            final long t6 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t6 - t5) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" statement close running long (" + (t6 - t5)+ " ms)");
                }
            }

            final long t7 = System.currentTimeMillis();
            ret = get_last_rowid_pstmt();
            final long t8 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t8 - t7) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" getLastRowId running long (" + (t8 - t7)+ " ms)");
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
            final String sql = this.sql_start + " " + this.sql_set + " " + this.sql_where;
            log_bindvars_where_and_set(sql, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars);
            PreparedStatement statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where_and_set(statement, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return;
            }
            statement.executeUpdate();
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
            this.sql_start = "SELECT count(*) as count FROM " + this.getClass().getSimpleName();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            PreparedStatement statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where(statement, bind_where_count, bind_where_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return 0;
            }
            ResultSet rs = statement.executeQuery();
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


    // ----------------- Set funcs ---------------------- //
    public Message id(long id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " id=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " message_id=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_set_count++;
        return this;
    }

    public Message tox_friendpubkey(String tox_friendpubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_friendpubkey=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_set_count++;
        return this;
    }

    public Message direction(int direction)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " direction=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_set_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPE(int TOX_MESSAGE_TYPE)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_set_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPE(int TRIFA_MESSAGE_TYPE)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TRIFA_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_set_count++;
        return this;
    }

    public Message state(int state)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " state=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " ft_accepted=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " ft_outgoing_started=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " filedb_id=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " filetransfer_id=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " sent_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_set_count++;
        return this;
    }

    public Message sent_timestamp_ms(long sent_timestamp_ms)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " sent_timestamp_ms=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " rcvd_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_set_count++;
        return this;
    }

    public Message rcvd_timestamp_ms(long rcvd_timestamp_ms)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " rcvd_timestamp_ms=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " read=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_set_count++;
        return this;
    }

    public Message send_retries(int send_retries)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " send_retries=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " is_new=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " text=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " filename_fullpath=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " msg_id_hash=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " raw_msgv2_bytes=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " msg_version=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " resend_count=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " ft_outgoing_queued=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " msg_at_relay=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " msg_idv3_hash=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " sent_push=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " filetransfer_kind=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public Message idEq(long id)
    {
        this.sql_where = this.sql_where + " and id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idLt(long id)
    {
        this.sql_where = this.sql_where + " and id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idLe(long id)
    {
        this.sql_where = this.sql_where + " and id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idGt(long id)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idGe(long id)
    {
        this.sql_where = this.sql_where + " and id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public Message message_idEq(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idNotEq(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idLt(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idLe(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idGt(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idGe(long message_id)
    {
        this.sql_where = this.sql_where + " and message_id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idBetween(long message_id1, long message_id2)
    {
        this.sql_where = this.sql_where + " and message_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and message_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id2));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyNotEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyLike(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyNotLike(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and direction<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and direction<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and direction>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and direction>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and direction>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPENotEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPELt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPELe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEGt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEGe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEBetween(int TOX_MESSAGE_TYPE1, int TOX_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPENotEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPELt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPELe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEGt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEGe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEBetween(int TRIFA_MESSAGE_TYPE1, int TRIFA_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public Message stateEq(int state)
    {
        this.sql_where = this.sql_where + " and state=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateNotEq(int state)
    {
        this.sql_where = this.sql_where + " and state<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateLt(int state)
    {
        this.sql_where = this.sql_where + " and state<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateLe(int state)
    {
        this.sql_where = this.sql_where + " and state<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateGt(int state)
    {
        this.sql_where = this.sql_where + " and state>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateGe(int state)
    {
        this.sql_where = this.sql_where + " and state>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateBetween(int state1, int state2)
    {
        this.sql_where = this.sql_where + " and state>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and state<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state2));
        bind_where_count++;
        return this;
    }

    public Message ft_acceptedEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and ft_accepted=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Message ft_acceptedNotEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and ft_accepted<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_startedEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and ft_outgoing_started=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_startedNotEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and ft_outgoing_started<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Message filedb_idEq(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idNotEq(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idLt(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idLe(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idGt(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idGe(long filedb_id)
    {
        this.sql_where = this.sql_where + " and filedb_id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idBetween(long filedb_id1, long filedb_id2)
    {
        this.sql_where = this.sql_where + " and filedb_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filedb_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id2));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idNotEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idLt(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idLe(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idGt(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idGe(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and filetransfer_id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idBetween(long filetransfer_id1, long filetransfer_id2)
    {
        this.sql_where = this.sql_where + " and filetransfer_id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filetransfer_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id2));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampNotEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampLe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampGe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msEq(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msNotEq(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msLt(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msLe(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msGt(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msGe(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msBetween(long sent_timestamp_ms1, long sent_timestamp_ms2)
    {
        this.sql_where = this.sql_where + " and sent_timestamp_ms>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms2));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampNotEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampLt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampLe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampGt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampGe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampBetween(long rcvd_timestamp1, long rcvd_timestamp2)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp2));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msEq(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msNotEq(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msLt(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msLe(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msGt(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msGe(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msBetween(long rcvd_timestamp_ms1, long rcvd_timestamp_ms2)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp_ms>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms2));
        bind_where_count++;
        return this;
    }

    public Message readEq(boolean read)
    {
        this.sql_where = this.sql_where + " and read=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public Message readNotEq(boolean read)
    {
        this.sql_where = this.sql_where + " and read<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public Message send_retriesEq(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesNotEq(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesLt(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesLe(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesGt(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesGe(int send_retries)
    {
        this.sql_where = this.sql_where + " and send_retries>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesBetween(int send_retries1, int send_retries2)
    {
        this.sql_where = this.sql_where + " and send_retries>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and send_retries<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries2));
        bind_where_count++;
        return this;
    }

    public Message is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and is_new=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public Message is_newNotEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and is_new<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public Message textEq(String text)
    {
        this.sql_where = this.sql_where + " and text=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textNotEq(String text)
    {
        this.sql_where = this.sql_where + " and text<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textLike(String text)
    {
        this.sql_where = this.sql_where + " and text LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textNotLike(String text)
    {
        this.sql_where = this.sql_where + " and text NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathNotEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathNotLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashNotEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashNotLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesEq(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and raw_msgv2_bytes=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesNotEq(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and raw_msgv2_bytes<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesLike(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and raw_msgv2_bytes LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesNotLike(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and raw_msgv2_bytes NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message msg_versionEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionNotEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionLt(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionLe(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionGt(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionGe(int msg_version)
    {
        this.sql_where = this.sql_where + " and msg_version>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionBetween(int msg_version1, int msg_version2)
    {
        this.sql_where = this.sql_where + " and msg_version>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and msg_version<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version2));
        bind_where_count++;
        return this;
    }

    public Message resend_countEq(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countNotEq(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countLt(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countLe(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countGt(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countGe(int resend_count)
    {
        this.sql_where = this.sql_where + " and resend_count>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countBetween(int resend_count1, int resend_count2)
    {
        this.sql_where = this.sql_where + " and resend_count>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and resend_count<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count2));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_queuedEq(boolean ft_outgoing_queued)
    {
        this.sql_where = this.sql_where + " and ft_outgoing_queued=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_queuedNotEq(boolean ft_outgoing_queued)
    {
        this.sql_where = this.sql_where + " and ft_outgoing_queued<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_where_count++;
        return this;
    }

    public Message msg_at_relayEq(boolean msg_at_relay)
    {
        this.sql_where = this.sql_where + " and msg_at_relay=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_where_count++;
        return this;
    }

    public Message msg_at_relayNotEq(boolean msg_at_relay)
    {
        this.sql_where = this.sql_where + " and msg_at_relay<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashEq(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and msg_idv3_hash=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashNotEq(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and msg_idv3_hash<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashLike(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and msg_idv3_hash LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashNotLike(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and msg_idv3_hash NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message sent_pushEq(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushNotEq(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushLt(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushLe(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushGt(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushGe(int sent_push)
    {
        this.sql_where = this.sql_where + " and sent_push>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushBetween(int sent_push1, int sent_push2)
    {
        this.sql_where = this.sql_where + " and sent_push>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_push<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push2));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindEq(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindNotEq(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindLt(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindLe(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindGt(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindGe(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindBetween(int filetransfer_kind1, int filetransfer_kind2)
    {
        this.sql_where = this.sql_where + " and filetransfer_kind>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filetransfer_kind<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind2));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public Message orderByIdAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id ASC ";
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

    public Message orderByMessage_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " message_id ASC ";
        return this;
    }

    public Message orderByMessage_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " message_id DESC ";
        return this;
    }

    public Message orderByTox_friendpubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_friendpubkey ASC ";
        return this;
    }

    public Message orderByTox_friendpubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_friendpubkey DESC ";
        return this;
    }

    public Message orderByDirectionAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " direction ASC ";
        return this;
    }

    public Message orderByDirectionDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " direction DESC ";
        return this;
    }

    public Message orderByTOX_MESSAGE_TYPEAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_MESSAGE_TYPE ASC ";
        return this;
    }

    public Message orderByTOX_MESSAGE_TYPEDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_MESSAGE_TYPE DESC ";
        return this;
    }

    public Message orderByTRIFA_MESSAGE_TYPEAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TRIFA_MESSAGE_TYPE ASC ";
        return this;
    }

    public Message orderByTRIFA_MESSAGE_TYPEDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TRIFA_MESSAGE_TYPE DESC ";
        return this;
    }

    public Message orderByStateAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " state ASC ";
        return this;
    }

    public Message orderByStateDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " state DESC ";
        return this;
    }

    public Message orderByFt_acceptedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_accepted ASC ";
        return this;
    }

    public Message orderByFt_acceptedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_accepted DESC ";
        return this;
    }

    public Message orderByFt_outgoing_startedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_outgoing_started ASC ";
        return this;
    }

    public Message orderByFt_outgoing_startedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_outgoing_started DESC ";
        return this;
    }

    public Message orderByFiledb_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filedb_id ASC ";
        return this;
    }

    public Message orderByFiledb_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filedb_id DESC ";
        return this;
    }

    public Message orderByFiletransfer_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filetransfer_id ASC ";
        return this;
    }

    public Message orderByFiletransfer_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filetransfer_id DESC ";
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

    public Message orderBySent_timestamp_msDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp_ms DESC ";
        return this;
    }

    public Message orderByRcvd_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " rcvd_timestamp ASC ";
        return this;
    }

    public Message orderByRcvd_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " rcvd_timestamp DESC ";
        return this;
    }

    public Message orderByRcvd_timestamp_msAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " rcvd_timestamp_ms ASC ";
        return this;
    }

    public Message orderByRcvd_timestamp_msDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " rcvd_timestamp_ms DESC ";
        return this;
    }

    public Message orderByReadAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " read ASC ";
        return this;
    }

    public Message orderByReadDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " read DESC ";
        return this;
    }

    public Message orderBySend_retriesAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " send_retries ASC ";
        return this;
    }

    public Message orderBySend_retriesDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " send_retries DESC ";
        return this;
    }

    public Message orderByIs_newAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " is_new ASC ";
        return this;
    }

    public Message orderByIs_newDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " is_new DESC ";
        return this;
    }

    public Message orderByTextAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " text ASC ";
        return this;
    }

    public Message orderByTextDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " text DESC ";
        return this;
    }

    public Message orderByFilename_fullpathAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filename_fullpath ASC ";
        return this;
    }

    public Message orderByFilename_fullpathDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filename_fullpath DESC ";
        return this;
    }

    public Message orderByMsg_id_hashAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_id_hash ASC ";
        return this;
    }

    public Message orderByMsg_id_hashDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_id_hash DESC ";
        return this;
    }

    public Message orderByRaw_msgv2_bytesAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " raw_msgv2_bytes ASC ";
        return this;
    }

    public Message orderByRaw_msgv2_bytesDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " raw_msgv2_bytes DESC ";
        return this;
    }

    public Message orderByMsg_versionAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_version ASC ";
        return this;
    }

    public Message orderByMsg_versionDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_version DESC ";
        return this;
    }

    public Message orderByResend_countAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " resend_count ASC ";
        return this;
    }

    public Message orderByResend_countDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " resend_count DESC ";
        return this;
    }

    public Message orderByFt_outgoing_queuedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_outgoing_queued ASC ";
        return this;
    }

    public Message orderByFt_outgoing_queuedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " ft_outgoing_queued DESC ";
        return this;
    }

    public Message orderByMsg_at_relayAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_at_relay ASC ";
        return this;
    }

    public Message orderByMsg_at_relayDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_at_relay DESC ";
        return this;
    }

    public Message orderByMsg_idv3_hashAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_idv3_hash ASC ";
        return this;
    }

    public Message orderByMsg_idv3_hashDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msg_idv3_hash DESC ";
        return this;
    }

    public Message orderBySent_pushAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_push ASC ";
        return this;
    }

    public Message orderBySent_pushDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_push DESC ";
        return this;
    }

    public Message orderByFiletransfer_kindAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filetransfer_kind ASC ";
        return this;
    }

    public Message orderByFiletransfer_kindDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filetransfer_kind DESC ";
        return this;
    }



}

