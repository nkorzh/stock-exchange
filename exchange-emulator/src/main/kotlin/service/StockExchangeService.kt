package service

import repository.StockRepository
import model.Company
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*

class StockExchangeService(
    private val stocksDao: StockRepository,
    private val priceGeneratorService: PriceGeneratorService
) {
    fun launch(port: Int) {
        embeddedServer(Netty, port) {
            routing {
                get("/company/add") {
                    val name = getParam("company")!!
                    val price = getParam("price")!!.toDouble()
                    val mbStocksCount = getParam("count")?.toInt()

                    if (stocksDao.getCompanyByName(name) != null) {
                        badRequest("Company $name already exists")
                        return@get
                    }
                    val stocksCount = mbStocksCount ?: 0
                    stocksDao.addCompany(Company(name, price, stocksCount))
                    call.response.status(HttpStatusCode.Created)
                }

                get("/stock/add") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val company = stocksDao.getCompanyByName(name)
                    if (company == null) {
                        badRequest("Company $name does not exist")
                    } else {
                        stocksDao.updateCompany(
                            company.copy(
                                amount = company.amount + count
                            )
                        )
                        call.response.status(HttpStatusCode.OK)
                    }
                }

                get("/stock/price") {
                    val name = getParam("company")!!
                    val company = stocksDao.getCompanyByName(name)
                    if (company == null) {
                        badRequest("Company $name does not exist")
                    } else {
                        call.respondText { company.price.toString() }
                    }
                }

                get("/stock/count") {
                    val name = getParam("company")!!
                    val company = stocksDao.getCompanyByName(name)
                    if (company == null) {
                        badRequest("Company $name does not exist")
                    } else {
                        call.respondText { company.amount.toString() }
                    }
                }

                get("/stock/buy") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val company = stocksDao.getCompanyByName(name)
                    if (company == null) {
                        badRequest("Company $name does not exist")
                    } else if (company.amount < count) {
                        badRequest("Company $name does not have enough stocks")
                    } else {
                        stocksDao.updateCompany(
                            company.copy(amount = company.amount - count)
                        )
                        call.response.status(HttpStatusCode.OK)
                        call.respondText("OK")

                        updateCompanyPrice(company)
                    }
                }

                get("/stock/sell") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val company = stocksDao.getCompanyByName(name)
                    if (company == null) {
                        badRequest("Company $name does not exist")
                    } else {
                        stocksDao.updateCompany(
                            company.copy(amount = company.amount + count)
                        )
                        call.response.status(HttpStatusCode.OK)
                        call.respondText("OK")

                        updateCompanyPrice(company)
                    }
                }

                // Needed only for test purposes
                get("/stock/clear") {
                    stocksDao.clear()
                }
            }
        }.start(wait = true)
    }

    private fun updateCompanyPrice(company: Company) {
        val newPrice = priceGeneratorService.generateNewPrice(company.price)

        stocksDao.updateCompany(company.copy(price = newPrice))
    }

    private fun PipelineContext<*, ApplicationCall>.getParam(key: String) = context.request.queryParameters[key]

    private suspend fun PipelineContext<*, ApplicationCall>.badRequest(msg: String) {
        call.response.status(HttpStatusCode.BadRequest)
        call.respondText { msg }
    }
}
