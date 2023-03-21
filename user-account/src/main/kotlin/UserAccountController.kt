import client.ExchangeClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import model.User
import repository.UserRepository

class UserAccountController(
    private val userDao: UserRepository,
    private val exchangeClient: ExchangeClient
) : AutoCloseable {
    private var server: ApplicationEngine? = null

    fun start(port: Int) {
        server = embeddedServer(Netty, port) {
            routing {
                get("/user/register") {
                    val name = getParam("name")!!
                    userDao.addUser(
                        User(
                            id = null,
                            name = name,
                            balance = 0.0,
                            stocks = mutableMapOf()
                        )
                    )
                    call.response.status(HttpStatusCode.Created)
                }

                get("/user/deposit") {
                    val id = getParam("id")!!.toLong()
                    val amount = getParam("amount")!!.toDouble()
                    if (amount <= 0) {
                        badRequest("Amount should be positive")
                    } else {
                        val user = userDao.getUser(id)
                        if (user == null) {
                            badRequest("User with id $id does not exist")
                        } else {
                            userDao.updateUser(id, user.copy(balance = user.balance + amount))
                            call.response.status(HttpStatusCode.OK)
                        }
                    }
                }

                get("/user/withdraw") {
                    val id = getParam("id")!!.toLong()
                    val amount = getParam("amount")!!.toDouble()
                    if (amount <= 0) {
                        badRequest("Amount should be positive")
                    } else {
                        val user = userDao.getUser(id)
                        if (user == null) {
                            badRequest("User with id $id does not exist")
                        } else {
                            if (user.balance < amount) {
                                badRequest("User with id $id does not have enough balance")
                            } else {
                                userDao.updateUser(id, user.copy(balance = user.balance - amount))
                                call.response.status(HttpStatusCode.OK)
                            }
                        }
                    }
                }

                get("/user/stocks") {
                    val id = getParam("id")!!.toLong()
                    val user = userDao.getUser(id)

                    if (user == null) {
                        badRequest("User with id $id does not exist")
                    } else {
                        call.respondText { user.stocks.toList().joinToString { "${it.first}:${it.second}" } }
                        call.response.status(HttpStatusCode.OK)
                    }
                }

                get("/user/total") {
                    val id = getParam("id")!!.toLong()
                    val user = userDao.getUser(id)

                    if (user == null) {
                        badRequest("User with id $id does not exist")
                    } else {
                        val total = user.stocks.map { (companyName, count) ->
                            val price = exchangeClient.getPrice(companyName)
                            count * price
                        }.sum()
                        call.respondText { total.toString() }
                    }
                }

                get("/user/balance") {
                    val id = getParam("id")!!.toLong()
                    val user = userDao.getUser(id)

                    if (user == null) {
                        badRequest("User with id $id does not exist")
                    } else {
                        call.respondText { user.balance.toString() }
                    }
                }

                get("/user/buy") {
                    val id = getParam("id")!!.toLong()
                    val company = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val user = userDao.getUser(id)
                    if (user == null) {
                        badRequest("User with id $id does not exist")
                    } else {
                        val price = exchangeClient.getPrice(company)
                        if (user.balance < price * count) {
                            badRequest("User with id $id does not have enough balance")
                        } else {
                            val response = exchangeClient.buy(company, count)
                            if (response == "OK") {
                                user.stocks.putIfAbsent(company, 0)
                                user.stocks[company] = user.stocks[company]!! + count
                                userDao.updateUser(
                                    id, user.copy(
                                        balance = user.balance - price * count
                                    )
                                )
                            } else {
                                badRequest(response)
                            }
                        }
                    }
                }

                get("/user/sell") {
                    val id = getParam("id")!!.toLong()
                    val company = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val user = userDao.getUser(id)
                    if (user == null) {
                        badRequest("User with id $id does not exist")
                    } else {
                        val response = exchangeClient.sell(company, count)
                        if (response == "OK") {
                            if (user.stocks.getOrDefault(company, 0) < count) {
                                badRequest("User with id $id does not have enough stocks")
                            } else {
                                user.stocks[company] = user.stocks[company]!! - count
                                val price = exchangeClient.getPrice(company)
                                userDao.updateUser(
                                    id, user.copy(
                                        balance = user.balance + price * count
                                    )
                                )
                            }
                        } else {
                            badRequest(response)
                        }
                    }
                }
            }
        }
        server?.start(wait = true)
    }

    override fun close() {
        server?.stop(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
    }

    private fun PipelineContext<*, ApplicationCall>.getParam(key: String) = context.request.queryParameters[key]

    private suspend fun PipelineContext<*, ApplicationCall>.badRequest(msg: String) {
        call.response.status(HttpStatusCode.BadRequest)
        call.respondText { msg }
    }

    companion object {
        const val STOP_GRACE_PERIOD_MS = 3000L
        const val STOP_TIMEOUT_MS = 3000L
    }
}
