package ru.mpei.grasshopper

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.*
import org.junit.Test
import ru.mpei.grasshopper.cipher.GOST_Kuz_Decript
import ru.mpei.grasshopper.cipher.GOST_Kuz_Encript
import ru.mpei.grasshopper.cipher.GOST_Kuz_Expand_Key
import ru.mpei.grasshopper.cipher.blk
import ru.mpei.grasshopper.cipher.key_1
import ru.mpei.grasshopper.cipher.key_2
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        GOST_Kuz_Expand_Key(key_1, key_2)
        val encriptBlok = GOST_Kuz_Encript(blk)
        println(encriptBlok.joinToString())
        val decriptBlok = GOST_Kuz_Decript(encriptBlok)
        println(decriptBlok.decodeToString())
    }

    @Test
    fun test() {
        val bcProvider = BouncyCastleProvider()
        Security.addProvider(bcProvider)
        val data = Random.nextBytes(32)
        val cipher = Cipher.getInstance("GOST28147")
        val keyGenerator = KeyGenerator.getInstance("GOST28147")
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
    }
}