package com.yandex.demeter.annotations

/**
 * Marks declarations that are **internal** in Demeter API, which means that should not be used outside of
 * Demeter.
 */
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal Demeter API that should not be used from outside of Demeter."
)
annotation class InternalDemeterApi
