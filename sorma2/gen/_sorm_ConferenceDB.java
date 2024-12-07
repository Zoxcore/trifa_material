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
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;

@Table
public class ConferenceDB
{
    private static final String TAG = "DB.ConferenceDB";

    // conference id is always saved as lower case hex string!! -----------------
    @PrimaryKey
    String conference_identifier = "";
    // conference id is always saved as lower case hex string!! -----------------

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String who_invited__tox_public_key_string = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String name = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long peer_count = -1;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long own_peer_number = -1;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int kind = TOX_CONFERENCE_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long tox_conference_number = -1; // this changes often!!

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean conference_active = false; // is this conference active now? are we invited?

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this conference?

    static ConferenceDB deep_copy(ConferenceDB in)
    {
        ConferenceDB out = new ConferenceDB();
        out.conference_identifier = in.conference_identifier;
        out.name = in.name;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.kind = in.kind;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
        out.tox_conference_number = in.tox_conference_number;
        out.conference_active = in.conference_active;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "tox_conference_number=" + tox_conference_number + ", conference_active=" + conference_active +
                ", conference_identifier=" + conference_identifier + ", who_invited__tox_public_key_string=" +
                who_invited__tox_public_key_string + ", name=" + name + ", kind=" + kind + ", peer_count=" + peer_count +
                ", own_peer_number=" + own_peer_number + ", notification_silent=" + notification_silent;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<ConferenceDB> toList()
    {
        List<ConferenceDB> list = new ArrayList<>();

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit);
            while (rs.next())
            {
                ConferenceDB out = new ConferenceDB();

                out.conference_identifier = rs.getString("conference_identifier");
                out.name = rs.getString("name");
                out.peer_count = rs.getLong("peer_count");
                out.own_peer_number = rs.getLong("own_peer_number");
                out.kind = rs.getInt("kind");
                out.who_invited__tox_public_key_string = rs.getString("who_invited__tox_public_key_string");
                out.tox_conference_number = rs.getLong("tox_conference_number");
                out.conference_active = rs.getBoolean("conference_active");
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
            // @formatter:off
            Statement statement = sqldb.createStatement();
            final String sql_str="insert into " + this.getClass().getSimpleName() +
                    "(" +
                    "conference_identifier,"	+
                    "name,"+
                    "peer_count,"+
                    "own_peer_number,"	+
                    "kind,"	+
                    "who_invited__tox_public_key_string,"+
                    "tox_conference_number,"+
                    "conference_active,"+
                    "notification_silent"	+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.conference_identifier)+"'," +
                    "'"+s(this.name)+"'," +
                    "'"+s(this.peer_count)+"'," +
                    "'"+s(this.own_peer_number)+"'," +
                    "'"+s(this.kind)+"'," +
                    "'"+s(this.who_invited__tox_public_key_string)+"'," +
                    "'"+s(this.tox_conference_number)+"'," +
                    "'"+b(this.conference_active)+"'," +
                    "'"+b(this.notification_silent)+"'" +
                    ")";

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

    public ConferenceDB get(int i)
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

    public ConferenceDB limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public ConferenceDB conference_identifierEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and conference_identifier='" + s(conference_identifier) + "' ";
        return this;
    }

    public ConferenceDB conference_active(boolean b)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " conference_active='" + b(b) + "' ";
        return this;
    }

    public ConferenceDB kind(int kind)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " kind='" + s(kind) + "' ";
        return this;
    }

    public ConferenceDB tox_conference_number(long conference_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_conference_number='" + s(conference_number) + "' ";
        return this;
    }

    public ConferenceDB orderByConference_activeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " Conference_active DESC ";
        return this;
    }

    public ConferenceDB orderByNotification_silentAsc()
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

    public ConferenceDB tox_conference_numberEq(long conference_number)
    {
        this.sql_where = this.sql_where + " and tox_conference_number='" + s(conference_number) + "' ";
        return this;
    }

    public ConferenceDB conference_activeEq(boolean b)
    {
        this.sql_where = this.sql_where + " and conference_active='" + b(b) + "' ";
        return this;
    }

    public ConferenceDB name(String name)
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

    public ConferenceDB tox_conference_numberNotEq(int tox_conference_number)
    {
        this.sql_where = this.sql_where + " and tox_conference_number<>'" + s(tox_conference_number) + "' ";
        return this;
    }
}
