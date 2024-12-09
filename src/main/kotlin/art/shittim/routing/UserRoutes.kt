package art.shittim.routing

import art.shittim.db.PasswordUser
import art.shittim.db.userService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    get("/user/{name}") {
        val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val user = userService.readByName(name) ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(user)
    }

    put("/user/{name}") {
        val name = call.parameters["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
        val id = userService.readIdByName(name)

        val user = call.receive<PasswordUser>()

        if(id == null) {
            userService.create(user)
        } else {
            userService.update(id, user)
        }
    }

    delete("/user/{name}") {
        val name = call.parameters["name"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val id = userService.readIdByName(name) ?: return@delete call.respond(HttpStatusCode.NotFound)

        userService.delete(id)
    }
}