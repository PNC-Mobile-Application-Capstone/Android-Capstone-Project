package com.pnc.adventureworksmobile.presentation.shared

import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

class ErrorMessagesTest {

    private val allErrors = listOf(
        AppError.NoConnection,
        AppError.Timeout,
        AppError.Unauthorized,
        AppError.NotFound,
        AppError.Server(500),
        AppError.InvalidData("Field 'id' is required"),
        AppError.LocalStorage,
        AppError.Unknown,
    )

    @Test
    fun `message starts with what the customer was doing`() {
        assertEquals(
            "We couldn't load your orders. Check your internet connection and try again.",
            AppError.NoConnection.toFriendlyMessage(ErrorSubject.Orders),
        )
    }

    @Test
    fun `every error has its own reason`() {
        val messages = allErrors.map { it.toFriendlyMessage(ErrorSubject.Catalog) }

        assertEquals(messages.size, messages.toSet().size)
    }

    @Test
    fun `messages never leak codes or raw details`() {
        allErrors.forEach { error ->
            val message = error.toFriendlyMessage(ErrorSubject.ProductDetails)
            assertFalse(message, message.contains("500"))
            assertFalse(message, message.contains("Field 'id'"))
            assertFalse(message, message.contains("Exception"))
        }
    }

    @Test
    fun `a failed Result reads its AppError`() {
        val throwable = AppException(AppError.NotFound)

        assertEquals(
            "We couldn't load this product. It may no longer be available.",
            throwable.toFriendlyMessage(ErrorSubject.ProductDetails),
        )
    }

    @Test
    fun `an unwrapped exception gets the generic message`() {
        assertEquals(
            "We couldn't load this order. Something went wrong. Please try again.",
            IllegalStateException("boom").toFriendlyMessage(ErrorSubject.OrderDetails),
        )
    }
}
