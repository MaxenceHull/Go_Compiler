package tiny;

import tiny.parser.*;
import tiny.lexer.*;
import tiny.node.*;
import tiny.analysis.*;
import java.util.*;
import java.io.*;

public class PrettyPrinter extends DepthFirstAdapter {

    public int level = 0;
    public String pretty = "";

    public static void print(Node node, String fileName) {
        node.apply(new PrettyPrinter());
    }

    public void createFile(String fileName) {
       FilterFile();
        try {
            //System.out.println(fileName + ".pretty.min");
            PrintWriter writer = new PrintWriter(fileName + ".pptype.go", "UTF-8");
            writer.print(pretty);
            writer.close();
        } catch (IOException e) {
            System.out.println("File: " + fileName + " cannot be created!");
        }
    }
    
    private void FilterFile() {
        boolean foundChange = true;
        while (foundChange) {
            foundChange = false;
            while (this.pretty.contains("\t;")) {
                this.pretty = this.pretty.replace("\t;", ";");
                foundChange = true;
            }
            while (this.pretty.contains("\t\n")) {
                this.pretty = this.pretty.replace("\t\n", "\n");
                foundChange = true;
            }
            while (this.pretty.contains("\n;")) {
                this.pretty = this.pretty.replace("\n;", ";");
                foundChange = true;

            }
            while (this.pretty.contains("\n\n")) {
                this.pretty = this.pretty.replace("\n\n", "\n");
                foundChange = true;
            }
            while (this.pretty.contains(" \n")) {
                this.pretty = this.pretty.replace(" \n", "\n");
                foundChange = true;
            }
            while (this.pretty.contains("  ")) {
                this.pretty = this.pretty.replace("  ", " ");
                foundChange = true;
            }
            while (this.pretty.contains("\t{"))
            {
                this.pretty = this.pretty.replace("\t{", "{");
                foundChange = true;
            }
            while (this.pretty.contains("\n{"))
            {
                this.pretty = this.pretty.replace("\n{", "{");
                foundChange = true;
            }
            
        }
    }



    private void puts(String s) {
        if (this.pretty.endsWith("\n")) {
            for (int i = 0; i < level; i++) {
                pretty += "\t";
            }
        }
        if (s.startsWith("\n")) {
            String firstPart = "";
            while (s.length() > 0 && s.charAt(0) == '\n')
            {
                firstPart += "\n";
                s = s.substring(1);
            }
            pretty += firstPart;
            for (int i = 0; i < level; i++) {
                pretty += "\t";
            }
        }
        pretty += s;
        // System.out.print(s);
            // System.out.flush();
    }

    /*  Upper Level Declarations */
    ///functions
    public void caseAFuncToplvlDcl(AFuncToplvlDcl node) {
        node.getFuncDcl().apply(this);
    }
    
    public void caseAVarToplvlDcl(AVarToplvlDcl node)
    {
        if (node.getVarDclList() != null)
        {
            node.getVarDclList().apply(this);
            puts(";");
        }
    }

    public void caseATypeToplvlDcl(ATypeToplvlDcl node) {
        if (node.getTypeDclList() != null) {
            node.getTypeDclList().apply(this);
            puts(";");
        }
    }
    
    public void caseAVarDclStatement(AVarDclStatement node)
    {
        node.getVarDclList().apply(this);
    }
    
    public void caseAVarDclList(AVarDclList node)
    {
        if (node.getDcls() != null) {
            for (PVarDcl currDcl : node.getDcls()) {
                AVarDcl wNode = (AVarDcl) currDcl;
                wNode.apply(this);
            }
        }
    } 
        
    public void caseAFuncDcl(AFuncDcl node) {
        puts("\n" + "func " + node.getName().getText() + " ( ");
        if (node.getArguments() != null) {
            for(PArgument currArg : node.getArguments())
            {
                currArg.apply(this);
            }
        }
        puts(" ) ");
        if (node.getReturnType() != null)
        {
            node.getReturnType().apply(this);
        }
        puts("\n");
        node.getBlock().apply(this);
        puts(";");
        puts("\n");
    }

    ////Var declarations
    public void caseAVarDcl(AVarDcl node) {
       puts ("var ");
        if (node.getNames() != null)
        {
            boolean alreadyOne = false;
            for (TIdentifier currIdent : node.getNames())
            {
                if (alreadyOne)
                {
                    puts (" , ");
                }
                else {
                    alreadyOne = true;
                }
                puts (currIdent.getText().toString());
                currIdent.apply(this);
            }
        }
        
        if (node.getType()!= null)
        {
            puts(" " + node.getType().toString() + " " );
        }
        
        if (node.getExpressions()!= null && node.getExpressions().size() > 0) {
            puts(" = ");
            for (PExpression currIdent : node.getExpressions()) {
                currIdent.apply(this);
            }
        }
    }
    
  
//    public void casePExpression(PExpression node)
//    {
//        
//    }
//    

    
    
