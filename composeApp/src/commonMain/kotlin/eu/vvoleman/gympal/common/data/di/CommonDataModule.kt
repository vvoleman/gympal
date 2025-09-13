package eu.vvoleman.gympal.common.data.di

import eu.vvoleman.gympal.common.data.factory.sql.DatabaseFactory
import eu.vvoleman.gympal.common.data.remote.supabase.SupabaseClientFactory
import eu.vvoleman.gympal.common.data.remote.supabase.SupabaseConfig
import eu.vvoleman.gympal.common.data.remote.supabase.provideSupabaseConfig
import eu.vvoleman.gympal.common.data.repository.UserRepository
import eu.vvoleman.gympal.common.domain.repository.user.CreateUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.DeleteUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.GetAllUsersRepository
import eu.vvoleman.gympal.database.GymPalDatabase
import org.koin.dsl.module

val commonDataModule = module {
    single<SupabaseConfig> { provideSupabaseConfig() }
    single<SupabaseClientFactory> { SupabaseClientFactory() }
    single { get<SupabaseClientFactory>().create(get()) }
    single<DatabaseFactory> { DatabaseFactory(get()) }
    single<GymPalDatabase> { DatabaseFactory(get()).createDatabase() }

    // Repository
    single<UserRepository> { UserRepository(get(), get()) }
    single<GetAllUsersRepository> { get<UserRepository>() }
    single<CreateUserRepository> { get<UserRepository>() }
    single<DeleteUserRepository> { get<UserRepository>() }
}