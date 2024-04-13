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
        out.name = in.name;
        out.topic = in.topic;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.privacy_state = in.privacy_state;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
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
                    "group_identifier,"	+
                    "who_invited__tox_public_key_string,"+
                    "name,"+
                    "topic,"	+
                    "peer_count,"	+
                    "own_peer_number,"+
                    "privacy_state,"+
                    "tox_group_number,"+
                    "group_active,"+
                    "notification_silent"	+
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
                    "?10" +
                    ")";

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

            orma_semaphore_lastrowid_on_insert.acquire();
            insert_pstmt.executeUpdate();
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

    public GroupDB limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public GroupDB group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and group_identifier=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
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
        this.sql_set = this.sql_set + " privacy_state='" + s(privacy_state) + "' ";
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
        this.sql_set = this.sql_set + " tox_group_number='" + s(tox_group_number) + "' ";
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
        this.sql_set = this.sql_set + " group_active='" + b(group_active) + "' ";
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
        this.sql_set = this.sql_set + " topic='" + s(topic) + "' ";
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
        this.sql_orderby = this.sql_orderby + " Notification_silent ASC ";
        return this;
    }

    public GroupDB tox_group_numberNotEq(int tox_group_number)
    {
        this.sql_where = this.sql_where + " and tox_group_number<>'" + s(tox_group_number) + "' ";
        return this;
    }
}