    public void caseAPackageDcl(APackageDcl node)
    {
        puts("package " + node.getName().getText() + " " + ";\n");
    }

    /*operands*/
    public void caseARuneExpression(ARuneExpression node) {
        puts(node.getRune().getText());
    }

    public void caseAStringExpression(AStringExpression node) {
        puts(node.getString().getText());
    }

    public void caseAFloatExpression(AFloatExpression node) {
        puts(node.getFloat().getText());
    }

    public void caseAIntExpression(AIntExpression node) {
        puts(node.getInteger().getText());
    }

    public void caseAIdentifierExpression(AIdentifierExpression node) {
        puts(node.getIdentifier().getText());
    }

    /*term*/
//    public void caseAParExprTerm(AParExprTerm node) {
//        puts("(");
//        node.getExpression().apply(this);
//        puts(")");
//    }
//
//    public void caseAFunccallTerm(AFunccallTerm node) {
//        node.getTerm().apply(this);
//        puts("(");
//        if (node.getExpressionList() != null) {
//            node.getExpressionList().apply(this);
//        }
//        puts(")");
//    }
//
//    public void caseAArrayAccessTerm(AArrayAccessTerm node) {
//        puts(" ( ");
//        node.getTerm().apply(this);
//        puts("[");
//        node.getExpression().apply(this);
//        puts("]");
//        puts(" ) ");
//    }
//
//    public void caseASelectTerm(ASelectTerm node) {
//        puts(" ( ");
//        node.getTerm().apply(this);
//        puts(".");
//        puts(node.getIdentifier().getText());
//        puts(" ) ");
//    }
//
//    public void caseAAppendTerm(AAppendTerm node) {
//        puts(" ( ");
//        puts("append(");
//        node.getAppendident().apply(this);
//        node.getAppendval().apply(this);
//        puts(")");
//        puts(" ) ");
//    }
//
//    /*expr5*/
//    public void caseAUnaryOpExpr5(AUnaryOpExpr5 node) {
//        puts(" ( ");
//        node.getUnaryOp().apply(this);
//        node.getTerm().apply(this);
//        puts(" ) ");
//    }
//
//    /*expr4*/
//    public void caseAMulOpExpr4(AMulOpExpr4 node) {
//        puts(" ( ");
//        node.getExpr4().apply(this);
//        node.getMulOp().apply(this);
//        node.getExpr5().apply(this);
//        puts(" ) ");
//    }
//
//    /*expr3*/
//    public void caseAAddOpExpr3(AAddOpExpr3 node) {
//        puts(" ( ");
//        node.getExpr3().apply(this);
//        node.getAddOp().apply(this);
//        node.getExpr4().apply(this);
//        puts(" ) ");
//    }
//
//    /*expr2*/
//    public void caseARelOpExpr2(ARelOpExpr2 node) {
//        puts(" ( ");
//        node.getExpr2().apply(this);
//        node.getRelOp().apply(this);
//        node.getExpr3().apply(this);
//        puts(" ) ");
//    }
//
//    /*expr1*/
//    public void caseAAndExpExpr1(AAndExpExpr1 node) {
//        puts(" ( ");
//        node.getExpr1().apply(this);
//        puts(node.getLogicAnd().getText());
//        node.getExpr2().apply(this);
//        puts(" ) ");
//    }
//
//    /*expr1*/
//    public void caseAOrExpExpression(AOrExpExpression node) {
//        puts(" ( ");
//        node.getExpression().apply(this);
//        puts(node.getLogicOr().getText());
//        node.getExpr1().apply(this);
//        puts(" ) ");
//    }
//
    /*unary_op*/
    public void caseAPlusUnaryOperator(APlusUnaryOperator node) {
        puts(node.getPlus().getText());
    }

    public void caseAMinusUnaryOperator(AMinusUnaryOperator node) {
        puts(node.getMinus().getText());
    }

    public void caseANoptUnaryOperator(ANotUnaryOperator node) {
        puts(node.getExclamation().getText());
    }

    public void caseAUnaryXorUnaryOperator(AUnaryXorUnaryOperator node) {
       puts(node.getCaret().getText());
   }

    /* mul_op*/
    public void caseAMultBinaryOperator(AMultBinaryOperator node) {
        puts(node.getStar().getText());
    }

