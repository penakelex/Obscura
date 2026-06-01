package org.penakelex.obscura.domain.exception

import org.penakelex.obscura.domain.validation.ValidationError

class ValidationException(
    val errors: List<ValidationError>
) : Exception("Validation failed: ${errors.size} error(s)") {
    constructor(vararg errors: ValidationError) : this(errors.toList())
}