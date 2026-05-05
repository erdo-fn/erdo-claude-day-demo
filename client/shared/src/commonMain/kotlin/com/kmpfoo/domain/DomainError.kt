package com.kmpfoo.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class DomainError {
    data object NoError : DomainError()
    data class Misc(val message: String) : DomainError()
    data object RetryLater : DomainError()
    data object RetryAfterNetworkCheck : DomainError()
    data object RetryAfterLogin : DomainError()
}
