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
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class FileDB
{
    private static final String TAG = "DB.FileDB";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int kind = TOX_FILE_KIND_DATA.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = TRIFA_FT_DIRECTION_INCOMING.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_public_key_string = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String file_name = "";

    @Column(defaultExpr = "-1", indexed = true, helpers = Column.Helpers.ALL)
    public long filesize = -1;

    @Column(indexed = true, defaultExpr = "true", helpers = Column.Helpers.ALL)
    public boolean is_in_VFS = true;

    static FileDB deep_copy(FileDB in)
    {
        FileDB out = new FileDB();
        out.kind = in.kind;
        out.direction = in.direction;
        out.tox_public_key_string = in.tox_public_key_string;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.filesize = in.filesize;
        out.is_in_VFS = in.is_in_VFS;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", kind=" + kind + ", is_in_VFS=" + is_in_VFS + ", path_name=" + path_name + ", file_name" +
                file_name + ", filesize=" + filesize + ", direction=" + direction;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<FileDB> toList()
    {
        List<FileDB> list = new ArrayList<>();

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
                FileDB out = new FileDB();

                out.id = rs.getLong("id");
                out.kind = rs.getInt("kind");
                out.direction = rs.getInt("direction");
                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.path_name = rs.getString("path_name");
                out.file_name = rs.getString("file_name");
                out.filesize = rs.getLong("filesize");
                out.is_in_VFS = rs.getBoolean("is_in_VFS");

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
                    "kind,"	+
                    "direction,"+
                    "tox_public_key_string,"+
                    "path_name,"	+
                    "file_name,"	+
                    "filesize,"+
                    "is_in_VFS"+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.kind)+"'," +
                    "'"+s(this.direction)+"'," +
                    "'"+s(this.tox_public_key_string)+"'," +
                    "'"+s(this.path_name)+"'," +
                    "'"+s(this.file_name)+"'," +
                    "'"+s(this.filesize)+"'," +
                    "'"+b(this.is_in_VFS)+"'" +
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

    public FileDB get(int i)
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

    public FileDB limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public FileDB tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string='" + s(tox_public_key_string) + "' ";
        return this;
    }

    public FileDB file_nameEq(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name='" + s(file_name) + "' ";
        return this;
    }

    public FileDB orderByIdDesc()
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

    public FileDB path_nameEq(String path_name)
    {
        this.sql_where = this.sql_where + " and path_name='" + s(path_name) + "' ";
        return this;
    }

    public FileDB directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction='" + s(direction) + "' ";
        return this;
    }

    public FileDB filesizeEq(long filesize)
    {
        this.sql_where = this.sql_where + " and filesize='" + s(filesize) + "' ";
        return this;
    }

    public FileDB idEq(long id)
    {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }
}
