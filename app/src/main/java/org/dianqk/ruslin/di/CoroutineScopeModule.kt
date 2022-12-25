package org.dianqk.ruslin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideCoroutineScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope = CoroutineScope(
        SupervisorJob() + defaultDispatcher
    )
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

