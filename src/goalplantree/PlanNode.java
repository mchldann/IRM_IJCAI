package goalplantree;

import beliefbase.Condition;

import java.util.ArrayList;

public class PlanNode extends TreeNode {

    /**
     * precondition
     */
    final private Condition[] prec;

    /**
     * incondition
     */
    final private Condition[] inc;

    /**
     * plan body
     */
    final private TreeNode[] body;

    /**
     * basic coverage
     */
    private double bc;

    /**
     * extended coverage
     */
    private double ec;

    /**
     * initialisation with an empty plan with no precondition
     * @param id id of the plan
     * @param type type of the plan
     */
    public PlanNode(String id, String type){
        super(id, type);
        this.prec = new Condition[0];
        this.inc = new Condition[0];
        this.body = new TreeNode[0];
    }

    /**
     * initialisation
     * @param id id of the plan
     * @param type type of the plan
     * @param preconditions precondition of the plan
     * @param inconditions in-condition of the plan
     * @param nodes planbody of the plan
     */
    public PlanNode(String id, String type, Condition[] preconditions, Condition[] inconditions, TreeNode[] nodes){
        super(id, type);
        this.prec = preconditions == null ? new Condition[0] : preconditions;
        this.inc = inconditions == null ? new Condition[0] : inconditions;
        this.body = nodes == null ? new TreeNode[0] : nodes;
        init();
    }

    public PlanNode(String id, PlanTemplate pt){
        super(id, pt.name);
        this.prec = pt.prec == null ? new Condition[0] : pt.prec;
        this.inc = pt.inc == null ? new Condition[0] : pt.inc;
        this.body = pt.body == null ? new TreeNode[0] : pt.body;
        init();
    }

    private void init(){
        // set the parent-child relationship
        for(int i = 0; i < this.body.length; i++){
            body[i].setParent(this);
        }
        // set the next step relationship
        for(int i = 0; i < this.body.length-1; i++){
            body[i].setNext(body[i+1]);
        }
    }


    /**
     * @return the precondition of this plan
     */
    public Condition[] getPrec(){
        return this.prec;
    }

    public Condition[] getInc(){
        return this.inc;
    }


    /**
     * @return the plan body of this plan
     */
    public TreeNode[] getPlanbody(){
        return body;
    }

    /**
     * @return if this plan is empty
     */
    public boolean isEmpty(){
        return getPlanbody().length == 0;
    }

    /**
     * @return the list of subgoals in this plan
     */
    public ArrayList<GoalNode> getSubgoals(){
        ArrayList<GoalNode> subgoals = new ArrayList<>();
        if(this.getPlanbody() != null){
            for(int i = 0; i < this.getPlanbody().length; i++){
                if(this.getPlanbody()[i] instanceof GoalNode){
                    subgoals.add((GoalNode) this.getPlanbody()[i]);
                }
            }
        }
        return subgoals;
    }

    /**
     * set the basic coverage of this plan
     * @param coverage
     */
    public void setBc(double coverage){
        this.bc = coverage;
    }

    /**
     * @return the basic coverage of this plan
     */
    public double getBc(){
        return this.bc;
    }

    /**
     * set the extended coverage of this plan
     * @param coverage
     */
    public void setEc(double coverage){
        this.ec = coverage;
    }

    /**
     * @return the extended coverage of this plan
     */
    public double getEc(){
        return this.ec;
    }


    @Override
    public String onPrintNode(int num){
        String result = "Plan:[type = " + type +
                        "; status = " + status +
                        "; prec = {";
        for(int i = 0; i < prec.length; i++){
            result += "(" + prec[i].getLiteral() + "," + prec[i].isPositive() + ");";
        }
        result += "}; inc = {";
        for(int i = 0; i < inc.length; i++){
            result += "(" + inc[i].getLiteral() + "," + inc[i].isPositive() + ");";
        }
        result += "}; planbody = {";

        if(!isEmpty()){
            result += (getPlanbody()[0].getType());
        }
        for(int i = 1; i < getPlanbody().length; i++){
            result += (";" + getPlanbody()[i].getType());
        }
        result += "}]";


        for(int i = 0; i < getPlanbody().length; i++){
            result += "\n";
            for(int j = 0; j < num + 1; j++){
                result += indent;
            }
            result += getPlanbody()[i].onPrintNode(num + 1);
        }



        return result;
    }


}