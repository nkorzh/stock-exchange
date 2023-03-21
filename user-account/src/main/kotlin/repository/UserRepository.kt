package repository

import model.User

interface UserRepository {
    fun addUser(user: User)

    fun getUser(userId: Long): User?

    fun updateUser(userId: Long, user: User)

    fun clear()
}
