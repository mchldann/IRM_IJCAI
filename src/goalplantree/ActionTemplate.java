package goalplantree;

import beliefbase.Condition;

public class ActionTemplate {
    /**
     * the precondition
     */
    public Condition[] prec;
    /**
     * the incondition
     */
    public Condition[] inc;
    /**
     * the postcondition
     */
    public Condition[] postc;
    /**
     * template name
     */
    public String name;

    public ActionTemplate(String n, Condition[] precondition, Condition[] incondition, Condition[] postcondition){
        this.prec = precondition;
        this.inc = incondition;
        this.postc = postcondition;
        this.name = n;
    }
}
