package com.zoffcc.applications.trifa

import java.nio.ByteBuffer

class ByteBufferCompat(bf: ByteBuffer) {
    var b: ByteArray? = null
    var f: ByteBuffer? = null

    init {
        f = bf
        bf.rewind()
        b = ByteArray(bf.remaining())
        bf.slice()[b]
    }

    fun array(): ByteArray? {
        return b
    }

    fun limit(): Int {
        return f!!.limit()
    }

    fun arrayOffset(): Int {
        return 0
    }
}
