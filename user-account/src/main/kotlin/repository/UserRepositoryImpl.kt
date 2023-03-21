package repository

import model.User
import java.util.concurrent.atomic.AtomicLong

class UserRepositoryImpl : UserRepository {
    private val userIdCounter = AtomicLong(0)
    private val users: MutableMap<Long, User> = mutableMapOf()

    override fun addUser(user: User) {
        val userId = userIdCounter.incrementAndGet()
        users[userId] = user.copy(id = userId)
    }

    override fun getUser(userId: Long): User? = users[userId]

    override fun updateUser(userId: Long, user: User) {
        users[userId] = user
    }

    override fun clear() = users.clear()
}
