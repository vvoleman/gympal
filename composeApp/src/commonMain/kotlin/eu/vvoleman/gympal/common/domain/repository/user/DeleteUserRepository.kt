package eu.vvoleman.gympal.common.domain.repository.user

interface DeleteUserRepository{
    suspend fun deleteUser(userId: String): Result<Unit>
}