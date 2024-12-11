package art.shittim.secure

import art.shittim.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val PermissionAuthorizationPlugin = createRouteScopedPlugin(
    name = "PermAuthorizationPlugin",
    createConfiguration = ::PermAuthConfiguration
) {
    val requiresPerms = pluginConfig.required

    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            val principal = call.principal<JWTPrincipal>() ?: return@on call.respond(HttpStatusCode.Unauthorized)
            val perm = principal.payload.getClaim("perm").asLong()
            val username = principal.payload.getClaim("username").asString()
            logger.info("User {} with perm {} have requested", username, perm)
            logger.debug("{} auth with {}", perm, requiresPerms)

            var passed = true

            requiresPerms.forEach {
                passed = passed && (perm hasPerm it)
            }

            if(!passed) {
                call.respond(HttpStatusCode.Forbidden)
            }

            logger.debug("Result: $passed")
        }
    }
}

class PermAuthConfiguration {
    var required: List<Long> = emptyList()
}

fun Route.authenticatePerm(
    vararg required :Long,
    build: Route.() -> Unit
) : Route {
    val authenticatedRoute = createChild(AuthenticationRouteSelector(emptyList()))

    authenticatedRoute.install(PermissionAuthorizationPlugin) {
        this.required = required.toList()
    }
    authenticatedRoute.build()
    return authenticatedRoute
}