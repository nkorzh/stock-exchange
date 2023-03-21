package service

import java.util.Random

class PriceGeneratorService {
    private val random = Random()

    fun generateNewPrice(price: Double): Double {
        val sign = 2 * (random.nextInt() % 2) - 1
        val absDeviation = (random.nextInt() % 100) / 100.0
        val deltaPrice = sign * price * absDeviation

        return price + deltaPrice
    }
}
