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
        String params="";
        for(int i=1;i<= n.pd.fs.size();++i){
         params+=", MEM(BINOP(TEMP FP, SUB, CONST " + i + "))";
        }
        String a="EXP(CALL(NAME "+n.pd.id+params+"))";
        a=seq(a,"JUMP (NAME _END)");
        a=seq(a, n.pd.accept(this));
        for(MethodDecl s:n.mds){
                a=seq(a, s.accept(this));
            }
            return a;
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
        // return "MOVE(TEMP " + n.v.toString() + ", " + n.e.accept(this) + ")";
        String b = "MOVE(MEM(BINOP(TEMP FP, ADD, CONST " + n.v.offset + ")," + n.e.accept(this) + ")";
        return b;
    }

    @Override
    public String visit(StmIf n) {
        String trueCase = FreshLabelGenerator.makeLabel("trueCase");
        String falseCase = FreshLabelGenerator.makeLabel("falseCase");
        String done = FreshLabelGenerator.makeLabel("done");

        String ifStm = "CJUMP(" + n.e.accept(this) + ", EQ, CONST 1, " + trueCase + "," + falseCase + ")";
        String labelTrue = "LABEL " + trueCase;
        String block1 = n.b1.accept(this);
        String jumpDone = "JUMP(NAME " + done + ")";
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
        String b1 = FreshLabelGenerator.makeLabel("b");
        String done = FreshLabelGenerator.makeLabel("done");
        String start = FreshLabelGenerator.makeLabel("start");
        String a = "LABEL " + start;
        String b = "CJUMP (" + n.e.accept(this) + ", EQ, CONST 1, " + b1 + " , " + done + ")";
        String c = "LABEL " + b1;
        String d = visit(n.b);
        String e = "JUMP(NAME start)";
        String f = "LABEL " + done;
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
        // return "TEMP " + n.v.toString();
        return "MEM(BINOP(TEMP FP" + ", ADD, CONST " + n.v.offset + "))";
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
    @Override
    public String visit(ExpCall n){
            String a="CALL(NAME "+n.id;
            for (Exp s : n.es) {
                a=a+", ";
                a = seq(a, s.accept(this));
            }
            a=a+")";
             return a;


    }
    
    @Override
    public String visit(StmCall n){
             String a="EXP(CALL(NAME "+n.id;
            for (Exp s : n.es) {
                a=a+", ";
                a = seq(a, s.accept(this));
            }
            a=a+"))";
             return a;
            
    }

    @Override
    public String visit(ProcDecl n){
            String a1="LABEL "+n.id;
            String a="PROLOGUE("+n.fs.size()+", "+n.stackAllocation+")";
            String b="NOOP";
            for(Stm s:n.ss){
                b=seq(b, s.accept(this));
            }
            String c="EPILOGUE("+n.fs.size()+", "+n.stackAllocation+")";
            String retStatement;
            retStatement=seq(a1,a);
            retStatement=seq(retStatement,b);
            retStatement=seq(retStatement,c);
            return retStatement;
    }
    
    @Override
    public String visit(FunDecl n){
            String a1="LABEL "+n.id;
            String a="PROLOGUE("+n.fs.size()+", "+n.stackAllocation+")";
            String b="NOOP";
            for(Stm s:n.ss){
                b=seq(b, s.accept(this));
            }
            String move="MOVE(TEMP RV,"+n.e.accept(this)+")";
            String c="EPILOGUE("+n.fs.size()+", "+n.stackAllocation+")";
            String returnStatement;
            returnStatement=seq(a1,a);
            returnStatement=seq(returnStatement,b);
            returnStatement=seq(returnStatement,move);
            returnStatement=seq(returnStatement,c);
            return returnStatement;
    }
    @Override
    public String visit(ExpNewArray n){
        String a="ESEQ(MOVE(TEMP "+n.e.accept(this)+", "
        "CALL(NAME _malloc, "+n.e.accept(this)+")";
    }
    @Override
    public String visit(ExpArrayLookup n){
        
         "CALL(NAME _malloc, "+n.e.accept(this)+")";
    }
}