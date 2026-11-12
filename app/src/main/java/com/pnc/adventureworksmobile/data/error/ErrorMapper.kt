package com.pnc.adventureworksmobile.data.error

import android.database.SQLException
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException
import java.io.IOException
import java.io.InterruptedIOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException


 // Turns a raw exception into an AppError.
 //
 // Order matters:
 // - InterruptedIOException is checked before IOException. It covers
 //   `SocketTimeoutException` and OkHttp's call timeout, which are both IOExceptions.
 // - TimeoutCancellationException (from `withTimeout`) is a timeout, not a cancel.
 
fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is TimeoutCancellationException -> AppError.Timeout
    is InterruptedIOException -> AppError.Timeout
    is IOException -> AppError.NoConnection
    is HttpException -> httpCodeToAppError(code())
    is SerializationException -> AppError.InvalidData(message)
    is SQLException -> AppError.LocalStorage
    else -> AppError.Unknown
}

// Maps an HTTP status code to an AppError. Only called for non-2xx responses.
fun httpCodeToAppError(code: Int): AppError = when (code) {
    401, 403 -> AppError.Unauthorized
    404 -> AppError.NotFound
    408, 504 -> AppError.Timeout
    else -> AppError.Server(code)
}


 // Use in repositories in place of `runCatching`.
 //
 // - Success comes back as Result.success.
 // - A failure comes back as Result.failure holding an AppException.
 // - A real CancellationException is thrown again so `viewModelScope` can cancel cleanly.
 //   `runCatching` swallows it, which can leave a screen stuck.
 
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: TimeoutCancellationException) {
    Result.failure(AppException(AppError.Timeout, e))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(AppException(e.toAppError(), e))
}
