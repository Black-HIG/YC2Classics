package art.shittim.secure

object RefreshTokenStore {
    private val refreshTokens = mutableMapOf<String, String>()

    operator fun get(username: String): String? {
        return refreshTokens[username]
    }

    operator fun set(username: String, refreshToken: String) {
        refreshTokens[username] = refreshToken
    }

    @Suppress("unused")
    fun invalidateRefreshToken(username: String) {
        refreshTokens.remove(username)
    }
}