package eu.vvoleman.gympal.common.data.di

import eu.vvoleman.gympal.common.data.remote.supabase.SupabaseClientFactory
import eu.vvoleman.gympal.common.data.remote.supabase.SupabaseConfig
import eu.vvoleman.gympal.common.data.remote.supabase.provideSupabaseConfig
import org.koin.dsl.module

val commonDataModule = module {
    single<SupabaseConfig> { provideSupabaseConfig() }
    single<SupabaseClientFactory> { SupabaseClientFactory() }
    single { get<SupabaseClientFactory>().create(get()) }
}