package compiler;

import visitor.VisitorAdapter;
import syntaxtree.*;

// Use makeLabel to generate fresh labels for jump targets.
import static compiler.FreshLabelGenerator.makeLabel;

/**
 * Prototype Mapl compiler.
 */
public class Compiler extends VisitorAdapter<String> {

    // FreshLabelGenerator makeLabels = new FreshLabelGenerator();

    public Compiler() {
    }

    private String seq(String l, String r) {
        return "SEQ(" + l + ", " + r + ")";
    }

    @Override
    public String visit(Program n) {
        String ir = "NOOP";
        for (Stm s : n.pd.ss) {
            ir = seq(ir, s.accept(this));
        }
        return ir;
    }

    /* ======================================================== */
    /* Statement visitors (all return an IR statement string) */
    /* ======================================================== */

    @Override
    public String visit(StmVarDecl n) {
        return "NOOP";
    }

    @Override
    public String visit(StmOutchar n) {
        return "EXP(CALL(NAME _printchar, " + n.e.accept(this) + "))";
    }

    @Override
    public String visit(StmBlock n) {
        String ir = "NOOP";
        for (Stm s : n.ss) {
            ir = seq(ir, s.accept(this));
        }
        return ir;
    }

    @Override
    public String visit(StmOutput n) {
        return "EXP(CALL(NAME _printint, " + n.e.accept(this) + "))";
    }

    @Override
    public String visit(StmAssign n) {
        return "MOVE(TEMP " + n.v.toString() + ", " + n.e.accept(this) + ")";
    }

    @Override
    public String visit(StmIf n) {
        String trueCase = FreshLabelGenerator.makeLabel("trueCase");
        String falseCase = FreshLabelGenerator.makeLabel("falseCase");
        String done = FreshLabelGenerator.makeLabel("done");

        String ifStm = "CJUMP(" + n.e.accept(this) + ", EQ, CONST 1, " + trueCase + "," + falseCase + ")";
        String labelTrue = "LABEL " + trueCase;
        String block1 = n.b1.accept(this);
        String jumpDone = "JUMP(NAME done)";
        String labelFalse = "LABEL " + falseCase;
        String block2 = n.b2.accept(this);
        String labelDone = "LABEL " + done;

        String finishedStm = ifStm;
        finishedStm = seq(finishedStm, labelTrue);
        finishedStm = seq(finishedStm, block1);
        finishedStm = seq(finishedStm, jumpDone);
        finishedStm = seq(finishedStm, labelFalse);
        finishedStm = seq(finishedStm, block2);
        finishedStm = seq(finishedStm, labelDone);
        return finishedStm;
    }

    @Override
    public String visit(StmWhile n) {

        String a = "LABEL start";
        String b = "CJUMP (" + n.e.accept(this) + ", EQ, CONST 1, b , done)";
        String c = "LABEL b";
        String d = visit(n.b);
        String e = "JUMP(NAME start)";
        String f = "LABEL done";
        String completedWhileStatement = a;
        completedWhileStatement = seq(completedWhileStatement, b);
        completedWhileStatement = seq(completedWhileStatement, c);
        completedWhileStatement = seq(completedWhileStatement, d);
        completedWhileStatement = seq(completedWhileStatement, e);
        completedWhileStatement = seq(completedWhileStatement, f);
        return completedWhileStatement;
    }

    /* ========================================================== */
    /* Expression visitors (all return an IR expression string) */
    /* ========================================================== */

    @Override
    public String visit(ExpInteger n) {
        return "CONST " + n.i;
    }

    @Override
    public String visit(ExpFalse n) {
        return "CONST 0";

    }

    @Override
    public String visit(ExpTrue n) {
        return "CONST 1";
    }

    @Override
    public String visit(ExpVar n) {
        return "TEMP " + n.v.toString();
    }

    @Override
    public String visit(ExpNot n) {
        String a = "BINOP(" + n.e.accept(this) + ", EQ , CONST 0)";
        return a;
    }

    @Override
    public String visit(ExpOp n) {
        String operator = "";
        switch (n.op) {
        case LESSTHAN:
            operator = "LT";
            break;
        case EQUALS:
            operator = "EQ";
            break;
        case DIV:
            operator = "DIV";
            break;
        case PLUS:
            operator = "ADD";
            break;
        case MINUS:
            operator = "SUB";
            break;
        case TIMES:
            operator = "MUL";
            break;
        case AND:
            String a = "BINOP(" + n.e1.accept(this) + ", EQ, CONST 1)";
            String b = "BINOP(" + n.e2.accept(this) + ",EQ, CONST 1)";
            operator = "BINOP(" + a + " , AND, " + b + ")";
            return operator;
        }
        return "BINOP(" + n.e1.accept(this) + ", " + operator + ", " + n.e2.accept(this) + ")";
    }

}