package edu.kit.iti.formal.automation.st.util;

/*-
 * #%L
 * iec61131lang
 * %%
 * Copyright (C) 2016 Alexander Weigl
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.datatypes.Any;
import edu.kit.iti.formal.automation.datatypes.values.ScalarValue;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.st.ast.*;
import edu.kit.iti.formal.automation.visitors.DefaultVisitor;
import edu.kit.iti.formal.automation.visitors.Visitable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>AstCopyVisitor class.</p>
 *
 * @author weigla 26.06.2014
 *         <p>
 *         <b>ATTTENTION</b> this is only useable on statement level and below!
 *         <p>
 *         This visitors defines all function with go down and setting the results of visit as the new value.
 * @version $Id: $Id
 */
public class AstCopyVisitor extends DefaultVisitor<Object> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object defaultVisit(Visitable visitable) {
        System.err.println("AstTransform.defaultVisit");
        System.err.println(("AstCopyVisitor: Copy command not implemented for "
                + visitable.getClass()
                + " from implementation "
                + getClass()));
        return visitable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(AssignmentStatement assignmentStatement) {
        AssignmentStatement statement = new AssignmentStatement(
                (Reference) assignmentStatement.getLocation().visit(this),
                (Expression) assignmentStatement.getExpression().visit(this)
        );
        setPositions(assignmentStatement, statement);
        return statement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(CaseConditions.IntegerCondition integerCondition) {
        return integerCondition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(CaseConditions.Enumeration enumeration) {
        return enumeration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(BinaryExpression binaryExpression) {
        BinaryExpression be = new BinaryExpression(
                (Expression) binaryExpression.getLeftExpr().visit(this),
                (Expression) binaryExpression.getRightExpr().visit(this),
                binaryExpression.getOperator());
        return be;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(UnaryExpression unaryExpression) {
        UnaryExpression ue = new UnaryExpression(unaryExpression.getOperator(),
                (Expression) unaryExpression.getExpression().visit(this));
        return ue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(RepeatStatement repeatStatement) {
        RepeatStatement w = new RepeatStatement();
        w.setCondition((Expression) repeatStatement.getCondition().visit(this));
        w.setStatements((StatementList) repeatStatement.getStatements().visit(this));
        setPositions(repeatStatement, w);
        return w;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(WhileStatement whileStatement) {
        WhileStatement w = new WhileStatement();
        w.setCondition((Expression) whileStatement.getCondition().visit(this));
        w.setStatements((StatementList) whileStatement.getStatements().visit(this));
        setPositions(whileStatement, w);
        return w;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(CaseStatement caseStatement) {
        CaseStatement cs = new CaseStatement();

        for (CaseStatement.Case c : caseStatement.getCases()) {
            cs.addCase((CaseStatement.Case) c.visit(this));
        }

        cs.setExpression((Expression) caseStatement.getExpression().visit(this));
        cs.setElseCase((StatementList) caseStatement.getElseCase().visit(this));

        setPositions(caseStatement, cs);
        return cs;
    }

    /*@Override
    public Object visit(SymbolicReference symbolicReference) {
        SymbolicReference sr = new SymbolicReference(symbolicReference);

        if (symbolicReference.getSubscripts() != null) {
            sr.setSubscripts(new ExpressionList());
            for (Expression e : symbolicReference.getSubscripts()) {
                sr.getSubscripts().add((Expression) e.visit(this));
            }
        }

        if (sr.getSub() != null)
            sr.setSub((SymbolicReference) sr.getSub().visit(this));

        return sr;
    }
*/

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(StatementList statements) {
        StatementList r = new StatementList();
        for (Statement s : statements) {
            //       if(s != null)
            if (s == null) {
                System.out.println("statements = [" + statements + "]");
            }
            Object t = s.visit(this);
            if (t instanceof StatementList) {
                StatementList statementList = (StatementList) t;
                r.addAll(statementList);
            } else {
                r.add((Statement) t);
            }
        }
        return r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(ProgramDeclaration programDeclaration) {
        ProgramDeclaration pd = new ProgramDeclaration(programDeclaration);
        pd.setLocalScope((LocalScope) programDeclaration.getLocalScope().visit(this));
        pd.setProgramBody((StatementList) programDeclaration.getProgramBody().visit(this));
        return pd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(LocalScope localScope) {
        LocalScope vs = new LocalScope();
        //copy global function/block and types declaration
        vs.setGlobalScope(localScope.getGlobalScope());
        for (VariableDeclaration vd : localScope.getLocalVariables().values())
            vs.add((VariableDeclaration) vd.visit(this));
        return vs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(ScalarValue<? extends Any, ?> tsScalarValue) {
        return new ScalarValue<>(tsScalarValue.getDataType(), tsScalarValue.getValue());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(FunctionCall functionCall) {
        FunctionCall fc = new FunctionCall(functionCall);

        fc.getParameters().clear();
        for (FunctionCall.Parameter p : functionCall.getParameters()) {
            fc.getParameters().add(new FunctionCall.Parameter(p.getName(), p.isOutput(), (Expression) p.getExpression().visit(this)));
        }
        return fc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(ForStatement forStatement) {
        ForStatement f = new ForStatement();
        f.setVariable(forStatement.getVariable());
        f.setStart((Expression) forStatement.getStart().visit(this));
        f.setStatements((StatementList) forStatement.getStatements().visit(this));
        if (forStatement.getStep() != null)
            f.setStep((Expression) forStatement.getStep().visit(this));
        f.setStop((Expression) forStatement.getStop().visit(this));
        setPositions(forStatement, f);
        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(IfStatement ifStatement) {
        IfStatement i = new IfStatement();
        for (GuardedStatement gc : ifStatement.getConditionalBranches()) {
            i.addGuardedCommand((GuardedStatement) gc.visit(this));
        }
        i.setElseBranch((StatementList) ifStatement.getElseBranch().visit(this));
        setPositions(ifStatement, i);
        return i;
    }

    private Top setPositions(Top old, Top fresh) {
        fresh.setStartPosition(old.getStartPosition());
        fresh.setEndPosition(old.getEndPosition());
        return fresh;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(CommentStatement commentStatement) {
        return commentStatement;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(GuardedStatement guardedStatement) {
        GuardedStatement gs = new GuardedStatement();
        gs.setCondition((Expression) guardedStatement.getCondition().visit(this));
        gs.setStatements((StatementList) guardedStatement.getStatements().visit(this));
        return setPositions(guardedStatement, gs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(FunctionCallStatement functionCallStatement) {
        FunctionCallStatement fc = new FunctionCallStatement((FunctionCall) functionCallStatement.getFunctionCall().visit(this));
        return fc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(CaseStatement.Case aCase) {
        CaseStatement.Case c = new CaseStatement.Case();

        List<CaseConditions> v = this.<CaseConditions>visitList(aCase.getConditions());
        c.setConditions(v);
        c.setStatements((StatementList) aCase.getStatements().visit(this));

        return setPositions(aCase, c);
    }

    private <T> List<T> visitList(List<? extends Visitable> list) {
        List l = new ArrayList();
        for (Visitable v : list)
            l.add((T) v.visit(this));
        return l;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(VariableDeclaration variableDeclaration) {
        VariableDeclaration vd = new VariableDeclaration(variableDeclaration);
        /*vd.setDataType(variableDeclaration.getDataType());
        vd.setDataTypeName(variableDeclaration.getDataTypeName());
        if (variableDeclaration.getInit() != null)
            vd.setInit((Initialization) variableDeclaration.getInit().visit(this));
        vd.setIdentifier(variableDeclaration.getIdentifier());
        vd.setType(variableDeclaration.getType());*/
        return vd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visit(ReturnStatement returnStatement) {
        return setPositions(returnStatement, new ReturnStatement());
    }
}
