package art.shittim.routing

import art.shittim.db.*
import art.shittim.logger
import art.shittim.secure.PAccountModify
import art.shittim.secure.authenticatePerm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.h2.engine.User
import org.jetbrains.exposed.sql.selectAll

fun Route.userRoutes() {
    get("/user/{name}") {
        val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val user = userService.readByName(name) ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(user)
    }

    authenticate("auth-jwt") {
        authenticatePerm(PAccountModify) {
            get("/user/list") {
                val users = userService.dbQuery {
                    UserService.UserTable
                        .selectAll()
                        .orderBy(UserService.UserTable.id)
                        .map {
                            BeanUser(
                                it[UserService.UserTable.username],
                                it[UserService.UserTable.perm]
                            )
                        }.toList()
                }

                call.respond(users)
            }

            put("/user/{name}") {
                val name = call.parameters["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val id = userService.readIdByName(name)

                val user = call.receive<UserData>().let {
                    PasswordUser(
                        name,
                        it.password,
                        it.perm
                    )
                }

                if(id == null) {
                    userService.create(user)
                    logger.info("Created new user named {} with perm {}", name, user.perm)
                } else {
                    userService.update(id, user)
                    logger.info("Updated user named {} with perm {}", name, user.perm)
                }

                call.respond(HttpStatusCode.Created)
            }

            delete("/user/{name}") {
                val name = call.parameters["name"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = userService.readIdByName(name) ?: return@delete call.respond(HttpStatusCode.NotFound)

                userService.delete(id)

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}