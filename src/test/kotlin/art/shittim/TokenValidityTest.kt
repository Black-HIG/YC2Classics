package art.shittim

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class TokenValidityTest {

    @Test
    fun testValidToken() {
        val token = "ae2d44aa-ab76-47bb-8317-28f46a02e015"

        val result = runBlocking {
            tokenValidity(token)
        }

        assert(result)
    }
}