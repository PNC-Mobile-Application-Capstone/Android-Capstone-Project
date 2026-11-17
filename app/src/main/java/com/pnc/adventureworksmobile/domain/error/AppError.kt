package com.pnc.adventureworksmobile.domain.error

sealed interface AppError {

    data object NoConnection : AppError

    data object Timeout : AppError

    data object Unauthorized : AppError

    data object NotFound : AppError

    data class Server(val code: Int) : AppError

    data class InvalidData(val detail: String? = null) : AppError

    data object LocalStorage : AppError

    data object Unknown : AppError
}

class AppException(
    val error: AppError,
    cause: Throwable? = null,
) : Exception(error.toString(), cause)

fun Throwable.asAppError(): AppError = (this as? AppException)?.error ?: AppError.Unknown
