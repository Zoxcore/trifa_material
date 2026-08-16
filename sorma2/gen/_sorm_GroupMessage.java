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
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;

@Table
public class GroupMessage
{
    private static final Str ing TAG = "DB.GroupMessage";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    public String message_id_tox = ""; // Tox Group Message_ID (4 bytes as hex string lowercase)

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public String group_identifier = "-1"; // f_key -> GroupDB.group_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey; // uppercase

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int tox_group_peer_role = 2; // TOX_GROUP_ROLE_USER.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public int private_message = 0; // 0 -> message to group, 1 -> msg privately to/from peer

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peername = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    public int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    public int TRIFA_MESSAGE_TYPE = 0; // TRIFA_MSG_TYPE_TEXT.value;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    public long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    public boolean read = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String text = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public boolean was_synced = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_id_hash = null; // 32byte hash (as hex string uppercase)

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String sent_privately_to_tox_group_peer_pubkey = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String path_name = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String file_name = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public long filesize = 0L;


