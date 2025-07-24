package com.yandex.demeter.internal.utils

import com.yandex.demeter.annotations.InternalDemeterApi
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

@InternalDemeterApi
val <T : Any> KClass<T>.constructorProperties
    get() =
        primaryConstructor?.let { ctor ->
            declaredMemberProperties.filter { prop ->
                ctor.parameters.any { param ->
                    param.name == prop.name &&
                        param.type == prop.returnType
                }
            }
        } ?: emptyList()
