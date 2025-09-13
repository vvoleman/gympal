package eu.vvoleman.gympal.common.data.remote.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

class SupabaseClientFactory {

    fun create(config: SupabaseConfig): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }

}