package io.github.qumn.interceptor

import io.github.qumn.base.LOGICAL_DELETED_COLUMN
import org.ktorm.dsl.and
import org.ktorm.expression.*
import org.ktorm.schema.BooleanSqlType

class LogicalVisitorInterceptor : SqlExpressionVisitorInterceptor {

    // will execute only once, because the function always return non-null
    override fun intercept(expr: SqlExpression, visitor: SqlExpressionVisitor): SqlExpression {
        return interceptHelper(expr).first
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : SqlExpression> interceptHelper(
        expr: T,
    ): Pair<T, ScalarExpression<Boolean>?> {
        return when (expr) {
            is SelectExpression -> {
                val (from, condition) = interceptHelper(expr.from)
                Pair(
                    expr.copy(
                        from = from,
                        where = expr.where.and(condition)
                    ) as T,
                    null
                )
            }

            is TableExpression -> {
                val conditions = buildLogicalDeleteExpression(expr)
                Pair(expr, conditions)
            }

            is JoinExpression -> {
                val (left, leftCondition) = interceptHelper(expr.left)
                val (right, rightCondition) = interceptHelper(expr.right)

                // if the `expr` parent is joinExpression, the superCondition will be used as join condition or return to grandfather based on the join type
                // if the `expr` parent is selectExpression, the superCondition will be used as where condition
                val (joinCondition, superCondition) = when (expr.type) {
                    JoinType.LEFT_JOIN -> Pair(rightCondition, leftCondition)
                    JoinType.RIGHT_JOIN -> Pair(leftCondition, rightCondition)
                    JoinType.INNER_JOIN -> Pair(leftCondition.and(rightCondition), null)
                    JoinType.FULL_JOIN -> Pair(null, null) // not support full join logical delete
                    JoinType.CROSS_JOIN -> Pair(null, leftCondition.and(rightCondition))
                }

                Pair(
                    expr.copy(left = left, right = right, condition = expr.condition.and(joinCondition)) as T,
                    superCondition
                )
            }

            else -> Pair(expr, null)
        }

    }

    private fun buildLogicalDeleteExpression(
        expr: TableExpression,
    ): BinaryExpression<Boolean>? {
        val logicalColumnName = expr.extraProperties[LOGICAL_DELETED_COLUMN] as String? ?: return null
        return BinaryExpression(
            type = BinaryExpressionType.EQUAL,
            left = ColumnExpression(table = expr, name = logicalColumnName, sqlType = BooleanSqlType),
            right = ArgumentExpression(value = false, BooleanSqlType),
            sqlType = BooleanSqlType
        )
    }

    private fun ScalarExpression<Boolean>?.and(condition: ScalarExpression<Boolean>?): ScalarExpression<Boolean>? {
        if (this == null) {
            return condition
        }
        if (condition == null) {
            return this
        }
        return this and condition
    }

}