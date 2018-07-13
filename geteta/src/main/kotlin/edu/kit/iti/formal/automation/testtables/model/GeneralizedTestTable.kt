package edu.kit.iti.formal.automation.testtables.model

import edu.kit.iti.formal.automation.datatypes.AnyDt
import edu.kit.iti.formal.automation.st.ArrayLookupList
import edu.kit.iti.formal.automation.st.Identifiable
import edu.kit.iti.formal.automation.st.LookupList
import edu.kit.iti.formal.automation.st.ast.FunctionDeclaration
import edu.kit.iti.formal.automation.testtables.GetetaFacade
import edu.kit.iti.formal.automation.testtables.grammar.TestTableLanguageParser
import edu.kit.iti.formal.automation.testtables.model.options.PropertyInitializer
import edu.kit.iti.formal.automation.testtables.model.options.TableOptions
import edu.kit.iti.formal.smv.SMVTypes
import edu.kit.iti.formal.smv.ast.SMVExpr
import edu.kit.iti.formal.smv.ast.SVariable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


enum class IoVariableType {
    INPUT, OUTPUT, STATE_INPUT, STATE_OUTPUT
}

sealed class Variable : Identifiable {
    abstract override var name: String
    abstract var dataType: AnyDt
}

data class IoVariable(
        override var name: String,
        override var dataType: AnyDt,
        var io: IoVariableType) : Variable() {
    var realName: String = name

    val isInput
        get() = io == IoVariableType.INPUT || io == IoVariableType.STATE_INPUT

    val isOutput
        get() = !isInput
}

data class ConstraintVariable(
        override var name: String,
        override var dataType: AnyDt,
        var constraint: TestTableLanguageParser.CellContext? = null)
    : Variable()

class ParseContext(val vars: MutableMap<Variable, SVariable> = hashMapOf(),
                   val refs: MutableMap<SVariable, Int> = hashMapOf(),
                   val fillers: MutableMap<IoVariable, TestTableLanguageParser.CellContext> = hashMapOf()) {

    public fun isVariable(v: Variable) = v in vars
    public fun getSMVVariable(v: Variable) =
            vars.computeIfAbsent(v) { GetetaFacade.asSMVVariable(it) }

    fun getReference(columnVariable: SVariable, i: Int): SMVExpr {
        if (i == 0) {
            return columnVariable
        } else {
            val ref = SReference(i, columnVariable)
            val max = Math.max(refs.getOrDefault(columnVariable, i), i)
            refs[columnVariable] = max
            return ref.asVariable()
        }
    }

    operator fun contains(varText: String) = vars.values.any { it.name == varText }
    fun getSMVVariable(v: String?): SVariable? {
        if (v == null) return null
        val va = vars.keys.find { it.name == v } ?: return null
        return getSMVVariable(va)
    }
}

class GeneralizedTestTable(
        var name: String = "anonym",
        val ioVariables: MutableList<IoVariable> = ArrayList<IoVariable>(),
        val constraintVariables: MutableList<ConstraintVariable> = ArrayList<ConstraintVariable>(),
        var properties: Properties = Properties(),
        var region: Region = Region(0),
        val functions: LookupList<FunctionDeclaration> = ArrayLookupList()
) {
    val options: TableOptions by lazy {
        val o = TableOptions()
        PropertyInitializer.initialize(o, properties)
        o
    }

    fun clearReachability() {
        for (s in this.region.flat()) {
            s.outgoing.clear()
            s.incoming.clear()
            s.automataStates.clear()

            for (a in s.automataStates) {
                a.outgoing.clear()
                a.incoming.clear()
            }
        }
    }

    /*fun getSMVVariable(text: IoVariable): SVariable = getSMVVariable(text.name)
    override fun getSMVVariable(text: String): SVariable {
        return variableMap.computeIfAbsent(text) { k ->
            IOFacade.asSMVVariable(getVariable(k))
        }
    }*/

    /*fun isVariable(text: String) = text in ioVariables || text in constraintVariables
    private fun getVariable(text: String): Variable {
        val a = ioVariables[text]
        val b = constraintVariables[text]

        if (a != null && b != null)
            throw IllegalStateException(
                    "constraint and io variable have the same name.")

        return a ?: b ?: error("Could not found a variable with $text in signature.")
    }*/

    fun add(v: IoVariable) {
        ioVariables += v
    }

    fun add(v: ConstraintVariable) {
        constraintVariables += v
    }

    fun addOption(key: String, value: String) {
        properties[key] = value
    }

    fun getIoVariables(i: Int): IoVariable = ioVariables[i]

    val DEFAULT_CELL_CONTENT = "-"

    fun generateSmvExpression(): ParseContext {
        val vc = generateParseContext()
        region.flat().forEach {
            it.generateSmvExpression(vc)
        }
        return vc
    }

    fun generateParseContext(): ParseContext {
        val vc = ParseContext()
        constraintVariables.forEach {
            vc.getSMVVariable(it)
        }
        ioVariables.forEach {
            vc.getSMVVariable(it)
            vc.fillers[it] = GetetaFacade.parseCell(DEFAULT_CELL_CONTENT)
        }
        return vc
    }
}

