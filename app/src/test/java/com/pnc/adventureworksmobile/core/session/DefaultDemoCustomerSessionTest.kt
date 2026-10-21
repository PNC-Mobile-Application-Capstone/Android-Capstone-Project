package com.pnc.adventureworksmobile.core.session

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDemoCustomerSessionTest {
    @Test
    fun assignedCustomerIsCentralizedInSession() {
        val customer = DefaultDemoCustomerSession().activeCustomer

        assertEquals(11_711, customer.id)
        assertEquals("Daniel Davis", customer.displayName)
    }
}
