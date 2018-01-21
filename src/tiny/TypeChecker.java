package tiny;
import tiny.parser.*;
import tiny.lexer.*;
import tiny.node.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import tiny.analysis.DepthFirstAdapter;

public class TypeChecker extends DepthFirstAdapter{
  public SymbolTable st;
  public HashMap<String, String> mTL;
  public HashMap<String, String> mStructTL;
  public ArrayList<String[]> mTemporaryFunctionParamList = new ArrayList<String[]>();
  private boolean mDumpOnScopeExit, mDumpAllOnScopeExit;
  private String mFileName = "";

  private HashMap<String, FunctionDefinition> mFunctionTable;

  private void TerminateProgram(String iMessage)
  {
     // System.out.println(iMessage);
      System.err.println(iMessage);
      System.exit(1);
  }

  private class FunctionDefinition {
      public boolean mHasReturn = false;
      public String mReturnType = "";
      public String[] mParameterTypeList;
  }

  public boolean safeTypeCompare(String typeA, String typeB)
  {
      return (typeA.trim().equals(typeB.trim()));
      //String currBasicTypeA = st.GetBasicType(typeA.trim()).trim();
      //String currBasicTypeB = st.GetBasicType(typeB.trim()).trim();
      //return currBasicTypeA.equals(currBasicTypeB);
  }


    public TypeChecker(String ifileName, boolean iDumpOnScopeExit, boolean iDumpAllOnScopeExit) {
        mFunctionTable = new HashMap<String, FunctionDefinition>();
        mDumpOnScopeExit = iDumpOnScopeExit;
        mDumpAllOnScopeExit = iDumpAllOnScopeExit;
        mFileName = ifileName + ".symtab";
        st = new SymbolTable();
        mTL = new HashMap<String, String>();
        mStructTL = new HashMap<String, String>();
    }

  public void outAProgram(AProgram node){
    //st.print();
  }

  public void caseAVarDcl(AVarDcl node){
    // type is explicit
    if(node.getExpressions().isEmpty()){
      for(TIdentifier id: node.getNames()){
        if(!(node.getType() instanceof AStructTypeT)) {
          st.addVar(id.getText(), typeToString(node.getType()), getAbstractType(node.getType()));
        } else {
          addAStruct(id.getText(), node.getType());
        }

      }
    // Type is not explicit
    }else if(node.getType() == null){
      if(node.getNames().size()!=node.getExpressions().size()){
        TerminateProgram("Declaration must have the same number of identifiers and expressions");
      }
      for(int i=0; i<node.getNames().size(); i++){
        String type = _getExpressionType(node.getExpressions().get(i));
        if(isArrayType(type) || isSliceType(type)){
          st.addVar(node.getNames().get(i).getText(), type, type.replaceAll("(\\[(([0-9])+)*\\])",""));
        }else {
          st.addVar(node.getNames().get(i).getText(), type, type);
        }
      }
    // Type and expressions are explicit
    }else {
      if(node.getNames().size()!=node.getExpressions().size()){
        TerminateProgram("Declaration must have the same number of identifiers and expressions");
      }
      String explicitType = typeToString(node.getType());
      for(int i=0; i<node.getNames().size(); i++){
        String exprType = _getExpressionType(node.getExpressions().get(i));
        PExpression currExpr = node.getExpressions().get(i);
        //if (safeTypeCompare(exprType, explicitType) || safeTypeCompare(exprType, st.RT(explicitType))){
        if (safeTypeCompare(exprType, explicitType)){
          st.addVar(node.getNames().get(i).getText(), explicitType, getAbstractType(node.getType()));
        } else {
            try {
                if (currExpr.getClass() == AArrayAccessExpression.class)
                {
                    String cleanedType = exprType;
                    int wIndexOfLastBracket = exprType.lastIndexOf("]");
                    if (wIndexOfLastBracket < exprType.length()-1) //array pos at start
                    {
                        cleanedType = cleanedType.substring(wIndexOfLastBracket+1);
                    }
                    else {
                        cleanedType = cleanedType.substring(0, cleanedType.indexOf("[")-1);
                    }
                    if (safeTypeCompare(cleanedType, st.RT(explicitType)))
                    {
                        st.addVar(node.getNames().get(i).getText(), explicitType, getAbstractType(node.getType()));
                    }
                }
                else if (safeTypeCompare(exprType, st.RT(explicitType)))
                {
                   st.addVar(node.getNames().get(i).getText(), explicitType, getAbstractType(node.getType()));
                }
                else {
                    TerminateProgram("Explicit type "+explicitType+" does not correspond to expression type "+exprType);
                }
            }
            catch (Exception e)
            {
                TerminateProgram("Explicit type "+explicitType+" does not correspond to expression type "+exprType);
            }
        }
      }
    }

  }

