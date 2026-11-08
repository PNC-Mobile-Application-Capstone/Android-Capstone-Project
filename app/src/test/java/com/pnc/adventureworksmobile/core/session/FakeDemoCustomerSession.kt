package com.pnc.adventureworksmobile.core.session

class FakeDemoCustomerSession(
    override val activeCustomer: DemoCustomer = DemoCustomer(id = 42, displayName = "Test Customer"),
) : DemoCustomerSession