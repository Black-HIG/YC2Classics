package art.shittim

import art.shittim.db.configureDatabases
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    //io.ktor.server.netty.EngineMain.main(args)

    embeddedServer(Netty, port = 31234) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
