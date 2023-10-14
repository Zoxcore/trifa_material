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
    long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    public String message_id_tox = ""; // Tox Group Message_ID (4 bytes as hex string lowercase)

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public String group_identifier = "-1"; // f_key -> GroupDB.group_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey;

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
    String msg_id_hash = null; // 32byte hash

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

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", tox_group_peername=" + tox_group_peername +
                ", tox_peerpubkey=" + "*tox_peerpubkey*" + ", private_message=" + private_message + ", direction=" +
                direction + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
                ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
                ", text=" + "xxxxxx" + ", is_new=" + is_new + ", was_synced=" + was_synced;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<GroupMessage> toList()
    {
        List<GroupMessage> list = new ArrayList<>();

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit);
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
            // @formatter:off
            Statement statement = sqldb.createStatement();
            final String sql_str="insert into " + this.getClass().getSimpleName() +
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
                    "msg_id_hash"+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.message_id_tox)+"'," +
                    "'"+s(this.group_identifier)+"'," +
                    "'"+s(this.tox_group_peer_pubkey)+"'," +
                    "'"+s(this.private_message)+"'," +
                    "'"+s(this.tox_group_peername)+"'," +
                    "'"+s(this.direction)+"'," +
                    "'"+s(this.TOX_MESSAGE_TYPE)+"'," +
                    "'"+s(this.TRIFA_MESSAGE_TYPE)+"'," +
                    "'"+s(this.sent_timestamp)+"'," +
                    "'"+s(this.rcvd_timestamp)+"'," +
                    "'"+b(this.read)+"'," +
                    "'"+b(this.is_new)+"'," +
                    "'"+s(this.text)+"'," +
                    "'"+b(this.was_synced)+"'," +
                    "'"+s(this.msg_id_hash)+"'" +
                    ")";

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql_str);
            }

            statement.execute(sql_str);
            ret = get_last_rowid(statement);
            // @formatter:on

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
        this.sql_where = this.sql_where + " and text='" + s(text) + "' ";
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
}
