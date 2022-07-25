package goalplantree;

import beliefbase.Condition;

public class PlanTemplate {
    final public Condition[] prec;
    final public Condition[] inc;
    final public TreeNode[] body;
    final String name;

    public PlanTemplate(String n, Condition[] precondition, Condition[] incondition, TreeNode[] planbody){
        this.prec = precondition;
        this.inc = incondition;
        this.body = planbody;
        this.name = n;
    }
}
