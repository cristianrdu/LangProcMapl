package compiler;

import visitor.VisitorAdapter;
import syntaxtree.*;

// Use makeLabel to generate fresh labels for jump targets.
import static compiler.FreshLabelGenerator.makeLabel;

/**
 * Prototype Mapl compiler.
 */
public class Compiler extends VisitorAdapter<String> {

    public Compiler() {}

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
    

    /*========================================================*/
    /* Statement visitors (all return an IR statement string) */
    /*========================================================*/

    @Override
    public String visit(StmVarDecl n) {
        return "NOOP";
    }
    @Override
    public String visit(StmOutchar n) {
        return "EXP(CALL(NAME _printint, " + n.e.accept(this) + "))";
    }
    @Override
    public String visit(StmBlock n){
        String ir="";
        for(Stm s : n.ss){
            ir=seq(ir,s.accept(this));
        }
        return ir;
    }
    @Override
    public String visit(StmOutput n){
        
    }

    /*==========================================================*/
    /* Expression visitors (all return an IR expression string) */
    /*==========================================================*/

    @Override
    public String visit(ExpInteger n) {
        return "CONST " + n.i;
    }
    @Override
    public String visit(ExpFalse n){
        return "false";

    }
    @Override
    public String visit(ExpTrue n){
        return "true";
    }
    @Override
    public String visit(ExpVar n){
        return "TEMP "+ n.v;
    }
    @Override
    public String visit(ExpNot n){
        String a= "CJUMP("+n.e+ ", LT , 1 , true , false)";
        
        String seqLabel1 ;
        FreshLabelgenerator label1=new FreshLabelGenerator();
        seqLabel1=seq(label1.makeLabel("true"),
        "TEMP 1");

        String seqLabel2 ;
        FreshLabelgenerator label2=new FreshLabelGenerator();
        seqLabel2=seq(label2.makeLabel("false"),
        "TEMP 0");

        String seqLabels=seq(seqLabel1, seqLabel2);
        return seq(a,seqLabels);
    }

    @Override
    public String visit(ExpOp n){
    switch(n.name) {
        case "and":
            return "AND";
            break;
        case "<":
            return "LT";
            break;
        case "==":
            return "EQ";
            break;
        case "div":
            return "DIV";
            break;
        case "+":
            return "ADD";
            break;
        case "-":
            return "SUB";
            break;
        case "*":
            return "MUL";
            break;
        case "<==":
            return "LE";
            break;
        }
    /* AND case doesn't exist in the IR grammar 
        LE is missing from the java class
            clarifications
     */
    }

    
    

}