    public void caseADivdBinaryOperator(ADivdBinaryOperator node) {
        puts(node.getSlash().getText());
    }

    public void caseARemainBinaryOperator(ARemainBinaryOperator node) {
        puts(node.getPercent().getText());
    }

    public void caseALeftShiftBinaryOperator(ALeftShiftBinaryOperator node) {
        puts(node.getLessLess().getText());
    }

    public void caseARightShiftBinaryOperator(ARightShiftBinaryOperator node) {
        puts(node.getGreaterGreater().getText());
    }

    public void caseABitAndBinaryOperator(ABitAndBinaryOperator node) {
        puts(node.getAmpersand().getText());
    }

    public void caseABitAndNotBinaryOperator(ABitAndNotBinaryOperator node) {
        puts(node.getAmpersandCaret().getText());
    }

    /*rel_op*/
    public void caseALogicEqualBinaryOperator(ALogicEqualBinaryOperator node) {
        puts(node.getLogicEqual().getText());
    }

    public void caseALogicNotEqualBinaryOperator(ALogicNotEqualBinaryOperator node) {
        puts(node.getLogicNotEqual().getText());
    }

    public void caseALogicLessBinaryOperator(ALogicLessBinaryOperator node) {
        puts(node.getLess().getText());
    }

    public void caseALogicLessEqualBinaryOperator(ALogicLessEqualBinaryOperator node) {
        puts(node.getLessOrEqual().getText());
    }

    public void caseALogicGreaterBinaryOperator(ALogicGreaterBinaryOperator node) {
        puts(node.getGreater().getText());
    }

    public void caseALogicGreaterEqualBinaryOperator(ALogicGreaterEqualBinaryOperator node) {
        puts(node.getGreaterOrEqual().getText());
    }

    /*add_op*/
    public void caseAPlusBinaryOperator(APlusBinaryOperator node) {
        puts(node.getPlus().getText());
    }

    public void caseAMinusBinaryOperator(AMinusBinaryOperator node) {
        puts(node.getMinus().getText());
    }

    public void caseABitOrBinaryOperator(ABitOrBinaryOperator node) {
        puts(node.getVerticalBar().getText());
    }

