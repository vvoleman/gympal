package eu.vvoleman.gympal.di

import android.content.Context
import eu.vvoleman.gympal.datastore.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single { createDataStore(get<Context>()) }
    }