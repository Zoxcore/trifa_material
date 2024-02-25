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
public class RelayListDB
{
    private static final String TAG = "DB.RelayListDB";

    // pubkey is always saved as UPPER CASE hex string!! -----------------
    @PrimaryKey
    public String tox_public_key_string = "";
    // pubkey is always saved as UPPER CASE hex string!! -----------------

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean own_relay = false; // false --> friends relay, true --> my relay

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public long last_online_timestamp = -1L;

    // pubkey is always saved as UPPER CASE hex string!! -----------------
    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_public_key_string_of_owner = "";
    // pubkey is always saved as UPPER CASE hex string!! -----------------

    static RelayListDB deep_copy(RelayListDB in)
    {
        RelayListDB out = new RelayListDB();
        out.tox_public_key_string = in.tox_public_key_string;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.own_relay = in.own_relay;
        out.last_online_timestamp = in.last_online_timestamp;
        out.tox_public_key_string_of_owner = in.tox_public_key_string_of_owner;

        return out;
    }

    @Override
    public String toString()
    {
        try
        {
            return "tox_public_key_string=" + tox_public_key_string.substring(0, 4) + ", ownder_pubkey=" +
                    tox_public_key_string_of_owner.substring(0, 4) + ", own_relay=" + own_relay + ", TOX_CONNECTION=" +
                    TOX_CONNECTION + ", TOX_CONNECTION_on_off=" + TOX_CONNECTION_on_off + ", last_online_timestamp=" +
                    last_online_timestamp;
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

    public List<RelayListDB> toList()
    {
        List<RelayListDB> list = new ArrayList<>();

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
                RelayListDB out = new RelayListDB();

                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.TOX_CONNECTION = rs.getInt("TOX_CONNECTION");
                out.TOX_CONNECTION_on_off = rs.getInt("TOX_CONNECTION_on_off");
                out.own_relay = rs.getBoolean("own_relay");
                out.last_online_timestamp = rs.getLong("last_online_timestamp");
                out.tox_public_key_string_of_owner = rs.getString("tox_public_key_string_of_owner");

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
                    "tox_public_key_string,"	+
                    "TOX_CONNECTION,"+
                    "TOX_CONNECTION_on_off,"+
                    "own_relay,"	+
                    "last_online_timestamp,"	+
                    "tox_public_key_string_of_owner"+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.tox_public_key_string)+"'," +
                    "'"+s(this.TOX_CONNECTION)+"'," +
                    "'"+s(this.TOX_CONNECTION_on_off)+"'," +
                    "'"+b(this.own_relay)+"'," +
                    "'"+s(this.TOX_CONNECTION)+"'," +
                    "'"+s(this.tox_public_key_string_of_owner)+"'" +
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

    public RelayListDB get(int i)
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

    public RelayListDB limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public RelayListDB own_relayEq(boolean own_relay)
    {
        this.sql_where = this.sql_where + " and  own_relay='" + b(own_relay) + "' ";
        return this;
    }

    public RelayListDB tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and  tox_public_key_string='" + s(tox_public_key_string) + "' ";
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerEq(String tox_public_key_string_of_owner) {
        this.sql_where = this.sql_where + " and  tox_public_key_string_of_owner='" + s(tox_public_key_string_of_owner) + "' ";
        return this;
    }
}
