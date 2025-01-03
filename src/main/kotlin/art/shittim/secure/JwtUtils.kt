package art.shittim.secure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object JwtUtils {
    val refresh_secret = System.getenv("REFRESH_JWT_SECRET") ?: "yc2_waterstarlake_0"
    val access_secret = System.getenv("ACCESS_TOKEN_SECRET") ?: "yc2_waterstarlake_1"
    const val ISSUER = "classics.shittim.art"
    const val AUDIENCE = "classics.shittim.art"
    const val MY_REALM = "YC2 Classics Access"

    val refreshExpires = 7.days.inWholeMilliseconds
    val accessExpires = 15.minutes.inWholeMilliseconds

    val refresh_alg: Algorithm = Algorithm.HMAC256(refresh_secret)
    val access_alg: Algorithm = Algorithm.HMAC256(access_secret)

    val refreshJwtVerifier: JWTVerifier = JWT
        .require(refresh_alg)
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    val accessJwtVerifier: JWTVerifier = JWT
        .require(access_alg)
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    fun makeAccessToken(username: String, perm: Long): String =
        JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("username", username)
            .withClaim("perm", perm)
            .withExpiresAt(Date(System.currentTimeMillis() + accessExpires))
            .sign(access_alg)

    fun makeRefreshToken(username: String, perm: Long): String =
        JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("username", username)
            .withClaim("perm", perm)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshExpires))
            .sign(refresh_alg)
}