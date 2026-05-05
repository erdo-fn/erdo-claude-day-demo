package com.kmpfoo.data

import com.kmpfoo.domain.DomainError
import kotlinx.serialization.Serializable

@Serializable
sealed class DataError(val resolution: DomainError) {
    object Unknown : DataError(DomainError.RetryLater)
    object UnexpectedFormat : DataError(DomainError.RetryLater)
    object InvalidJWT : DataError(DomainError.RetryLater)
    object Network : DataError(DomainError.RetryAfterNetworkCheck)
    object SecurityUnknown : DataError(DomainError.RetryAfterNetworkCheck)
    object Server : DataError(DomainError.RetryLater)
    object AlreadyExecuted : DataError(DomainError.RetryLater)
    object Client : DataError(DomainError.RetryLater)
    object Cancellation : DataError(DomainError.NoError)
    object RateLimited : DataError(DomainError.RetryLater)
    object SessionTimedOut : DataError(DomainError.RetryAfterLogin)
    object Busy : DataError(DomainError.RetryLater)
}
