package art.shittim

import art.shittim.db.configureDatabases
import art.shittim.routing.configureRouting
import art.shittim.secure.configureSecurity
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.logging.*
import org.slf4j.LoggerFactory

/*val config by lazy {
    // Create if not exist

    Path("config/application-config.yml").let {
        if(it.notExists()) {
            it.toFile().mkdirs()
            it.writeBytes(App::class.java.getResourceAsStream("/default.yml")!!.readBytes())
        }
    }

    ConfigLoaderBuilder.default()
        .addFileSource("config/application-config.yml")
        .build()
        .loadConfigOrThrow<CConfig>()
}*/

fun main() {
    //io.ktor.server.netty.EngineMain.main(args)

    embeddedServer(Netty, port = System.getenv("SERVER_PORT").toInt()) {
        module()
    }.start(wait = true)
}

lateinit var logger: Logger

fun Application.module() {
    logger = LoggerFactory.getLogger("Classics")
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()
}

@Suppress("unused")
object App