  public void caseATypeDcl(ATypeDcl node){
    if(!(node.getType() instanceof AStructTypeT)){
      st.addType(node.getName().getText(), typeToString(node.getType()), getAbstractType(node.getType()));
      mTL.put(node.getName().getText().toString(), typeToString(node.getType()));
    }else {
      HashMap<String, SymbolTableEntry> struct = new HashMap<String, SymbolTableEntry>();
      String resultType = "";
      String resultAbstractType = "";
      AStructTypeT typeT = (AStructTypeT)node.getType();
      for(PFieldDcl field: typeT.getFieldDcl()){
        AFieldDcl temp = (AFieldDcl) field;
        for(TIdentifier id: temp.getIdentifiers()){
          resultType = resultType+","+typeToString(temp.getTypeT());
          resultAbstractType = resultAbstractType+","+getAbstractType(temp.getTypeT());
          if(struct.containsKey(id.getText())){
            TerminateProgram("Identifier "+id.getText()+" is already used in struct "+node.getName().getText());
          }
          struct.put(id.getText(), new SymbolTableEntry(id.getText(), Constants.CATEGORY_VAR, typeToString(temp.getTypeT()), getAbstractType(temp.getTypeT()) ));
        }
      }
      st.addTypeStruct(node.getName().getText(), resultType, resultAbstractType, struct);
      mStructTL.put(node.getName().getText().toString(), typeToString(node.getType()));
    }

  }

  public void inABlock(ABlock node){
    int iHashCode = GetHashCodeForParent(node);
    st.pushTable(mTemporaryFunctionParamList, iHashCode);
    mTemporaryFunctionParamList.clear();
  }


    public static int GetHashCodeForParent(Node iNode) {
        Node currNode = iNode;
        boolean currCondition = false;
        while (!currCondition) {
            if (currNode == null)
            {
                return 0;
            }
            currCondition |= currNode instanceof AForStmt;
            currCondition |= currNode instanceof AFuncDcl;
            currCondition |= currNode instanceof AIfStmt;
            currCondition |= currNode instanceof AElseElseStmt;
            currNode = currNode.parent();
        }
        return currNode.hashCode();
    }

  public void caseAAssignmentStatement(AAssignmentStatement node){
    LinkedList<PExpression> lvalues = node.getLvalue();
    LinkedList<PExpression> rvalues = node.getRvalue();

      for (PExpression currexpr : lvalues) {
          if (currexpr instanceof AFunccallExpression) {
              AFunccallExpression currFuncExpr = (AFunccallExpression) currexpr;
              currFuncExpr.apply(this);
          }
      }

      for (PExpression currexpr : rvalues) {
          if (currexpr instanceof AFunccallExpression) {
              AFunccallExpression currFuncExpr = (AFunccallExpression) currexpr;
              currFuncExpr.apply(this);
          }
      }

    if(lvalues.size() != rvalues.size()){
      TerminateProgram("Assignment statement: number of rvalues do not match number of lvalues");
    }
    for(int i=0; i < lvalues.size(); i++){
      PExpression lvalue = lvalues.get(i);
      if(!(lvalue instanceof AIdentifierExpression || lvalue instanceof AArrayAccessExpression || lvalue instanceof ASelectExpression)){
        TerminateProgram("Assignment: lvalue must be an identifier, an array indexing...");
      }
      PExpression rvalue = rvalues.get(i);
       String lvalue_type = _getExpressionType(lvalue);
      if(lvalue_type.startsWith("func")){
        TerminateProgram("Assignment: lvalue must be an identifier, an array indexing...");
      }
      String rvalue_type = _getExpressionType(rvalue);
      if(!safeTypeCompare(lvalue_type, rvalue_type)){
        TerminateProgram("lvalue type ("+lvalue_type+") do not correspond to rvalue type ("+rvalue_type+")");
      }
    }
  }

