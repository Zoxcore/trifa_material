package com.zoffcc.applications.trifa

import com.zoffcc.applications.trifa.Log.i
import com.zoffcc.applications.trifa.MainActivity.Companion.update_savedata_file

object HelperGeneric {
    private const val TAG = "trifa.Hlp.Generic"
    fun update_savedata_file_wrapper() {
        try {
            MainActivity.semaphore_tox_savedata!!.acquire()
            val password_hash_2 = MainActivity.password_hash
            val start_timestamp = System.currentTimeMillis()
            update_savedata_file(password_hash_2)
            val end_timestamp = System.currentTimeMillis()
            MainActivity.semaphore_tox_savedata!!.release()
            i(TAG, "update_savedata_file() took:" + (end_timestamp - start_timestamp).toFloat() / 1000f + "s")
        } catch (e: InterruptedException) {
            MainActivity.semaphore_tox_savedata!!.release()
            e.printStackTrace()
        }
    }
}
