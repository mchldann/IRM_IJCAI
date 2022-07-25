package decision;

import beliefbase.Condition;
import craftworld.CraftWorldRenderer;
import goalplantree.ActionNode;
import goalplantree.GoalNode;
import goalplantree.PlanNode;
import goalplantree.TreeNode;
import main.Main;
import main.Main.Environment;
import officeworld.OfficeWorldRenderer;
import scheduler.Agent;
import scheduler.Match;
import scheduler.State;
import util.Log;

public class Decision_GPT extends Decision {
	
    public int iChoice;
    public int pChoice;
    public boolean forcedPass;
    public boolean resetIntentions;
    
    public Decision_GPT(int iChoice, int pChoice, boolean forcedPass)
    {
    	this(iChoice, pChoice, forcedPass, false);
    }
    
    public Decision_GPT(int iChoice, int pChoice, boolean forcedPass, boolean resetIntentions)
    {
    	this.iChoice = iChoice;
    	this.pChoice = pChoice;
    	this.forcedPass = forcedPass;
    	this.resetIntentions = resetIntentions;
    }
    
	@Override
	public boolean is_pass()
	{
        return (iChoice == -1);
	}
	
	@Override
	public boolean is_pass_forced()
	{
		return forcedPass;
	}
	
	@Override
    public void update_state(Match m, State currentState, boolean verbose, boolean draw)
    {
        if (resetIntentions)
        {
        	Log.info("Intention reset selected", verbose);
        	Agent agent = m.agents[currentState.playerTurn];
        	currentState.resetIncompleteIntentions(agent.agent_num, agent.available_intentions[agent.agent_num], verbose);
        	return;
        }
        
    	// Execute the selected intention
        if (is_pass())
        {
			currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
			currentState.age++;
        }
        else
        {
        	int agent_num = currentState.playerTurn;
        	
        	Log.info("intention " + iChoice +  " selected", verbose);
        	
            TreeNode selected = currentState.intentions.get(iChoice);
            
            if (selected instanceof GoalNode)
            {
                // Select plans based on the result of plan selection
                PlanNode plan = ((GoalNode) selected).getPlanAt(pChoice);
                
                Log.info(plan.getType() + " selected", verbose);

                // Check pre-condition
                Condition[] prec = plan.getPrec();
                if(currentState.beliefs.evaluate(prec))
                {
                    Log.info(plan.getType() + " starts", verbose);
                    
                    // check the first step in this plan
                    TreeNode first = plan.getPlanbody()[0];
                    if (first instanceof ActionNode)
                    {
                        Log.info(first.getType() + " starts", verbose);
                        
                        // get its precondition
                        Condition[] precA = ((ActionNode) first).getPrec();
                        if (currentState.beliefs.evaluate(precA))
                        {
                            // update the environment
                            Condition[] post = ((ActionNode) first).getPostc();
                            currentState.beliefs.apply(post);
                            
                            // update intentions
                            currentState.intentions.set(iChoice, nextIstep(first));
                            
                            Log.info(first.getType() + " succeeds", verbose);
                            
                			if (((ActionNode)first).getPostc().length > 0)
                			{
                				currentState.actionTakenSinceReset[currentState.playerTurn] = true;
                			}
                        }
                        else
                        {
                            Log.info(first.getType() + " fails", verbose);
                        }
                        
            			if (((ActionNode)first).getPostc().length > 0)
            			{
            				currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
            				currentState.age++;
            			}  
                    }
                    else if (first instanceof GoalNode)
                    {
                    	currentState.intentions.set(iChoice, first);
                    }
                }
                else
                {
                    Log.info(plan.getType() + " fails", verbose);
                }

            }
            else if (selected instanceof ActionNode)
            {
                // cast it to an action
                ActionNode action = (ActionNode) selected;
                
                // check the precondition of this action
                Condition[] prec = action.getPrec();
                
                Log.info(action.getType() + " starts", verbose);
                
                if (currentState.beliefs.evaluate(prec))
                {
                    // get postcondition
                    Condition[] postc = action.getPostc();
                    
                    // update the environment
                    currentState.beliefs.apply(postc);
                    
                    // update the intentions
                    currentState.intentions.set(iChoice, nextIstep(selected));
                    
                    Log.info(action.getType() + " succeeds", verbose);
                    
        			if (action.getPostc().length > 0)
        			{
        				currentState.actionTakenSinceReset[currentState.playerTurn] = true;
        			}
                }
                else
                {
                    Log.info(action.getType() + " fails", verbose);
                }
                
    			if (action.getPostc().length > 0)
    			{
    				currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
    				currentState.age++;
    			}  
            }
            
            currentState.refreshAgentPosition(agent_num);
    	}
        
		if (draw)
		{
			if (Main.environment == Environment.OFFICE_WORLD)
			{
		        OfficeWorldRenderer renderer = new OfficeWorldRenderer();
		        renderer.draw(m.m_name + "_" + String.format("%05d", m.drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
			}
			else if (Main.environment == Environment.CRAFT_WORLD)
			{
		        CraftWorldRenderer renderer = new CraftWorldRenderer();
		        renderer.draw(m.m_name + "_" + String.format("%05d", m.drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
			}
		}
    }
    
    /**
     * @return the next step of an action in the given intention
     */
    private TreeNode nextIstep(TreeNode node)
    {
        // if it is not the last step in a plan
        if(node.getNext() != null)
        {
            return node.getNext();
        }
        // if it is
        else
        {
            // if it is the top-level goal
            if(node.getParent() == null)
            {
                return null;
            }
            else
            {
                // get the goal it is going to achieve
                GoalNode gn = (GoalNode) node.getParent().getParent();
                return nextIstep(gn);
            }
        }
    }
}
