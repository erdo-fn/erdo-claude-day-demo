package com.kmpfoo.data

import co.early.fore.core.type.Either
import co.early.fore.core.type.Either.Companion.fail
import co.early.fore.core.type.Either.Companion.success
import co.early.fore.core.type.Either.Fail
import co.early.fore.core.type.Either.Success
import com.kmpfoo.domain.DomainError

fun <Data, Domain> toDomain(
    dataEither: Either<DataError, Data>,
    toDomainBlock: (Data) -> Domain?,
): Either<DomainError, Domain> {
    return when (dataEither) {
        is Success -> {
            val domain = toDomainBlock(dataEither.value)
            domain?.let { success(it) } ?: fail(DomainError.NoError)
        }
        is Fail -> fail(dataEither.value.resolution)
    }
}
