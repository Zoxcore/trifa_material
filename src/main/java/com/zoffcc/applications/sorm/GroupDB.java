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
import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_PRIVACY_STATE;

@Table
public class GroupDB
{
    private static final String TAG = "DB.GroupDB";

    // group id is always saved as lower case hex string!! -----------------
    @PrimaryKey
    public String group_identifier = "";
    // group id is always saved as lower case hex string!! -----------------

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String who_invited__tox_public_key_string = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String name = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String topic = "";

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long peer_count = -1;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long own_peer_number = -1;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int privacy_state = TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long tox_group_number = -1; // this changes often!!

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean group_active = false; // is this conference active now? are we invited?

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this conference?

    static GroupDB deep_copy(GroupDB in)
    {
        GroupDB out = new GroupDB();
        out.group_identifier = in.group_identifier;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
        out.name = in.name;
        out.topic = in.topic;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.privacy_state = in.privacy_state;
        out.tox_group_number = in.tox_group_number;
        out.group_active = in.group_active;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "tox_group_number=" + tox_group_number + ", group_identifier=" + group_identifier +
                ", who_invited__tox_public_key_string=" + who_invited__tox_public_key_string + ", name=" + name +
                ", topic=" + topic + ", privacy_state=" + privacy_state + ", peer_count=" + peer_count +
                ", own_peer_number=" + own_peer_number + ", notification_silent=" + notification_silent +
                ", group_active=" + group_active;
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

    public List<GroupDB> toList()
    {
        List<GroupDB> list = new ArrayList<>();
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
                GroupDB out = new GroupDB();
                out.group_identifier = rs.getString("group_identifier");
                out.who_invited__tox_public_key_string = rs.getString("who_invited__tox_public_key_string");
                out.name = rs.getString("name");
                out.topic = rs.getString("topic");
                out.peer_count = rs.getLong("peer_count");
                out.own_peer_number = rs.getLong("own_peer_number");
                out.privacy_state = rs.getInt("privacy_state");
                out.tox_group_number = rs.getLong("tox_group_number");
                out.group_active = rs.getBoolean("group_active");
                out.notification_silent = rs.getBoolean("notification_silent");

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
                    + "group_identifier"
                    + ",who_invited__tox_public_key_string"
                    + ",name"
                    + ",topic"
                    + ",peer_count"
                    + ",own_peer_number"
                    + ",privacy_state"
                    + ",tox_group_number"
                    + ",group_active"
                    + ",notification_silent"
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
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.group_identifier);
            insert_pstmt.setString(2, this.who_invited__tox_public_key_string);
            insert_pstmt.setString(3, this.name);
            insert_pstmt.setString(4, this.topic);
            insert_pstmt.setLong(5, this.peer_count);
            insert_pstmt.setLong(6, this.own_peer_number);
            insert_pstmt.setInt(7, this.privacy_state);
            insert_pstmt.setLong(8, this.tox_group_number);
            insert_pstmt.setBoolean(9, this.group_active);
            insert_pstmt.setBoolean(10, this.notification_silent);
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

    public GroupDB get(int i)
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

    public GroupDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public GroupDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public GroupDB group_identifier(String group_identifier)
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

    public GroupDB who_invited__tox_public_key_string(String who_invited__tox_public_key_string)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " who_invited__tox_public_key_string=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_set_count++;
        return this;
    }

    public GroupDB name(String name)
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

