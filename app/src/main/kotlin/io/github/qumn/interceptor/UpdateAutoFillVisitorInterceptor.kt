package io.github.qumn.ktorm.interceptor

import io.github.qumn.ktorm.base.UPDATE_FILL_COLUMNS
import org.ktorm.expression.*

class UpdateAutoFillVisitorInterceptor : SqlExpressionVisitorInterceptor {
    override fun intercept(expr: SqlExpression, visitor: SqlExpressionVisitor): SqlExpression? {
        if (expr !is InsertExpression) {
            return null
        }
        val autoFillColumns =
            expr.table.extraProperties.get(UPDATE_FILL_COLUMNS) as Array<ColumnGenerateFillValue<Any>>? ?: return null

        val assignments = addAutoFillAssignments(expr.assignments, autoFillColumns, expr.table)

        return if (expr.assignments == assignments)
            null
        else {
            expr.copy(assignments = assignments)
        }
    }
}

fun addAutoFillAssignments(
    assignments: List<ColumnAssignmentExpression<*>>,
    autoFillColumns: Array<ColumnGenerateFillValue<Any>>,
    table: TableExpression,
): List<ColumnAssignmentExpression<*>> {
    var assignments = assignments
    for (autoFillColumn in autoFillColumns) {
        if (assignments.find { it.column.name == autoFillColumn.columnName } != null) continue
        assignments = assignments + ColumnAssignmentExpression(
            column = ColumnExpression(
                table = table,
                name = autoFillColumn.columnName,
                sqlType = autoFillColumn.sqlType
            ),
            expression = ArgumentExpression(value = autoFillColumn.generateFun(), sqlType = autoFillColumn.sqlType)
        )
    }
    return assignments
}