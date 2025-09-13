package eu.vvoleman.gympal.common.di

import eu.vvoleman.gympal.common.data.di.commonDataModule
import eu.vvoleman.gympal.common.domain.di.commonDomainModule
import org.koin.dsl.module

val commonModule = module {
    includes(listOf(
        commonDomainModule,
        commonDataModule,
    ))
}