    public void caseABitXorBinaryOperator(ABitXorBinaryOperator node) {
        puts(node.getCaret().getText());
    }

//    /*if_stmt*/
//    public void caseAIfStmt(AIfStmt node) {
//        puts("if ");
//        if (node.getIfOptExpr() != null) {
//            node.getIfOptExpr().apply(this);
//        }
//        node.getExpression().apply(this);
//        node.getBlock().apply(this);
//        if (node.getElseStmt() != null) {
//            node.getElseStmt().apply(this);
//        }
//        puts("\n");
//    }
//
//    public void caseAIfOptExpr(AIfOptExpr node) {
//        if (node.getSimpleStmt() != null) {
//            node.getSimpleStmt().apply(this);
//            puts(";");
//        }
//    }
//
//    public void caseAElifElseStmt(AElifElseStmt node) {
//        puts("else ");
//        node.getIfStmt().apply(this);
//    }
//
//    public void caseAElseElseStmt(AElseElseStmt node) {
//        puts("else ");
//        node.getBlock().apply(this);
//    }
//
//    /*for stmt*/
//    public void caseAInfiniteForForStmt(AInfiniteForForStmt node) {
//        puts("for ");
//        node.getBlock().apply(this);
//    }
//
//    public void caseAForCondForStmt(AForCondForStmt node) {
//        puts("for ");
//        node.getExpression().apply(this);
//        node.getBlock().apply(this);
//    }
//
//    public void caseAForClauseForStmt(AForClauseForStmt node) {
//        puts("for ");
//        if (node.getInitStmt() != null) {
//            node.getInitStmt().apply(this);
//        }
//        puts(";");
//        if (node.getCondition() != null) {
//            node.getCondition().apply(this);
//        }
//        puts(";");
//        if (node.getPostStmt() != null) {
//            node.getPostStmt().apply(this);
//        }
//        node.getBlock().apply(this);
//    }
//
//    /*block*/
//    public void caseABlock(ABlock node) {
//        puts("\n{\n");
//        this.level++;
//        for (PStatement stmt : node.getStatement()) {
//            stmt.apply(this);
//        }
//        this.level--;
//        if (!this.pretty.endsWith("\n")) {
//            puts("\n");
//        }
//        puts("}\n");
//    }
//
//    /*expression list*/
//    public void caseAOneExpressionList(AOneExpressionList node) {
//        node.getExpression().apply(this);
//    }
//
//    public void caseAManyExpressionList(AManyExpressionList node) {
//        node.getExpression().apply(this);
//        puts(",");
//        node.getExpressionList().apply(this);
//    }
//
//    /*identifier list*/
//    public void caseAOneIdentifierList(AOneIdentifierList node) {
//        puts(node.getIdentifier().getText());
//    }
//
//    public void caseAManyIdentifierList(AManyIdentifierList node) {
//        puts(node.getIdentifier().getText());
//        puts(" , ");
//        node.getIdentifierList().apply(this);
//    }
//    
//    public void caseAManyIdenTypePair(AManyIdenTypePair node) {
//        puts(node.getArgument().toString());
//        puts(" , ");
//        node.getIdenTypePair().apply(this);
//    }
//
//        public void caseAOneIdenTypePair(AOneIdenTypePair node) {
//        puts(" " + node.getArgument().toString());
//    }
//        
//    /*type spec*/
//    public void caseATypeSpec(ATypeSpec node) {
//        node.getName().apply(this);
//        node.getTypeid().apply(this);
//        puts(";");
//    }
//
//    public void caseAOneTypeDecl(AOneTypeDecl node) {
//        puts("type ");
//        node.getType().apply(this);
//        node.getTypeSpec().apply(this);
//    }
//
//    public void caseAManyTypeDecl(AManyTypeDecl node) {
//        puts("type (\n");
//        for (PTypeSpec i : node.getTypeSpec()) {
//            i.apply(this);
//        }
//        puts(")");
//        puts(";");
//        puts("\n");
//    }
//
//    /*var spec*/
//    public void caseAExpressionOnlyVarSpec(AExpressionOnlyVarSpec node) {
//        node.getIdentifierList().apply(this);
//        puts("=");
//        node.getExpressionList().apply(this);
//        puts(";");
//    }
//
//    public void caseATypeAndExpressionVarSpec(ATypeAndExpressionVarSpec node) {
//        node.getIdentifierList().apply(this);
//        puts (" " );
//        node.getType().apply(this);
//        puts(" = ");
//        node.getExpressionList().apply(this);
//        puts(";");
//    }
//
//    public void caseATypeOnlyVarSpec(ATypeOnlyVarSpec node) {
//        node.getIdentifierList().apply(this);
//        node.getType().apply(this);
//        puts(";");
//    }
//
//
//    /*type t*/
//    public void caseAFieldDcl(AFieldDcl node) {
//        node.getIdentifiers().apply(this);
//        node.getTypeT().apply(this);
//        puts(";");
//    }
//
//    public void caseANameTypeT(ANameTypeT node) {
//        puts(node.getIdentifier().getText());
//    }
//
//    public void caseASliceTypeT(ASliceTypeT node) {
//        puts(" []");
//        node.getType().apply(this);
//    }
//
//    public void caseAArrayTypeT(AArrayTypeT node) {
//        puts(" [");
//        puts(node.getInteger().getText());
//        puts("]");
//        node.getType().apply(this);
//    }
//
//    public void caseAStructTypeT(AStructTypeT node) {
//        puts("struct {\n");
//        for (PFieldDcl i : node.getFieldDcl()) {
//            i.apply(this);
//        }
//        puts("}\n");
//    }
//
    /* inc / dec stmt*/
    public void caseAIncStatement(AIncStatement node) {
        node.getExpression().apply(this);
        puts("++\n");
    }

    public void caseADecStatement(ADecStatement node) {
        node.getExpression().apply(this);
        puts("--\n");
    }

//    public void caseAOneLvalueList(AOneLvalueList node) {
//        node.getTerm().apply(this);
//    }
//
//    public void caseAManyLvalueList(AManyLvalueList node) {
//        node.getTerm().apply(this);
//        puts(",");
//        node.getLvalueList().apply(this);
//    }

    /* assign op*/
    public void caseAPlusEqualAssignOperator(APlusEqualAssignOperator node) {
        puts(node.getPlusEqual().getText());
    }

    public void caseAMinusEqualAssignOperator(AMinusEqualAssignOperator node) {
        puts(node.getMinusEqual().getText());
    }

    public void caseAVertEqualAssignOperator(AVertEqualAssignOperator node) {
        puts(node.getVertEqual().getText());
    }

    public void caseACaretEqualAssignOperator(ACaretEqualAssignOperator node) {
        puts(node.getCaretEqual().getText());
    }

    public void caseASlashEqualAssignOperator(ASlashEqualAssignOperator node) {
        puts(node.getSlashEqual().getText());
    }

    public void caseAStarEqualAssignOperator(AStarEqualAssignOperator node) {
        puts(node.getStarEqual().getText());
    }

