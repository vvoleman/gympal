package eu.vvoleman.gympal.common.domain.repository.user

import eu.vvoleman.gympal.common.domain.entity.UserModel
import kotlinx.coroutines.flow.Flow

interface GetAllUsersRepository {

    suspend fun getAllUsers(): List<UserModel>

    fun getAllUsersFlow(): Flow<List<UserModel>>

}