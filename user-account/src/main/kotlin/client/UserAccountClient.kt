package client

class UserAccountClient(baseUrl: String) : AbstractHttpClient(baseUrl) {
    fun registerUser(name: String) {
        get(
            "/user/register",
            mutableMapOf("name" to name)
        )
    }

    fun deposit(userId: Long, amount: Double) {
        get(
            "/user/deposit",
            mutableMapOf(
                "id" to userId.toString(),
                "amount" to amount.toString()
            )
        )
    }

    fun withdraw(userId: Long, amount: Double) {
        get(
            "/user/withdraw",
            mutableMapOf(
                "id" to userId.toString(),
                "amount" to amount.toString()
            )
        )
    }

    fun getStocks(userId: Long): String {
        return get(
            "/user/stocks",
            mutableMapOf("id" to userId.toString())
        )
    }

    fun getTotal(userId: Long): Double {
        return get(
            "/user/total",
            mutableMapOf("id" to userId.toString())
        ).toDouble()
    }

    fun getBalance(userId: Long): Double {
        return get(
            "/user/balance",
            mutableMapOf("id" to userId.toString())
        ).toDouble()
    }

    fun buy(userId: Long, company: String, count: Int): String {
        return get(
            "/user/buy", mutableMapOf(
                "id" to userId.toString(),
                "company" to company,
                "count" to count.toString()
            )
        )
    }

    fun sell(userId: Long, company: String, count: Int): String {
        return get(
            "/user/sell", mutableMapOf(
                "id" to userId.toString(),
                "company" to company,
                "count" to count.toString()
            )
        )
    }
}
