package eu.vvoleman.gympal.di

import android.content.Context
import eu.vvoleman.gympal.common.data.factory.sql.DriverFactory
import eu.vvoleman.gympal.datastore.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single { createDataStore(get<Context>()) }
        single<DriverFactory> { DriverFactory(get<Context>()) }
    }