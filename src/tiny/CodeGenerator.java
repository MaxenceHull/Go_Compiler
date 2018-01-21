/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tiny;

import java.io.IOException;
import java.io.PrintWriter;
import tiny.analysis.DepthFirstAdapter;
import tiny.node.*;
import java.util.*;
import java.util.regex.*;

public class CodeGenerator extends DepthFirstAdapter {

    public int level = 0;
    public String pretty = "";
    public TypeChecker mTC;
    public Boolean isElifStmt = false;
    private int st_level = 0;
    private String[] fake_names = {"yolo1", "yolo2", "yolo3", "yolo4", "yolo5"};
    private int fake_name_id = 0;


    public static void print(Node node, String fileName) {
        node.apply(new PrettyPrinter());
    }

    CodeGenerator(TypeChecker iTC) {
        mTC = iTC;
    }


    public void createFile(String fileName) {
        FilterFile();
        try {
            PrintWriter writer = new PrintWriter(fileName + ".py", "UTF-8");
            writer.print("import sys\n");
            writer.print(pretty);
            //should we leave this?
            writer.print("main()");
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
        }
    }



    private void puts(String s) {
        if (this.pretty.endsWith("\n")) {
            for (int i = 0; i < level; i++) {
                pretty += "\t";
            }
        }
        else if (s.startsWith("\n")) {
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
        }
    }

