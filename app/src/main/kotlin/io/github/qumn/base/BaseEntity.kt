package io.github.qumn.ktorm.base

import io.github.qumn.ktorm.interceptor.ColumnGenerateFillValue
import org.ktorm.dsl.Query
import org.ktorm.entity.Entity
import org.ktorm.entity.EntitySequence
import org.ktorm.expression.*
import org.ktorm.schema.*
import java.time.Instant
import kotlin.reflect.KClass


const val UPDATE_FILL_COLUMNS: String = "UPDATE_FILL_COLUMNS"
const val INSERT_FILL_COLUMNS: String = "INSERT_FILL_COLUMNS"
const val LOGICAL_DELETED_COLUMN: String = "LOGICAL_DELETED_COLUMN"
const val ENTITY_CLASS_PROPERTY_KEY: String = "ENTITY_CLASS_PROPERTY_KEY"

interface BaseEntity<E : BaseEntity<E>> : Entity<E> {
    var uid: Long
    var deptId: Long
    var deleted: Boolean
    var createdAt: Instant
    var updatedAt: Instant
}

abstract class BaseTable<E : BaseEntity<E>>(
    tableName: String,
    alias: String? = null,
    catalog: String? = null,
    schema: String? = null,
    entityClass: KClass<E>? = null,
    userIdColumn: String = "uid",
    deptIdColumn: String = "dept_id",
) : Table<E>(
    tableName,
    alias,
    catalog,
    schema,
    entityClass
) {
    private val _extraProperties = mutableMapOf<String, Any>().also {
        if (this.entityClass != null) it[ENTITY_CLASS_PROPERTY_KEY] = this.entityClass as Any
    }

    // TODO: impl data permission control
    val uid = long(userIdColumn).bindTo { it.uid }
    val deptId = long(deptIdColumn).bindTo { it.deptId }

    val deleted = boolean("deleted").logicalDeleted().bindTo { it.deleted }
    val createdAt = timestamp("created_at").fillAtInsert { Instant.now() }.bindTo { it.createdAt }
    val updatedAt =
        timestamp("updated_at").fillAtUpdate { Instant.now() }.fillAtInsert { Instant.now() }.bindTo { it.updatedAt }

    public fun Column<Boolean>.logicalDeleted(): Column<Boolean> {
        // save the information of logical deleted column into extraProperties
        // for using in logicalVisitorInterceptor
        _extraProperties[LOGICAL_DELETED_COLUMN] = this.name
        return this
    }

    // TODO: make the generateFillValue: () -> T to generateFillValue: (entity: E) -> T
    public fun <T : Any> Column<T>.fillAtUpdate(generateFun: () -> T): Column<T> {
        // the auto fill columns information will be used in autofillAtUpdateVisitorInterceptor
        val updateFills = _extraProperties[UPDATE_FILL_COLUMNS] as Array<ColumnGenerateFillValue<T>>? ?: arrayOf()
        _extraProperties[UPDATE_FILL_COLUMNS] =
            updateFills + ColumnGenerateFillValue(
                columnName = this.name,
                generateFun = generateFun,
                sqlType = this.sqlType
            )
        return this
    }

    public fun <T : Any> Column<T>.fillAtInsert(generateFun: () -> T): Column<T> {
        // the auto fill columns information will be used in autofillAtUpdateVisitorInterceptor
        val insertFills = _extraProperties[INSERT_FILL_COLUMNS] as Array<ColumnGenerateFillValue<T>>? ?: arrayOf()
        _extraProperties[INSERT_FILL_COLUMNS] = insertFills + ColumnGenerateFillValue(
            columnName = this.name,
            generateFun = generateFun,
            sqlType = this.sqlType
        )
        return this
    }

    override fun asExpression(): TableExpression {
        return TableExpression(tableName, alias, catalog, schema, extraProperties = _extraProperties.toMap())
    }
}

// template disable the logical delete for sequence api
fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.disableLogicalDeleted(): EntitySequence<E, T> {
    return this.withExpression(
        expression = this.expression.copy(
            from = disableLogicalDeletedHelper(
                this.expression.from,
                this.sourceTable.tableName
            )
        )
    )
}


// template disable the logical delete for dsl api
fun Query.disableLogicalDeleted(): Query {
    return this.withExpression(
        when (expression) {
            is SelectExpression -> {
                val selectExpression = expression as SelectExpression
                selectExpression.copy(from = disableLogicalDeletedHelper(selectExpression.from, null))
            }

            is UnionExpression -> throw IllegalStateException("logical delete is not supported in a union expression.")
        }
    )
}

/**
 * recursive the LOGICAL_DELETED_COLUMN key in extraProperties of tableExpression
 * @param tableName if the table name is not null remove properties of the tableExpression naming the `tableName`
 *                  otherwise remove the properties of all tableExpression
 */
private fun disableLogicalDeletedHelper(expression: QuerySourceExpression, tableName: String?): QuerySourceExpression {
    return when (expression) {
        is TableExpression -> {
            if (tableName == null || tableName == expression.name) {
                expression.copy(
                    extraProperties =
                    expression.extraProperties.filter { it.key != LOGICAL_DELETED_COLUMN }
                )
            } else {
                expression
            }
        }

        is JoinExpression -> {
            expression.copy(
                left = disableLogicalDeletedHelper(expression.left, tableName),
                right = disableLogicalDeletedHelper(expression.right, tableName)
            )
        }

        else -> expression
    }
}