  public void caseAOpAssignStatement(AOpAssignStatement node){
    String type_l = _getExpressionType(node.getLvalue());
    if(!(node.getLvalue() instanceof AIdentifierExpression) && !(node.getRvalue() instanceof AIdentifierExpression)){
      TerminateProgram("Op-assignment: an identifier must be present");
    }
    ABinaryExpression binary_exp = new ABinaryExpression();
    binary_exp.setR((PExpression)node.getRvalue().clone());
    binary_exp.setL((PExpression)node.getLvalue().clone());
    String op = "";
    PBinaryOperator operator = null;
    if(node.getOp() instanceof APlusEqualAssignOperator){
      APlusEqualAssignOperator temp = (APlusEqualAssignOperator) node.getOp();
      op = temp.getPlusEqual().getText().substring(0,temp.getPlusEqual().getText().length()-1);
      operator = new APlusBinaryOperator();
    } else if(node.getOp() instanceof AMinusEqualAssignOperator){
      AMinusEqualAssignOperator temp = (AMinusEqualAssignOperator) node.getOp();
      op = temp.getMinusEqual().getText().substring(0,temp.getMinusEqual().getText().length()-1);
      operator = new AMinusBinaryOperator();
    } else if(node.getOp() instanceof AVertEqualAssignOperator){
      AVertEqualAssignOperator temp = (AVertEqualAssignOperator) node.getOp();
      op = temp.getVertEqual().getText().substring(0,temp.getVertEqual().getText().length()-1);
      operator = new ABitOrBinaryOperator();
    } else if(node.getOp() instanceof ACaretEqualAssignOperator){
      ACaretEqualAssignOperator temp = (ACaretEqualAssignOperator) node.getOp();
      op = temp.getCaretEqual().getText().substring(0,temp.getCaretEqual().getText().length()-1);
      operator = new ABitXorBinaryOperator();
    } else if(node.getOp() instanceof ASlashEqualAssignOperator){
      ASlashEqualAssignOperator temp = (ASlashEqualAssignOperator) node.getOp();
      op = temp.getSlashEqual().getText().substring(0,temp.getSlashEqual().getText().length()-1);
      operator = new ADivdBinaryOperator();
    } else if(node.getOp() instanceof AStarEqualAssignOperator){
      AStarEqualAssignOperator temp = (AStarEqualAssignOperator) node.getOp();
      op = temp.getStarEqual().getText().substring(0,temp.getStarEqual().getText().length()-1);
      operator = new AMultBinaryOperator();
    } else if(node.getOp() instanceof APercentEqualAssignOperator){
      APercentEqualAssignOperator temp = (APercentEqualAssignOperator) node.getOp();
      op = temp.getPercentEqual().getText().substring(0,temp.getPercentEqual().getText().length()-1);
      operator = new ARemainBinaryOperator();
    } else if(node.getOp() instanceof AAmpersandEqualAssignOperator){
      AAmpersandEqualAssignOperator temp = (AAmpersandEqualAssignOperator) node.getOp();
      op = temp.getAmpersandEqual().getText().substring(0,temp.getAmpersandEqual().getText().length()-1);
      operator = new ABitAndBinaryOperator();
    } else if(node.getOp() instanceof ALessLessEqualAssignOperator){
      ALessLessEqualAssignOperator temp = (ALessLessEqualAssignOperator) node.getOp();
      op = temp.getLessLessEqual().getText().substring(0,temp.getLessLessEqual().getText().length()-1);
      operator = new ALeftShiftBinaryOperator();
    } else if(node.getOp() instanceof AGreaterGreaterEqualAssignOperator){
      AGreaterGreaterEqualAssignOperator temp = (AGreaterGreaterEqualAssignOperator) node.getOp();
      op = temp.getGreaterGreaterEqual().getText().substring(0,temp.getGreaterGreaterEqual().getText().length()-1);
      operator = new ARightShiftBinaryOperator();
    } else if(node.getOp() instanceof AAmpersandCaretEqualAssignOperator){
      AAmpersandCaretEqualAssignOperator temp = (AAmpersandCaretEqualAssignOperator) node.getOp();
      op = temp.getAmpersandCaretEqual().getText().substring(0,temp.getAmpersandCaretEqual().getText().length()-1);
      operator = new ABitAndNotBinaryOperator();
    } else {
      TerminateProgram("Houston we have a problem");
    }
    binary_exp.setOp(operator);
    String type_r = _getExpressionType(binary_exp);
    if(!safeTypeCompare(type_r, type_l)){
      TerminateProgram("Assign-op is not possible with type("+type_l+") and type ("+type_r+")");
    }

  }

  public void caseAPrintlnStatement(APrintlnStatement node){
    for(PExpression expression: node.getExpressions()){
      String type = _getExpressionType(expression);
      String base_type = st.RT(type);
      if(!isBaseType(base_type)){
        TerminateProgram("Cannot println type: "+type);
      }
    }
  }

  public void caseAPrintStatement(APrintStatement node){
    for(PExpression expression: node.getExpressions()){
      String type = _getExpressionType(expression);
      String base_type = st.RT(type);
      if (base_type.indexOf("[") != -1 && base_type.indexOf("]") != -1)
      {
          String finalBaseType = base_type.substring(0, base_type.indexOf("["));
          if (base_type.indexOf("[")+1 < base_type.length())
          {
              finalBaseType += base_type.substring(base_type.indexOf("]")+1 );
          }
          base_type = finalBaseType;
      }
      if(!isBaseType(base_type)){
        TerminateProgram("Cannot print type: "+type);
      }
    }
  }

  public void caseAForStmt(AForStmt node){

    if(node.getInitStmt()!=null){
       if(node.getInitStmt() instanceof AShortVarDeclStatement){
         AShortVarDeclStatement initStmt = (AShortVarDeclStatement)node.getInitStmt();
         for(int i=0; i<initStmt.getIdentifiers().size(); i++){
           AIdentifierExpression id = (AIdentifierExpression) initStmt.getIdentifiers().get(i);
           PExpression expr = initStmt.getExpressions().get(i);
           String type = _getExpressionType(expr);
           st.alwaysAddVar(id.getIdentifier().getText(), type, type);
         }
       } else {
         node.getInitStmt().apply(this);
       }
     }

      if (node.getCondition() != null) {
          String type = _getExpressionType(node.getCondition());
          String base_type = st.RT(type);
          if (!safeTypeCompare(base_type, Constants.TYPE_BOOL)) {
              TerminateProgram("For statement condition must be a boolean (current type:" + type + ")");
          }
      }

     if(node.getPostStmt() != null){
       node.getPostStmt().apply(this);
     }

     node.getBlock().apply(this);
}

  public void caseADecStatement(ADecStatement node){
    if(!isNumeric(st.RT(_getExpressionType(node.getExpression())))){
      TerminateProgram("Decrement expression must be a numeric");

    }
  }

  public void caseAIncStatement(AIncStatement node){
    if(!isNumeric(st.RT(_getExpressionType(node.getExpression())))){
      TerminateProgram("Increment expression must be a numeric");

    }
  }

  public void caseAIfStmt(AIfStmt node){
    if(node.getOptStmt()!= null){
       if(node.getOptStmt() instanceof AShortVarDeclStatement){
         AShortVarDeclStatement stmt = (AShortVarDeclStatement)node.getOptStmt();
         for(int i=0; i<stmt.getIdentifiers().size(); i++){
           AIdentifierExpression id = (AIdentifierExpression) stmt.getIdentifiers().get(i);
           PExpression expr = stmt.getExpressions().get(i);
           String type_expr = _getExpressionType(expr);
           st.alwaysAddVar(id.getIdentifier().getText(), type_expr, type_expr);
         }
       } else {
         node.getOptStmt().apply(this);
       }
    }

    String type = _getExpressionType(node.getCondition());
    String base_type = st.RT(type);
    if(!safeTypeCompare( base_type, Constants.TYPE_BOOL)){
      TerminateProgram("If statement condition must be a boolean (current type:"+type+")");
    }

    node.getBlock().apply(this);
     if(node.getElse()!= null){
       node.getElse().apply(this);
     }
  }

