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

fun httpCodeToAppError(code: Int): AppError = when (code) {
    401, 403 -> AppError.Unauthorized
    404 -> AppError.NotFound
    408, 504 -> AppError.Timeout
    else -> AppError.Server(code)
}

suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: TimeoutCancellationException) {
    Result.failure(AppException(AppError.Timeout, e))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(AppException(e.toAppError(), e))
}
