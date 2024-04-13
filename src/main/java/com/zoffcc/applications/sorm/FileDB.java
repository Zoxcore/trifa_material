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
    List<OrmaBindvar> bind_where_vars = new ArrayList<>();
    int bind_where_count = 0;
    List<OrmaBindvar> bind_set_vars = new ArrayList<>();
    int bind_set_count = 0;

    public List<FileDB> toList()
    {
        List<FileDB> list = new ArrayList<>();

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
            String insert_pstmt_sql = null;
            PreparedStatement insert_pstmt = null;

            // @formatter:off
            insert_pstmt_sql="insert into " + this.getClass().getSimpleName() +
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
                    "?1," +
                    "?2," +
                    "?3," +
                    "?4," +
                    "?5," +
                    "?6," +
                    "?7" +
                    ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);

            insert_pstmt.clearParameters();

            insert_pstmt.setInt(1, this.kind);
            insert_pstmt.setInt(2, this.direction);
            insert_pstmt.setString(3, this.tox_public_key_string);
            insert_pstmt.setString(4, this.path_name);
            insert_pstmt.setString(5, this.file_name);
            insert_pstmt.setLong(6, this.filesize);
            insert_pstmt.setBoolean(7, this.is_in_VFS);

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + insert_pstmt);
            }

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

    public FileDB get(int i)
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
        this.sql_where = this.sql_where + " and tox_public_key_string=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public FileDB file_nameEq(String file_name)
    {
        this.sql_where = this.sql_where + " and file_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
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
        this.sql_where = this.sql_where + " and path_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
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
