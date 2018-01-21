package tiny;
import java.util.*;
import tiny.node.*;

public class SymbolTable {
  //private HashMap<String, SymbolTableEntry> table = new HashMap<String, SymbolTableEntry>();
  private Stack<HashMap<String, SymbolTableEntry>> st = new Stack<HashMap<String, SymbolTableEntry>>();
  private HashMap<Integer, HashMap<String, SymbolTableEntry>> all = new HashMap<Integer, HashMap<String, SymbolTableEntry>>();
  public SymbolTable(){
    HashMap<String, SymbolTableEntry> table = new HashMap<String, SymbolTableEntry>();
    table.put("true", new SymbolTableEntry("true", Constants.CATEGORY_VAR, Constants.TYPE_BOOL, Constants.TYPE_BOOL, true));
    table.put("false", new SymbolTableEntry("false", Constants.CATEGORY_VAR, Constants.TYPE_BOOL, Constants.TYPE_BOOL, true));
    table.put("int", new SymbolTableEntry("int", Constants.CATEGORY_TYPE, Constants.TYPE_INT, Constants.TYPE_INT, true));
    //table.put("float", new SymbolTableEntry("float", Constants.CATEGORY_TYPE, Constants.TYPE_FLOAT, Constants.TYPE_FLOAT, true));
    table.put("float64", new SymbolTableEntry("float64", Constants.CATEGORY_TYPE, Constants.TYPE_FLOAT, Constants.TYPE_FLOAT, true));
    table.put("rune", new SymbolTableEntry("rune", Constants.CATEGORY_TYPE, Constants.TYPE_RUNE, Constants.TYPE_RUNE, true));
    table.put("bool", new SymbolTableEntry("bool", Constants.CATEGORY_TYPE, Constants.TYPE_BOOL, Constants.TYPE_BOOL, true));
    table.put("string", new SymbolTableEntry("string", Constants.CATEGORY_TYPE, Constants.TYPE_STRING, Constants.TYPE_STRING, true));
    st.push(table);
    all.put(0, table);
  }

  public Stack<HashMap<String, SymbolTableEntry>> GetTable()
  {
      return this.st;
  }

  private String[] mBasicTypeList = { "true", "false", "int", "float64", "rune", "bool", "string"};

