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
    public String name;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String alias_name;

    @Column
    @Nullable
    public String status_message;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_real; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_on_off_real; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_USER_STATUS; // 0 --> NONE, 1 --> online AWAY, 2 --> online BUSY

    @Column
    @Nullable
    public String avatar_pathname = null;

    @Column
    @Nullable
    public String avatar_filename = null;

    @Column
    @Nullable
    public String avatar_hex = null;

    @Column
    @Nullable
    public String avatar_hash_hex = null;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    public boolean avatar_update = false; // has avatar changed for this friend?

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public long avatar_update_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    public boolean notification_silent = false; // show notifications for this friend?

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int sort = 0;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public long last_online_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public long last_online_timestamp_real = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public long added_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    public boolean is_relay = false;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String push_url;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public long capabilities = 0;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public long msgv3_capability = 0;

    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.name = in.name;
        out.alias_name = in.alias_name;
        out.status_message = in.status_message;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_real = in.TOX_CONNECTION_real;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.TOX_CONNECTION_on_off_real = in.TOX_CONNECTION_on_off_real;
        out.TOX_USER_STATUS = in.TOX_USER_STATUS;
        out.avatar_pathname = in.avatar_pathname;
        out.avatar_filename = in.avatar_filename;
        out.avatar_hex = in.avatar_hex;
        out.avatar_hash_hex = in.avatar_hash_hex;
        out.avatar_update = in.avatar_update;
        out.avatar_update_timestamp = in.avatar_update_timestamp;
        out.notification_silent = in.notification_silent;
        out.sort = in.sort;
        out.last_online_timestamp = in.last_online_timestamp;
        out.last_online_timestamp_real = in.last_online_timestamp_real;
        out.added_timestamp = in.added_timestamp;
        out.is_relay = in.is_relay;
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
    List<OrmaBindvar> bind_where_vars = new ArrayList<>();
    int bind_where_count = 0;
    List<OrmaBindvar> bind_set_vars = new ArrayList<>();
    int bind_set_count = 0;

    public List<FriendList> toList()
    {
        List<FriendList> list = new ArrayList<>();
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
                FriendList out = new FriendList();
                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.name = rs.getString("name");
                out.alias_name = rs.getString("alias_name");
                out.status_message = rs.getString("status_message");
                out.TOX_CONNECTION = rs.getInt("TOX_CONNECTION");
                out.TOX_CONNECTION_real = rs.getInt("TOX_CONNECTION_real");
                out.TOX_CONNECTION_on_off = rs.getInt("TOX_CONNECTION_on_off");
                out.TOX_CONNECTION_on_off_real = rs.getInt("TOX_CONNECTION_on_off_real");
                out.TOX_USER_STATUS = rs.getInt("TOX_USER_STATUS");
                out.avatar_pathname = rs.getString("avatar_pathname");
                out.avatar_filename = rs.getString("avatar_filename");
                out.avatar_hex = rs.getString("avatar_hex");
                out.avatar_hash_hex = rs.getString("avatar_hash_hex");
                out.avatar_update = rs.getBoolean("avatar_update");
                out.avatar_update_timestamp = rs.getLong("avatar_update_timestamp");
                out.notification_silent = rs.getBoolean("notification_silent");
                out.sort = rs.getInt("sort");
                out.last_online_timestamp = rs.getLong("last_online_timestamp");
                out.last_online_timestamp_real = rs.getLong("last_online_timestamp_real");
                out.added_timestamp = rs.getLong("added_timestamp");
                out.is_relay = rs.getBoolean("is_relay");
                out.push_url = rs.getString("push_url");
                out.capabilities = rs.getLong("capabilities");
                out.msgv3_capability = rs.getLong("msgv3_capability");

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
                    + "tox_public_key_string"
                    + ",name"
                    + ",alias_name"
                    + ",status_message"
                    + ",TOX_CONNECTION"
                    + ",TOX_CONNECTION_real"
                    + ",TOX_CONNECTION_on_off"
                    + ",TOX_CONNECTION_on_off_real"
                    + ",TOX_USER_STATUS"
                    + ",avatar_pathname"
                    + ",avatar_filename"
                    + ",avatar_hex"
                    + ",avatar_hash_hex"
                    + ",avatar_update"
                    + ",avatar_update_timestamp"
                    + ",notification_silent"
                    + ",sort"
                    + ",last_online_timestamp"
                    + ",last_online_timestamp_real"
                    + ",added_timestamp"
                    + ",is_relay"
                    + ",push_url"
                    + ",capabilities"
                    + ",msgv3_capability"
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
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.tox_public_key_string);
            insert_pstmt.setString(2, this.name);
            insert_pstmt.setString(3, this.alias_name);
            insert_pstmt.setString(4, this.status_message);
            insert_pstmt.setInt(5, this.TOX_CONNECTION);
            insert_pstmt.setInt(6, this.TOX_CONNECTION_real);
            insert_pstmt.setInt(7, this.TOX_CONNECTION_on_off);
            insert_pstmt.setInt(8, this.TOX_CONNECTION_on_off_real);
            insert_pstmt.setInt(9, this.TOX_USER_STATUS);
            insert_pstmt.setString(10, this.avatar_pathname);
            insert_pstmt.setString(11, this.avatar_filename);
            insert_pstmt.setString(12, this.avatar_hex);
            insert_pstmt.setString(13, this.avatar_hash_hex);
            insert_pstmt.setBoolean(14, this.avatar_update);
            insert_pstmt.setLong(15, this.avatar_update_timestamp);
            insert_pstmt.setBoolean(16, this.notification_silent);
            insert_pstmt.setInt(17, this.sort);
            insert_pstmt.setLong(18, this.last_online_timestamp);
            insert_pstmt.setLong(19, this.last_online_timestamp_real);
            insert_pstmt.setLong(20, this.added_timestamp);
            insert_pstmt.setBoolean(21, this.is_relay);
            insert_pstmt.setString(22, this.push_url);
            insert_pstmt.setLong(23, this.capabilities);
            insert_pstmt.setLong(24, this.msgv3_capability);
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

    public FriendList get(int i)
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

    public FriendList limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public FriendList limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public FriendList tox_public_key_string(String tox_public_key_string)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_public_key_string=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " name=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " alias_name=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, alias_name));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " status_message=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, status_message));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " TOX_CONNECTION=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " TOX_CONNECTION_real=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " TOX_CONNECTION_on_off=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " TOX_CONNECTION_on_off_real=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " TOX_USER_STATUS=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " avatar_pathname=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_pathname));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " avatar_filename=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_filename));
        bind_set_count++;
        return this;
    }

    public FriendList avatar_hex(String avatar_hex)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_hex=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hex));
        bind_set_count++;
        return this;
    }

    public FriendList avatar_hash_hex(String avatar_hash_hex)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " avatar_hash_hex=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hash_hex));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " avatar_update=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, avatar_update));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " avatar_update_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_set_count++;
        return this;
    }

    public FriendList notification_silent(boolean notification_silent)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " notification_silent=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_set_count++;
        return this;
    }

    public FriendList sort(int sort)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " sort=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " last_online_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " last_online_timestamp_real=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_set_count++;
        return this;
    }

    public FriendList added_timestamp(long added_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " added_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " is_relay=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_relay));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " push_url=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, push_url));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " capabilities=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_set_count++;
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
        this.sql_set = this.sql_set + " msgv3_capability=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public FriendList tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public FriendList tox_public_key_stringNotEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public FriendList tox_public_key_stringLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public FriendList tox_public_key_stringNotLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public FriendList nameEq(String name)
    {
        this.sql_where = this.sql_where + " and name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public FriendList nameNotEq(String name)
    {
        this.sql_where = this.sql_where + " and name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public FriendList nameLike(String name)
    {
        this.sql_where = this.sql_where + " and name LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public FriendList nameNotLike(String name)
    {
        this.sql_where = this.sql_where + " and name NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public FriendList alias_nameEq(String alias_name)
    {
        this.sql_where = this.sql_where + " and alias_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, alias_name));
        bind_where_count++;
        return this;
    }

    public FriendList alias_nameNotEq(String alias_name)
    {
        this.sql_where = this.sql_where + " and alias_name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, alias_name));
        bind_where_count++;
        return this;
    }

    public FriendList alias_nameLike(String alias_name)
    {
        this.sql_where = this.sql_where + " and alias_name LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, alias_name));
        bind_where_count++;
        return this;
    }

    public FriendList alias_nameNotLike(String alias_name)
    {
        this.sql_where = this.sql_where + " and alias_name NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, alias_name));
        bind_where_count++;
        return this;
    }

    public FriendList status_messageEq(String status_message)
    {
        this.sql_where = this.sql_where + " and status_message=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, status_message));
        bind_where_count++;
        return this;
    }

    public FriendList status_messageNotEq(String status_message)
    {
        this.sql_where = this.sql_where + " and status_message<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, status_message));
        bind_where_count++;
        return this;
    }

    public FriendList status_messageLike(String status_message)
    {
        this.sql_where = this.sql_where + " and status_message LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, status_message));
        bind_where_count++;
        return this;
    }

    public FriendList status_messageNotLike(String status_message)
    {
        this.sql_where = this.sql_where + " and status_message NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, status_message));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONEq(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONNotEq(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONLt(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONLe(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONGt(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONGe(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTIONBetween(int TOX_CONNECTION1, int TOX_CONNECTION2)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION2));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realEq(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realNotEq(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realLt(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realLe(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realGt(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realGe(int TOX_CONNECTION_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_realBetween(int TOX_CONNECTION_real1, int TOX_CONNECTION_real2)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION_real<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_real2));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offEq(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offNotEq(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offLt(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offLe(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offGt(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offGe(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_offBetween(int TOX_CONNECTION_on_off1, int TOX_CONNECTION_on_off2)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION_on_off<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off2));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realEq(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realNotEq(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realLt(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realLe(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realGt(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realGe(int TOX_CONNECTION_on_off_real)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_CONNECTION_on_off_realBetween(int TOX_CONNECTION_on_off_real1, int TOX_CONNECTION_on_off_real2)
    {
        this.sql_where = this.sql_where + " and TOX_CONNECTION_on_off_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION_on_off_real<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off_real2));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSEq(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSNotEq(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSLt(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSLe(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSGt(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSGe(int TOX_USER_STATUS)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS));
        bind_where_count++;
        return this;
    }

    public FriendList TOX_USER_STATUSBetween(int TOX_USER_STATUS1, int TOX_USER_STATUS2)
    {
        this.sql_where = this.sql_where + " and TOX_USER_STATUS>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_USER_STATUS<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_USER_STATUS2));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_pathnameEq(String avatar_pathname)
    {
        this.sql_where = this.sql_where + " and avatar_pathname=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_pathname));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_pathnameNotEq(String avatar_pathname)
    {
        this.sql_where = this.sql_where + " and avatar_pathname<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_pathname));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_pathnameLike(String avatar_pathname)
    {
        this.sql_where = this.sql_where + " and avatar_pathname LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_pathname));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_pathnameNotLike(String avatar_pathname)
    {
        this.sql_where = this.sql_where + " and avatar_pathname NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_pathname));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_filenameEq(String avatar_filename)
    {
        this.sql_where = this.sql_where + " and avatar_filename=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_filename));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_filenameNotEq(String avatar_filename)
    {
        this.sql_where = this.sql_where + " and avatar_filename<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_filename));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_filenameLike(String avatar_filename)
    {
        this.sql_where = this.sql_where + " and avatar_filename LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_filename));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_filenameNotLike(String avatar_filename)
    {
        this.sql_where = this.sql_where + " and avatar_filename NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_filename));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hexEq(String avatar_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hex=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hexNotEq(String avatar_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hex<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hexLike(String avatar_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hex LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hexNotLike(String avatar_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hex NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hash_hexEq(String avatar_hash_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hash_hex=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hash_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hash_hexNotEq(String avatar_hash_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hash_hex<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hash_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hash_hexLike(String avatar_hash_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hash_hex LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hash_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_hash_hexNotLike(String avatar_hash_hex)
    {
        this.sql_where = this.sql_where + " and avatar_hash_hex NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, avatar_hash_hex));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_updateEq(boolean avatar_update)
    {
        this.sql_where = this.sql_where + " and avatar_update=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, avatar_update));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_updateNotEq(boolean avatar_update)
    {
        this.sql_where = this.sql_where + " and avatar_update<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, avatar_update));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampEq(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampNotEq(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampLt(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampLe(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampGt(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampGe(long avatar_update_timestamp)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList avatar_update_timestampBetween(long avatar_update_timestamp1, long avatar_update_timestamp2)
    {
        this.sql_where = this.sql_where + " and avatar_update_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and avatar_update_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, avatar_update_timestamp2));
        bind_where_count++;
        return this;
    }

    public FriendList notification_silentEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and notification_silent=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public FriendList notification_silentNotEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and notification_silent<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public FriendList sortEq(int sort)
    {
        this.sql_where = this.sql_where + " and sort=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortNotEq(int sort)
    {
        this.sql_where = this.sql_where + " and sort<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortLt(int sort)
    {
        this.sql_where = this.sql_where + " and sort<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortLe(int sort)
    {
        this.sql_where = this.sql_where + " and sort<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortGt(int sort)
    {
        this.sql_where = this.sql_where + " and sort>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortGe(int sort)
    {
        this.sql_where = this.sql_where + " and sort>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort));
        bind_where_count++;
        return this;
    }

    public FriendList sortBetween(int sort1, int sort2)
    {
        this.sql_where = this.sql_where + " and sort>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sort<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sort2));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampEq(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampNotEq(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampLt(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampLe(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampGt(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampGe(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestampBetween(long last_online_timestamp1, long last_online_timestamp2)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and last_online_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp2));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realEq(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realNotEq(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realLt(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realLe(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realGt(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realGe(long last_online_timestamp_real)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real));
        bind_where_count++;
        return this;
    }

    public FriendList last_online_timestamp_realBetween(long last_online_timestamp_real1, long last_online_timestamp_real2)
    {
        this.sql_where = this.sql_where + " and last_online_timestamp_real>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and last_online_timestamp_real<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp_real2));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampEq(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampNotEq(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampLt(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampLe(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampGt(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampGe(long added_timestamp)
    {
        this.sql_where = this.sql_where + " and added_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp));
        bind_where_count++;
        return this;
    }

    public FriendList added_timestampBetween(long added_timestamp1, long added_timestamp2)
    {
        this.sql_where = this.sql_where + " and added_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and added_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, added_timestamp2));
        bind_where_count++;
        return this;
    }

    public FriendList is_relayEq(boolean is_relay)
    {
        this.sql_where = this.sql_where + " and is_relay=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_relay));
        bind_where_count++;
        return this;
    }

    public FriendList is_relayNotEq(boolean is_relay)
    {
        this.sql_where = this.sql_where + " and is_relay<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_relay));
        bind_where_count++;
        return this;
    }

    public FriendList push_urlEq(String push_url)
    {
        this.sql_where = this.sql_where + " and push_url=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, push_url));
        bind_where_count++;
        return this;
    }

    public FriendList push_urlNotEq(String push_url)
    {
        this.sql_where = this.sql_where + " and push_url<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, push_url));
        bind_where_count++;
        return this;
    }

    public FriendList push_urlLike(String push_url)
    {
        this.sql_where = this.sql_where + " and push_url LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, push_url));
        bind_where_count++;
        return this;
    }

    public FriendList push_urlNotLike(String push_url)
    {
        this.sql_where = this.sql_where + " and push_url NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, push_url));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesEq(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesNotEq(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesLt(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesLe(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesGt(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesGe(long capabilities)
    {
        this.sql_where = this.sql_where + " and capabilities>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities));
        bind_where_count++;
        return this;
    }

    public FriendList capabilitiesBetween(long capabilities1, long capabilities2)
    {
        this.sql_where = this.sql_where + " and capabilities>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and capabilities<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, capabilities2));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityEq(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityNotEq(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityLt(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityLe(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityGt(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityGe(long msgv3_capability)
    {
        this.sql_where = this.sql_where + " and msgv3_capability>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability));
        bind_where_count++;
        return this;
    }

    public FriendList msgv3_capabilityBetween(long msgv3_capability1, long msgv3_capability2)
    {
        this.sql_where = this.sql_where + " and msgv3_capability>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and msgv3_capability<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, msgv3_capability2));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public FriendList orderByTox_public_key_stringAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_public_key_string ASC ";
        return this;
    }

    public FriendList orderByTox_public_key_stringDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_public_key_string DESC ";
        return this;
    }

    public FriendList orderByNameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " name ASC ";
        return this;
    }

    public FriendList orderByNameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " name DESC ";
        return this;
    }

    public FriendList orderByAlias_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " alias_name ASC ";
        return this;
    }

    public FriendList orderByAlias_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " alias_name DESC ";
        return this;
    }

    public FriendList orderByStatus_messageAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " status_message ASC ";
        return this;
    }

    public FriendList orderByStatus_messageDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " status_message DESC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTIONAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION ASC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTIONDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION DESC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTION_realAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_real ASC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTION_realDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_real DESC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTION_on_offAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_on_off ASC ";
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

    public FriendList orderByTOX_CONNECTION_on_off_realAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_on_off_real ASC ";
        return this;
    }

    public FriendList orderByTOX_CONNECTION_on_off_realDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_CONNECTION_on_off_real DESC ";
        return this;
    }

    public FriendList orderByTOX_USER_STATUSAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_USER_STATUS ASC ";
        return this;
    }

    public FriendList orderByTOX_USER_STATUSDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " TOX_USER_STATUS DESC ";
        return this;
    }

    public FriendList orderByAvatar_pathnameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_pathname ASC ";
        return this;
    }

    public FriendList orderByAvatar_pathnameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_pathname DESC ";
        return this;
    }

    public FriendList orderByAvatar_filenameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_filename ASC ";
        return this;
    }

    public FriendList orderByAvatar_filenameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_filename DESC ";
        return this;
    }

    public FriendList orderByAvatar_hexAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_hex ASC ";
        return this;
    }

    public FriendList orderByAvatar_hexDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_hex DESC ";
        return this;
    }

    public FriendList orderByAvatar_hash_hexAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_hash_hex ASC ";
        return this;
    }

    public FriendList orderByAvatar_hash_hexDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_hash_hex DESC ";
        return this;
    }

    public FriendList orderByAvatar_updateAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_update ASC ";
        return this;
    }

    public FriendList orderByAvatar_updateDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_update DESC ";
        return this;
    }

    public FriendList orderByAvatar_update_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_update_timestamp ASC ";
        return this;
    }

    public FriendList orderByAvatar_update_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " avatar_update_timestamp DESC ";
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
        this.sql_orderby = this.sql_orderby + " notification_silent ASC ";
        return this;
    }

    public FriendList orderByNotification_silentDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " notification_silent DESC ";
        return this;
    }

    public FriendList orderBySortAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sort ASC ";
        return this;
    }

    public FriendList orderBySortDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sort DESC ";
        return this;
    }

    public FriendList orderByLast_online_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " last_online_timestamp ASC ";
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
        this.sql_orderby = this.sql_orderby + " last_online_timestamp DESC ";
        return this;
    }

    public FriendList orderByLast_online_timestamp_realAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " last_online_timestamp_real ASC ";
        return this;
    }

    public FriendList orderByLast_online_timestamp_realDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " last_online_timestamp_real DESC ";
        return this;
    }

    public FriendList orderByAdded_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " added_timestamp ASC ";
        return this;
    }

    public FriendList orderByAdded_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " added_timestamp DESC ";
        return this;
    }

    public FriendList orderByIs_relayAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " is_relay ASC ";
        return this;
    }

    public FriendList orderByIs_relayDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " is_relay DESC ";
        return this;
    }

    public FriendList orderByPush_urlAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " push_url ASC ";
        return this;
    }

    public FriendList orderByPush_urlDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " push_url DESC ";
        return this;
    }

    public FriendList orderByCapabilitiesAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " capabilities ASC ";
        return this;
    }

    public FriendList orderByCapabilitiesDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " capabilities DESC ";
        return this;
    }

    public FriendList orderByMsgv3_capabilityAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msgv3_capability ASC ";
        return this;
    }

    public FriendList orderByMsgv3_capabilityDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " msgv3_capability DESC ";
        return this;
    }



}