    public GroupDB topic(String topic)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " topic=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, topic));
        bind_set_count++;
        return this;
    }

    public GroupDB peer_count(long peer_count)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " peer_count=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_set_count++;
        return this;
    }

    public GroupDB own_peer_number(long own_peer_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " own_peer_number=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_set_count++;
        return this;
    }

    public GroupDB privacy_state(int privacy_state)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " privacy_state=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state));
        bind_set_count++;
        return this;
    }

    public GroupDB tox_group_number(long tox_group_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_group_number=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number));
        bind_set_count++;
        return this;
    }

    public GroupDB group_active(boolean group_active)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " group_active=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, group_active));
        bind_set_count++;
        return this;
    }

    public GroupDB notification_silent(boolean notification_silent)
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


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public GroupDB group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupDB group_identifierNotEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupDB who_invited__tox_public_key_stringEq(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and who_invited__tox_public_key_string=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public GroupDB who_invited__tox_public_key_stringNotEq(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and who_invited__tox_public_key_string<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public GroupDB nameEq(String name)
    {
        this.sql_where = this.sql_where + " and name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public GroupDB nameNotEq(String name)
    {
        this.sql_where = this.sql_where + " and name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public GroupDB topicEq(String topic)
    {
        this.sql_where = this.sql_where + " and topic=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, topic));
        bind_where_count++;
        return this;
    }

    public GroupDB topicNotEq(String topic)
    {
        this.sql_where = this.sql_where + " and topic<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, topic));
        bind_where_count++;
        return this;
    }

    public GroupDB peer_countEq(long peer_count)
    {
        this.sql_where = this.sql_where + " and peer_count=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public GroupDB peer_countNotEq(long peer_count)
    {
        this.sql_where = this.sql_where + " and peer_count<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public GroupDB peer_countLt(long peer_count)
    {
        this.sql_where = this.sql_where + " and peer_count<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public GroupDB peer_countGt(long peer_count)
    {
        this.sql_where = this.sql_where + " and peer_count>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public GroupDB peer_countBetween(long peer_count1, long peer_count2)
    {
        this.sql_where = this.sql_where + " and peer_count>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and peer_count<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count2));
        bind_where_count++;
        return this;
    }

    public GroupDB own_peer_numberEq(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and own_peer_number=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public GroupDB own_peer_numberNotEq(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and own_peer_number<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public GroupDB own_peer_numberLt(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and own_peer_number<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public GroupDB own_peer_numberGt(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and own_peer_number>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public GroupDB own_peer_numberBetween(long own_peer_number1, long own_peer_number2)
    {
        this.sql_where = this.sql_where + " and own_peer_number>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and own_peer_number<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number2));
        bind_where_count++;
        return this;
    }

    public GroupDB privacy_stateEq(int privacy_state)
    {
        this.sql_where = this.sql_where + " and privacy_state=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state));
        bind_where_count++;
        return this;
    }

    public GroupDB privacy_stateNotEq(int privacy_state)
    {
        this.sql_where = this.sql_where + " and privacy_state<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state));
        bind_where_count++;
        return this;
    }

    public GroupDB privacy_stateLt(int privacy_state)
    {
        this.sql_where = this.sql_where + " and privacy_state<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state));
        bind_where_count++;
        return this;
    }

    public GroupDB privacy_stateGt(int privacy_state)
    {
        this.sql_where = this.sql_where + " and privacy_state>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state));
        bind_where_count++;
        return this;
    }

    public GroupDB privacy_stateBetween(int privacy_state1, int privacy_state2)
    {
        this.sql_where = this.sql_where + " and privacy_state>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and privacy_state<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, privacy_state2));
        bind_where_count++;
        return this;
    }

    public GroupDB tox_group_numberEq(long tox_group_number)
    {
        this.sql_where = this.sql_where + " and tox_group_number=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number));
        bind_where_count++;
        return this;
    }

    public GroupDB tox_group_numberNotEq(long tox_group_number)
    {
        this.sql_where = this.sql_where + " and tox_group_number<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number));
        bind_where_count++;
        return this;
    }

    public GroupDB tox_group_numberLt(long tox_group_number)
    {
        this.sql_where = this.sql_where + " and tox_group_number<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number));
        bind_where_count++;
        return this;
    }

    public GroupDB tox_group_numberGt(long tox_group_number)
    {
        this.sql_where = this.sql_where + " and tox_group_number>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number));
        bind_where_count++;
        return this;
    }

    public GroupDB tox_group_numberBetween(long tox_group_number1, long tox_group_number2)
    {
        this.sql_where = this.sql_where + " and tox_group_number>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_group_number<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_number2));
        bind_where_count++;
        return this;
    }

    public GroupDB group_activeEq(boolean group_active)
    {
        this.sql_where = this.sql_where + " and group_active=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, group_active));
        bind_where_count++;
        return this;
    }

    public GroupDB group_activeNotEq(boolean group_active)
    {
        this.sql_where = this.sql_where + " and group_active<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, group_active));
        bind_where_count++;
        return this;
    }

    public GroupDB notification_silentEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and notification_silent=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public GroupDB notification_silentNotEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and notification_silent<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public GroupDB orderByGroup_identifierAsc()
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

    public GroupDB orderByGroup_identifierDesc()
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

    public GroupDB orderByWho_invited__tox_public_key_stringAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " who_invited__tox_public_key_string ASC ";
        return this;
    }

    public GroupDB orderByWho_invited__tox_public_key_stringDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " who_invited__tox_public_key_string DESC ";
        return this;
    }

    public GroupDB orderByNameAsc()
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

    public GroupDB orderByNameDesc()
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

    public GroupDB orderByTopicAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " topic ASC ";
        return this;
    }

    public GroupDB orderByTopicDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " topic DESC ";
        return this;
    }

    public GroupDB orderByPeer_countAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_count ASC ";
        return this;
    }

    public GroupDB orderByPeer_countDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_count DESC ";
        return this;
    }

    public GroupDB orderByOwn_peer_numberAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " own_peer_number ASC ";
        return this;
    }

    public GroupDB orderByOwn_peer_numberDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " own_peer_number DESC ";
        return this;
    }

    public GroupDB orderByPrivacy_stateAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " privacy_state ASC ";
        return this;
    }

    public GroupDB orderByPrivacy_stateDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " privacy_state DESC ";
        return this;
    }

    public GroupDB orderByTox_group_numberAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_number ASC ";
        return this;
    }

    public GroupDB orderByTox_group_numberDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " tox_group_number DESC ";
        return this;
    }

    public GroupDB orderByGroup_activeAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " group_active ASC ";
        return this;
    }

    public GroupDB orderByGroup_activeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " group_active DESC ";
        return this;
    }

    public GroupDB orderByNotification_silentAsc()
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

    public GroupDB orderByNotification_silentDesc()
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



}

