package eu.vvoleman.gympal.common.di

import eu.vvoleman.gympal.app.AppDestinationMapper
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import eu.vvoleman.gympal.common.domain.service.LoggerService
import org.koin.dsl.module

val commonModule = module {
    single<LoggerInterface> { LoggerService() }
    single<AppDestinationMapper> { AppDestinationMapper() }
}