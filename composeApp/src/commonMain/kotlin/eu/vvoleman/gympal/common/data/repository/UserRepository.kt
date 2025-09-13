package eu.vvoleman.gympal.common.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import eu.vvoleman.gympal.common.domain.entity.UserModel
import eu.vvoleman.gympal.common.domain.repository.user.CreateUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.DeleteUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.GetAllUsersRepository
import eu.vvoleman.gympal.database.GymPalDatabase
import eu.vvoleman.gympal.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class UserRepository(
    private val coroutineScope: CoroutineScope,
    private val database: GymPalDatabase,
) : GetAllUsersRepository, CreateUserRepository, DeleteUserRepository {

    override suspend fun getAllUsers(): List<UserModel> {
        return database.userQueries
            .getAll()
            .executeAsList()
            .map {
                mapToUserModel(it)
            }
    }

    override fun getAllUsersFlow(): Flow<List<UserModel>> {
        return database.userQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map {
                    mapToUserModel(it)
                }
            }
    }

    override suspend fun createUser(model: UserModel): Result<Unit> {
       return try {
           val result = database.userQueries.insert(
               id = model.id,
               name = model.name,
               email = model.email,
           )
           println("Inserted user with result: $result")
           Result.success(Unit)
       } catch (e: Exception) {
           println("Error inserting user: ${e.message}")
           Result.failure(e)
       }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            database.userQueries.deleteById(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUserModel(model: User): UserModel {
        // Dummy implementation for example purposes
        return UserModel(
            id = model.id,
            name = model.name,
            email = model.email,
        )
    }
}