operator fun <T : Identifiable> Iterable<T>.get(text: String) = find { it.name == text }
operator fun <T : Identifiable> Iterable<T>.contains(text: String) = this[text] != null


sealed class Duration {
    object Omega : Duration() {
        override fun contains(cycles: Int): Boolean = true

        override val isUnbounded: Boolean
            get() = true
        override val isOneStep: Boolean
            get() = false
        override val isSkippable: Boolean
            get() = false
        override val isRepeatable: Boolean
            get() = true
    }

    data class OpenInterval(val lower: Int, var pflag: Boolean) : Duration() {
        override fun contains(cycles: Int): Boolean = lower <= cycles

        override val isUnbounded: Boolean
            get() = true
        override val isOneStep: Boolean
            get() = false
        override val isSkippable: Boolean
            get() = lower == 0
        override val isRepeatable: Boolean
            get() = true
    }

    data class ClosedInterval(val lower: Int, val upper: Int, val pflag: Boolean = false) : Duration() {
        override fun contains(cycles: Int): Boolean = cycles in lower..upper

        override val isUnbounded: Boolean
            get() = false
        override val isOneStep: Boolean
            get() = lower == 1 && upper == 1
        override val isSkippable: Boolean
            get() = lower == 0
        override val isRepeatable: Boolean
            get() = true

    }

    /**
     * returns true, iff the step can be applied arbitrary often (no upper bound)
     * @return
     */
    abstract val isUnbounded: Boolean

    /**
     * returns true, iff the step is applied only once
     * @return
     */
    abstract val isOneStep: Boolean

    /**
     * returns true, if the step can be overjumped directly
     *
     * @return
     */
    abstract val isSkippable: Boolean

    /**
     *
     * @return true iff this row can be applied more than once.
     */
    abstract val isRepeatable: Boolean

    /**
     * checks if the given integer lies within the duration
     *
     * @param cycles
     * @return
     */
    abstract operator fun contains(cycles: Int): Boolean
}

sealed class TableNode(open val id: String, var duration: Duration = Duration.ClosedInterval(1, 1, false)) {
    abstract val automataStates: List<State.AutomatonState>

    abstract fun count(): Int
    abstract fun flat(): List<State>
    abstract fun depth(): Int
}

data class Region(override val id: String,
                  var children: MutableList<TableNode> = arrayListOf()) : TableNode(id) {

    override val automataStates: List<State.AutomatonState> by lazy {
        val seq = java.util.ArrayList<State.AutomatonState>(100)
        flat().forEach { state -> seq.addAll(state.automataStates) }
        seq
    }

    constructor(id: Int) : this("" + id)

    override fun count(): Int = this.children.sumBy { it.count() }
    override fun flat(): List<State> = this.children.flatMap { a -> a.flat() }
    override fun depth() = 1 + (this.children.maxBy { it.depth() }?.depth() ?: 0)
}

