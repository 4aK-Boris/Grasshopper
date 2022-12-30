package ru.mpei.grasshopper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.crypto.engines.GOST3412_2015Engine
import org.bouncycastle.jcajce.provider.symmetric.GOST3412_2015
import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.mpei.grasshopper.cipher.GOST_Kuz_Decript
import ru.mpei.grasshopper.cipher.GOST_Kuz_Encript
import ru.mpei.grasshopper.cipher.GOST_Kuz_Expand_Key
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.random.Random

class MainViewModel: ViewModel() {

    private val _time1: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _time2: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _time4: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _time8: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _time16: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _timeAES: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _timeGOST: MutableStateFlow<String> = MutableStateFlow(value = "")

    val time1: StateFlow<String> = _time1
    val time2: StateFlow<String> = _time2
    val time4: StateFlow<String> = _time4
    val time8: StateFlow<String> = _time8
    val time16: StateFlow<String> = _time16
    val timeAES: StateFlow<String> = _timeAES
    val timeGOST: StateFlow<String> = _timeGOST

    private val scope = viewModelScope

    fun cipher(count: Int) = scope.launch(context = Dispatchers.Default) {
        val data = generateFile()
        val blockSize = DATA_SIZE / count
        val dataList = mutableListOf<ByteArray>()
        for (i in 0 until count) {
            dataList.add(data.copyOfRange(fromIndex = blockSize * i, toIndex = blockSize * (i + 1)))
        }
        val (key1, key2) = generateSecretKey()
        GOST_Kuz_Expand_Key(key1, key2)
        val timeJob = crypto(dataList)
        val time = timeJob.await()
        when(count) {
            1 -> _time1.value = time.toString()
            2 -> _time2.value = time.toString()
            4 -> _time4.value = time.toString()
            8 -> _time8.value = time.toString()
            16 -> _time16.value = time.toString()
        }
    }

    fun cipherAES() = scope.launch(context = Dispatchers.Default) {
        val data = generateFile()
        val cipher = Cipher.getInstance("AES")
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secureRandom = SecureRandom()
        val keyBitSize = 256
        keyGenerator.init(keyBitSize, secureRandom)
        val secretKey = keyGenerator.generateKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val start = System.currentTimeMillis()
        val cipherData = cipher.doFinal(data)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val clearData = cipher.doFinal(cipherData)
        val finish = System.currentTimeMillis()
        if (!data.contentEquals(clearData)) {
            throw Exception("Неверное шифрование")
        }
        _timeAES.value = (finish - start).toString()
    }

    fun cipherGOST3412_2015() = scope.launch(context = Dispatchers.Default) {
        val bcProvider = BouncyCastleProvider()
        Security.addProvider(bcProvider)
        val data = generateFile()
        val cipher = Cipher.getInstance("GOST3412-2015", bcProvider)
        val keyGenerator = KeyGenerator.getInstance("GOST3412-2015", bcProvider)
        val secureRandom = SecureRandom()
        val keyBitSize = 256
        keyGenerator.init(keyBitSize, secureRandom)
        val secretKey = keyGenerator.generateKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val start = System.currentTimeMillis()
        val cipherData = cipher.doFinal(data)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val clearData = cipher.doFinal(cipherData)
        val finish = System.currentTimeMillis()
        if (!data.contentEquals(clearData)) {
            throw Exception("Неверное шифрование")
        }
        _timeGOST.value = (finish - start).toString()
    }

    private suspend fun crypto(data: List<ByteArray>) = scope.async(context = Dispatchers.Default) {
        val start = System.currentTimeMillis()
        val result = data.map { encrypt(data = it) }
        result.first().join()
        val finish = System.currentTimeMillis()
        return@async finish - start
    }

    private fun encrypt(data: ByteArray) = scope.launch(Dispatchers.Default) {
        println("dwadwadwa")
        var result = byteArrayOf()
        for (i in data.indices step 16) {
            result += encryptBlock(data = data.copyOfRange(fromIndex = i, toIndex = i + 16))
        }
        decrypt(result)
    }

    private fun decrypt(data: ByteArray) {
        var result = byteArrayOf()
        for (i in data.indices step 16) {
            result += decryptBlock(data = data.copyOfRange(fromIndex = i, toIndex = i + 16))
        }
    }

    private fun encryptBlock(data: ByteArray): ByteArray {
        return GOST_Kuz_Encript(data)
    }

    private fun decryptBlock(data: ByteArray): ByteArray {
        return GOST_Kuz_Decript(data)
    }

    private fun generateSecretKey(): Pair<ByteArray, ByteArray> {
        return rnd.nextBytes(size = KEY_SIZE / 2) to rnd.nextBytes(size = KEY_SIZE / 2)
    }

    private fun generateFile(): ByteArray = rnd.nextBytes(size = DATA_SIZE)

    companion object {

        private const val DATA_SIZE = 1024 * 1 * 1

        private val rnd = Random

        private const val KEY_SIZE = 32
    }
}