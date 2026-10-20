package com.pnc.adventureworksmobile.di

import com.pnc.adventureworksmobile.core.session.DefaultDemoCustomerSession
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindDemoCustomerSession(
        implementation: DefaultDemoCustomerSession,
    ): DemoCustomerSession
}
