package eu.vvoleman.gympal.common.domain.di

import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import eu.vvoleman.gympal.common.domain.service.LoggerService
import org.koin.dsl.module

val commonDomainModule = module {
    single<LoggerInterface> { LoggerService() }
}