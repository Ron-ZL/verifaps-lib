package edu.kit.iti.formal.automation.run

import edu.kit.iti.formal.automation.IEC61131Facade
import edu.kit.iti.formal.automation.datatypes.AnyBit
import edu.kit.iti.formal.automation.datatypes.SINT
import edu.kit.iti.formal.automation.datatypes.USINT
import edu.kit.iti.formal.automation.datatypes.values.VAnyInt
import edu.kit.iti.formal.automation.datatypes.values.VBool
import edu.kit.iti.formal.automation.scope.Scope
import edu.kit.iti.formal.automation.st.ast.AssignmentStatement
import edu.kit.iti.formal.automation.st.util.AstVisitor
import edu.kit.iti.formal.automation.visitors.Visitable
import org.junit.Test
import java.math.BigInteger
import kotlin.test.assertEquals

class ExpressionVisitorTest {
    @Test
    fun basicTest() {
        val ast = IEC61131Facade.file(this.javaClass.getResourceAsStream("expressionVisitorTest.basicTest.st"))
        val expressions = mutableListOf<Visitable>()

        ast.accept(object : AstVisitor<Unit>() {
            override fun defaultVisit(obj: Any) {}
            override fun visit(assignmentStatement: AssignmentStatement) {
                expressions.add(assignmentStatement.expression)
            }
        })

        val eValues: List<EValue> = expressions.map {
            it.accept(ExpressionVisitor(State(), Scope()))
        }

        println(eValues.map { it.toString() }.joinToString("\n"))
        assertEquals(arrayListOf(
                VBool(AnyBit.BOOL, true),
                VAnyInt(SINT, BigInteger.valueOf(-19)),
                VAnyInt(USINT, BigInteger.valueOf(3))
        ).toString(),
                eValues.toString())
    }
}