data class State(override val id: String,
                 val rawFields: MutableMap<IoVariable, TestTableLanguageParser.CellContext?> = linkedMapOf()) : TableNode(id) {

    /** Input constraints as list. */
    val inputExpr: MutableList<SMVExpr> = arrayListOf()

    /** Output constraints as list. */
    val outputExpr: MutableList<SMVExpr> = arrayListOf()

    /** incoming states */
    val incoming: MutableSet<State> = HashSet()

    /** outgoing states */
    val outgoing: MutableSet<State> = HashSet()

    /** variable of input contraint definition */
    val defOutput = SVariable("s" + id + "_out", SMVTypes.BOOLEAN)
    val defForward = SVariable("s" + id + "_fwd", SMVTypes.BOOLEAN)
    val defFailed = SVariable("s" + id + "_fail", SMVTypes.BOOLEAN)
    val defInput = SVariable("s" + id + "_in", SMVTypes.BOOLEAN)

    /**
     * The predicate that allows keeping in this state.
     * Only necessary iff duration is DET_WAIT.
     */
    val defKeep = SVariable("s" + id + "_keep", SMVTypes.BOOLEAN)


    override val automataStates: MutableList<AutomatonState> = ArrayList()
        get() {
            if (field.isEmpty()) {
                val duration = duration
                when (duration) {
                    is Duration.Omega ->
                        field.add(AutomatonState(1))
                    is Duration.OpenInterval ->
                        for (i in 1..duration.lower)
                            field.add(AutomatonState(i))
                    is Duration.ClosedInterval ->
                        for (i in 1..duration.upper) {
                            field.add(AutomatonState(i))
                        }
                }
            }
            assert(field.size != 0)
            return field
        }

    var isInitialReachable: Boolean = false
    var isEndState: Boolean = false

    constructor(id: Int) : this(id.toString())

    /*
    fun add(v: IoVariable, e: SMVExpr) {
        val a = if (v.io == IoVariableType.INPUT || v.io == IoVariableType.STATE_INPUT) inputExpr else outputExpr
        a.add(e)
    }
    */

    override fun count(): Int {
        return 1
    }

    override fun flat(): List<State> {
        val l = LinkedList<State>()
        l.add(this)
        return l
    }

    override fun depth(): Int {
        return 0
    }

    fun generateSmvExpression(vc: ParseContext) {
        inputExpr.clear()
        outputExpr.clear()

        val new = HashMap<IoVariable, TestTableLanguageParser.CellContext>()
        for (k in vc.fillers.keys.toHashSet()) {
            val v = rawFields[k]
            if (v != null) {
                vc.fillers[k] = v
                new[k] = v
            } else {
                val w = vc.fillers[k]
                if (w != null) new[k] = w
            }
        }
        rawFields.putAll(new)

        inputExpr.addAll(GetetaFacade.exprsToSMV(vc, new.filter { it.key.isInput }))
        outputExpr.addAll(GetetaFacade.exprsToSMV(vc, new.filter { it.key.isOutput }))
    }

    inner class AutomatonState(private val position: Int, private val name: String) {
        val incoming: MutableSet<AutomatonState> = HashSet()
        val outgoing: MutableSet<AutomatonState> = HashSet()

        constructor(count: Int) : this(count, "${State@ id}_$id")

        val isOptional: Boolean
            get() {
                val duration = duration
                return when (duration) {
                    is Duration.Omega -> false
                    is Duration.ClosedInterval -> position >= duration.lower
                    is Duration.OpenInterval -> position >= duration.lower
                }
            }

        val isFirst: Boolean
            get() = position == 1

        val state: State
            get() = this@State

        val smvVariable: SVariable = SVariable.create("s_$name").asBool()

        val defForward: SVariable = SVariable.create("s_${name}_fwd").asBool()

        val defFailed: SVariable = SVariable.create("s_${name}_fail").asBool()

        /**
         * Returns true iff this is the automaton state that can infinitely repeated.
         *
         * @return
         */
        val isUnbounded: Boolean
            get() {
                val duration = duration
                return when (duration) {
                    is Duration.Omega -> true
                    is Duration.ClosedInterval -> false
                    is Duration.OpenInterval -> position == duration.lower
                }
            }

        val isStartState: Boolean
            get() = isInitialReachable && isFirst

        val isEndState: Boolean
            get() = if (outgoing.isEmpty()) {
                true //TODO check for omega?
            } else {
                outgoing.stream()
                        .map { s -> s.isEndState || s.isOptional }
                        .reduce { a, b -> a or b }.orElse(false)
            }
    }
}
