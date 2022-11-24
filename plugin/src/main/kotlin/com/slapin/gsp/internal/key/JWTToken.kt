package com.slapin.gsp.internal.key

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.time.temporal.ChronoUnit

internal fun getJWTToken(
  privateKey: String,
  serviceAccountId: String,
  scopes: List<String>,
): String {
  val rsaPrivateKey = privateKey.readPrivateKey()
  return JWT.create()
    .withIssuedAt(Instant.now())
    .withExpiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
    .withIssuer(serviceAccountId)
    .withArrayClaim("scopes", scopes.toTypedArray())
    .sign(Algorithm.RSA256(rsaPrivateKey))
}
