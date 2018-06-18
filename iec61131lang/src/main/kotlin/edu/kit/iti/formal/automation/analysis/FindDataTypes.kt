package edu.kit.iti.formal.automation.analysis

import edu.kit.iti.formal.automation.scope.Scope
import edu.kit.iti.formal.automation.st.ast.*
import edu.kit.iti.formal.automation.st.util.AstVisitorWithScope

/**
 * @author Alexander Weigl, Augusto Modanese
 * @version 1 (25.06.17)
 */
class FindDataTypes(val globalScope: Scope) : AstVisitorWithScope<Unit>() {
    override fun defaultVisit(obj: Any) {}

    override fun visit(pd: ProgramDeclaration) {
        goIntoScope(pd)
        scope.registerProgram(pd)
        pd.actions.forEach { a -> pd.scope.registerAction(a) }
        
    }

    override fun visit(fbd: FunctionBlockDeclaration) {
        goIntoScope(fbd)
        scope.registerFunctionBlock(fbd)
        fbd.actions.forEach { scope.registerAction(it)  }
        fbd.methods.forEach { scope.registerMethod(it); }
        goOutoScope()
        
    }

    override fun visit(functionDeclaration: FunctionDeclaration) {
        goIntoScope(functionDeclaration)
        scope.registerFunction(functionDeclaration)
        goOutoScope()
        
    }

    override fun visit(subRangeTypeDeclaration: SubRangeTypeDeclaration) {
        scope.registerType(subRangeTypeDeclaration)
        
    }

    override fun visit(variableDeclaration: VariableDeclaration) {
        //weigl: do not register anonymous datatype, or references to data types.
        /*
        if (variableDeclaration.getTypeDeclaration() instanceof ArrayTypeDeclaration)
            variableDeclaration.getTypeDeclaration().accept(this);
        return super.visit(variableDeclaration);
        */
        
    }

    override fun visit(simpleTypeDeclaration: SimpleTypeDeclaration) {
        scope.registerType(simpleTypeDeclaration)
         //super.visit(simpleTypeDeclaration)
    }

    override fun visit(ptd: PointerTypeDeclaration) {
        scope.registerType(ptd)
        return super.visit(ptd)
    }

    override fun visit(clazz: ClassDeclaration) {
        scope.registerClass(clazz)
        goIntoScope(clazz)
        clazz.methods.forEach { scope.registerMethod(it) }
        goOutoScope()
        return super.visit(clazz)
    }

    override fun visit(interfaceDeclaration: InterfaceDeclaration) {
        //goIntoScope(interfaceDeclaration)
        scope.registerInterface(interfaceDeclaration)
         //super.visit(interfaceDeclaration)
    }

    override fun visit(arrayTypeDeclaration: ArrayTypeDeclaration) {
        scope.registerType(arrayTypeDeclaration)
        
    }

    /**
     * {@inheritDoc}
     */
    override fun visit(enumerationTypeDeclaration: EnumerationTypeDeclaration) {
        scope.registerType(enumerationTypeDeclaration)
        
    }

    /**
     * {@inheritDoc}
     */
    override fun visit(stringTypeDeclaration: StringTypeDeclaration) {
        scope.registerType(stringTypeDeclaration)
        
    }

    override fun visit(typeDeclarations: TypeDeclarations) {
        for (td in typeDeclarations)
            td.accept(this)
        
    }

    override fun visit(structureTypeDeclaration: StructureTypeDeclaration) {
        scope.registerType(structureTypeDeclaration)
        
    }

    override fun visit(gvl: GlobalVariableListDeclaration) {
        gvl.scope = scope // global variables does not open an own scope.
        scope.addVariables(gvl.scope)
        //return super.visit(gvl)
        
    }

    private fun goIntoScope(hasScope: HasScope) {
        hasScope.scope.parent = scope
        scope = hasScope.scope
    }

    private fun goOutoScope() {
        scope = if (scope.parent != null) scope.parent!! else globalScope
    }
}
