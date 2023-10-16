package io.github.qumn.ktorm.dialet

import org.ktorm.database.Database
import org.ktorm.expression.*
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.ktorm.support.postgresql.PostgreSqlExpressionVisitor
import org.ktorm.support.postgresql.PostgreSqlFormatter

class KtAdmPostgreSqlDialect(
    private val sqlExpressionVisitorInterceptorList: SqlExpressionVisitorInterceptor,
) : PostgreSqlDialect() {
    override fun createExpressionVisitor(interceptor: SqlExpressionVisitorInterceptor): SqlExpressionVisitor {
        return KtAdmPostgreSqlExpressionVisitor::class.newVisitorInstance(interceptor)
    }

    override fun createSqlFormatter(
        database: Database,
        beautifySql: Boolean,
        indentSize: Int,
    ): SqlFormatter {
        return KtAdmPostgreSqlFormatter(database, beautifySql, indentSize, sqlExpressionVisitorInterceptorList)
    }
}

open class KtAdmPostgreSqlFormatter(
    database: Database,
    beautifySql: Boolean,
    indentSize: Int,
    private val sqlExpressionVisitorInterceptorList: SqlExpressionVisitorInterceptor,
) : PostgreSqlFormatter(database, beautifySql, indentSize), KtAdmPostgreSqlExpressionVisitor {
    override fun visit(expr: SqlExpression): SqlExpression {
        var nexpr = expr
        if (expr is SelectExpression || expr is UpdateExpression || expr is DeleteExpression || expr is InsertExpression) {
            nexpr = database.dialect.createExpressionVisitor(sqlExpressionVisitorInterceptorList).visit(expr)
        }
        val result = super<KtAdmPostgreSqlExpressionVisitor>.visit(nexpr)
        check(result === nexpr) { "SqlFormatter cannot modify the expression tree." }
        return result
    }
}

interface KtAdmPostgreSqlExpressionVisitor : PostgreSqlExpressionVisitor