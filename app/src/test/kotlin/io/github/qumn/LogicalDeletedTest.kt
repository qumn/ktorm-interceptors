import org.junit.jupiter.api.Test
import org.ktorm.dsl.from
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.entity.add

class LogicalDeletedTest : BaseTest() {
    @Test
    fun test() {
        val departments = database.from(Departments).leftJoin(Employees).leftJoin(Customers).select()
            .map { Departments.createEntity(it) }
        println(departments)
    }

    @Test
    fun test2() {
        val department = Department()
        department.name = "zs"
        department.location = LocationWrapper("武汉")
        database.departments.add(department)

    }
}