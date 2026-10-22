package com.pnc.adventureworksmobile.core.session

import javax.inject.Inject
import javax.inject.Singleton

data class DemoCustomer(
    val id: Int,
    val displayName: String,
)

interface DemoCustomerSession {
    val activeCustomer: DemoCustomer
}

@Singleton
class DefaultDemoCustomerSession @Inject constructor() : DemoCustomerSession {
    override val activeCustomer = DemoCustomer(
        id = 11_711,
        displayName = "Daniel Davis",
    )
}
