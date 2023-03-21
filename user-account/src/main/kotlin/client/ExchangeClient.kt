package client

class ExchangeClient(baseUrl: String) : AbstractHttpClient(baseUrl) {

    fun addCompany(companyName: String, count: Int, price: Double) {
        get(
            "/company/add",
            mutableMapOf(
                "company" to companyName,
                "price" to price.toString(),
                "count" to count.toString()
            )
        )
    }

    fun getPrice(companyName: String): Double {
        return get(
            "/stock/price",
            mutableMapOf("company" to companyName)
        ).toDouble()
    }

    fun buy(companyName: String, count: Int): String {
        return get(
            "/stock/buy",
            mutableMapOf(
                "company" to companyName,
                "count" to count.toString()
            )
        )
    }

    fun sell(companyName: String, count: Int): String {
        return get(
            "/stock/sell",
            mutableMapOf(
                "company" to companyName,
                "count" to count.toString()
            )
        )
    }

    fun clear() {
        get("/stock/clear", mutableMapOf())
    }
}
