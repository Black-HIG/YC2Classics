package art.shittim.routing

import art.shittim.db.BeanUser
import art.shittim.db.PasswordUser
import art.shittim.db.UserData
import art.shittim.db.UserService
import art.shittim.db.UserService.UserTable
import art.shittim.logger
import art.shittim.secure.PAccountDelete
import art.shittim.secure.PAccountList
import art.shittim.secure.PAccountModify
import art.shittim.secure.authenticatePerm
import art.shittim.secure.hashed
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Route.userRoutes() {
    get("/user/{name}") {
        val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val user = newSuspendedTransaction {
            UserService.UserEntity.find { UserTable.username eq name }
                .map {
                    BeanUser(
                        it.username,
                        it.perm
                    )
                }.firstOrNull()
        } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(user)
    }

    authenticate("auth-jwt") {
        authenticatePerm(PAccountList) {
            get("/user/list") {
                val users = newSuspendedTransaction {
                    UserService.UserEntity.all()
                        .map {
                            BeanUser(
                                it.username,
                                it.perm
                            )
                        }
                }

                call.respond(users)
            }
        }

        authenticatePerm(PAccountModify) {
            put("/user/{name}") {
                val name = call.parameters["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val id = newSuspendedTransaction {
                    UserService.UserEntity.find { UserTable.username eq name }.singleOrNull()?.id
                }
                val user = call.receive<UserData>().let {
                    PasswordUser(
                        name,
                        it.password,
                        it.perm
                    )
                }

                if (id == null) {
                    newSuspendedTransaction {
                        UserService.UserEntity.new {
                            username = user.username
                            password = user.password.hashed()
                            perm = user.perm
                        }
                    }
                    logger.info("Created new user named {} with perm {}", name, user.perm)
                } else {
                    newSuspendedTransaction {
                        UserService.UserEntity.findByNameAndUpdate(name) {
                            it.username = user.username
                            it.password = user.password.hashed()
                            it.perm = user.perm
                        }
                    }
                    logger.info("Updated user named {} with perm {}", name, user.perm)
                }

                call.respond(HttpStatusCode.Created)
            }
        }

        authenticatePerm(PAccountDelete) {
            delete("/user/{name}") {
                val name = call.parameters["name"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = newSuspendedTransaction {
                    UserService.UserEntity.findByName(name).singleOrNull()
                } ?: return@delete call.respond(HttpStatusCode.NotFound)
                newSuspendedTransaction {
                    id.delete()
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}