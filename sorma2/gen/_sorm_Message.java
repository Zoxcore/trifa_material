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
import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;

@Table
public class Message
{
    private static final String TAG = "DB.Message";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long message_id = -1; // ID given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_friendpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    public int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    public int TRIFA_MESSAGE_TYPE = 0; // TRIFA_MSG_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "1", helpers = Column.Helpers.ALL)
    public int state = 1; // TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_started = false;

    @Column(indexed = true, defaultExpr = "-1")
    public long filedb_id; // f_key -> FileDB.id

    @Column(indexed = true, defaultExpr = "-1")
    public long filetransfer_id; // f_key -> Filetransfer.id

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public long sent_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public long sent_timestamp_ms = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    public long rcvd_timestamp = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    public long rcvd_timestamp_ms = 0L;

    @Column(helpers = Column.Helpers.ALL)
    public boolean read = false;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int send_retries = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new = true;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_id_hash = null; // 32byte hash, used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String raw_msgv2_bytes = null; // used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, defaultExpr = "0")
    public int msg_version; // 0 -> old Message, 1 -> for MessageV2 Message

    @Column(indexed = true, defaultExpr = "2")
    public int resend_count; // for msgV2 "> 1" -> do not resend msg anymore, 0 or 1 -> resend count

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_queued = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    public boolean msg_at_relay = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_idv3_hash = null; // 32byte hash, used for MessageV3 Messages! and otherwise NULL

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public int sent_push = 0;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    public int filetransfer_kind = 0; // TOX_FILE_KIND_DATA.value;

    // ______@@SORMA_END@@______


