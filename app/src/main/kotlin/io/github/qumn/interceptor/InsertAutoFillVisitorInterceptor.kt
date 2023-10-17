package io.github.qumn.interceptor

import io.github.qumn.base.INSERT_FILL_COLUMNS
import org.ktorm.expression.InsertExpression
import org.ktorm.expression.SqlExpression
import org.ktorm.expression.SqlExpressionVisitor
import org.ktorm.expression.SqlExpressionVisitorInterceptor

class InsertAutoFillVisitorInterceptor : SqlExpressionVisitorInterceptor {
    override fun intercept(expr: SqlExpression, visitor: SqlExpressionVisitor): SqlExpression? {
        if (expr !is InsertExpression) {
            return null
        }
        val autoFillColumns =
            expr.table.extraProperties.get(INSERT_FILL_COLUMNS) as Array<ColumnGenerateFillValue<Any>>? ?: return null

        val assignments = addAutoFillAssignments(expr.assignments, autoFillColumns, expr.table)

        return if (expr.assignments == assignments)
            null
        else {
            expr.copy(assignments = assignments)
        }
    }
}