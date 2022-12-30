package ru.mpei.grasshopper.cipher

private fun GOST_Kuz_X(a: ByteArray, b: ByteArray): ByteArray {
    val c = ByteArray(BLOCK_SIZE)
    var i = 0
    while (i < BLOCK_SIZE) {
        c[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        i++
    }
    return c
}

private fun GOST_Kuz_S(in_data: ByteArray): ByteArray {
    val outData = ByteArray(in_data.size)
    var i = 0
    while (i < BLOCK_SIZE) {
        var data = in_data[i].toInt()
        if (data < 0) {
            data += 256
        }
        outData[i] = Pi[data]
        i++
    }
    return outData
}

private fun GOST_Kuz_GF_mul(a: Byte, b: Byte): Byte {
    var a1 = a
    var b1 = b
    var c: Byte = 0
    var hiBit: Byte
    var i = 0
    while (i < 8) {
        if (b1.toInt() and 1 == 1) c = (c.toInt() xor a1.toInt()).toByte()
        hiBit = (a1.toInt() and 0x80).toByte()
        a1 = (a1.toInt() shl 1).toByte()
        if (hiBit < 0) a1 = (a1.toInt() xor 0xc3).toByte()
        b1 = (b1.toInt() shr 1).toByte()
        i++
    }
    return c
}

private fun GOST_Kuz_R(state: ByteArray): ByteArray {
    var a: Byte = 0
    val internal = ByteArray(size = 16)
    var i = 15
    while (i >= 0) {
        if (i == 0) internal[15] = state[i] else internal[i - 1] = state[i]
        a = (a.toInt() xor GOST_Kuz_GF_mul(state[i], l_vec[i]).toInt()).toByte()
        i--
    }
    internal[15] = a
    return internal
}

private fun GOST_Kuz_L(in_data: ByteArray): ByteArray {
    val outData: ByteArray
    var internal = in_data
    var i = 0
    while (i < 16) {
        internal = GOST_Kuz_R(internal)
        i++
    }
    outData = internal
    return outData
}

private fun GOST_Kuz_reverse_S(in_data: ByteArray): ByteArray {
    val outData = ByteArray(in_data.size)
    var i = 0
    while (i < BLOCK_SIZE) {
        var data = in_data[i].toInt()
        if (data < 0) {
            data += 256
        }
        outData[i] = reverse_Pi[data]
        i++
    }
    return outData
}

private fun GOST_Kuz_reverse_R(state: ByteArray): ByteArray {
    var a: Byte
    a = state[15]
    val internal = ByteArray(16)
    var i = 1
    while (i < 16) {
        internal[i] = state[i - 1]
        a = (a.toInt() xor GOST_Kuz_GF_mul(internal[i], l_vec[i]).toInt()).toByte()
        i++
    }
    internal[0] = a
    return internal
}

private fun GOST_Kuz_reverse_L(in_data: ByteArray): ByteArray {
    val outData: ByteArray
    var internal: ByteArray
    internal = in_data
    var i = 0
    while (i < 16) {
        internal = GOST_Kuz_reverse_R(internal)
        i++
    }
    outData = internal
    return outData
}

private fun GOST_Kuz_Get_C() {
    val iterNum = Array(size = 32) { ByteArray(size = 16) }
    var i = 0
    while (i < 32) {
        for (j in 0 until BLOCK_SIZE) iterNum[i][j] = 0
        iterNum[i][0] = (i + 1).toByte()
        i++
    }
    i = 0
    while (i < 32) {
        iter_C[i] = GOST_Kuz_L(iterNum[i])
        i++
    }
}

private fun GOST_Kuz_F(
    in_key_1: ByteArray?,
    in_key_2: ByteArray?,
    iter_const: ByteArray
): Array<ByteArray?> {
    var internal: ByteArray = GOST_Kuz_X(in_key_1!!, iter_const)
    internal = GOST_Kuz_S(internal)
    internal = GOST_Kuz_L(internal)
    val outKey = GOST_Kuz_X(internal, in_key_2!!)
    val key = arrayOfNulls<ByteArray>(2)
    key[0] = outKey
    key[1] = in_key_1
    return key
}

fun GOST_Kuz_Expand_Key(key_1: ByteArray, key_2: ByteArray) {
    var iter12 = arrayOfNulls<ByteArray>(2)
    var iter34 = arrayOfNulls<ByteArray>(2)
    GOST_Kuz_Get_C()
    iter_key[0] = key_1
    iter_key[1] = key_2
    iter12[0] = key_1
    iter12[1] = key_2
    var i = 0
    while (i < 4) {
        iter34 = GOST_Kuz_F(iter12[0], iter12[1], iter_C[0 + 8 * i])
        iter12 = GOST_Kuz_F(iter34[0], iter34[1], iter_C[1 + 8 * i])
        iter34 = GOST_Kuz_F(iter12[0], iter12[1], iter_C[2 + 8 * i])
        iter12 = GOST_Kuz_F(iter34[0], iter34[1], iter_C[3 + 8 * i])
        iter34 = GOST_Kuz_F(iter12[0], iter12[1], iter_C[4 + 8 * i])
        iter12 = GOST_Kuz_F(iter34[0], iter34[1], iter_C[5 + 8 * i])
        iter34 = GOST_Kuz_F(iter12[0], iter12[1], iter_C[6 + 8 * i])
        iter12 = GOST_Kuz_F(iter34[0], iter34[1], iter_C[7 + 8 * i])
        iter_key[2 * i + 2] = iter12[0]!!
        iter_key[2 * i + 3] = iter12[1]!!
        i++
    }
}

fun GOST_Kuz_Encript(blk: ByteArray?): ByteArray {
    var outBlk: ByteArray?
    outBlk = blk
    var i = 0
    while (i < 9) {
        outBlk = GOST_Kuz_X(iter_key[i], outBlk!!)
        outBlk = GOST_Kuz_S(outBlk)
        outBlk = GOST_Kuz_L(outBlk)
        i++
    }
    outBlk = GOST_Kuz_X(outBlk!!, iter_key[9])
    return outBlk
}

fun GOST_Kuz_Decript(blk: ByteArray): ByteArray {
    var outBlk: ByteArray
    outBlk = blk
    outBlk = GOST_Kuz_X(outBlk, iter_key[9])
    var i: Int = 8
    while (i >= 0) {
        outBlk = GOST_Kuz_reverse_L(outBlk)
        outBlk = GOST_Kuz_reverse_S(outBlk)
        outBlk = GOST_Kuz_X(iter_key[i], outBlk)
        i--
    }
    return outBlk
}