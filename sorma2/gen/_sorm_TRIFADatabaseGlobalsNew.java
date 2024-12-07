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
public class TRIFADatabaseGlobalsNew
{
    private static final String TAG = "DB.TRIFADatabaseGlobalsNew";

    @PrimaryKey
    public String key;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String value;

    static TRIFADatabaseGlobalsNew deep_copy(TRIFADatabaseGlobalsNew in)
    {
        TRIFADatabaseGlobalsNew out = new TRIFADatabaseGlobalsNew();
        out.key = in.key;
        out.value = in.value;

        return out;
    }

    @Override
    public String toString()
    {
        return "key=" + key + ", value=" + value;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<TRIFADatabaseGlobalsNew> toList()
    {
        List<TRIFADatabaseGlobalsNew> list = new ArrayList<>();

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
                TRIFADatabaseGlobalsNew out = new TRIFADatabaseGlobalsNew();

                out.key = rs.getString("key");
                out.value = rs.getString("value");

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
                    "key," +
                    "value" +
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.key)+"'," +
                    "'"+s(this.value)+"'" +
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

    public TRIFADatabaseGlobalsNew get(int i)
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

    public TRIFADatabaseGlobalsNew limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    public TRIFADatabaseGlobalsNew keyEq(String key)
    {
        this.sql_where = this.sql_where + " and  key='" + s(key) + "' ";
        return this;
    }

    public TRIFADatabaseGlobalsNew value(String value)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " value='" + s(value) + "' ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

}