    public void caseAPercentEqualAssignOperator(APercentEqualAssignOperator node) {
        puts(node.getPercentEqual().getText());
    }

    public void caseAAmpersandEqualAssignOperator(AAmpersandEqualAssignOperator node) {
        puts(node.getAmpersandEqual().getText());
    }

    public void caseALessLessEqualAssignOperator(ALessLessEqualAssignOperator node) {
        puts(node.getLessLessEqual().getText());
    }

    public void caseAGreaterGreaterEqualAssignOperator(AGreaterGreaterEqualAssignOperator node) {
        puts(node.getGreaterGreaterEqual().getText());
    }

    public void caseAAmpersandCaretEqualAssignOperator(AAmpersandCaretEqualAssignOperator node) {
        puts(node.getAmpersandCaretEqual().getText());
    }
//
//    /*  Statements */
//    
//    public void caseASwitchStmt(ASwitchStmt node)
//    {
//        puts("\n");
//        if (node.getIfOptExpr() != null)
//        {
//            node.getIfOptExpr().apply(this);
//        }
//        puts("  ");
//        if (node.getCondition() != null)
//        {
//            node.getCondition().apply(this);
//        }
//        puts(" {\n");
//        LinkedList<PSwitchBody> wSwitchstmts = node.getSwitchBody();
//        for (PSwitchBody curr : wSwitchstmts)
//        {
//            curr.apply(this);
//        }
//        puts("\n};");
//    }
//
//
//    public void caseACaseSwitchBody(ACaseSwitchBody node) {
//        puts(" case ");
//        if (node.getExpressionList() != null)
//        {
//            node.getExpressionList().apply(this);
//            puts(" : ");
//            for (PStatement curr : node.getStatement()) {
//                curr.apply(this);
//            }
//        }
//        puts("\n");
//    }
//
//    public void caseADefaultSwitchBody(ADefaultSwitchBody node) {
//        puts(" default ");
//        puts(" : ");
//        for (PStatement curr : node.getStatement()) {
//            curr.apply(this);
//        }
//        puts("\n");
//    }
//        
//    
//    public void caseABreakStatement(ABreakStatement node)
//    {
//        puts(" break; ");
//    }
//    
//    public void caseAPrintlnStmt(APrintlnStmt node) {
//        puts("println( ");
//        node.getExpressionList().apply(this);
//        puts(" )\n");
//    }
//
//    public void caseAPrintStmt(APrintStmt node) {
//        puts("print( ");
//        node.getExpressions().apply(this);
//        puts(" )\n");
//    }
//    
//    public void caseAReturnStatement(AReturnStatement node)
//    {
//        puts("return " );
//        node.getExpression().apply(this);
//        puts(";");
//        puts("\n");
//    }
//
//    public void caseAAssignAssignmentStmt(AAssignAssignmentStmt node) {
//        node.getLvalue().apply(this);
//        puts(" = ");
//        node.getRvalue().apply(this);
//        puts("\n");
//    }
//
//    public void caseAOpAssignAssignmentStmt(AOpAssignAssignmentStmt node) {
//        node.getLvalue().apply(this);
//        puts(" ");
//        node.getAssignOp().apply(this);
//        puts(" ");
//        node.getRvalue().apply(this);
//        puts("\n");
//    }
//
//    public void caseAExpressionStmtSimpleStmt(AExpressionStmtSimpleStmt node) {
//        node.getExpression().apply(this);
//        puts("\n");
//    }
//
//    public void caseAShortVarDeclSimpleStmt(AShortVarDeclSimpleStmt node) {
//        puts(" " + node.getIdentifiers().getText());
//        puts(" := ");
//        node.getExpressions().apply(this);
//        puts(" \n");
//    }
//
//    public void caseAIncrementStmtSimpleStmt(AIncrementStmtSimpleStmt node) {
//        node.getIncrementStmt().apply(this);
//        puts("++");
//        puts("\n");
//    }
//
//    public void caseADecrementStmtSimpleStmt(ADecrementStmtSimpleStmt node) {
//        node.getDecrementStmt().apply(this);
//        puts("--");
//        puts("\n");
//    }
//
//    public void caseAAssignmentSimpleStmt(AAssignmentSimpleStmt node) {
//        node.getAssignmentStmt().apply(this);
//        puts("\n");
//    }
//
//    public void caseATypeDecl(ATypeDecl node) {
//        node.getTypeDecl().apply(this);
//        puts("\n");
//    }
//
//    public void caseAIfStatement(AIfStatement node) {
//        node.getIfStmt().apply(this);
//        puts("\n");
//        puts(";");
//        puts("\n");
//    }
}
