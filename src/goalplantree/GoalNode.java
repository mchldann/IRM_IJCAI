package goalplantree;
import beliefbase.Condition;

import java.util.ArrayList;

public class GoalNode extends TreeNode {

    /**
     * relevant plans
     */
    final private PlanNode[] plans;

    /**
     * in-conditions
     */
    final private Condition[] inc;

    /**
     * goal-conditions
     */
    final private Condition[] goalConds;

    /**
     * extended coverage
     */
    private double ec;

    /**
     * index of the relevant plan which was selected to achieve this goal, initially set to -1
     */
    private int choice = -1;

    /**
     * the list of attempt plans
     */
    ArrayList<String> attemptPlans;




    /**
     * initialisation with no relevant plans
     * @param id id of the goal
     * @param type type of the goal
     * @param goalCondition the goal condition of this goal
     */
    public GoalNode(String id, String type, Condition[] goalCondition){
        super(id, type);
        // there is no relevant plan
        plans = new PlanNode[0];
        inc = new Condition[0];
        goalConds = goalCondition;
        attemptPlans = new ArrayList<>();
    }

    /**
     * initialisation with a list of plans to achieve this goal
     * @param id id of the goal
     * @param type type of the goal
     * @param plannodes a list of relevant plans
     */
    public GoalNode(String id, String type, Condition[] incondition, PlanNode[] plannodes, Condition[] goalCondition){
        super(id, type);
        this.plans = plannodes == null ? new PlanNode[0] : plannodes;
        // set the parent-child relationship
        for(int i = 0; i < this.plans.length; i++){
            this.plans[i].setParent(this);
        }
        this.inc = incondition == null ? new Condition[0] : incondition;
        this.goalConds = goalCondition == null ? new Condition[0] : goalCondition;
        attemptPlans = new ArrayList<>();
    }


    /**
     * get the list of plans to achieve this goal
     * @return
     */
    public PlanNode[] getPlans(){
        return this.plans;
    }

    /**
     *
     * @param index
     * @return a plan with the specified index
     */
    public PlanNode getPlanAt(int index){
        return this.plans[index];
    }

    /**
     * @return the list of failed plans
     */
    public ArrayList<String> getAttemptPlans(){
        return this.attemptPlans;
    }

    /**
     * add the name of the failed plan
     * @param planName the name of the failed plan
     */
    public void addAttemptPlan(String planName){
        this.attemptPlans.add(planName);
    }

    /**
     * @return the choice of this goal
     */
    public int getChoice(){
        return this.choice;
    }

    /**
     * set the choice of this goal
     * @param index
     * @return true if index is a reasonable number, false otherwise
     */
    public boolean setChoice(int index){
        if(index >= getPlanNum() || index < 0){
            return false;
        }
        else{
            choice = index;
        }
        return true;
    }

    /**
     * @return the number of plans
     */
    public int getPlanNum(){
        return this.plans.length;
    }


    /**
     * set the extended coverage of this goal
     * @param coverage
     */
    public void setEc(double coverage){
        this.ec = coverage;
    }

    /**
     * @return the extended coverage of this goal
     */
    public double getEc(){
        return this.ec;
    }



    /**
     * @return null, if it is a top-level goal; the next step of this goal if it is a subgoal in a plan
     */
    @Override
    public TreeNode getNext(){
        // if it is not the last step in a plan or a top-level goal
        if(this.next != null){
            // return the next step of this subgoal in its associated
            return next;
        }
        // otherwise
        else {
            // if it is the last step in a plan
            if(this.parent != null){
                // return the next step of the goal its associated plan tries to achieve
                this.getParent().getParent().getNext();
            }
            // if it is a top-level goal, then return null
            return null;
        }
    }

    /**
     * @return the goal condition of this goal
     */
    public Condition[] getGoalConds(){
        return this.goalConds;
    }



    @Override
    public String onPrintNode(int num) {

        String result = "Goal:[type = " + type +
                        "; status = " + status;
        result += "; inc = {";
        for(int i = 0; i < inc.length; i++){
            result += "(" + inc[i].getLiteral() + "," + inc[i].isPositive() + ");";
        }
        result += "}; goalConds = {";
        for(int i = 0; i < goalConds.length; i++){
            result += "(" + goalConds[i].getLiteral() + "," + goalConds[i].isPositive() + ");";
        }

        result += "}; relevant plans = {";
        if(getPlanNum() > 0){
            result+=plans[0].getType();
        }
        for(int i = 1; i < getPlanNum(); i++){
            result += ", " + plans[i].getType();
        }
        result+="}]";

        for(int i = 0; i < plans.length; i++){
            result += "\n";
            for(int j = 0; j < num + 1; j++){
                result += indent;
            }
            result += plans[i].onPrintNode(num + 1);
        }

        return result;
    }

}