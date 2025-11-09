package org.example.bank

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.io.StringReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

@Component
class RsaDecryptor(
    private val resourceLoader: ResourceLoader,
    @Value("\${bank.private-key-path:classpath:private.pem}")
    private val privateKeyLocation: String
) {
    private val logger = LoggerFactory.getLogger(RsaDecryptor::class.java)
    private val privateKey: PrivateKey

    init {
        Security.addProvider(BouncyCastleProvider())
        privateKey = loadPrivateKey(privateKeyLocation)
        logger.info("Loaded RSA private key from {}", privateKeyLocation)
    }

    fun decrypt(base64CipherText: String): String {
        val cipherBytes = Base64.getDecoder().decode(base64CipherText)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(cipherBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun loadPrivateKey(location: String): PrivateKey {
        val resource = resourceLoader.getResource(location)
        require(resource.exists()) { "Private key resource not found: $location" }
        val pemText = resource.inputStream.bufferedReader().use { it.readText() }

        PEMParser(StringReader(pemText)).use { pemParser ->
            val converter = JcaPEMKeyConverter().setProvider("BC")
            val pemObject = pemParser.readObject()
            return when (pemObject) {
                is PEMKeyPair -> converter.getKeyPair(pemObject).private
                is PrivateKeyInfo -> converter.getPrivateKey(pemObject)
                null -> throw IllegalStateException("No key material found in $location")
                else -> extractPkcs8FromString(pemText)
            }
        }
    }

    private fun extractPkcs8FromString(pem: String): PrivateKey {
        val sanitized = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(sanitized)
        val pkcs8Bytes = if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            val rsaKey = RSAPrivateKey.getInstance(ASN1Primitive.fromByteArray(decoded))
            PrivateKeyInfo(AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), rsaKey).encoded
        } else {
            decoded
        }

        val keySpec = PKCS8EncodedKeySpec(pkcs8Bytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
}
