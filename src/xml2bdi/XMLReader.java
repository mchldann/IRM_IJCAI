package xml2bdi;
import beliefbase.BeliefBase;
import beliefbase.Condition;
import goalplantree.ActionNode;
import goalplantree.GoalNode;
import goalplantree.PlanNode;
import goalplantree.TreeNode;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLReader {

    private BeliefBase beliefs;
    private ArrayList<TreeNode> intentions;

    public XMLReader(String url) throws Exception{
        translate(url);
    }

    private void translate(String url) throws Exception{

        SAXBuilder builder = new SAXBuilder();
        Document read_doc = builder.build(new File(url));
        Element root = read_doc.getRootElement(); // get the root node of the xml file

        // get the goals, plans and actions
        List<Element> toplevelgoals = root.getChildren();
        // get the environment
        Element environment = toplevelgoals.get(0);
        List<Element> literals = environment.getChildren();
        beliefs = new BeliefBase("default");
        for(int i = 0; i < literals.size(); i++){
            beliefs.apply(new Condition(literals.get(i).getAttributeValue("name"),
                    literals.get(i).getAttributeValue("initVal").equals("true")));
        }

        intentions = new ArrayList<>();
        // generate goal-plan tree for each intention
        for( int i = 1; i < toplevelgoals.size(); i++){
            intentions.add(readGoal(toplevelgoals.get(i)));
        }
    }


    /**
     * translate an xml element to a goal node
     * @param element the input xml element
     * @return the goal node
     */
    private GoalNode readGoal(Element element){
        // get the name of this goal
        String name = element.getAttributeValue("name");
        // read and translate the goal-condition of this goal
        Condition[] conditions = readCondition(element.getAttributeValue("goal-condition"));
        // get the child element
        List<Element> children = element.getChildren();
        // get the number of plan node
        PlanNode[] plans = new PlanNode[children.size()];
        // read and construct the plan node
        for(int i = 0; i < children.size(); i++){
            plans[i] = readPlan(children.get(i));
        }
        // generate a new goal node
        GoalNode gn = new GoalNode("", name, new Condition[0], plans, conditions);

        return gn;
    }

    /**
     * translate an xml element to a plan node
     * @param element
     * @return
     */
    private PlanNode readPlan(Element element){
        // read the name of this plan
        String name = element.getAttributeValue("name");
        // read and translate the precondition of this plan
        Condition[] conditions = readCondition(element.getAttributeValue("precondition"));
        // read and construct the execution steps in this plan
        List<Element> elements = element.getChildren();
        TreeNode[] steps = new TreeNode[elements.size()];
        for(int i = 0; i < steps.length; i++){
            // get the type of this step
            String type = elements.get(i).getName();
            if(type.equals("Action")){
                ActionNode actionNode = readAction(elements.get(i));
                steps[i] = actionNode;
            }
            else if(type.equals("Goal")){
                GoalNode goalNode = readGoal(elements.get(i));
                steps[i] = goalNode;
            }
        }

        PlanNode planNode = new PlanNode("", name, conditions, new Condition[0], steps);
        return planNode;
    }

    /**
     * translate an xml element to an action node
     * @param element
     * @return
     */
    private ActionNode readAction(Element element){
        // read the name of this action
        String name = element.getAttributeValue("name");
        
        // read and translate the precondition of this action
        String prec_str = element.getAttributeValue("precondition");
        Condition[] prec = (prec_str == null)? null : readCondition(prec_str);

        // read and translate the postcondition of this action
        String postc_str = element.getAttributeValue("postcondition");
        Condition[] postc = (postc_str == null)? null : readCondition(postc_str);
        
        // generate a new action node
        ActionNode actionNode = new ActionNode("", name, prec, new Condition[0], postc);
        return actionNode;
    }


    /**
     * read a string and generate corresponding conditions
     * @param conditions a list of string representing conditions
     * @return a list of conditions
     */
    private Condition[] readCondition(String conditions){
        conditions = conditions.replaceAll("\\)", "");
        conditions = conditions.replaceAll("\\(", "");
        conditions = conditions.replaceAll(";", "");
        conditions = conditions.replaceAll(" ", ""); // The original code didn't account for spaces!
        String[] literals = conditions.split(",");
        Condition[] cons = new Condition[literals.length / 2];
        for(int i = 0; i < cons.length; i++){
            cons[i] = new Condition(literals[i*2], literals[i*2 + 1].equals("true"));
        }
        return cons;
    }


    /**
     * get the belief set read from an xml file
     * @return
     */
    public BeliefBase getBeliefs(){
        return this.beliefs;
    }

    /**
     * get the intentions from the xml file
     * @return
     */
    public ArrayList<TreeNode> getIntentions(){
        return this.intentions;
    }

}
