package eu.vvoleman.gympal.common.domain.repository.user

import eu.vvoleman.gympal.common.domain.entity.UserModel

interface CreateUserRepository {

    suspend fun createUser(model: UserModel): Result<Unit>

}