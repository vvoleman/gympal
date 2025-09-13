package eu.vvoleman.gympal.common.data.remote.supabase

import eu.vvoleman.gympal.BuildKonfig

data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)

fun provideSupabaseConfig(): SupabaseConfig = SupabaseConfig(
    url = BuildKonfig.GP_SUPABASE_URL,
    anonKey = BuildKonfig.GP_SUPABASE_ANON_KEY,
)
