package com.pnc.adventureworksmobile.domain.error


// Every kind of failure the app knows how to explain to a customer.

// The data layer turns raw exceptions (OkHttp, Retrofit, kotlinx.serialization, Room)
// into one of these. The presentation layer turns one of these into plain text.
// This file is pure Kotlin so the domain layer stays free of Android and library imports.
 
sealed interface AppError {
    // No internet, the host could not be found, or the connection dropped.
    data object NoConnection : AppError

    // The server took too long to answer (socket, call, or coroutine timeout, HTTP 408 or 504).
    data object Timeout : AppError

    // The server refused the request (HTTP 401 or 403). 
    data object Unauthorized : AppError

    // The item does not exist on the server (HTTP 404), for example a removed product. 
    data object NotFound : AppError

    // Any other HTTP error. code is kept for logs and tests. Never show it to the customer. 
    data class Server(val code: Int) : AppError

    // The server answered, but the body could not be decoded into our DTOs. 
    data class InvalidData(val detail: String? = null) : AppError

    // Reading from or writing to the Room database on the phone failed. 
    data object LocalStorage : AppError

    // Anything we did not plan for. 
    data object Unknown : AppError
}

// Carries an AppError inside a Kotlin Result, since Result.failure needs a Throwable.
// cause keeps the original exception for Logcat. 
class AppException(
    val error: AppError,
    cause: Throwable? = null,
) : Exception(error.toString(), cause)

// Reads the AppError back out of a failed Result. Unknown if the failure was not wrapped. 
fun Throwable.asAppError(): AppError = (this as? AppException)?.error ?: AppError.Unknown
