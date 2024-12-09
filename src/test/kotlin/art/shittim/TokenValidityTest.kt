package art.shittim

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class TokenValidityTest {

    @Test
    fun testValidToken() {
        val token = "ec7f49c0-9a55-4f1a-91bc-dd429915a671"

        val result = runBlocking {
            tokenValidity(token)
        }

        assert(result)
    }
}