/*
 * Briar Desktop
 * Copyright (C) 2021-2023 The Briar Project
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

package org.briarproject.briar.desktop.threading

import java.util.concurrent.Executor

class BriarExecutorsImpl
@Inject
constructor(
    @UiExecutor private val uiExecutor: Executor,
    @IoExecutor private val ioExecutor: Executor,
) : BriarExecutors {

    override fun onUiThread(@UiExecutor task: () -> Unit) = uiExecutor.execute(task)

    override fun onIoThread(@IoExecutor task: () -> Unit) = ioExecutor.execute(task)
}

annotation class Inject
