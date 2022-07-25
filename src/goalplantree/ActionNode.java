package goalplantree;

import beliefbase.Condition;

public class ActionNode extends TreeNode {

    /**
     * ActionTemplates: an action contains its precondition, incondition and postcondition
     */
    /**
     * precondition
     */
    final private Condition[] prec;
    /**
     * in-condition
     */
    final private Condition[] inc;
    /**
     * postcondition
     */
    final private Condition[] postc;


    public ActionNode(String id, String type){
        super(id, type);
        this.prec = null;
        this.inc = null;
        this.postc = null;
    }


    public ActionNode(String id, String type, Condition[] precondition, Condition[] incondition, Condition[] postcondition){
        super(id, type);
        this.prec = precondition == null ? new Condition[0] : precondition;
        this.inc = incondition == null ? new Condition[0] : incondition;
        this.postc = postcondition == null ? new Condition[0] : postcondition;
    }

    /**
     * @param id id of this node
     * @param at the action template
     */
    public ActionNode(String id, ActionTemplate at){
        super(id, at.name);
        this.prec = at.prec == null ? new Condition[0] : at.prec;
        this.inc = at.inc == null ? new Condition[0] : at.inc;
        this.postc = at.postc == null ? new Condition[0] : at.postc;
    }

    /**
     * get the precondition of this action
     * @return
     */
    public Condition[] getPrec(){
            return this.prec;
    }


    /**
     * get the incondition of this action
     * @return
     */
    public Condition[] getInc(){
            return this.inc;
    }


    /**
     * get the postcondition of this action
     * @return
     */
    public Condition[] getPostc(){
            return this.postc;
    }


    public String onPrintNode(int num){
        String result = "Action:[type = " + type +
                        "; status = " + status +
                        "; prec = {";
        if(getPrec().length > 0){
            result += getPrec()[0].onPrintCondition();
            for(int i = 1; i < getPrec().length; i++){
                result += ";" + getPrec()[i].onPrintCondition();
            }
        }
        result += "}; inc = {";
        if(getInc().length > 0){
            result += getInc()[0].onPrintCondition();
            for(int i = 1; i < getInc().length; i++){
                result += ";" + getInc()[i].onPrintCondition();
            }
        }
        result += "}; postc = {";
        if(getPostc().length > 0){
            result += getPostc()[0].onPrintCondition();
            for(int i = 1; i < getPostc().length; i++){
                result += ";" + getPostc()[i].onPrintCondition();
            }
        }
        result += "}]";
        return result;
    }

    public String onPrintActionDetail(){
        String result = "Action: " + id + "-" + type;
        result += " = {prec: ";
        if(prec == null){
            result += "none";
        }else{
            for(int i = 0; i < prec.length; i++){
                result += prec[i].onPrintCondition();
            }
        }
        result += "}";

        result += " {inc: ";
        if(inc == null){
            result += "none";
        }else{
            for(int i = 0; i < inc.length; i++){
                result += inc[i].onPrintCondition();
            }
        }
        result += "}";

        result += " {postc: ";
        if(postc == null){
            result += "none";
        }else{
            for(int i = 0; i < postc.length; i++){
                result += postc[i].onPrintCondition();
            }
        }
        result += "}";

        return  result;
    }
}
