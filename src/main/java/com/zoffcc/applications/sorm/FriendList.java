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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;

@Table
public class FriendList
{
    private static final String TAG = "DB.FriendList";

    // pubkey is always saved as UPPER CASE hex string!! -----------------
    @PrimaryKey
    public String tox_public_key_string = "";
    // pubkey is always saved as UPPER CASE hex string!! -----------------

    @Column
    @Nullable
    String name;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String alias_name;

    @Column
    @Nullable
    String status_message;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_real; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_on_off_real; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_USER_STATUS; // 0 --> NONE, 1 --> online AWAY, 2 --> online BUSY

    @Column
    @Nullable
    String avatar_pathname = null;

    @Column
    @Nullable
    String avatar_filename = null;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean avatar_update = false; // has avatar changed for this friend?

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long avatar_update_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this friend?

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int sort = 0;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp_real = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long added_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean is_relay = false;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String push_url;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public long capabilities = 0;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    long msgv3_capability = 0;

    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.name = in.name;
        out.status_message = in.status_message;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_real = in.TOX_CONNECTION_real;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.TOX_CONNECTION_on_off_real = in.TOX_CONNECTION_on_off_real;
        out.TOX_USER_STATUS = in.TOX_USER_STATUS;
        out.avatar_filename = in.avatar_filename;
        out.avatar_pathname = in.avatar_pathname;
        out.avatar_update = in.avatar_update;
        out.notification_silent = in.notification_silent;
        out.sort = in.sort;
        out.last_online_timestamp = in.last_online_timestamp;
        out.last_online_timestamp_real = in.last_online_timestamp_real;
        out.alias_name = in.alias_name;
        out.is_relay = in.is_relay;
        out.avatar_update_timestamp = in.avatar_update_timestamp;
        out.added_timestamp = in.added_timestamp;
        out.push_url = in.push_url;
        out.capabilities = in.capabilities;
        out.msgv3_capability = in.msgv3_capability;

