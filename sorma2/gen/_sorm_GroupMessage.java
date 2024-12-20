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
    private static final Str ing TAG = "DB.GroupMessage";

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
    public int tox_group_peer_role = TOX_GROUP_ROLE_USER.value;

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
        out.tox_group_peername = in.tox_group_peername;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
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
                    + "message_id_tox"
                    + ",group_identifier"
                    + ",tox_group_peer_pubkey"
                    + ",private_message"
                    + ",tox_group_peername"
                    + ",direction"
                    + ",TOX_MESSAGE_TYPE"
                    + ",TRIFA_MESSAGE_TYPE"
                    + ",sent_timestamp"
                    + ",rcvd_timestamp"
                    + ",read"
                    + ",is_new"
                    + ",text"
                    + ",was_synced"
                    + ",msg_id_hash"
                    + ",path_name"
                    + ",file_name"
                    + ",filename_fullpath"
                    + ",filesize"
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
                    + ")";

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

    public GroupMessage limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
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


    // ----------------- Set funcs ---------------------- //
    public GroupMessage id(long id)
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

    public GroupMessage message_id_tox(String message_id_tox)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " message_id_tox=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_set_count++;
        return this;
    }

    public GroupMessage group_identifier(String group_identifier)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " group_identifier=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey(String tox_group_peer_pubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_group_peer_pubkey=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_set_count++;
        return this;
    }

    public GroupMessage private_message(int private_message)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " private_message=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peername(String tox_group_peername)
    {
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

    public GroupMessage direction(int direction)
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

    public GroupMessage TOX_MESSAGE_TYPE(int TOX_MESSAGE_TYPE)
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

    public GroupMessage TRIFA_MESSAGE_TYPE(int TRIFA_MESSAGE_TYPE)
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

    public GroupMessage sent_timestamp(long sent_timestamp)
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

    public GroupMessage rcvd_timestamp(long rcvd_timestamp)
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

    public GroupMessage read(boolean read)
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
        this.sql_set = this.sql_set + " is_new=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_set_count++;
        return this;
    }

    public GroupMessage text(String text)
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

    public GroupMessage was_synced(boolean was_synced)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " was_synced=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_set_count++;
        return this;
    }

    public GroupMessage msg_id_hash(String msg_id_hash)
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

    public GroupMessage path_name(String path_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " path_name=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_set_count++;
        return this;
    }

    public GroupMessage file_name(String file_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " file_name=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_set_count++;
        return this;
    }

    public GroupMessage filename_fullpath(String filename_fullpath)
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

    public GroupMessage filesize(long filesize)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filesize=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public GroupMessage idEq(long id)
    {
        this.sql_where = this.sql_where + " and id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idLt(long id)
    {
        this.sql_where = this.sql_where + " and id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idLe(long id)
    {
        this.sql_where = this.sql_where + " and id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idGt(long id)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idGe(long id)
    {
        this.sql_where = this.sql_where + " and id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and message_id_tox=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxNotEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and message_id_tox<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and message_id_tox LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxNotLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and message_id_tox NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierNotEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierNotLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyNotEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyNotLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and tox_group_peer_pubkey NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageEq(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageNotEq(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageLt(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageLe(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageGt(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageGe(int private_message)
    {
        this.sql_where = this.sql_where + " and private_message>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageBetween(int private_message1, int private_message2)
    {
        this.sql_where = this.sql_where + " and private_message>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and private_message<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message2));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameIsNull()
    {
        this.sql_where = this.sql_where + " and tox_group_peername IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peernameIsNotNull()
    {
        this.sql_where = this.sql_where + " and tox_group_peername IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peernameEq(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and tox_group_peername=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameNotEq(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and tox_group_peername<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameLike(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and tox_group_peername LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameNotLike(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and tox_group_peername NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and direction<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and direction<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and direction>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and direction>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and direction>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPENotEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPELt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPELe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEGt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEGe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEBetween(int TOX_MESSAGE_TYPE1, int TOX_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and TOX_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPENotEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPELt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPELe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEGt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEGe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEBetween(int TRIFA_MESSAGE_TYPE1, int TRIFA_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampNotEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampLe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampGe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and sent_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampNotEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampLt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampLe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampGt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampGe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampBetween(long rcvd_timestamp1, long rcvd_timestamp2)
    {
        this.sql_where = this.sql_where + " and rcvd_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage readEq(boolean read)
    {
        this.sql_where = this.sql_where + " and read=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public GroupMessage readNotEq(boolean read)
    {
        this.sql_where = this.sql_where + " and read<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public GroupMessage is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and is_new=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public GroupMessage is_newNotEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and is_new<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public GroupMessage textEq(String text)
    {
        this.sql_where = this.sql_where + " and text=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textNotEq(String text)
    {
        this.sql_where = this.sql_where + " and text<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textLike(String text)
    {
        this.sql_where = this.sql_where + " and text LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textNotLike(String text)
    {
        this.sql_where = this.sql_where + " and text NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage was_syncedEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and was_synced=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public GroupMessage was_syncedNotEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and was_synced<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashNotEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashNotLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and msg_id_hash NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameEq(String path_name)
    {
        this.sql_where = this.sql_where + " and path_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameNotEq(String path_name)
    {
        this.sql_where = this.sql_where + " and path_name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameLike(String path_name)
    {
        this.sql_where = this.sql_where + " and path_name LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameNotLike(String path_name)
    {
        this.sql_where = this.sql_where + " and path_name NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameEq(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameNotEq(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameLike(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameNotLike(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathNotEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathNotLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and filename_fullpath NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeEq(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeNotEq(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeLt(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeLe(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeGt(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeGe(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeBetween(long filesize1, long filesize2)
    {
        this.sql_where = this.sql_where + " and filesize>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filesize<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize2));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public GroupMessage orderByIdAsc()
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

    public GroupMessage orderByMessage_id_toxAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " message_id_tox ASC ";
        return this;
    }

    public GroupMessage orderByMessage_id_toxDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " message_id_tox DESC ";
        return this;
    }

    public GroupMessage orderByGroup_identifierAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " group_identifier ASC ";
        return this;
    }

    public GroupMessage orderByGroup_identifierDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " group_identifier DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_peer_pubkey ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_peer_pubkey DESC ";
        return this;
    }

    public GroupMessage orderByPrivate_messageAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " private_message ASC ";
        return this;
    }

    public GroupMessage orderByPrivate_messageDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " private_message DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peernameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_peername ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peernameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_peername DESC ";
        return this;
    }

    public GroupMessage orderByDirectionAsc()
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

    public GroupMessage orderByDirectionDesc()
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

    public GroupMessage orderByTOX_MESSAGE_TYPEAsc()
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

    public GroupMessage orderByTOX_MESSAGE_TYPEDesc()
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

    public GroupMessage orderByTRIFA_MESSAGE_TYPEAsc()
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

    public GroupMessage orderByTRIFA_MESSAGE_TYPEDesc()
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

    public GroupMessage orderBySent_timestampDesc()
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

    public GroupMessage orderByRcvd_timestampAsc()
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

    public GroupMessage orderByRcvd_timestampDesc()
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

    public GroupMessage orderByReadAsc()
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

    public GroupMessage orderByReadDesc()
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

    public GroupMessage orderByIs_newAsc()
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

    public GroupMessage orderByIs_newDesc()
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

    public GroupMessage orderByTextAsc()
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

    public GroupMessage orderByTextDesc()
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

    public GroupMessage orderByWas_syncedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " was_synced ASC ";
        return this;
    }

    public GroupMessage orderByWas_syncedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " was_synced DESC ";
        return this;
    }

    public GroupMessage orderByMsg_id_hashAsc()
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

    public GroupMessage orderByMsg_id_hashDesc()
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

    public GroupMessage orderByPath_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " path_name ASC ";
        return this;
    }

    public GroupMessage orderByPath_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " path_name DESC ";
        return this;
    }

    public GroupMessage orderByFile_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " file_name ASC ";
        return this;
    }

    public GroupMessage orderByFile_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " file_name DESC ";
        return this;
    }

    public GroupMessage orderByFilename_fullpathAsc()
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

    public GroupMessage orderByFilename_fullpathDesc()
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

    public GroupMessage orderByFilesizeAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filesize ASC ";
        return this;
    }

    public GroupMessage orderByFilesizeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " filesize DESC ";
        return this;
    }



}

