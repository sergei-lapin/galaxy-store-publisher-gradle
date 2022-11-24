package com.slapin.gsp.internal.key

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

private const val PKCS1Header = "-----BEGIN RSA PRIVATE KEY-----"
private const val PKCS1Footer = "-----END RSA PRIVATE KEY-----"

private const val PKCS8Header = "-----BEGIN PRIVATE KEY-----"
private const val PKCS8Footer = "-----END PRIVATE KEY-----"

internal fun String.readPrivateKey(): RSAPrivateKey {
  val isPKCS1 = contains(PKCS1Header)
  val isPKCS8 = contains(PKCS8Header)

  val base64text =
    remove("\n").remove(PKCS1Header).remove(PKCS1Footer).remove(PKCS8Header).remove(PKCS8Footer)

  val encodedKey =
    when {
      isPKCS1 -> Base64.getDecoder().decode(base64text).withPKCS8HeaderPrefix()
      isPKCS8 -> Base64.getDecoder().decode(base64text)
      else -> error("Unknown key format")
    }

  val keySpec = PKCS8EncodedKeySpec(encodedKey)

  return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
}

private fun String.remove(other: String): String = replace(other, "")

/**
 * Do it like they do it on Discovery channel
 * [(Mastercard)](https://github.com/Mastercard/client-encryption-java/blob/main/src/main/java/com/mastercard/developer/utils/EncryptionUtils.java)
 */
private fun ByteArray.withPKCS8HeaderPrefix(): ByteArray {
  val pkcs1Length = this.size
  val totalLength = pkcs1Length + 22
  val pkcs8Header =
    byteArrayOf(
      0x30,
      0x82.toByte(),
      (totalLength shr 8 and 0xff).toByte(),
      (totalLength and 0xff).toByte(), // Sequence + total length
      0x2,
      0x1,
      0x0, // Integer (0)
      0x30,
      0xD,
      0x6,
      0x9,
      0x2A,
      0x86.toByte(),
      0x48,
      0x86.toByte(),
      0xF7.toByte(),
      0xD,
      0x1,
      0x1,
      0x1,
      0x5,
      0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
      0x4,
      0x82.toByte(),
      (pkcs1Length shr 8 and 0xff).toByte(),
      (pkcs1Length and 0xff).toByte(), // Octet string + length
    )
  return pkcs8Header + this
}
