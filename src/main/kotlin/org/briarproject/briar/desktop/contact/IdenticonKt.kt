/*
 * Briar Desktop
 * Copyright (C) 2021-2022 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.briarproject.briar.desktop.contact

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.zoffcc.applications.trifa.HelperFiletransfer
import com.zoffcc.applications.trifa.Log

/**
 * Copyright 2014 www.delight.im (info@delight.im)
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
internal class IdenticonKt(private val input: String, width: Float, height: Float) {
    private val TAG = "trifa.IdenticonKt"
    companion object {
        private const val ROWS = 5
        private const val COLUMNS = 6 //(ROWS + 1) / 2 // == 3
        private const val CENTER_COLUMN_INDEX = COLUMNS / 2 + COLUMNS % 2
    }

    private lateinit var input_bytes: ByteArray
    private val colors: Array<Array<Color>>
    private val cellWidth: Float
    private val cellHeight: Float

    private fun setBytes(input: String) {
        // Log.i(TAG, "create_identicon:in=" + input);
        val input_bytes_tmp = ByteArray(input.length / 2)
        var jj = 0
        while (jj < input.length / 2)
        {
            val hex_ = "0x" + input.substring(jj * 2, jj * 2 + 2).lowercase()
            val cur_byte = Integer.decode(hex_)
            // Log.i(TAG, "create_identicon:loop:byte=" + cur_byte + " hex=" + hex_);
            input_bytes_tmp[jj] = cur_byte.toByte()
            jj++
        }
        input_bytes = HelperFiletransfer.sha256(input_bytes_tmp)
    }

    private fun getByte(index: Int): Byte
    {
        return input_bytes[index % input_bytes.size]
    }

    init {
        setBytes(input)
        require(input_bytes.isNotEmpty())

        cellWidth = width / COLUMNS
        cellHeight = height / ROWS

        colors = Array(ROWS) { Array(COLUMNS) { Color(0) } }
        for (r in 0 until ROWS) {
            for (c in 0 until COLUMNS) {
                colors[r][c] = if (isCellVisible(r, c)) foregroundColor else backgroundColor
            }
        }
    }

    private fun isCellVisible(row: Int, column: Int): Boolean {
        val index = 3 + row * CENTER_COLUMN_INDEX + getSymmetricColumnIndex(column)
        return getByte(index) >= 0
    }

    private fun getSymmetricColumnIndex(index: Int): Int {
        return if (index < CENTER_COLUMN_INDEX) index else COLUMNS - index - 1
    }

    private val foregroundColor: Color
        get() {
            val r = getByte(0) * 3 / 4 + 96
            val g = getByte(1) * 3 / 4 + 96
            val b = getByte(2) * 3 / 4 + 96
            return Color(r, g, b)
        }

    // http://www.google.com/design/spec/style/color.html#color-themes
    private val backgroundColor: Color
        get() = Color(0xFA, 0xFA, 0xFA)

    fun draw(g: DrawScope) {
        for (r in 0 until ROWS) {
            for (c in 0 until COLUMNS) {
                val x = cellWidth * c
                val y = cellHeight * r

                // Log.i(TAG, "x="+x+" y="+y+ " w="+cellWidth+ " h="+cellHeight)
                g.drawRect(
                    color = colors[r][c],
                    topLeft = Offset(x, y),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}