  public void addVar(String id, String type, String abstractType){
      if(!(typeExists(abstractType)) && !(typeExists(type))){
        System.out.println("add var: Type "+abstractType+" does not exist");
        System.exit(1);
      }
      HashMap<String, SymbolTableEntry> currentScope = st.peek();
      if (currentScope.containsKey(id)){
          SymbolTableEntry entry = currentScope.get(id);
          if(!entry.canBeShadowed()){
              System.out.println("add var: Identifier "+id+" already exists");
              System.exit(1);
          }
          else {
              entry.setCategory(Constants.CATEGORY_VAR);
              entry.setType(type);
              entry.setAbstractType(abstractType);
              entry.setCanBeShadowed(entry.canBeShadowed());
          }
      } else {
          currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_VAR, type, abstractType));
      }
  }

  public void addFunc(String id, String type){
    HashMap<String, SymbolTableEntry> currentScope = st.peek();
    if (currentScope.containsKey(id)){
      System.out.println("add func: id "+id+" already exists");
      System.exit(1);
    }
    currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_VAR, type, type));
  }

  public void alwaysAddVar(String id, String type, String abstractType){
     if(!typeExists(abstractType)){
       System.out.println("Type "+abstractType+" does not exist");
       System.exit(1);
     }
     HashMap<String, SymbolTableEntry> currentScope = st.peek();
     if (currentScope.containsKey(id)){
       SymbolTableEntry entry = currentScope.get(id);
       entry.setCategory(Constants.CATEGORY_VAR);
       entry.setType(type);
       entry.setAbstractType(abstractType);
       entry.setCanBeShadowed(true);
     } else {
         currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_VAR, type, abstractType, true));
     }
  }

  public void addType(String id, String type, String abstractType){
    if(!typeExists(abstractType)){
      System.out.println("add type: Type "+type+" does not exist");
      System.exit(1);
    }
    HashMap<String, SymbolTableEntry> currentScope = st.peek();
    if (currentScope.containsKey(id)){
        SymbolTableEntry entry = currentScope.get(id);
        if(!entry.canBeShadowed()){
            System.out.println("add type: Type "+id+" already exists");
            System.exit(1);
        }
        else {
            entry.setCategory(Constants.CATEGORY_TYPE);
            entry.setType(type);
            entry.setAbstractType(abstractType);
            entry.setCanBeShadowed(false);
        }
    } else {
        currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_TYPE, type, abstractType));
    }
  }

    public boolean isBasicTypeOrAlias(String typeId) {
        typeId = typeId.trim();
        String currBasicType = GetBasicType(typeId);
        if (currBasicType == null) {
            return false;
        }
        if (currBasicType.equals("")) {
            return false;
        }
        return true;
    }


    public String GetBasicType(String typeId) {
        typeId = typeId.trim();
        if (isBasicType(typeId)) {
            return typeId;
        }
        HashMap<String, SymbolTableEntry> currentScope = st.peek();
        if (currentScope.containsKey(typeId)) {
            SymbolTableEntry entry = currentScope.get(typeId);
            if (isBasicType(entry.getType())) {
                return entry.getType();
            }
        }
        return "";
    }

    public boolean isBasicType(String typeId) {
        for (String curStr : mBasicTypeList) {
            if (curStr.equals(typeId.trim())) {
                return true;
            }
        }
        return false;
    }

  public void addTypeStruct(String id, String type, String abstractType, HashMap<String, SymbolTableEntry> hm){
    for(SymbolTableEntry entry: hm.values()){
      if(!typeExists(entry.getAbstractType())){
        System.out.println("Struct "+id+": Type "+type+" does not exist");
        System.exit(1);
      }
    }
    HashMap<String, SymbolTableEntry> currentScope = st.peek();
    if (currentScope.containsKey(id)){
        SymbolTableEntry entry = currentScope.get(id);
        if(!entry.canBeShadowed()){
            System.out.println("Type "+id+" already exists");
            System.exit(1);
        }
        else {
            entry.setCategory(Constants.CATEGORY_TYPE);
            entry.setType(type);
            entry.setAbstractType(abstractType);
            entry.setCanBeShadowed(false);
            entry.setStruct(hm);
        }
    } else {
        currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_TYPE, type, abstractType, hm));
    }
  }

  public void addVarStruct(String id, String type, String abstractType, HashMap<String, SymbolTableEntry> hm){
    for(SymbolTableEntry entry: hm.values()){
      if(!typeExists(entry.getAbstractType())){
        System.out.println("Struct "+id+": Type "+type+" does not exist");
        System.exit(1);
      }
    }
    HashMap<String, SymbolTableEntry> currentScope = st.peek();
    if (currentScope.containsKey(id)){
        SymbolTableEntry entry = currentScope.get(id);
        if(!entry.canBeShadowed()){
            System.out.println("Type "+id+" already exists");
            System.exit(1);
        }
        else {
            entry.setCategory(Constants.CATEGORY_TYPE);
            entry.setType(type);
            entry.setAbstractType(abstractType);
            entry.setCanBeShadowed(false);
            entry.setStruct(hm);
        }
    } else {
        currentScope.put(id, new SymbolTableEntry(id, Constants.CATEGORY_VAR, type, abstractType, hm));
    }
  }

  public void pushTable(ArrayList<String[]> iFunctionDeclParams, int iHash){
      HashMap<String, SymbolTableEntry> wNewEntry = new HashMap<String, SymbolTableEntry>();
      HashMap<String, SymbolTableEntry> current = st.peek();
      for(SymbolTableEntry entry: current.values()){
                wNewEntry.put(entry.getIdentifier(), new SymbolTableEntry(entry));
      }
      st.push(wNewEntry);
      all.put(iHash, wNewEntry);
      if (iFunctionDeclParams != null && iFunctionDeclParams.size() > 0) {
          for (String[] entry : iFunctionDeclParams) {
              if (entry.length >= 3)
              {
                 this.addVar(entry[0], entry[1], entry[2]);
              }
          }
      }
      //st.push(new HashMap<String, SymbolTableEntry>());
  }

  public Stack<HashMap<String, SymbolTableEntry>> popTable(boolean iDumpOnExit, boolean iDumpAllOnExit){
      if (iDumpAllOnExit){
          Stack<HashMap<String, SymbolTableEntry>> tempST = new Stack<HashMap<String, SymbolTableEntry>>();
          for (HashMap<String, SymbolTableEntry> entry : st )
          {
              tempST.add(entry);
          }
          st.pop();
          return tempST;
      }
      else if (iDumpOnExit){
          Stack<HashMap<String, SymbolTableEntry>> retVal = new Stack<HashMap<String, SymbolTableEntry>> ();
          retVal.add(st.pop());
          return retVal;
      }
      else {
          st.pop();
          return null;
      }
  }

  public Boolean identifierExists(String identifier){
      Iterator<HashMap<String, SymbolTableEntry>> iter = st.iterator();
      while (iter.hasNext()){
        HashMap<String, SymbolTableEntry> scope = iter.next();
        if(scope.containsKey(identifier)){
          if (scope.get(identifier).isVarEntry()) {
            return true;
          }
        }
      }
      return false;
  }

  public String getIdentifierType(String identifier){
      /*for (HashMap<String, SymbolTableEntry> currScope : st) {
          if (currScope.containsKey(identifier)) {
              SymbolTableEntry entry = currScope.get(identifier);
              if (entry.isVarEntry()) {
                  return entry.getType();
              }
          }
      }
    System.out.println("Identifier "+identifier+" does not exist");
    System.exit(1);
    return "";*/
    String type = "";
     HashMap<String, SymbolTableEntry> scope = st.peek();
       if(scope.containsKey(identifier)){
         SymbolTableEntry entry = scope.get(identifier);
         if (entry.isVarEntry()){
           return entry.getType();
         }
         else if (entry.isTypeEntry())
         {
             return entry.getType();
         }
       }
     if(type.equals("")){
       System.out.println("Identifier "+identifier+" does not exist");
       System.exit(1);
      }
      return type;
  }

  public String getStructIdType(String type, String id){
    HashMap<String, SymbolTableEntry> scope = st.peek();
    if(scope.containsKey(type)){
        SymbolTableEntry entry = scope.get(type);
        if(entry.getStruct().containsKey(id)){
          return entry.getStruct().get(id).getType();
        }
    }
    type = RT(type);
    if(scope.containsKey(type)){
        SymbolTableEntry entry = scope.get(type);
        if(entry.getStruct().containsKey(id)){
          return entry.getStruct().get(id).getType();
        }
    }
    for (SymbolTableEntry entry: scope.values()){
      if(entry.getType().equals(type)){
        return entry.getStruct().get(id).getType();
      }
    }
    System.out.println("Struct "+type+" does not contain "+id);
    System.exit(1);
    return "";
  }

 public String getStructIdTypeWithIdentifier(String identifier, String id){
    HashMap<String, SymbolTableEntry> scope = st.peek();
    if(scope.containsKey(identifier)){
        SymbolTableEntry entry = scope.get(identifier);
        if(entry.getStruct().containsKey(id)){
          return entry.getStruct().get(id).getType();
        }
    }

    //System.out.println("Struct "+identifier+" does not contain "+id);
    //System.exit(1);
    return "";
  }

  public Boolean typeIsStruct(String type){
    if(isStructType(type)){
      return true;
    }
    if(isStructType(RT(type))){
      return true;
    }
    Iterator<HashMap<String, SymbolTableEntry>> iter = st.iterator();
    while (iter.hasNext()){
      HashMap<String, SymbolTableEntry> scope = iter.next();
      if(scope.containsKey(type)){
        if(scope.get(type).getStruct().size() != 0){
          return true;
        }
      }
    }
    return false;
  }

  public String RT(String type){
    return _RT(type);
  }

  public String RT(String type, int iHashCode) {
    return _RT(type, iHashCode);
    }

  private String _RT(String type , int iHashCode) {
      if (isArrayType(type.trim()) || isSliceType(type.trim()) || isStructType(type.trim())) {
          return type.trim();
      }
      if (type.trim().equals(Constants.TYPE_INT) || type.trim().equals(Constants.TYPE_BOOL) || type.trim().equals(Constants.TYPE_FLOAT) || type.trim().equals(Constants.TYPE_RUNE) || type.trim().equals(Constants.TYPE_STRING)) {
          return type.trim();
      }
      HashMap<String, SymbolTableEntry> scope = all.get(iHashCode);
      if (scope.containsKey(type.trim())) {
          SymbolTableEntry entry = scope.get(type.trim());
          if (entry.isTypeEntry()) {
              return _RT(entry.getType(), iHashCode);
          }
      }
      System.out.println("Type " + type + " does not exist");
      System.exit(1);
      return "";
  }

    private String _RT(String type) {
        if (isArrayType(type.trim()) || isSliceType(type.trim()) || isStructType(type.trim())) {
            return type.trim();
        }
        if (isFuncType(type.trim())) {
            return type.trim().replace("func", "");
        }
        if (type.trim().equals(Constants.TYPE_INT) || type.trim().equals(Constants.TYPE_BOOL) || type.trim().equals(Constants.TYPE_FLOAT) || type.trim().equals(Constants.TYPE_RUNE) || type.trim().equals(Constants.TYPE_STRING)) {
            return type.trim();
        }
        
        HashMap<String, SymbolTableEntry> scope = st.peek();
        if (scope.containsKey(type.trim())) {
            SymbolTableEntry entry = scope.get(type.trim());
            if (entry.isTypeEntry()) {
                return _RT(entry.getType());
            }
        }
        System.out.println("Type " + type + " does not exist");
        System.exit(1);
        return "";
    }

    public Boolean typeIsBasic(String type)
    {
        HashMap<String, SymbolTableEntry> scope = st.peek();
        if (scope.containsKey(type.trim())) {
            SymbolTableEntry entry = scope.get(type.trim());
            if (entry.isTypeEntry()) {
                return true;
            }
        }
        return false;
    }

  public Boolean typeExists(String type){
    type = type.trim();
    for(HashMap<String, SymbolTableEntry> hm: st){
      for(SymbolTableEntry entry: hm.values()){
        if (entry.isTypeEntry() && entry.getIdentifier().equals(type)){
          return true;
        }
      }
    }
    return false;
  }

  public void print(){
    System.out.println("Identifier  |  Category  |  Type | abstractType ");
    for(HashMap<String, SymbolTableEntry> hm: st){
      for(SymbolTableEntry entry: hm.values()){
        System.out.println(entry.getIdentifier()+" "+entry.getCategory()+" "+entry.getType()+" "+entry.getAbstractType()+" ");
      }
    }
  }

  public Boolean isArrayType(String type){
    return type.matches("(\\[([0-9])+\\])+(.*)");
  }
  public Boolean isFuncType(String type){
    return type.matches("(func)+(.*)");
  }
  public Boolean isSliceType(String type){
    return type.matches("(\\[\\])+(.*)");
  }
  public Boolean isStructType(String type){
    return type.contains(",");
  }

}
