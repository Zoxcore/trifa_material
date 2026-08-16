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
    public String tox_public_key_string = ""; // pubkey is always saved as UPPER CASE hex string!!
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
    public String tox_public_key_string_of_owner = ""; // pubkey is always saved as UPPER CASE hex string!!
    // pubkey is always saved as UPPER CASE hex string!! -----------------


