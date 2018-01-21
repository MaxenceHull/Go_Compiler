package tiny;
import java.util.*;

public class SymbolTableEntry {
  private String identifier;
  private String category;
  private String type;
  private String abstractType;
  private Boolean canBeShadowed = false;
  private HashMap<String, SymbolTableEntry> struct = new HashMap<String, SymbolTableEntry>();

  public SymbolTableEntry(String identifier, String category, String type, String abstractType){
    this.identifier = identifier;
    this.category = category;
    this.type = type;
    this.abstractType = abstractType;
  }
  public SymbolTableEntry(String identifier, String category, String type, String abstractType, HashMap<String, SymbolTableEntry> hm){
    this.identifier = identifier;
    this.category = category;
    this.type = type;
    this.abstractType = abstractType;
    this.struct = hm;
  }
  public SymbolTableEntry(String identifier, String category, String type, String abstractType, Boolean canBeShadowed){
    this.identifier = identifier;
    this.category = category;
    this.type = type;
    this.canBeShadowed = canBeShadowed;
    this.abstractType = abstractType;
  }
  public SymbolTableEntry(SymbolTableEntry entry){
     this.identifier = entry.identifier;
     this.category = entry.category;
     this.type = entry.type;
     this.abstractType = entry.abstractType;
     this.canBeShadowed = entry.canBeShadowed;
     this.struct = entry.struct;
   }
  @Override
  public String toString()
  {
      return "Identifier: " + this.identifier + " category: " + this.category + " type: " + this.type + "  Can_Be_shadowed : " + this.canBeShadowed + " Abstract_Type: " + this.abstractType;

  }

  public String getIdentifier(){
    return identifier;
  }
  public void setIdentifier(String identifier){
    this.identifier = identifier;
  }
  public String getCategory(){
    return category;
  }
  public void setCategory(String category){
    this.category = category;
  }
  public String getType(){
    return type;
  }
  public void setType(String type){
    this.type = type;
  }
  public String getAbstractType(){
    return abstractType;
  }
  public void setAbstractType(String type){
    this.abstractType = type;
  }
  public Boolean canBeShadowed(){
    return canBeShadowed;
  }
  public void setCanBeShadowed(Boolean canBe){
    this.canBeShadowed = canBe;
  }
  public Boolean isVarEntry(){
    return this.category.equals(Constants.CATEGORY_VAR);
  }
  public Boolean isTypeEntry(){
    return this.category.equals(Constants.CATEGORY_TYPE);
  }
  public void setStruct(HashMap<String, SymbolTableEntry> struct){
    this.struct = struct;
  }
  public HashMap<String, SymbolTableEntry> getStruct(){
    return this.struct;
  }
}
