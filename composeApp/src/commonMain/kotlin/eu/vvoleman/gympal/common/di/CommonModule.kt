package eu.vvoleman.gympal.common.di

import eu.vvoleman.gympal.common.data.di.commonDataModule
import eu.vvoleman.gympal.common.domain.di.commonDomainModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val commonModule = module {
    single<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
    includes(listOf(
        commonDomainModule,
        commonDataModule,
    ))
}