  public void caseASwitchStmt(ASwitchStmt node){
    if(node.getOptStmt()!=null){
      if(node.getOptStmt() instanceof AShortVarDeclStatement){
        AShortVarDeclStatement stmt = (AShortVarDeclStatement)node.getOptStmt();
        for(int i=0; i<stmt.getIdentifiers().size(); i++){
          AIdentifierExpression id = (AIdentifierExpression) stmt.getIdentifiers().get(i);
          PExpression expr = stmt.getExpressions().get(i);
          String type_expr = _getExpressionType(expr);
          st.alwaysAddVar(id.getIdentifier().getText(), type_expr, type_expr);
        }
      } else {
        node.getOptStmt().apply(this);
      }
    }

    if(node.getCondition()!= null){
      String type = _getExpressionType(node.getCondition());
      for(PSwitchBody body: node.getBody()){
        if(body instanceof ACaseSwitchBody){
          ACaseSwitchBody temp = (ACaseSwitchBody) body;
          for(PExpression exp: temp.getExpressions()){
            String exp_type = _getExpressionType(exp);
            if(!safeTypeCompare(exp_type, type)){
              TerminateProgram("Switch main expression "+type+" do not correspond to "+exp_type);
            }
          }
          for(PStatement stmt: temp.getStatements()) {
            stmt.apply(this);
          }
        } else {
          ADefaultSwitchBody temp = (ADefaultSwitchBody) body;
          for(PStatement stmt: temp.getStatements()) {
            stmt.apply(this);
          }
        }
      }
    }
    else {
      for(PSwitchBody body: node.getBody()){
        if(body instanceof ACaseSwitchBody){
          ACaseSwitchBody temp = (ACaseSwitchBody) body;
          for(PExpression exp: temp.getExpressions()){
            String exp_type = _getExpressionType(exp);
            if(!safeTypeCompare( exp_type, Constants.TYPE_BOOL)){
              TerminateProgram("Switch expression "+exp_type+" is not a boolean");
            }
          }
          for(PStatement stmt: temp.getStatements()) {
            stmt.apply(this);
          }
        } else {
          ADefaultSwitchBody temp = (ADefaultSwitchBody) body;
          for(PStatement stmt: temp.getStatements()) {
            stmt.apply(this);
        }
      }
    }
  }
}

  public void caseAShortVarDeclStatement(AShortVarDeclStatement node){
    // check if lvalues are identfiers
    for(PExpression expr: node.getIdentifiers()){
      if(!(expr instanceof AIdentifierExpression)){
        TerminateProgram("Short Variable Declaration: lvalue must be an identifier");

      }
    }
    if(node.getIdentifiers().size()!= node.getExpressions().size()){
      TerminateProgram("Short Variable Declaration: number of identifiers do not match number of expressions");

    }
    int idNotAlreadyDeclared = 0;
    for(PExpression expr: node.getIdentifiers()){
      AIdentifierExpression id = (AIdentifierExpression) expr;
      if(!st.identifierExists(id.getIdentifier().getText())){
        idNotAlreadyDeclared++;
      }
    }
    if(idNotAlreadyDeclared == 0){
      TerminateProgram("Short Variable Declaration: at least one id must not be declared");

    }
    for(int i=0; i<node.getIdentifiers().size(); i++){
      AIdentifierExpression id = (AIdentifierExpression) node.getIdentifiers().get(i);
      PExpression expr = node.getExpressions().get(i);
      String type = _getExpressionType(expr);
      //String abstract_type = getAbstractType(type);
      if(!st.identifierExists(id.getIdentifier().getText())){
        st.addVar(id.getIdentifier().getText(), type, type);
      } else {
        if(!safeTypeCompare( st.getIdentifierType(id.getIdentifier().getText()), type)){
          TerminateProgram("Short Variable Declaration: identifier "+id.getIdentifier().getText()+" do not have type "+type);

        }
      }
    }

  }

    //    func_decl {-> func_dcl} =
    //    {rettype}   func  [funcid]:identifier l_par [arguments]:iden_type_pair? r_par [rettype]:type_t block semicolon? {-> New func_dcl(funcid, [arguments.argument], rettype.type_t, block)}|
     //   {norettype} func  [funcid]:identifier l_par [arguments]:iden_type_pair? r_par block semicolon? {-> New func_dcl(funcid, [arguments.argument], Null, block)};

  public void caseAFuncToplvlDcl(AFuncToplvlDcl node)
  {
      node.getFuncDcl().apply(this);
  }

