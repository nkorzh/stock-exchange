import repository.StockRepositoryImpl
import service.PriceGeneratorService
import service.StockExchangeService

fun main(args: Array<String>) {
    val stocksDao = StockRepositoryImpl()

    StockExchangeService(stocksDao, PriceGeneratorService(stocksDao))
        .launch(args[0].toInt())
}
