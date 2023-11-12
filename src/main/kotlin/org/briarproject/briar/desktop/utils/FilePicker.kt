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

package org.briarproject.briar.desktop.utils

import com.zoffcc.applications.trifa.Log
import java.awt.FileDialog
import java.awt.Frame

object FilePicker {

    val TAG = "trifa.FilePicker"

    private val SUPPORTED_EXTENSIONS_IMAGE = setOf("png", "jpg", "jpeg", "gif", "webp")

    fun pickFileUsingDialog(parent: Frame? = null, onCloseRequest: (directory: String?, filename: String?) -> Unit) {
        val dialog = java.awt.FileDialog(parent)
        dialog.isMultipleMode = false
        /*
        dialog.setFilenameFilter { dir, name ->
            val parts = name.split(".")
            if (parts.size < 2) {
                false
            } else {
                val extension = parts[parts.size - 1]
                SUPPORTED_EXTENSIONS_IMAGE.contains(extension.lowercase())
            }
        }
        */
        dialog.mode = FileDialog.LOAD
        dialog.isVisible = true
        val files = dialog.files
        val file = if (files == null || files.isEmpty()) null else files[0]
        Log.i (TAG, "Loading from file '$file'")
        if (file != null) {
            try
            {
                onCloseRequest(file.absoluteFile.parent, file.absoluteFile.name)
            }
            catch (e: Exception)
            {
            }
        }
    }
}
