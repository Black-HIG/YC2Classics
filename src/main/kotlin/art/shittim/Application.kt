package art.shittim

import art.shittim.config.CConfig
import art.shittim.db.configureDatabases
import art.shittim.routing.configureRouting
import art.shittim.secure.allPerm
import art.shittim.secure.configureSecurity
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.addStreamSource
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.logging.*
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.writeBytes

val config by lazy {
    // Create if not exist

    Path("application-config.yml").let {
        if(it.notExists()) {
            it.writeBytes(App::class.java.getResourceAsStream("/default.yml")!!.readBytes())
        }
    }

    ConfigLoaderBuilder.default()
        .addFileSource("application-config.yml")
        .build()
        .loadConfigOrThrow<CConfig>()
}

fun main() {
    //io.ktor.server.netty.EngineMain.main(args)

    embeddedServer(Netty, port = config.server.port) {
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

object App