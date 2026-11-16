package com.pnc.adventureworksmobile.di

import junit.framework.TestCase.assertEquals
import org.junit.Test

class OkHttpTimeoutsTest {

    private val client = ApplicationModule.provideOkHttpClient()

    @Test
    fun `connect timeout is 10 seconds`() {
        assertEquals(10_000, client.connectTimeoutMillis)
    }

    @Test
    fun `read and write timeouts are 20 seconds`() {
        assertEquals(20_000, client.readTimeoutMillis)
        assertEquals(20_000, client.writeTimeoutMillis)
    }

    @Test
    fun `the whole call is capped at 30 seconds`() {
        // OkHttp's default is 0, which means no limit.
        assertEquals(30_000, client.callTimeoutMillis)
    }
}
