package eu.vvoleman.gympal.di

import eu.vvoleman.gympal.common.di.commonModule
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    includes(commonModule)
}