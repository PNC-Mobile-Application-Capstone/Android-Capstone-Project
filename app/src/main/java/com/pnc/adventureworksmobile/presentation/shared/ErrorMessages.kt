package com.pnc.adventureworksmobile.presentation.shared

import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.asAppError

enum class ErrorSubject(val action: String) {
    Catalog("load the catalog"),
    ProductDetails("load this product"),
    Orders("load your orders"),
    OrderDetails("load this order"),
    SavedProducts("load your saved products"),
    RecentlyViewed("load your recently viewed products"),
    UpdateSaved("update your saved products"),
    ClearRecentlyViewed("clear your recently viewed products"),
}

fun AppError.toFriendlyMessage(subject: ErrorSubject): String =
    "We couldn't ${subject.action}. ${reason()}"

fun Throwable.toFriendlyMessage(subject: ErrorSubject): String =
    asAppError().toFriendlyMessage(subject)

private fun AppError.reason(): String = when (this) {
    AppError.NoConnection -> "Check your internet connection and try again."
    AppError.Timeout -> "The server is taking too long to answer. Please try again."
    AppError.Unauthorized -> "The app doesn't have access to this right now. Please try again later."
    AppError.NotFound -> "It may no longer be available."
    is AppError.Server -> "Our server had a problem. Please try again in a few minutes."
    is AppError.InvalidData -> "We got information we couldn't read. Please try again later."
    AppError.LocalStorage -> "Something went wrong with the data saved on this phone. Please try again."
    AppError.Unknown -> "Something went wrong. Please try again."
}
