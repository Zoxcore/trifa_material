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
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;
import static com.zoffcc.applications.sorm.OrmaDatabase.orma_semaphore_lastrowid_on_insert;

@Table
public class BootstrapNodeEntryDB
{
    static final String TAG = "trifa.BtpNodeEDB";

    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long num;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean udp_node; // true -> UDP bootstrap node, false -> TCP relay node

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String ip;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long port;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String key_hex;

    @Override
    public String toString()
    {
        // return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + " key_hex=" + key_hex;
        // return "" + num + ":" + ip + " port=" + port + " udp_node="+  udp_node;
        return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + "\n";
    }

    public long get_port()
    {
        return port;
    }

    public String get_ip()
    {
        return ip;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public static List<BootstrapNodeEntryDB> bootstrap_node_list = new ArrayList<>();
    public static List<BootstrapNodeEntryDB> tcprelay_node_list = new ArrayList<>();

    static void insert_node_into_db_real(BootstrapNodeEntryDB n, OrmaDatabase orma)
    {
        try
        {
            orma.insertIntoBootstrapNodeEntryDB(n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void insert_default_udp_nodes_into_db(OrmaDatabase orma)
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        // @formatter:off
        n = BootstrapNodeEntryDB_(true, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n,orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a03:b0c0:0:1010::4c:5001",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:41d0:8:7a96::1",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox4.plastiras.org",33445,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox3.plastiras.org",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a01:4f8:211:c97::2",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "104.225.141.59",43334,"933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "139.162.110.188",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2400:8902::f03c:93ff:fe69:bf77",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2605:6400:10:caa:1:be:a:7001",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "172.105.109.31",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2600:3c04::f03c:92ff:fe30:5df",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "91.146.66.26",33445,"B5E7DAC610DBDE55F359C7F8690B294C8E4FCEC4385DE9525DBFA5523EAD9D53");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox01.ky0uraku.xyz",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:bc8:1820:24b::1",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox02.ky0uraku.xyz",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:bc8:47a0:67e::1",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.plastiras.org",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "kusoneko.moe",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:19f0:b001:379:5400:3ff:fe68:1cc6",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.plastiras.org",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a01:4f8:251:1930::2",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "172.104.215.182",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2600:3c03::f03c:93ff:fe7f:6096",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        // @formatter:on
    }

    public static void insert_default_tcprelay_nodes_into_db(OrmaDatabase orma)
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        // @formatter:off
        n = BootstrapNodeEntryDB_(false, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "144.217.167.73",3389,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.199.98.108",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:a880:1:20::32f:1001",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",443,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",3389,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",33445,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "46.101.197.175",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a03:b0c0:3:d0::ac:5001",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox1.mf-net.eu",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c2c:89f7::1",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:41d0:8:7a96::1",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.mf-net.eu",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:41d0:8:7a96::1",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.201.7.101",3389,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",3389,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",443,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",33445,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "188.225.9.167",3389,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",3389,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "122.116.39.151",3389,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",3389,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox3.plastiras.org",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:211:c97::2",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox3.plastiras.org",3389,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:211:c97::2",3389,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",443,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",443,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",3389,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",3389,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:10:caa:1:be:a:7001",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.105.109.31",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c04::f03c:92ff:fe30:5df",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox01.ky0uraku.xyz",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:bc8:1820:24b::1",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox02.ky0uraku.xyz",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:bc8:47a0:67e::1",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.plastiras.org",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.plastiras.org",443,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",443,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "kusoneko.moe",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:19f0:b001:379:5400:3ff:fe68:1cc6",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.plastiras.org",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:251:1930::2",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.plastiras.org",3389,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:251:1930::2",3389,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",3389,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",3389,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",443,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",443,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "5.19.249.240",38296,"DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "5.19.249.240",3389,"DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2607:f130:0:f8::4c85:a645",3389,"8AFE1FC6426E5B77AB80318ED64F5F76341695B9FB47AB8AC9537BF5EE9E9D29");insert_node_into_db_real(n, orma);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2607:f130:0:f8::4c85:a645",33445,"8AFE1FC6426E5B77AB80318ED64F5F76341695B9FB47AB8AC9537BF5EE9E9D29");insert_node_into_db_real(n, orma);num_++;
        // @formatter:on
    }

    public static BootstrapNodeEntryDB BootstrapNodeEntryDB_(boolean udp_node_, int num_, String ip_, long port_, String key_hex_)
    {
        BootstrapNodeEntryDB n = new BootstrapNodeEntryDB();
        n.num = num_;
        n.udp_node = udp_node_;
        n.ip = ip_;
        n.port = port_;
        n.key_hex = key_hex_;

        return n;
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

    public List<BootstrapNodeEntryDB> toList()
    {
        List<BootstrapNodeEntryDB> list = new ArrayList<>();

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit);
            while (rs.next())
            {
                BootstrapNodeEntryDB out = new BootstrapNodeEntryDB();

                out.id = rs.getLong("id");
                out.num = rs.getLong("num");
                out.udp_node = rs.getBoolean("udp_node");
                out.ip = rs.getString("ip");
                out.port = rs.getLong("port");
                out.key_hex = rs.getString("key_hex");

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

    private BootstrapNodeEntryDB orderByNumAsc() {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " num ASC ";
        return this;
    }

    private BootstrapNodeEntryDB udp_nodeEq(boolean udp_node) {
        this.sql_where = this.sql_where + " and udp_node='" + b(udp_node) + "' ";
        return this;
    }

    public static void get_udp_nodelist_from_db(OrmaDatabase orma)
    {
        bootstrap_node_list.clear();

        long udp_nodelist_count = 0;
        try
        {
            udp_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(true).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_udp_nodelist_from_db:udp_nodelist_count=" + udp_nodelist_count);

        if (udp_nodelist_count == 0)
        {
            Log.i(TAG, "get_udp_nodelist_from_db:insert_default_udp_nodes_into_db");
            insert_default_udp_nodes_into_db(orma);
        }

        // fill bootstrap_node_list with values from DB -----------------
        try
        {
            bootstrap_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(true).orderByNumAsc().toList());
            Log.i(TAG, "get_udp_nodelist_from_db:bootstrap_node_list.addAll");
        }
        catch (Exception e)
        {
        }
        // fill bootstrap_node_list with values from DB -----------------
    }

    public static void get_tcprelay_nodelist_from_db(OrmaDatabase orma)
    {
        tcprelay_node_list.clear();

        long tcprelay_nodelist_count = 0;
        try
        {
            tcprelay_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(false).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_nodelist_count=" + tcprelay_nodelist_count);

        if (tcprelay_nodelist_count == 0)
        {
            Log.i(TAG, "get_tcprelay_nodelist_from_db:insert_default_tcprelay_nodes_into_db");
            insert_default_tcprelay_nodes_into_db(orma);
        }

        // fill tcprelay_node_list with values from DB -----------------
        try
        {
            tcprelay_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(false).
                    orderByNumAsc().toList());
            Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_node_list.addAll");
        }
        catch (Exception e)
        {
        }
        // fill tcprelay_node_list with values from DB -----------------
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
                    "num,"+
                    "udp_node,"+
                    "ip,"	+
                    "port,"	+
                    "key_hex"+
                    ")" +
                    "values" +
                    "(" +
                    "'"+s(this.num)+"'," +
                    "'"+b(this.udp_node)+"'," +
                    "'"+s(this.ip)+"'," +
                    "'"+s(this.port)+"'," +
                    "'"+s(this.key_hex)+"'" +
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

    public BootstrapNodeEntryDB idEq(long id) {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }

    @NotNull
    public BootstrapNodeEntryDB orderByIdAsc() {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id ASC ";
        return this;
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
}
