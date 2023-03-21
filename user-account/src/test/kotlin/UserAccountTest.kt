import client.ExchangeClient
import client.UserAccountClient
import repository.UserRepositoryImpl
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAccountTest {

    private val exchangeContainer = ExchangeContainer(EXCHANGE_PORT)

    private val exchangeClient = ExchangeClient("http://localhost:$EXCHANGE_PORT")
    private val userAccountClient = UserAccountClient("http://localhost:$USER_ACCOUNT_PORT")

    private val userDao = UserRepositoryImpl()
    private val userAccountService = UserAccountController(userDao, exchangeClient)

    @BeforeAll
    fun beforeAll() {
        exchangeContainer.start()
        userAccountService.start(USER_ACCOUNT_PORT)
    }

    @AfterAll
    fun afterAll() {
        exchangeContainer.close()
        userAccountService.close()
    }

    @BeforeEach
    fun beforeEach() {
        exchangeClient.clear()
        userDao.clear()
    }

    @Test
    fun `should not register user with negative balance`() {
        userAccountClient.registerUser("user")
        val response = userAccountClient.deposit(0, -100.0)
        assertEquals("Amount should be positive", response)
    }

    @Test
    fun `should not buy stock for non-existent user`() {
        exchangeClient.addCompany("company", 10, 10.0)
        val response = userAccountClient.buy(0, "company", 1)
        assertEquals("User with id 0 does not exist", response)
    }

    @Test
    fun `should buy stock to user`() {
        exchangeClient.addCompany("company", 10, 10.0)

        userAccountClient.registerUser("user")
        userAccountClient.deposit(0, 100.0)

        val response = userAccountClient.buy(0, "company", 10)
        assertEquals("OK", response)

        val balanceAfter = userAccountClient.getBalance(0)
        val totalAfter = userAccountClient.getTotal(0)

        assertEquals(0, balanceAfter)
        assertEquals(100, totalAfter)
    }

    @Test
    fun `should not buy stock if balance is too low`() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        val response = userAccountClient.buy(0, "company", 1)
        assertEquals("User with id 0 does not have enough balance", response)
    }

    @Test
    fun `should not sell stocks user does not have`() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        val response = userAccountClient.sell(0, "company", 1)
        assertEquals("User with id 0 does not have enough stocks", response)
    }

    @Test
    fun `should buy and sell stocks`() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        userAccountClient.deposit(0, 1000.0)

        userAccountClient.buy(0, "company", 10)
        val response = userAccountClient.sell(0, "company", 10)
        assertEquals("OK", response)
    }

    companion object {
        const val EXCHANGE_PORT = 8000
        const val USER_ACCOUNT_PORT = 8001
    }
}
