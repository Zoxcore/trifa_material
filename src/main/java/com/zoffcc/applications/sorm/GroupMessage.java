/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2022 Zoff <zoff@zoff.cc>
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

import static com.zoffcc.applications.sorm.OrmaDatabase.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;

@Table
public class GroupMessage
{
    private static final String TAG = "DB.GroupMessage";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    public String message_id_tox = ""; // Tox Group Message_ID (4 bytes as hex string lowercase)

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public String group_identifier = "-1"; // f_key -> GroupDB.group_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey; // uppercase

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public int private_message = 0; // 0 -> message to group, 1 -> msg privately to/from peer

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peername = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    public int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    public int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    public long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    public boolean read = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String text = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public boolean was_synced = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_id_hash = null; // 32byte hash (as hex string uppercase)

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String path_name = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String file_name = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public long filesize = 0L;

    static GroupMessage deep_copy(GroupMessage in)
    {
        GroupMessage out = new GroupMessage();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id_tox = in.message_id_tox;
        out.group_identifier = in.group_identifier;
        out.tox_group_peer_pubkey = in.tox_group_peer_pubkey;
        out.private_message = in.private_message;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
        out.tox_group_peername = in.tox_group_peername;
        out.was_synced = in.was_synced;
        out.msg_id_hash = in.msg_id_hash;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.filename_fullpath = in.filename_fullpath;
        out.filesize = in.filesize;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", tox_group_peername=" + tox_group_peername +
                ", tox_peerpubkey=" + "*tox_peerpubkey*" + ", private_message=" + private_message + ", direction=" +
                direction + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
                ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
                ", text=" + "xxxxxx" + ", is_new=" + is_new + ", was_synced=" + was_synced + ", path_name=" + path_name +
                ", file_name=" + file_name + ", filename_fullpath=" + filename_fullpath + ", filesize=" + filesize;
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

    public List<GroupMessage> toList()
    {
        List<GroupMessage> list = new ArrayList<>();

        try
        {
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
                return null;
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                GroupMessage out = new GroupMessage();

                out.id = rs.getLong("id");
                out.message_id_tox = rs.getString("message_id_tox");
                out.group_identifier = rs.getString("group_identifier");
                out.tox_group_peer_pubkey = rs.getString("tox_group_peer_pubkey");
                out.private_message = rs.getInt("private_message");
                out.tox_group_peername = rs.getString("tox_group_peername");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.read = rs.getBoolean("read");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.was_synced = rs.getBoolean("was_synced");
                out.msg_id_hash = rs.getString("msg_id_hash");
                out.path_name = rs.getString("path_name");
                out.file_name = rs.getString("file_name");
                out.filename_fullpath = rs.getString("filename_fullpath");
                out.filesize = rs.getLong("filesize");
                list.add(out);
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
            insert_pstmt_sql="insert into " + this.getClass().getSimpleName() +
                    "(" +
                    "message_id_tox,"+
                    "group_identifier,"+
                    "tox_group_peer_pubkey,"+
                    "private_message,"+
                    "tox_group_peername,"+
                    "direction,"+
                    "TOX_MESSAGE_TYPE,"	+
                    "TRIFA_MESSAGE_TYPE,"	+
                    "sent_timestamp,"+
                    "rcvd_timestamp,"+
                    "read,"+
                    "is_new,"	+
                    "text,"	+
                    "was_synced,"+
                    "msg_id_hash,"+
                    "path_name,"	+
                    "file_name,"	+
                    "filename_fullpath,"	+
                    "filesize"	+
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
                    "?19" +
                    ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);

            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.message_id_tox);
            insert_pstmt.setString(2, this.group_identifier);
            insert_pstmt.setString(3, this.tox_group_peer_pubkey);
            insert_pstmt.setInt(4, this.private_message);
            insert_pstmt.setString(5, this.tox_group_peername);
            insert_pstmt.setInt(6, this.direction);
            insert_pstmt.setInt(7, this.TOX_MESSAGE_TYPE);
            insert_pstmt.setInt(8, this.TRIFA_MESSAGE_TYPE);
            insert_pstmt.setLong(9, this.sent_timestamp);
            insert_pstmt.setLong(10, this.rcvd_timestamp);
            insert_pstmt.setBoolean(11, this.read);
            insert_pstmt.setBoolean(12, this.is_new);
            insert_pstmt.setString(13, this.text);
            insert_pstmt.setBoolean(14, this.was_synced);
            insert_pstmt.setString(15, this.msg_id_hash);
            insert_pstmt.setString(16, this.path_name);
            insert_pstmt.setString(17, this.file_name);
            insert_pstmt.setString(18, this.filename_fullpath);
            insert_pstmt.setLong(19, this.filesize);

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
                    Log.i(TAG, "insertIntoGroupMessage acquire running long (" + (t2 - t1)+ " ms)");
                }
            }
            final long t3 = System.currentTimeMillis();
            insert_pstmt.executeUpdate();
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertIntoGroupMessage sql running long (" + (t4 - t3)+ " ms)");
                }
            }
            insert_pstmt.close();
            ret = get_last_rowid_pstmt();
            orma_semaphore_lastrowid_on_insert.release();
            // @formatter:on
        }
        catch (Exception e)
        {
            orma_semaphore_lastrowid_on_insert.release();
            throw new RuntimeException(e);
        }

        return ret;
    }

    public GroupMessage get(int i)
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

    public GroupMessage limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    public GroupMessage limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public GroupMessage group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier='" + s(group_identifier) + "' ";
        return this;
    }

    public GroupMessage is_new(boolean is_new)
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

    public GroupMessage tox_group_peer_pubkeyNotEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey<>'" + s(tox_group_peer_pubkey) + "' ";
        return this;
    }

    public GroupMessage is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and is_new='" + b(is_new) + "' ";
        return this;
    }

    public GroupMessage idEq(long id)
    {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }

    public GroupMessage orderByIdDesc()
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

    public GroupMessage tox_group_peer_pubkeyEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey='" + s(tox_group_peer_pubkey) + "' ";
        return this;
    }

    public GroupMessage message_id_toxEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and message_id_tox='" + s(message_id_tox) + "' ";
        return this;
    }

    public GroupMessage sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>'" + s(sent_timestamp) + "' ";
        return this;
    }

    public GroupMessage sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<'" + s(sent_timestamp) + "' ";
        return this;
    }

    public GroupMessage textEq(String text)
    {
        this.sql_where = this.sql_where + " and text=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage orderBySent_timestampAsc()
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

    public GroupMessage private_messageEq(int private_message) {
        this.sql_where = this.sql_where + " and private_message='" + s(private_message) + "' ";
        return this;
    }

    public GroupMessage orderByRcvd_timestampAsc() {
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

    public GroupMessage msg_id_hashEq(String msg_id_hash) {
        this.sql_where = this.sql_where + " and msg_id_hash='" + s(msg_id_hash) + "' ";
        return this;
    }

    public GroupMessage directionEq(int direction) {
        this.sql_where = this.sql_where + " and direction='" + s(direction) + "' ";
        return this;
    }

    public GroupMessage readEq(boolean read) {
        this.sql_where = this.sql_where + " and read='" + b(read) + "' ";
        return this;
    }

    public GroupMessage read(boolean read) {
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

    public GroupMessage tox_group_peername(String tox_group_peername) {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_group_peername=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_set_count++;
        return this;
    }
}