    public void caseAFuncDcl(AFuncDcl node) {
        ArrayList<String> currParameterTypeList = new ArrayList<String>();
        FunctionDefinition currFuncDef = new FunctionDefinition();
        //Check that all variables in function signature are not already declared. ensure all types exist. Add to scope.
        LinkedList<PArgument> currArgs = node.getArguments();
        for (PArgument currArg : currArgs) {
            AArgument currArgAArg = (AArgument) currArg;
            PTypeT currType = currArgAArg.getType();
            if (currArgAArg.getType() instanceof AArrayTypeT)
            {
                AArrayTypeT currArray = (AArrayTypeT) currArgAArg.getType();
                currArray.getLength();
                String currVarString = "[" + currArray.getLength().getText().trim() + "]" + currArray.getType().toString();
                currVarString = currVarString.trim();
                currParameterTypeList.add(currVarString);
                String currName = currArgAArg.getName().toString().replace("[", "").replace("]", "").trim();
                mTemporaryFunctionParamList.add(new String[]  { currName, currVarString, currArray.getType().toString()});
                //st.addVar(currName, currVarString, currArray.getType().toString());
                this.mTL.put(currArgAArg.getName().toString(), currVarString);
            }
            else if (currArgAArg.getType() instanceof ASliceTypeT)
            {
                ASliceTypeT currArray = (ASliceTypeT) currArgAArg.getType();
                String currVarString = "[" + "]" + currArray.getType().toString();
                currVarString = currVarString.trim();
                currParameterTypeList.add(currVarString);
                mTemporaryFunctionParamList.add(new String[]  { currArgAArg.getName().toString().replace("[","").replace("]","").trim(), currVarString.toString(), getAbstractType(currType)});
               //st.addVar(currArgAArg.getName().toString(), currType.toString(), getAbstractType(currType));
            }
            else {
                for (TIdentifier id : currArgAArg.getName()) {
                    currParameterTypeList.add(currType.toString());
                    mTemporaryFunctionParamList.add(new String[]  {id.getText(), "func"+currType.toString(), getAbstractType(currType)});
                    //st.addVar(id.getText(), "func"+currType.toString(), getAbstractType(currType));
                }
            }
        }
        String[] currTypeList = new String[currParameterTypeList.size()];
        int iter = 0;
        for (String currType : currParameterTypeList) {
            currTypeList[iter] = currType;
            iter++;
        }
        currFuncDef.mParameterTypeList = currTypeList;
        //Add return type to function return type table.
        if (node.getReturnType() != null && !node.getReturnType().toString().isEmpty()) {
            if (node.getReturnType() instanceof ASliceTypeT)
            {
                currFuncDef.mReturnType = "[]" + node.getReturnType().toString();
            } else if (node.getReturnType() instanceof AArrayTypeT ){
                currFuncDef.mReturnType = "[]" + node.getReturnType().toString();
            } else {
                currFuncDef.mReturnType = node.getReturnType().toString();
            }
            currFuncDef.mHasReturn = true;
        }




        mFunctionTable.put(node.getName().toString(),currFuncDef);
        //System.out.println("===="+node.getName().toString().substring(0, node.getName().toString(). length()-1)+"====");
        st.addFunc(node.getName().toString().substring(0, node.getName().toString(). length()-1), "");
        node.getBlock().apply(this);
    }

    public String GetBasicType(String iType)
    {
        return st.GetBasicType(iType);
    }

    public void caseAFunccallExpression(AFunccallExpression node) {
        String currName = node.getName().toString();
        FunctionDefinition currFuncDef = mFunctionTable.get(currName);

        LinkedList<PExpression> currArgs = node.getArgs();

        boolean isCast = IsCast(node);

        if (isCast) {
            PExpression currSubNode = currArgs.get(0);
            String wSubNodeType = this._getExpressionType(currSubNode);
            CheckCast(st.GetBasicType(currName), st.GetBasicType(wSubNodeType));
        } else {
            int iter = 0;
            for (PExpression currArg : currArgs) {
                String lExprType = currFuncDef.mParameterTypeList[iter].toString();
                String rExprType = _getExpressionType(currArg);
                if (!safeTypeCompare(lExprType, rExprType)) {
                    TerminateProgram("Error: Function call contains incorrect type for parameter # " + iter + " , Should be type " + lExprType + " but is type " + rExprType);
                }
                iter++;
            }
        }
    }


    public boolean IsCast(AFunccallExpression node) {
        boolean isCast = false;
        LinkedList<PExpression> currArgs = node.getArgs();
        String currName = node.getName().toString();
        if (currArgs.size() == 1) {
            PExpression currSubNode = currArgs.get(0);
            String wSubNodeType = this._getExpressionType(currSubNode);

            if (st.isBasicTypeOrAlias(currName) && st.isBasicTypeOrAlias(wSubNodeType)) {
                isCast = true;
            }
            else if (st.typeExists(currName))
            {
                TerminateProgram("Error: Attempting to cast to non basic type");
            }
        }
        return isCast;
    }

    public void CheckCast(String iTypeA, String iTypeB)
    {
        if (this.safeTypeCompare(iTypeA, iTypeB))
        {
            return;
        } else if (this.safeTypeCompare(iTypeA, "string"))
        {
            TerminateProgram("Error: Cannot cast to or from string type");
        } else if (this.safeTypeCompare(iTypeB, "string"))
        {
            TerminateProgram("Error: Cannot cast to or from string type");
        }

    }

    public String GetCastReturnType(AFunccallExpression node) {

        String currName = node.getName().toString().trim();
        FunctionDefinition currFuncDef = mFunctionTable.get(currName);
        LinkedList<PExpression> currArgs = node.getArgs();
        boolean isCast = IsCast(node);
        if (isCast) {
            PExpression currSubNode = currArgs.get(0);
            String wSubNodeType = this._getExpressionType(currSubNode);
            CheckCast(st.GetBasicType(currName), st.GetBasicType(wSubNodeType));
            //return st.GetBasicType(currName);
            return currName;
        }
        return "";
    }

