package art.shittim

import art.shittim.config.CConfig
import art.shittim.db.configureDatabases
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.addStreamSource
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
        .addResourceSource("/application-config.yml", optional = true)
        .addStreamSource(App::class.java.getResourceAsStream("/default.yml")!!, "yaml")
        .build()
        .loadConfigOrThrow<CConfig>()
}

fun main() {
    //io.ktor.server.netty.EngineMain.main(args)

    embeddedServer(Netty, port = config.server.port) {
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

object App