
package tiny;

import java.util.LinkedList;
import tiny.analysis.DepthFirstAdapter;
import tiny.node.ABreakStatement;
import tiny.node.ACaseSwitchBody;
import tiny.node.AContinueStatement;
import tiny.node.ADefaultSwitchBody;
import tiny.node.AForStmt;
import tiny.node.AFuncDcl;
import tiny.node.AReturnStatement;
import tiny.node.ASwitchStmt;
import tiny.node.PSwitchBody;


public class Weeder extends DepthFirstAdapter {

    public Boolean mFoundTwoDefaultCasesInASwitch = false;
    public int inALoop = 0;
    public Boolean inASwitch = false;
    public int numberReturnStmt = 0;
    public Weeder() {
    }

    public void inAFuncDcl(AFuncDcl node){
      numberReturnStmt = 0;
    }
    public void outAFuncDcl(AFuncDcl node){
      if(node.getReturnType() != null){
        if(numberReturnStmt == 0){
          System.out.print("INVALID: Function must returns something");
          System.exit(1);
        }
      }
    }
    public void inAReturnStatement(AReturnStatement node){
      numberReturnStmt++;
    }
    public void outAForStmt(AForStmt node) {
        inALoop++;
    }

    public void inAForStmt(AForStmt node) {
        inALoop--;
    }

    public void inABreakStatement(ABreakStatement node) {
        if(inALoop == 0 && !inASwitch){
          System.out.print("INVALID: Break statement must be used in a loop or in switch");
          System.exit(1);
        }
    }

    public void inAContinueStatement(AContinueStatement node) {
      if(inALoop == 0 && !inASwitch){
        System.out.print("INVALID: Continue statement must be used in a loop or in switch");
        System.exit(1);
      }
    }

    public void caseASwitchStmt(ASwitchStmt node) {
        inASwitch = true;
        int numberOfDefaultCase = 0;
        LinkedList<PSwitchBody> wSwitchBodies = node.getBody();
        for (int i = 0; i < wSwitchBodies.size(); i++) {
            boolean wFoundOneDefault = false;
            PSwitchBody wCurrNode = wSwitchBodies.get(i);
            if (wCurrNode.getClass() == ACaseSwitchBody.class) {
                wCurrNode.apply(this);
            } else if (wCurrNode.getClass() == ADefaultSwitchBody.class) {
                numberOfDefaultCase++;
            }
        }
        if (numberOfDefaultCase > 1) {
            System.out.print("INVALID: Caught two default cases in a switch!");
            System.exit(1);
        }
        inASwitch = false;
    }
}