    public void caseATypeToplvlDcl(ATypeToplvlDcl node) {
        if (node.getTypeDclList() != null) {
            node.getTypeDclList().apply(this);
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
        puts("\n" + "def " + node.getName().getText() + " ( ");
        if (node.getArguments() != null) {
            for(PArgument currArg : node.getArguments())
            {
                currArg.apply(this);
            }
        }
        puts(" ) : \n");
        node.getBlock().apply(this);
        //puts(";");
        puts("\n");
    }

    public void caseAArgument(AArgument node) {
        char last = FindLastNonNullChar();
        boolean notFirstName = true;
        if (last == 0 || last != '(')
        {
            notFirstName = false;
        }
        for (TIdentifier currName : node.getName()) {
            if (!notFirstName) {
                puts(", " + currName.toString());
            } else {
                puts(currName.toString());
                notFirstName = false;
            }
        }

    }

    public char FindLastNonNullChar()
    {
        int currSize = this.pretty.length();
        for ( int i = currSize-1 ; i > 0 ; i--)
        {
            if (this.pretty.charAt(i) != '\n' && this.pretty.charAt(i) != '\r' && this.pretty.charAt(i) != ' ')
            {
                return this.pretty.charAt(i);
            }
        }
        return 0;
    }

    public void caseAVarDcl(AVarDcl node){
        //CASE 1: No explicit type init
        if(node.getType() == null){
            for (int i=0; i<node.getNames().size(); i++){
                TIdentifier currName = node.getNames().get(i);
                puts(currName.getText().toString());
                if (node.getExpressions() != null && node.getExpressions().size() > 0){
                    puts(" = ");
                    PExpression currExpr = node.getExpressions().get(i);
                    currExpr.apply(this);
                    puts("\n");
                }
            }
        }
        // CASE 2 : Explicit type
        else {
            PTypeT currType = node.getType();
            // CASE 2.1: Array
            if(currType instanceof AArrayTypeT){
                instanciateNewArray(currType, node);
            }
            // CASE 2.2: Slice
            else if(currType instanceof ASliceTypeT){
                ASliceTypeT type = (ASliceTypeT) currType;
                puts(instanciateNewSlice(type, node));
            }
            //CASE 2.3: Struct
            else if (currType instanceof AStructTypeT){
                String struct_name = instaciateNewStruct((AStructTypeT) currType, "", node);
                for (int i=0; i<node.getNames().size(); i++){
                    TIdentifier currName = node.getNames().get(i);
                    puts(currName.getText().toString()+" = "+struct_name+"()\n");
                }
            }
            //CASE 2.4 : Identifier
            else {
                ANameTypeT name_type = (ANameTypeT) currType;
                int currHashCode = TypeChecker.GetHashCodeForParent(node);
                String string_type = mTC.st.RT(name_type.getIdentifier().getText(), currHashCode);
                if(mTC.st.isArrayType(string_type)){
                    for (int i=0; i<node.getNames().size(); i++){
                        TIdentifier currName = node.getNames().get(i);
                        puts(currName.getText().toString()+" = ");
                        instanciateNewArray(string_type, currName.getText().toString());
                    }
                }else if(mTC.st.isSliceType(string_type)){
                    String initial_type = string_type;
                    for (int i=0; i<node.getNames().size(); i++){
                        string_type = initial_type;
                        TIdentifier currName = node.getNames().get(i);
                        puts(currName.getText().toString());
                        String temp = "";
                        while(mTC.st.isSliceType(string_type)){
                            temp = "[" + temp + "]";
                            string_type = string_type.replaceFirst("\\[\\]", "");
                        }
                        puts(" = "+temp+"\n");
                    }
                }else if(mTC.st.isStructType(string_type)){
                    for (int i=0; i<node.getNames().size(); i++){
                        TIdentifier currName = node.getNames().get(i);
                        puts(currName.getText().toString()+" = "+name_type.getIdentifier().getText()+"()\n");
                    }
                }else {
                    for (int i=0; i<node.getNames().size(); i++){
                        TIdentifier currName = node.getNames().get(i);
                        puts(currName.getText().toString());
                        if (node.getExpressions() != null && node.getExpressions().size() > 0){
                            puts(" = ");
                            PExpression currExpr = node.getExpressions().get(i);
                            currExpr.apply(this);
                            puts("\n");
                        } else {
                            puts(" = "+getInitialDcl(string_type)+"\n");
                        }
                    }
                }
            }
        }
    }




    private String generateFakeStructName(){
      fake_name_id++;
      return "temporary_"+Integer.toString(fake_name_id);
    }

    public String instaciateNewStruct(AStructTypeT structType, String name, Node node){
        String struct_name = "";
        if (name.equals("")){
            struct_name =  generateFakeStructName();
        }else {
            struct_name = name;
        }
        String buffer = "class "+struct_name+" :\n";
        buffer = buffer+"\tdef __init__(self):\n";
        for(PFieldDcl field: structType.getFieldDcl()){
            AFieldDcl field_dcl = (AFieldDcl)field;
            PTypeT currType = field_dcl.getTypeT();
            for(TIdentifier identifier: field_dcl.getIdentifiers()){
                if(currType instanceof AArrayTypeT){
                    buffer = buffer + instanciateNewArray(currType, identifier.getText(), node);
                }
                // CASE 2.2: Slice
                else if(currType instanceof ASliceTypeT){
                    ASliceTypeT type = (ASliceTypeT) currType;
                    buffer = buffer+"\t\t"+ "self."+identifier.getText()+" = ";
                    String temp = "";
                    while(currType instanceof ASliceTypeT){
                        type = (ASliceTypeT) currType;
                        currType = type.getType();
                        temp = "[" + temp + "]";
                    }
                    buffer = buffer + temp + "\n";
                }
                //CASE 2.3: Struct
                else if (currType instanceof AStructTypeT){
                  //  System.out.println("No struct inside a struct.");
                  //  System.exit(1);
                }
                //CASE 2.4 : Identifier
                else {
                    ANameTypeT name_type = (ANameTypeT) currType;
                    int currHashCode = TypeChecker.GetHashCodeForParent(node);
                    String string_type = mTC.st.RT(name_type.getIdentifier().getText(), currHashCode);
                    if(mTC.st.isArrayType(string_type)){
                        instanciateNewArray(string_type, name_type.getIdentifier().getText());
                        //TODO
                    }else if(mTC.st.isSliceType(string_type)){
                        buffer = buffer+"\t\tself."+identifier.getText();
                        String temp = "";
                        while(mTC.st.isSliceType(string_type)){
                            temp = "[" + temp + "]";
                            string_type = string_type.replaceFirst("\\[\\]", "");
                        }
                        //puts(" = "+temp+"\n");
                        buffer = buffer + " = "+temp+"\n";
                    }else if(mTC.st.isStructType(string_type)){
                    //    System.out.println("No struct inside a struct.");
                     //   System.exit(1);
                    }else {
                        buffer = buffer+"\t\tself."+identifier.getText()+" = "+getInitialDcl(string_type)+"\n";
                    }
                }
            }
        }
        // Make a comparator
        buffer = buffer+"\tdef __eq__(self, other):\n\t\treturn ((";
        for(PFieldDcl field: structType.getFieldDcl()){
            AFieldDcl field_dcl = (AFieldDcl)field;
            PTypeT currType = field_dcl.getTypeT();
            for(TIdentifier identifier: field_dcl.getIdentifiers()){
                buffer = buffer + "self."+identifier.getText()+", ";
            }
        }
        buffer = buffer + ")) == ((";
        for(PFieldDcl field: structType.getFieldDcl()){
            AFieldDcl field_dcl = (AFieldDcl)field;
            PTypeT currType = field_dcl.getTypeT();
            for(TIdentifier identifier: field_dcl.getIdentifiers()){
                buffer = buffer + "other."+identifier.getText()+", ";
            }
        }
        buffer = buffer + "))\n";
        //pretty = buffer +"\n"+ pretty;

        // Make a comparator
        buffer = buffer+"\tdef __ne__(self, other):\n\t\treturn ((";
        for(PFieldDcl field: structType.getFieldDcl()){
            AFieldDcl field_dcl = (AFieldDcl)field;
            PTypeT currType = field_dcl.getTypeT();
            for(TIdentifier identifier: field_dcl.getIdentifiers()){
                buffer = buffer + "self."+identifier.getText()+", ";
            }
        }
        buffer = buffer + ")) != ((";
        for(PFieldDcl field: structType.getFieldDcl()){
            AFieldDcl field_dcl = (AFieldDcl)field;
            PTypeT currType = field_dcl.getTypeT();
            for(TIdentifier identifier: field_dcl.getIdentifiers()){
                buffer = buffer + "other."+identifier.getText()+", ";
            }
        }
        buffer = buffer + "))";
        pretty = buffer +"\n"+ pretty;
        return struct_name;
    }

    public String instanciateNewSlice(ASliceTypeT currType, AVarDcl node){
      String result = "";
      for (int i=0; i<node.getNames().size(); i++){
          TIdentifier currName = node.getNames().get(i);
          result = result + currName.getText().toString()+" = ";
          //puts(currName.getText().toString());
          //puts(" = []");
          String temp = "";
          PTypeT type = currType;
          while(type instanceof ASliceTypeT){
              currType = (ASliceTypeT) type;
              type = currType.getType();
              temp = "[" + temp + "]";
          }
          result = result + temp + "\n";
          for(int j = 0; j<level; j++){
              result = result + "\t";
          }
          if(type instanceof AStructTypeT){
              instaciateNewStruct((AStructTypeT) type, "", node);
          }
      }

      return result;
    }

    public void instanciateNewArray(PTypeT currType, AVarDcl node){
        PTypeT initialType = currType;
        //String result = "";
        for (int i=0; i<node.getNames().size(); i++){
            currType = initialType;
            TIdentifier currName = node.getNames().get(i);
            //result = result + currName.getText().toString() + " = ";
            puts(currName.getText().toString());
            puts(" = ");
            AArrayTypeT type = (AArrayTypeT) currType;
            Vector<String> sizes = new Vector<String>();
            while(currType instanceof AArrayTypeT) {
                type = (AArrayTypeT) currType;
                sizes.add(type.getLength().getText());
                currType = type.getType();
            }
            ANameTypeT name_type = (ANameTypeT) currType;
            //int currHashCode = TypeChecker.GetHashCodeForParent(node);
                //String string_type = mTC.st.RT(name_type.getIdentifier().getText(), currHashCode);
                //String init_value = getInitialDcl(string_type);
            puts(sizes.get(0)+"*[0]\n");
                //result = result + "[";
                //result = result + "]\n";

            for(int j=1; j<sizes.size(); j++){
                puts(fake_names[j-1]+" = [");
                  //result = result + fake_names[j-1]+" = [";
                for(int k=0; k<Integer.parseInt(sizes.get(j)); k++){
                    puts("0,");
                      //result = result + "0,";
                }
                puts("]\n");
                  //result = result + "]\n";
            }
            for(int j=1; j<sizes.size(); j++){
                  puts("for iterator_i in range("+sizes.get(j-1)+"):\n");
                    //result = result + "for iterator_i in range("+sizes.get(j)+"):\n";
                  if(j == 1){
                      puts("  "+currName.getText().toString()+"[iterator_i]="+fake_names[0]+"\n");
                        //result = result + "  "+currName.getText().toString()+"[iterator_i]="+fake_names[0]+"\n";
                  } else {
                      puts("  "+fake_names[j-2]+"[iterator_i]="+fake_names[j-1]+"\n");
                        //result = result + "  "+fake_names[j-2]+"[iterator_i]="+fake_names[j-1]+"\n";
                  }

            }

            if(currType instanceof AStructTypeT) {
                instaciateNewStruct((AStructTypeT) currType, "", node);
            }

        }
    }

    public void instanciateNewArray(String currType, String currName){
        ArrayList<String> sizes = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\[([0-9])+\\]");
        Matcher matcher = pattern.matcher(currType);
        while (matcher.find()) {
            String result = matcher.group();
            result = result.replaceAll("\\[", "");
            sizes.add(result.replaceAll("\\]", ""));
        }
        puts(sizes.get(0)+"*[0]\n");
        for(int j=1; j<sizes.size(); j++){
            puts(fake_names[j-1]+" = [");
            for(int k=0; k<Integer.parseInt(sizes.get(j)); k++){
                puts("0,");
            }
            puts("]\n");
        }
        for(int j=1; j<sizes.size(); j++){
            puts("for iterator_i in range("+sizes.get(j-1)+"):\n");
            if(j == 1){
                puts("  "+currName+"[iterator_i]="+fake_names[0]+"\n");
            } else {
                puts("  "+fake_names[j-2]+"[iterator_i]="+fake_names[j-1]+"\n");
            }

        }
    }

    public String instanciateNewArray(PTypeT currType, String name, Node node){
        //Array in a struct
        String result = "\t\t";
        result = result +"self."+ name + " = ";
        AArrayTypeT type = (AArrayTypeT) currType;
        Vector<String> sizes = new Vector<String>();
        while(currType instanceof AArrayTypeT) {
            type = (AArrayTypeT) currType;
            sizes.add(type.getLength().getText());
            currType = type.getType();
        }
            ANameTypeT name_type = (ANameTypeT) currType;
            int currHashCode = TypeChecker.GetHashCodeForParent(node);
            String string_type = mTC.st.RT(name_type.getIdentifier().getText(), currHashCode);
            String init_value = getInitialDcl(string_type);
            result = result + "[";
            for (int j=0; j<Integer.parseInt(sizes.get(0)); j++){
                result = result + "0, ";
            }
            result = result + "]\n";
            for(int j=1; j<sizes.size(); j++){
                result = result + "\t\t"+fake_names[j-1]+" = [";
                for(int k=0; k<Integer.parseInt(sizes.get(j)); k++){
                      result = result + "0,";
                }
                result = result + "]\n";
            }
            for(int j=1; j<sizes.size(); j++){
                result = result + "\t\tfor iterator_i in range("+sizes.get(j-1)+"):\n";
                if(j == 1){
                    result = result + "\t\t\t  self."+name+"[iterator_i]="+fake_names[0]+"\n";
                } else {
                      result = result + "\t\t\t  "+fake_names[j-2]+"[iterator_i]="+fake_names[j-1]+"\n";
                }
            }
        if(currType instanceof AStructTypeT) {
            instaciateNewStruct((AStructTypeT) currType, "", node);
        }
        return result;
    }


    private String getInitialDcl(String initialType){
      if (initialType.equals("float64")) {
          return "0.0";
      } else if (initialType.equals("int")) {
          return "0";
      } else if (initialType.equals("rune")) {
          return "0";
      } else if (initialType.equals("string")) {
          return "\"\"";
      } else if (initialType.equals("bool")) {
          return "False";
      } else {
          System.out.println("Problem!");
          System.exit(1);
      }
      return "";
    }

    public void caseATypeDcl(ATypeDcl node){
      //CASE 1 : Array Type
      if(node.getType() instanceof AArrayTypeT){
      }
      // CASE 2.2: Slice
      else if(node.getType() instanceof ASliceTypeT){
      }
      //CASE 2.3: Struct
      else if (node.getType() instanceof AStructTypeT){
          instaciateNewStruct((AStructTypeT)node.getType(), node.getName().getText(), node);
      }
      //CASE 2.4 : Identifier
      else {
          //TODO identifier referes to a struct
          //int currHashCode = TypeChecker.GetHashCodeForParent(node);
          //String string_type = mTC.st.RT(name_type.getIdentifier().getText(), currHashCode);
          //if(mTC.st.isStructType(string_type)){
          //    instaciateNewStruct(AStructTypeT structType, String name, Node node)
          //}
      }
    }


    public void caseAPackageDcl(APackageDcl node)
    {
        //not too sure what to do with package...
        //puts("package " + node.getName().getText() + " " + ";\n");
    }

    /*operands*/
    public void caseARuneExpression(ARuneExpression node) {
        int result = (int)node.getRune().getText().charAt(0);
        puts(Integer.toString(result));
    }

    public void caseAStringExpression(AStringExpression node) {
        String wVal = node.getString().getText();
        if (wVal.charAt(0) == '`') // raw string
        {
            wVal = wVal.replace("\\", "\\\\");
            wVal = wVal.replace("\"", "\\\"");
            wVal = wVal.replace("`", "\"");
        }
        puts(wVal);
    }

    public void caseAFloatExpression(AFloatExpression node) {
        puts(node.getFloat().getText());
    }

    public void caseAIntExpression(AIntExpression node) {
        String wVal = node.getInteger().getText();
        if (wVal.charAt(0) == '0' && wVal.length()> 1)
        {
            int wDecVal = Integer.parseInt(wVal, 8);
            wVal = wDecVal + "";
        }
        puts(wVal);
    }

    public void caseAIdentifierExpression(AIdentifierExpression node) {
        String wVal = node.getIdentifier().getText();
        if (wVal.equals("true"))
        {
            wVal = "True";
        }
        else if (wVal.equals("false"))
        {
            wVal = "False";
        }
        puts(wVal);
    }

    public void caseASelectExpression(ASelectExpression node){
        node.getLvalue().apply(this);
        puts("."+node.getRvalue().getText());
    }
    public void caseAArrayAccessExpression(AArrayAccessExpression node){
        node.getName().apply(this);
        puts("[");
        node.getValue().apply(this);
        puts("]");
    }


    /*unary_op*/
    public void caseAPlusUnaryOperator(APlusUnaryOperator node) {
        puts(node.getPlus().getText());
    }

    public void caseAMinusUnaryOperator(AMinusUnaryOperator node) {
        puts(node.getMinus().getText());
    }

    public void caseANoptUnaryOperator(ANotUnaryOperator node) {
        //puts(node.getExclamation().getText());
        puts(" not ");
    }

    public void caseAUnaryXorUnaryOperator(AUnaryXorUnaryOperator node) {
       //puts(node.getCaret().getText());
       puts(" ~");
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
        //puts(node.getAmpersandCaret().getText());
        puts(" ^ ~ ");
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

    public void caseALogicAndBinaryOperator(ALogicAndBinaryOperator node){
        puts(" and ");
    }

    public void caseALogicOrBinaryOperator(ALogicOrBinaryOperator node){
        puts(" or ");
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


    /*block*/
    public void caseABlock(ABlock node) {
        this.st_level++;
        puts("\n");
        this.level++;
        for (PStatement stmt : node.getStatements()) {
            stmt.apply(this);
            puts ("\n");
        }
        this.level--;
        if (!this.pretty.endsWith("\n")) {
            puts("\n");
        }
        puts("\n");
        st_level--;
    }

    public void caseABinaryExpression(ABinaryExpression node) {
        node.getL().apply(this);
        node.getOp().apply(this);
        node.getR().apply(this);
    }

    public void caseAParExprExpression(AParExprExpression node){
        puts("(");
        node.getExpression().apply(this);
        puts(")");
    }

    public void caseAUnaryExpression(AUnaryExpression node){
        node.getOp().apply(this);
        node.getExpression().apply(this);
    }

    public void caseAFunccallExpression(AFunccallExpression node){
        if (node.getName().toString().toUpperCase().trim().equals("FLOAT64"))
        {
            puts ("float");
        }
        else {
            node.getName().apply(this);
        }
        puts("(");
        int iter = 0;

        for (PExpression arg : node.getArgs()) {
            iter++;
            arg.apply(this);
            if (iter < node.getArgs().size()) {
                puts(" , ");
          }
        }
        puts(")");
    }

    public void caseAIfStmt(AIfStmt node){
        if(node.getOptStmt() != null){
          node.getOptStmt().apply(this);
          puts("\n");
        }
        if(this.isElifStmt){
          puts("elif ");
          this.isElifStmt = false;
        } else {
          puts("if ");
        }

        node.getCondition().apply(this);
        puts(" : \n");
        node.getBlock().apply(this);
        if(node.getElse() != null){
          node.getElse().apply(this);
        }
        puts("\n");
    }

    public void caseAElifElseStmt(AElifElseStmt node){
        this.isElifStmt = true;
        node.getIfStmt().apply(this);
    }

    public void caseAElseElseStmt(AElseElseStmt node){
        puts("else:\n");
        node.getBlock().apply(this);
    }

    public void caseAForStmt(AForStmt node){
        //Infinite loop
        boolean hasPoststmt = false;
        if(node.getInitStmt()==null && node.getCondition()==null && node.getPostStmt()==null){
            puts("while True:\n");
        } else if (node.getInitStmt()==null && node.getPostStmt()==null){
            puts("while ");
            node.getCondition().apply(this);
            puts(" :\n");
        } else {
            if (node.getInitStmt() != null)
            {
                node.getInitStmt().apply(this);
            }
            puts("while ");
            if (node.getCondition() != null)
            {
                node.getCondition().apply(this);
            }
            puts(" :\n");

            if (node.getPostStmt() != null)
            {
                hasPoststmt = true;
            }
        }
        node.getBlock().apply(this);
        if (hasPoststmt) {
            puts("\t");
            node.getPostStmt().apply(this);
        }
    }

    public void caseASwitchStmt(ASwitchStmt node){
        puts("for _ in range(1):\n");
        level++;
        if(node.getOptStmt()!=null){
            node.getOptStmt().apply(this);
            puts("\n");
        }
        Boolean hasCondition = false;
        PExpression condition = null;
        if(node.getCondition()!= null){
            hasCondition = true;
            condition = node.getCondition();
        }
        for(int i=0; i<node.getBody().size(); i++){
            if(node.getBody().get(i) instanceof ACaseSwitchBody){
                ACaseSwitchBody body = (ACaseSwitchBody) node.getBody().get(i);
                for(PExpression expresssion: body.getExpressions()){
                    if(i == 0){
                        puts("if ");
                    } else {
                        puts("elif ");
                    }
                    expresssion.apply(this);
                    if(hasCondition){
                        puts(" == ");
                        condition.apply(this);
                        puts(" :\n");
                    }else{
                        puts(" is True:\n");
                    }
                    level++;
                    for(PStatement stmt: body.getStatements()){
                        stmt.apply(this);
                        puts("\n");
                    }
                    level--;
                }
            }
        } // default case must be at the end (else case)
        for(int i=0; i<node.getBody().size(); i++){
            if(node.getBody().get(i) instanceof ADefaultSwitchBody){
                ADefaultSwitchBody body = (ADefaultSwitchBody) node.getBody().get(i);
                puts("else:\n");
                level++;
                for(PStatement stmt: body.getStatements()){
                    stmt.apply(this);
                    puts("\n");
                }
                level--;
            }
        }
        puts("\n");
        level--;
    }

    public void caseAShortVarDeclStatement(AShortVarDeclStatement node){
        for(int i=0; i < node.getExpressions().size(); i++){
            node.getIdentifiers().get(i).apply(this);
            puts(" = ");
            node.getExpressions().get(i).apply(this);
            puts("\n");
        }
    }

//
    /* inc / dec stmt*/
    public void caseAIncStatement(AIncStatement node) {
        node.getExpression().apply(this);
        puts("+= 1\n");
    }

    public void caseADecStatement(ADecStatement node) {
        node.getExpression().apply(this);
        puts("-= 1\n");
    }


    /* assign op*/
    public void caseAPlusEqualAssignOperator(APlusEqualAssignOperator node) {
        //puts(node.getPlusEqual().getText());
        puts("+");
    }

    public void caseAMinusEqualAssignOperator(AMinusEqualAssignOperator node) {
        //puts(node.getMinusEqual().getText());
        puts("-");
    }

    public void caseAVertEqualAssignOperator(AVertEqualAssignOperator node) {
        //puts(node.getVertEqual().getText());
        puts("|");
    }

    public void caseACaretEqualAssignOperator(ACaretEqualAssignOperator node) {
        //puts(node.getCaretEqual().getText());
        puts(" ~");
    }

    public void caseASlashEqualAssignOperator(ASlashEqualAssignOperator node) {
        //puts(node.getSlashEqual().getText());
        puts("/");
    }

    public void caseAStarEqualAssignOperator(AStarEqualAssignOperator node) {
        //puts(node.getStarEqual().getText());
        puts("*");
    }

    public void caseAPercentEqualAssignOperator(APercentEqualAssignOperator node) {
        //puts(node.getPercentEqual().getText());
        puts("%");
    }

    public void caseAAmpersandEqualAssignOperator(AAmpersandEqualAssignOperator node) {
        //puts(node.getAmpersandEqual().getText());
        puts("&");
    }

    public void caseALessLessEqualAssignOperator(ALessLessEqualAssignOperator node) {
        //puts(node.getLessLessEqual().getText());
        puts("<<");
    }

    public void caseAGreaterGreaterEqualAssignOperator(AGreaterGreaterEqualAssignOperator node) {
        //puts(node.getGreaterGreaterEqual().getText());
        puts(">>");
    }

    public void caseAAmpersandCaretEqualAssignOperator(AAmpersandCaretEqualAssignOperator node) {
        //puts(node.getAmpersandCaretEqual().getText());
        puts(" ^ ~ ");
    }

    public void caseAPrintlnStatement(APrintlnStatement node) {
        puts("print ");
        for (int i = 0; i<node.getExpressions().size(); i++){
            PExpression currExpr = node.getExpressions().get(i);
            currExpr.apply(this);
            if(i != node.getExpressions().size()-1){
                puts(" , ");
            }

        }
        puts(", \"\\n\"\n");
    }

    public void caseAPrintStatement(APrintStatement node) {
        puts("print ");
        for (int i = 0; i<node.getExpressions().size(); i++){
            PExpression currExpr = node.getExpressions().get(i);
            currExpr.apply(this);
            if(i != node.getExpressions().size()-1){
                puts(" , ");
            }

        }
        puts(" \n");
    }

    public void caseAReturnStatement(AReturnStatement node)
    {
        puts("return " );
        if(node.getExpression() != null){
            node.getExpression().apply(this);
        }
        puts("\n");
    }



    public void caseAAssignmentStatement(AAssignmentStatement node) {
        if(node.getLvalue().size() >= 2 && node.getRvalue().size() >= 2){
            if(node.getRvalue().get(0) instanceof AIdentifierExpression &&
              node.getRvalue().get(1) instanceof AIdentifierExpression &&
              node.getLvalue().get(0) instanceof AIdentifierExpression &&
              node.getLvalue().get(1) instanceof AIdentifierExpression) {
                AIdentifierExpression id_0 = (AIdentifierExpression) node.getLvalue().get(0);
                AIdentifierExpression id_1 = (AIdentifierExpression) node.getLvalue().get(1);
                AIdentifierExpression id_2 = (AIdentifierExpression) node.getRvalue().get(0);
                AIdentifierExpression id_3 = (AIdentifierExpression) node.getRvalue().get(1);
                if(id_0.getIdentifier().getText().equals(id_3.getIdentifier().getText()) &&
                   id_1.getIdentifier().getText().equals(id_2.getIdentifier().getText())) {
                      //SWAP
                      puts(id_0.getIdentifier().getText()+" = temp_123\n");
                      puts(id_0.getIdentifier().getText()+" = "+id_2.getIdentifier().getText()+"\n");
                      puts(id_2.getIdentifier().getText() +" = temp_123\n");
                      return;
                }
            }
        }
        try {
            if (node.getRvalue().get(0).getClass() == AAppendExpression.class) {
                node.getRvalue().get(0).apply(this);
                puts("\n");
                return;
            }
        } catch (Exception e) {
            //nah
        }
        for (int i = 0; i < node.getLvalue().size(); i++) {
            //PExpression currExpr = node.getLvalue().get(i);
            node.getLvalue().get(i).apply(this);
            puts(" = ");
            node.getRvalue().get(i).apply(this);
            puts("\n");
        }

    }

    public void caseAAppendExpression(AAppendExpression node)
    {
        puts (node.getTo().toString().trim() + ".append(" + node.getValue().toString().trim() + ")");
    }

    public void caseAOpAssignStatement(AOpAssignStatement node){
        node.getLvalue().apply(this);
        puts(" = ");
        node.getLvalue().apply(this);
        node.getOp().apply(this);
        node.getRvalue().apply(this);
        puts("\n");
    }

    public void caseABreakStatement(ABreakStatement node){
        puts("break\n");
    }

    public void caseAContinueStatement(AContinueStatement node){
        puts("continue\n");
    }
}