        return out;
    }

    @Override
    public String toString()
    {
        try
        {
            return "tox_public_key_string=" + tox_public_key_string.substring(0, 4) + ", is_relay=" + is_relay +
                    ", name=" + name + ", status_message=" + status_message + ", TOX_CONNECTION=" + TOX_CONNECTION +
                    ", TOX_CONNECTION_on_off=" + TOX_CONNECTION_on_off + ", TOX_CONNECTION_real=" + TOX_CONNECTION_real +
                    ", TOX_USER_STATUS=" + TOX_USER_STATUS + ", avatar_pathname=" + avatar_pathname +
                    ", avatar_filename=" + avatar_filename + ", notification_silent=" + notification_silent + ", sort=" +
                    sort + ", last_online_timestamp=" + last_online_timestamp + ", alias_name=" + alias_name +
                    ", avatar_update=" + avatar_update + ", added_timestamp=" + added_timestamp + ", push_url=" +
                    "*****";
        }
        catch (Exception e)
        {
            return "*Exception*";
        }
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<FriendList> toList()
    {
        List<FriendList> fl = new ArrayList<>();

        try
        {
            Statement statement = sqldb.createStatement();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }

            ResultSet rs = statement.executeQuery(sql);
            while (rs.next())
            {
                FriendList f = new FriendList();
                f.tox_public_key_string = rs.getString("tox_public_key_string");
                f.name = rs.getString("name");
                f.status_message = rs.getString("status_message");
                f.TOX_CONNECTION = rs.getInt("TOX_CONNECTION");
                f.TOX_CONNECTION_real = rs.getInt("TOX_CONNECTION_real");
                f.TOX_CONNECTION_on_off = rs.getInt("TOX_CONNECTION_on_off");
                f.TOX_CONNECTION_on_off_real = rs.getInt("TOX_CONNECTION_on_off_real");
                f.TOX_USER_STATUS = rs.getInt("TOX_USER_STATUS");
                f.avatar_filename = rs.getString("avatar_filename");
                f.avatar_pathname = rs.getString("avatar_pathname");
                f.avatar_update = rs.getBoolean("avatar_update");
                f.notification_silent = rs.getBoolean("notification_silent");
                f.sort = rs.getInt("sort");
                f.last_online_timestamp = rs.getLong("last_online_timestamp");
                f.last_online_timestamp_real = rs.getLong("last_online_timestamp_real");
                f.alias_name = rs.getString("alias_name");
                f.is_relay = rs.getBoolean("is_relay");
                f.avatar_update_timestamp = rs.getLong("avatar_update_timestamp");
                f.added_timestamp = rs.getLong("added_timestamp");
                f.push_url = rs.getString("push_url");
                f.capabilities = rs.getLong("capabilities");
                f.msgv3_capability = rs.getLong("msgv3_capability");

                fl.add(f);
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

        return fl;
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
                    "tox_public_key_string,"	+
                    "name,"+
                    "alias_name,"+
                    "status_message,"	+
                    "TOX_CONNECTION,"	+
                    "TOX_CONNECTION_real,"+
                    "TOX_CONNECTION_on_off,"+
                    "TOX_CONNECTION_on_off_real,"+
                    "TOX_USER_STATUS,"	+
                    "avatar_pathname,"+
                    "avatar_filename,"+
                    "avatar_update,"+
                    "avatar_update_timestamp,"+
                    "notification_silent,"	+
                    "sort,"+
                    "last_online_timestamp,"+
                    "last_online_timestamp_real,"+
                    "added_timestamp,"+
                    "is_relay,"	+
                    "push_url,"	+
                    "capabilities,"	+
                    "msgv3_capability"	+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.tox_public_key_string)+"'," +
                    "'"+s(this.name)+"'," +
                    "'"+s(this.alias_name)+"'," +
                    "'"+s(this.status_message)+"'," +
                    "'"+s(this.TOX_CONNECTION)+"'," +
                    "'"+s(this.TOX_CONNECTION_real)+"'," +
                    "'"+s(this.TOX_CONNECTION_on_off)+"'," +
                    "'"+s(this.TOX_CONNECTION_on_off_real)+"'," +
                    "'"+s(this.TOX_USER_STATUS)+"'," +
                    "'"+s(this.avatar_pathname)+"'," +
                    "'"+s(this.avatar_filename)+"'," +
                    "'"+b(this.avatar_update)+"'," +
                    "'"+s(this.avatar_update_timestamp)+"'," +
                    "'"+b(this.notification_silent)+"'," +
                    "'"+s(this.sort)+"'," +
                    "'"+s(this.last_online_timestamp)+"'," +
                    "'"+s(this.last_online_timestamp_real)+"'," +
                    "'"+s(this.added_timestamp)+"'," +
                    "'"+b(this.is_relay)+"'," +
                    "'"+s(this.push_url)+"'," +
                    "'"+s(this.capabilities)+"'," +
                    "'"+s(this.msgv3_capability)+"'" +
                    ")";

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql_str);
            }

            orma_semaphore_lastrowid_on_insert.acquire();
            statement.execute(sql_str);
            ret = get_last_rowid(statement);
            orma_semaphore_lastrowid_on_insert.release();
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
            orma_semaphore_lastrowid_on_insert.release();
            throw new RuntimeException(e);
        }

        return ret;
    }

    public FriendList get(int i)
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

    public FriendList limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public FriendList tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string='" + s(tox_public_key_string) + "' ";
        return this;
    }

    public FriendList is_relayNotEq(boolean b)
    {
        this.sql_where = this.sql_where + " and is_relay<>'" + b(b) + "' ";
        return this;
    }

    public FriendList added_timestampGt(long l)
    {
        this.sql_where = this.sql_where + " and added_timestamp>'" + s(l) + "' ";
        return this;
    }

    public FriendList orderByTOX_CONNECTION_on_offDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_on_off DESC ";
        return this;
    }

    public FriendList orderByNotification_silentAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " Notification_silent ASC ";
        return this;
    }

    public FriendList orderByLast_online_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " Last_online_timestamp DESC ";
        return this;
    }

    public FriendList added_timestampLe(long l)
    {
        this.sql_where = this.sql_where + " and added_timestamp <= '" + s(l) + "' ";
        return this;
    }

    public FriendList name(String name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " name='" + s(name) + "' ";
        return this;
    }

    public FriendList push_url(String push_url)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " push_url='" + s(push_url) + "' ";
        return this;
    }

    public FriendList status_message(String status_message)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " status_message='" + s(status_message) + "' ";
        return this;
    }

    public FriendList TOX_CONNECTION(int TOX_CONNECTION)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_CONNECTION='" + s(TOX_CONNECTION) + "' ";
        return this;
    }

    public FriendList TOX_CONNECTION_on_off(int TOX_CONNECTION_on_off)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_CONNECTION_on_off='" + s(TOX_CONNECTION_on_off) + "' ";
        return this;
    }

    public FriendList TOX_USER_STATUS(int TOX_USER_STATUS)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_USER_STATUS='" + s(TOX_USER_STATUS) + "' ";
        return this;
    }

    public FriendList TOX_CONNECTION_real(int TOX_CONNECTION_real)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_CONNECTION_real='" + s(TOX_CONNECTION_real) + "' ";
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_real(int TOX_CONNECTION_on_off_real)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " TOX_CONNECTION_on_off_real='" + s(TOX_CONNECTION_on_off_real) + "' ";
        return this;
    }

    public FriendList last_online_timestamp_real(long last_online_timestamp_real)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " last_online_timestamp_real='" + s(last_online_timestamp_real) + "' ";
        return this;
    }

    public FriendList last_online_timestamp(long last_online_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " last_online_timestamp='" + s(last_online_timestamp) + "' ";
        return this;
    }

    public FriendList last_online_timestampEq(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp='" + s(last_online_timestamp) + "' ";
        return this;
    }

    public FriendList last_online_timestamp_realEq(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real='" + s(last_online_timestamp_real) + "' ";
        return this;
    }

    public FriendList is_relay(boolean is_relay)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " is_relay='" + b(is_relay) + "' ";
        return this;
    }

    public FriendList alias_name(String alias_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " alias_name='" + s(alias_name) + "' ";
        return this;
    }

    public FriendList avatar_pathname(String avatar_pathname)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_pathname='" + s(avatar_pathname) + "' ";
        return this;
    }

    public FriendList avatar_filename(String avatar_filename)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_filename='" + s(avatar_filename) + "' ";
        return this;
    }

    public FriendList avatar_update(boolean avatar_update)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_update='" + b(avatar_update) + "' ";
        return this;
    }

    public FriendList avatar_update_timestamp(long avatar_update_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_update_timestamp='" + s(avatar_update_timestamp) + "' ";
        return this;
    }

    public FriendList msgv3_capability(long msgv3_capability)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msgv3_capability='" + s(msgv3_capability) + "' ";
        return this;
    }

    public FriendList capabilities(long capabilities)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " capabilities='" + s(capabilities) + "' ";
        return this;
    }
}
