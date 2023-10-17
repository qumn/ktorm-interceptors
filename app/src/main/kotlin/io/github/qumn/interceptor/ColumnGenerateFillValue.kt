package io.github.qumn.interceptor

import org.ktorm.schema.SqlType

data class ColumnGenerateFillValue<T : Any>(
    val columnName: String,
    val generateFun: () -> T,
    val sqlType: SqlType<T>,
)
