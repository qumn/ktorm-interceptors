package io.github.qumn.interceptor

import org.ktorm.expression.SqlExpression
import org.ktorm.expression.SqlExpressionVisitor
import org.ktorm.expression.SqlExpressionVisitorInterceptor

class CompositorVisitorInterceptor : SqlExpressionVisitorInterceptor {
    private val interceptors = mutableListOf<SqlExpressionVisitorInterceptor>()

    override fun intercept(expr: SqlExpression, visitor: SqlExpressionVisitor): SqlExpression? {
        var nexpr = expr
        for (interceptor in interceptors) {
            nexpr = interceptor.intercept(nexpr, visitor) ?: nexpr
        }
        return if (nexpr == expr) null else nexpr
    }

    public fun register(visitorInterceptor: SqlExpressionVisitorInterceptor): CompositorVisitorInterceptor {
        interceptors += visitorInterceptor
        return this
    }

    public fun remove(visitorInterceptor: SqlExpressionVisitorInterceptor) {
        interceptors.remove(visitorInterceptor)
    }
}