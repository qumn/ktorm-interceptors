package io.github.qumn

import BaseTest
import org.junit.jupiter.api.Test
import org.ktorm.dsl.*

class Test : BaseTest() {
    @Test
    fun dslSQl() {
        val sql = database.from(Departments)
            .leftJoin(Employees, on = Employees.departmentId eq Departments.id)
            .leftJoin(Positions, on = Positions.id eq Employees.positionId)
            .select()
            .sql
        println(sql)
    }


    @Test
    fun sequenceAPI() {
        println(database.employees.sql)

    }
}