  @Override
    public void caseAReturnStatement(AReturnStatement node) {
        String currType = _getExpressionType(node.getExpression());
        Node currParent = node.parent();
        if (currParent != null && currParent instanceof ABlock) {
            Node currParentParent = currParent.parent();
            if (currParentParent != null && currParentParent instanceof AFuncDcl) {
                AFuncDcl curFuncDclParent = (AFuncDcl) currParentParent;
                String currFuncName = curFuncDclParent.getName().toString();
                if (this.mFunctionTable.containsKey(currFuncName)) {
                    String currFuncReturnType = this.mFunctionTable.get(currFuncName).mReturnType;
                    String cleanedType = currType.trim().replace("func","");
                    if (!this.safeTypeCompare(cleanedType, currFuncReturnType)) {
                        this.TerminateProgram("Error: Function does not return the correct type, function declaration specifies type: " + currFuncReturnType + " and the current type being returned is: " + currType);
                    }
                }
            }
        }
    }


    public void outABlock(ABlock node) {
        Stack<HashMap<String, SymbolTableEntry>> retVal = st.popTable(this.mDumpOnScopeExit, this.mDumpAllOnScopeExit);
        if (retVal != null) {
            OutputSymtable(retVal);
        }
    }

    public void outputSymtableEnd()
    {
        if (mDumpOnScopeExit || mDumpAllOnScopeExit )
        {
            OutputSymtable(this.st.GetTable());
        }
    }

