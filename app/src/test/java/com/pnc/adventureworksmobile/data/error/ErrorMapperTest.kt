package com.pnc.adventureworksmobile.data.error

import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException
import com.pnc.adventureworksmobile.domain.error.asAppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ErrorMapperTest {

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    // toAppError

    @Test
    fun `no host and dropped connection map to NoConnection`() {
        assertEquals(AppError.NoConnection, UnknownHostException("api").toAppError())
        assertEquals(AppError.NoConnection, IOException("reset").toAppError())
    }

    @Test
    fun `socket timeout maps to Timeout, not NoConnection`() {
        assertEquals(AppError.Timeout, SocketTimeoutException().toAppError())
    }

    @Test
    fun `HTTP codes map to the right error`() {
        assertEquals(AppError.Unauthorized, httpError(401).toAppError())
        assertEquals(AppError.Unauthorized, httpError(403).toAppError())
        assertEquals(AppError.NotFound, httpError(404).toAppError())
        assertEquals(AppError.Timeout, httpError(504).toAppError())
        assertEquals(AppError.Server(500), httpError(500).toAppError())
    }

    @Test
    fun `bad JSON maps to InvalidData`() {
        assertTrue(SerializationException("missing field").toAppError() is AppError.InvalidData)
    }

    @Test
    fun `an unplanned exception maps to Unknown`() {
        assertEquals(AppError.Unknown, IllegalStateException().toAppError())
    }

    // safeCall

    @Test
    fun `safeCall returns the value on success`() = runTest {
        assertEquals(5, safeCall { 5 }.getOrNull())
    }

    @Test
    fun `safeCall wraps a failure in AppException`() = runTest {
        val result = safeCall<Int> { throw IOException("offline") }

        val error = result.exceptionOrNull()
        assertTrue(error is AppException)
        assertEquals(AppError.NoConnection, error!!.asAppError())
        assertTrue(error.cause is IOException)
    }

    @Test
    fun `safeCall turns withTimeout into Timeout`() = runTest {
        val result = safeCall { withTimeout(10) { delay(1_000) } }

        assertEquals(AppError.Timeout, result.exceptionOrNull()!!.asAppError())
    }

    @Test
    fun `safeCall rethrows a real cancel instead of returning a failure`() = runTest {
        var rethrown = false
        try {
            safeCall<Int> { throw CancellationException("screen closed") }
        } catch (e: CancellationException) {
            rethrown = true
        }
        assertTrue(rethrown)
    }

    // asAppError

    @Test
    fun `an unwrapped exception reads as Unknown`() {
        assertEquals(AppError.Unknown, IOException().asAppError())
    }
}
