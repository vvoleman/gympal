package eu.vvoleman.gympal.di

import eu.vvoleman.gympal.common.data.factory.sql.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DriverFactory> { DriverFactory() }
    }