    public void OutputSymtable(Stack<HashMap<String, SymbolTableEntry>> iTabToOutput) {
        String currSeperator = "\n\n\n\\\\ ==================SEPERATOR===================== \n\n\n";
        File currFile = new File(this.mFileName);
        try {
            boolean currNewfile = false;
            if (!currFile.exists())
            {
                currFile.createNewFile();
                currNewfile = true;
            }
            FileOutputStream currFileStream = new FileOutputStream(currFile, true);
            PrintWriter currPrintWriter = new PrintWriter ( currFileStream ) ;
            if (currNewfile)
            {
                currPrintWriter.println("\\\\Please note, default file types are automatically pushed to the top level stack, so they will appear after the last scope exit (file end)." + currSeperator);
            }
            for (HashMap<String, SymbolTableEntry> currEntry : iTabToOutput)
            {
                Set<Entry<String, SymbolTableEntry>> currEntrySet = currEntry.entrySet();
                for ( Entry<String,SymbolTableEntry> singleEntry : currEntrySet)
                {
                    currPrintWriter.print(singleEntry.getKey() + " " + singleEntry.getValue() + "\n");
                }
            }
            currPrintWriter.append(currSeperator);
            currPrintWriter.flush();
            currPrintWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(TypeChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

  public void caseAExpressionStatement(AExpressionStatement node){
      node.getExpression().apply(this);
    //TerminateProgram(getExpressionType(node)+" ");
  }

  public String getExpressionType(AExpressionStatement node){
    return _getExpressionType(node.getExpression());
  }

  private String _getExpressionType(PExpression expression){
    if(expression instanceof AIntExpression){
      return Constants.TYPE_INT;
    } else if(expression instanceof AFloatExpression){
      return Constants.TYPE_FLOAT;
    } else if(expression instanceof ARuneExpression){
      return Constants.TYPE_RUNE;
    } else if(expression instanceof AStringExpression){
      return Constants.TYPE_STRING;
    } else if(expression instanceof AIdentifierExpression){
      AIdentifierExpression node = (AIdentifierExpression) expression;
      // Check if identifier exists
      return st.getIdentifierType(node.getIdentifier().getText());
    } else if (expression instanceof AParExprExpression){
      AParExprExpression node =(AParExprExpression) expression;
      return _getExpressionType(node.getExpression());
    } else if (expression instanceof AUnaryExpression){
      AUnaryExpression node = (AUnaryExpression)expression;
      if(!isValidUnaryExpr(node)){
        TerminateProgram("Unary expression is not valid");

        }
        return _getExpressionType(node.getExpression());
    } else if (expression instanceof AArrayAccessExpression) {
        AArrayAccessExpression node = (AArrayAccessExpression) expression;
        String type = _getExpressionType(node.getName());
        type = type.replace("func","").trim();
        String rtType = st.RT(type);
        if ((!isArrayType(rtType)) && (!isSliceType(rtType))) {
            TerminateProgram("ArrayType: " + type + " " + "Type " + type + " is not an array or a slice");
            //return type;
        }
        if (node.getValue() != null) {
            if (!safeTypeCompare(st.RT(_getExpressionType(node.getValue())), Constants.TYPE_INT)) {
                TerminateProgram("ArrayType: " + type + " " + "Type " + _getExpressionType(node.getValue()) + " is not an integer. Index must be an integer.");
            }
        }
        return rtType.replaceFirst("(\\[([0-9]*)\\])", "");
    } else if (expression instanceof ABinaryExpression) {
        ABinaryExpression node = (ABinaryExpression) expression;
        if (!isValidBinaryExpr(node)) {
            TerminateProgram("Binary expression is not valid");

        }
        return getBinaryExprType(node);
    } else if (expression instanceof AAppendExpression) {
        AAppendExpression node = (AAppendExpression)expression;
      String toType = _getExpressionType(node.getTo());
      String rtType = st.RT(toType);
      if(!isSliceType(rtType)){
        TerminateProgram("Append expression: Type "+toType+" is not a slice");

      }
      String valueType = _getExpressionType(node.getValue());
      if(!safeTypeCompare( valueType, rtType.replaceAll("(\\[\\])",""))){
        TerminateProgram("Append expression: Type "+valueType+" is not equal to "+toType);

      }
      return toType;
    } else if(expression instanceof ASelectExpression){
      ASelectExpression node = (ASelectExpression)expression;
      String type = _getExpressionType(node.getLvalue());
      //String type = _getExpressionType(node.getLvalue());
      String id = node.getRvalue().getText();
      if (!st.typeIsStruct(type)){
        TerminateProgram("Type "+type+" is not a struct");

      }
      if(node.getLvalue() instanceof AIdentifierExpression){
        AIdentifierExpression identifier = (AIdentifierExpression)node.getLvalue();
        String test = st.getStructIdTypeWithIdentifier(identifier.getIdentifier().getText(), id);
        if(!test.equals("")){
          return test;
        }
      }
      return st.getStructIdType(type, id);
    } else if (expression instanceof AFunccallExpression)
    {
        AFunccallExpression funcNode = (AFunccallExpression) expression;
        if ( this.IsCast(funcNode))
        {
            return this.GetCastReturnType(funcNode);
        }
        else {
            return this.mFunctionTable.get(funcNode.getName().toString()).mReturnType;
        }
    }

    return "";
  }


  private Boolean isValidUnaryExpr(AUnaryExpression node){
    if(node.getOp() instanceof APlusUnaryOperator || node.getOp() instanceof AMinusUnaryOperator){
      return isNumeric(st.RT(_getExpressionType(node.getExpression())));
    } else if(node.getOp() instanceof ANotUnaryOperator) {
      return safeTypeCompare( st.RT(_getExpressionType(node.getExpression())), Constants.TYPE_BOOL);
    } else {
      return isInteger(st.RT(_getExpressionType(node.getExpression())));
    }
  }

  private Boolean isValidBinaryExpr(ABinaryExpression node){
      String rNode = _getExpressionType(node.getR()).trim().replace("func","");
      String lNode = _getExpressionType(node.getL()).trim().replace("func","");
    if(node.getOp() instanceof ALogicOrBinaryOperator || node.getOp() instanceof ALogicAndBinaryOperator){
      return safeTypeCompare( lNode, rNode) && safeTypeCompare( st.RT(rNode), Constants.TYPE_BOOL);
    } else if(node.getOp() instanceof ALogicEqualBinaryOperator || node.getOp() instanceof ALogicNotEqualBinaryOperator) {
      String typeR = rNode;
      String typeL = lNode;
      if(isSliceType(typeR) || isSliceType(typeL)){
        return false;
      }
      return isComparable(st.RT(lNode)) && safeTypeCompare( lNode, rNode);
    } else if (node.getOp() instanceof ALogicLessBinaryOperator || node.getOp() instanceof ALogicGreaterBinaryOperator ||
               node.getOp() instanceof ALogicEqualBinaryOperator || node.getOp() instanceof ALogicLessEqualBinaryOperator || node.getOp() instanceof ALogicGreaterEqualBinaryOperator ){
      return isOrdered(st.RT(lNode)) && safeTypeCompare( lNode, rNode);
    } else if(node.getOp() instanceof APlusBinaryOperator ){
        return isNumericOrString(st.RT(lNode)) && (safeTypeCompare(st.RT(lNode), st.RT(rNode)) || (safeTypeCompare(lNode, "float64") && safeTypeCompare(rNode, "int")));
    } else if(node.getOp() instanceof AMinusBinaryOperator || node.getOp() instanceof AMultBinaryOperator || node.getOp() instanceof ADivdBinaryOperator){
      return isNumeric(st.RT(lNode)) && safeTypeCompare( lNode, rNode);
    } else {
      return isInteger(st.RT(lNode)) && safeTypeCompare( lNode, rNode);
    }
  }

  private String getBinaryExprType(ABinaryExpression node){
    if(node.getOp() instanceof ALogicOrBinaryOperator || node.getOp() instanceof ALogicAndBinaryOperator){
      return Constants.TYPE_BOOL;
    } else if(node.getOp() instanceof ALogicEqualBinaryOperator || node.getOp() instanceof ALogicNotEqualBinaryOperator) {
      return Constants.TYPE_BOOL;
    } else if (node.getOp() instanceof ALogicLessBinaryOperator || node.getOp() instanceof ALogicGreaterBinaryOperator ||
               node.getOp() instanceof ALogicEqualBinaryOperator || node.getOp() instanceof ALogicLessEqualBinaryOperator || node.getOp() instanceof ALogicGreaterEqualBinaryOperator ){
      return Constants.TYPE_BOOL;
    } else {
      return _getExpressionType(node.getL());
    }
  }

  private Boolean isNumeric(String type){
    return safeTypeCompare( type, Constants.TYPE_INT) || safeTypeCompare( type, Constants.TYPE_FLOAT) || safeTypeCompare(type , Constants.TYPE_RUNE);
  }
  private Boolean isInteger(String type){
    return safeTypeCompare( type, Constants.TYPE_INT) || safeTypeCompare( type, Constants.TYPE_RUNE);
  }
  private Boolean isComparable(String type){
    if(isArrayType(type)){
      type = type.replaceAll("(\\[(.*)\\])","");
    }
    if(isStructType(type)){
      String[] types = type.split(",");
      for (String i: types){
        if(!(safeTypeCompare( i, "")) && !isComparable(i)){
          return false;
        }
      }
      return true;
    }
    return safeTypeCompare( type , Constants.TYPE_INT) || safeTypeCompare( type , Constants.TYPE_FLOAT) || safeTypeCompare( type , Constants.TYPE_STRING) || safeTypeCompare( type , Constants.TYPE_RUNE) || safeTypeCompare( type , Constants.TYPE_BOOL);
  }
  private Boolean isOrdered(String type){
    return safeTypeCompare( type , Constants.TYPE_INT) || safeTypeCompare( type , Constants.TYPE_FLOAT) || safeTypeCompare( type , Constants.TYPE_STRING) || safeTypeCompare( type , Constants.TYPE_RUNE);
  }
  private Boolean isNumericOrString(String type){
    return isNumeric(type) || safeTypeCompare( type , Constants.TYPE_STRING);
  }
  private Boolean isArrayType(String type){
    return type.matches("(\\[([0-9])+\\])+(.*)");
  }
  private Boolean isSliceType(String type){
    return type.matches("(\\[\\])+(.*)");
  }
  private Boolean isStructType(String type){
    return type.contains(",");
  }
  private Boolean isBaseType(String type){
    return safeTypeCompare( type , Constants.TYPE_INT) || safeTypeCompare( type , Constants.TYPE_FLOAT) || safeTypeCompare( type , Constants.TYPE_STRING) || safeTypeCompare( type , Constants.TYPE_RUNE) || safeTypeCompare( type , Constants.TYPE_BOOL);
  }
  private String typeToString(PTypeT node){
    String result = "";
    result = _typeToString(node, result);
    return result;
  }
  private String _typeToString(PTypeT node, String result){
    if(node instanceof ANameTypeT){
      ANameTypeT _node = (ANameTypeT)node;
      result = result+ _node.getIdentifier().getText();
    } else if(node instanceof ASliceTypeT){
      ASliceTypeT _node = (ASliceTypeT)node;
      result = result+"[]";
      result = _typeToString(_node.getType(), result);
    } else if(node instanceof AArrayTypeT){
      AArrayTypeT _node = (AArrayTypeT)node;
      result = result+"["+_node.getLength().getText()+"]";
      result = _typeToString(_node.getType(), result);
    } else {
      HashMap<String, SymbolTableEntry> struct = new HashMap<String, SymbolTableEntry>();
      String resultType = "";
      String resultAbstractType = "";
      AStructTypeT typeT = (AStructTypeT)node;
      for(PFieldDcl field: typeT.getFieldDcl()){
        AFieldDcl temp = (AFieldDcl) field;
        for(TIdentifier id: temp.getIdentifiers()){
          result = resultType+","+typeToString(temp.getTypeT());
          resultAbstractType = resultAbstractType+","+getAbstractType(temp.getTypeT());
          if(struct.containsKey(id.getText())){
            TerminateProgram("Identifier "+id.getText()+" is already used in struct ");
          }
          //struct.put(id.getText(), new SymbolTableEntry(id.getText(), Constants.CATEGORY_VAR, typeToString(temp.getTypeT()), getAbstractType(temp.getTypeT()) ));
        }
      }
      //st.addTypeStruct(node.getName().getText(), resultType, resultAbstractType, struct);
    }
    return result;
  }

  private void addAStruct(String identifier, PTypeT type){
    HashMap<String, SymbolTableEntry> struct = new HashMap<String, SymbolTableEntry>();
    String resultType = "";
    String resultAbstractType = "";
    AStructTypeT typeT = (AStructTypeT)type;
    for(PFieldDcl field: typeT.getFieldDcl()){
      AFieldDcl temp = (AFieldDcl) field;
      for(TIdentifier id: temp.getIdentifiers()){
        resultType = resultType+","+typeToString(temp.getTypeT());
        resultAbstractType = resultAbstractType+","+getAbstractType(temp.getTypeT());
        if(struct.containsKey(id.getText())){
          TerminateProgram("Identifier "+id.getText()+" is already used in struct ");
        }
        struct.put(id.getText(), new SymbolTableEntry(id.getText(), Constants.CATEGORY_VAR, typeToString(temp.getTypeT()), getAbstractType(temp.getTypeT()) ));
      }
    }
    //st.addTypeStruct("struct_"+Integer.toString(struct_number), resultType, resultAbstractType, struct);
    st.addVarStruct(identifier, resultType, resultAbstractType, struct);
  }
  private String getAbstractType(PTypeT node){
    String result = "";
    if(node instanceof ANameTypeT){
      ANameTypeT _node = (ANameTypeT)node;
      result =  _node.getIdentifier().getText();
    } else if(node instanceof ASliceTypeT){
      ASliceTypeT _node = (ASliceTypeT)node;
      result = getAbstractType(_node.getType());
    } else if(node instanceof AArrayTypeT){
      AArrayTypeT _node = (AArrayTypeT)node;
      result = getAbstractType(_node.getType());
    } else if(node instanceof AStructTypeT) {
      //Not here
    } else {
      TerminateProgram("Houston, we have a problem");

    }
    return result;
  }

  public SymbolTable getSymbolTable(){
    return st;
  }

}
