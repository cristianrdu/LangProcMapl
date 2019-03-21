package compiler;

import visitor.VisitorAdapter;
import syntaxtree.*;

// Use makeLabel to generate fresh labels for jump targets.
import static compiler.FreshLabelGenerator.makeLabel;

/**
 * Prototype Mapl compiler.
 */
public class Compiler extends VisitorAdapter<String> {

    //FreshLabelGenerator makeLabels = new FreshLabelGenerator();

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
        return "EXP(CALL(NAME _printchar, " + n.e.accept(this) + "))";
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
        return "EXP(CALL(NAME _printint, " + n.e.accept(this) + "))";
    }
    @Override
    public String visit(StmAssing n){

        n.v.toString()+
        return "EXP(CALL(NAME _printint, " + n.e.accept(this) + "))";
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
        return "TEMP "+ n.v.toString();
    }
    @Override
    public String visit(ExpNot n){
        String a= "CJUMP("+n.e.toString()+ ", LT , 1 , true , false)";
        
        String seqLabel1 ;
        seqLabel1=seq(makeLabel("true"),
        "TEMP 1");

        String seqLabel2 ;
        seqLabel2=seq(makeLabel("false"),
        "TEMP 0");

        String seqLabels=seq(seqLabel1, seqLabel2);
        return seq(a,seqLabels);
    }

    @Override
    public String visit(ExpOp n){
        String d="";
    switch(n.op.toString()) {
        case "and":
            if(Integer.parseInt(n.e1.accept(this))+Integer.parseInt(n.e2.accept(this))==2)
               { d=d+ "BINOP("+n.e1.accept(this)+", AND ,"+n.e2.accept(this) +")";}
            break;
        case "<":
            d=d+"BINOP("+n.e1.accept(this)+", LT ,"+n.e2.accept(this) +")";
            break;
        case "==":
            d=d+ "BINOP("+n.e1.accept(this)+", EQ ,"+n.e2.accept(this) +")";
            break;
        case "div":
            d=d+ "BINOP("+n.e1.accept(this)+", DIV ,"+n.e2.accept(this) +")";
            break;
        case "+":
            d=d+ "BINOP("+n.e1.accept(this)+", ADD ,"+n.e2.accept(this) +")";
            break;
        case "-":
            d=d+ "BINOP("+n.e1.accept(this)+", SUB ,"+n.e2.accept(this) +")";
            break;
        case "*":
            d=d+ "BINOP("+n.e1.accept(this)+", MUL ,"+n.e2.accept(this) +")";
            break;
        case "<=":
            d=d+ "BINOP("+n.e1.accept(this)+", LE ,"+n.e2.accept(this) +")";
            break;
            
        }
        return d;
    /* AND case doesn't exist in the IR grammar 
        LE is missing from the java class
            clarifications
     */
    }

    
    

}