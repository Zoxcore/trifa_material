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
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class Filetransfer
{
    private static final String TAG = "DB.Filetransfer";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // unique ID!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_public_key_string = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = TRIFA_FT_DIRECTION_INCOMING.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long file_number = -1; // given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int kind = TOX_FILE_KIND_DATA.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int state = TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_started = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String file_name = "";

    @Column(defaultExpr = "false")
    public boolean fos_open = false;

    @Column(defaultExpr = "-1")
    public long filesize = -1;

    @Column(defaultExpr = "0")
    public long current_position = 0;

    @Column(indexed = true, defaultExpr = "-1")
    public long message_id; // f_key -> Message.id

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_file_id_hex = "";

    static Filetransfer deep_copy(Filetransfer in)
    {
        Filetransfer out = new Filetransfer();
        out.tox_public_key_string = in.tox_public_key_string;
        out.direction = in.direction;
        out.file_number = in.file_number;
        out.kind = in.kind;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.fos_open = in.fos_open;
        out.filesize = in.filesize;
        out.current_position = in.current_position;
        out.message_id = in.message_id;
        out.tox_file_id_hex = in.tox_file_id_hex;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", kind=" + kind + ", state=" + state + ", direction=" + direction + ", path_name=" +
                path_name + ", file_name=" + file_name + ", filesize=" + filesize + ", current_position=" +
                current_position + ", message_id=" + message_id + ", tox_public_key_string=" + tox_public_key_string +
                ", tox_file_id_hex=" + tox_file_id_hex;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<Filetransfer> toList()
    {
        List<Filetransfer> fl = new ArrayList<>();

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
                Filetransfer out = new Filetransfer();
                out.id = rs.getLong("id");
                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.direction = rs.getInt("direction");
                out.file_number = rs.getLong("file_number");
                out.kind = rs.getInt("kind");
                out.state = rs.getInt("state");
                out.ft_accepted = rs.getBoolean("ft_accepted");
                out.ft_outgoing_started = rs.getBoolean("ft_outgoing_started");
                out.path_name = rs.getString("path_name");
                out.file_name = rs.getString("file_name");
                out.fos_open = rs.getBoolean("fos_open");
                out.filesize = rs.getLong("filesize");
                out.current_position = rs.getLong("current_position");
                out.message_id = rs.getLong("message_id");
                out.tox_file_id_hex = rs.getString("tox_file_id_hex");

                fl.add(out);
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
                    "direction,"+
                    "file_number,"+
                    "kind,"	+
                    "state,"	+
                    "ft_accepted,"+
                    "ft_outgoing_started,"+
                    "path_name,"+
                    "file_name,"	+
                    "fos_open,"+
                    "filesize,"+
                    "current_position,"+
                    "message_id,"+
                    "tox_file_id_hex"+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.tox_public_key_string)+"'," +
                    "'"+s(this.direction)+"'," +
                    "'"+s(this.file_number)+"'," +
                    "'"+s(this.kind)+"'," +
                    "'"+s(this.state)+"'," +
                    "'"+b(this.ft_accepted)+"'," +
                    "'"+b(this.ft_outgoing_started)+"'," +
                    "'"+s(this.path_name)+"'," +
                    "'"+s(this.file_name)+"'," +
                    "'"+b(this.fos_open)+"'," +
                    "'"+s(this.filesize)+"'," +
                    "'"+s(this.current_position)+"'," +
                    "'"+s(this.message_id)+"'," +
                    "'"+s(this.tox_file_id_hex)+"'" +
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

    public Filetransfer get(int i)
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

    public Filetransfer limit(int i)
    {
        this.sql_limit = " limit " + i + " ";
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //

    public Filetransfer directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and direction='" + s(direction) + "' ";
        return this;
    }

    public Filetransfer tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and tox_public_key_string='" + s(tox_public_key_string) + "' ";
        return this;
    }

    public Filetransfer file_numberEq(long file_number)
    {
        this.sql_where = this.sql_where + " and file_number='" + s(file_number) + "' ";
        return this;
    }

    public Filetransfer orderByIdDesc()
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

    public Filetransfer idEq(long id)
    {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }

    public Filetransfer tox_public_key_string(String tox_public_key_string)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_public_key_string='" + s(tox_public_key_string) + "' ";
        return this;
    }

    public Filetransfer direction(int direction)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " direction='" + s(direction) + "' ";
        return this;
    }

    public Filetransfer file_number(long file_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " file_number='" + s(file_number) + "' ";
        return this;
    }

    public Filetransfer kind(int kind)
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

    public Filetransfer state(int state)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " state='" + s(state) + "' ";
        return this;
    }

    public Filetransfer path_name(String path_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " path_name='" + s(path_name) + "' ";
        return this;
    }

    public Filetransfer message_id(long message_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " message_id='" + s(message_id) + "' ";
        return this;
    }

    public Filetransfer file_name(String file_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " file_name='" + s(file_name) + "' ";
        return this;
    }

    public Filetransfer fos_open(boolean fos_open)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " fos_open='" + b(fos_open) + "' ";
        return this;
    }

    public Filetransfer filesize(long filesize)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filesize='" + s(filesize) + "' ";
        return this;
    }

    public Filetransfer current_position(long current_position)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " current_position='" + s(current_position) + "' ";
        return this;
    }

    public Filetransfer ft_accepted(boolean ft_accepted)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_accepted='" + b(ft_accepted) + "' ";
        return this;
    }

    public Filetransfer ft_outgoing_started(boolean ft_outgoing_started)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_outgoing_started='" + b(ft_outgoing_started) + "' ";
        return this;
    }

    public Filetransfer stateNotEq(int state)
    {
        this.sql_where = this.sql_where + " and state<>'" + s(state) + "' ";
        return this;
    }

    public Filetransfer tox_file_id_hex(String tox_file_id_hex)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " tox_file_id_hex='" + s(tox_file_id_hex) + "' ";
        return this;
    }

    public Filetransfer file_numberNotEq(long file_number)
    {
        this.sql_where = this.sql_where + " and file_number<>'" + s(file_number) + "' ";
        return this;
    }
}
