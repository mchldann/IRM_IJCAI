package scheduler;

import decision.Decision;
import decision.Decision_GPT;
import decision.Decision_RL_CW;
import decision.Decision_RL_OW;
import main.Main;
import main.Main.Environment;
import officeworld.OfficeWorldPosition;
import util.Enums.Action;

import java.util.ArrayList;
import java.util.List;

import craftworld.CraftWorldPosition;
import craftworld.CraftWorldQLearner;
import craftworld.CraftWorldState;
import craftworld.CraftWorldQLearner.StateValuePair;

public class MCTS_Node {

    // state of this node
    public State state;

    // selection information
    public Decision decision;

    // child nodes
    public ArrayList<MCTS_Node> children;
    
    // statistics
    public int nVisits;
    public Match match;
	public double[] totValue;

    public MCTS_Node(State state, Match match)
    {
        this.state = state;
        this.match = match;
        
        // statistics initialisation
        init();
    }

    /**
     * initialisation
     */
    private void init()
    {
        nVisits = 0;
		this.totValue = new double[match.agents.length];
    }

    /**
     * @return true if it is the leaf node; false, otherwise
     */
    public boolean isLeaf()
    {
        return children == null;
    }
    
    /**
     * expand the current node
     */
    public void expand(boolean[] intentionAvailable, boolean include_pass, boolean expand_as_gpt_agent, boolean include_reset_decision)
    {
        children = new ArrayList<>();

    	List<Decision> availableDecisions;
    	
        if (expand_as_gpt_agent)
        {
        	availableDecisions = state.getAvailableDecisions(intentionAvailable);
        	
	        // Give the ability to reset incomplete intentions. ('include_reset_decision' will only be true for the first expansion call in MCTS.)
	        if (include_reset_decision && state.actionTakenSinceReset[state.playerTurn])
	        {
	        	availableDecisions.add(new Decision_GPT(-2, -2, false, true));
	        }
	        
	        // Give the ability for the player to pass. (Always allow it if there are no valid actions.)
	        if (include_pass || availableDecisions.size() == 0)
	        {
	        	availableDecisions.add(new Decision_GPT(Decision.PASS, Decision.PASS, availableDecisions.size() == 0));
	        }
        }
        else
        {
        	availableDecisions = new ArrayList<Decision>();
        	
        	if (Main.environment == Environment.OFFICE_WORLD)
        	{
	        	OfficeWorldPosition currentPos = state.ow_agent_positions[state.playerTurn];
	        	
	        	if (currentPos != null)
	        	{
		        	OfficeWorldPosition[] neighbours = currentPos.get_neighbours();
		        	
		        	for (int action = 0; action < neighbours.length; action++)
		        	{
		        		if (neighbours[action] != null)
		        		{
		        			availableDecisions.add(new Decision_RL_OW(action, false, currentPos, neighbours[action]));
		        		}
		        	}
		        	
			        // Give the ability for the player to pass. (Always allow it if there are no valid actions.)
			        if (include_pass || availableDecisions.size() == 0)
			        {
		        		availableDecisions.add(new Decision_RL_OW(-1, availableDecisions.size() == 0, currentPos, currentPos));
			        }
	        	}
	        	else // Add a dummy pass action if the RL agent has left (or was never in) the environment.
	        	{
	        		availableDecisions.add(new Decision_RL_OW(-1, true, null, null));
	        	}
        	}
        	else if (Main.environment == Environment.CRAFT_WORLD)
        	{
        		CraftWorldPosition currentPos = state.cw_agent_positions[state.playerTurn];
        		
	    		CraftWorldState cws = new CraftWorldState(state);
	    		CraftWorldQLearner qLearner = Main.cw_qLearners[state.playerTurn];
	    		float[] qVals = qLearner.getQValsForMaxGoalsAchievable(state.playerTurn, cws.getQLearningID(qLearner));
	    		StateValuePair greedyAct = qLearner.getGreedyAction(qVals);
	    		
	        	if (currentPos != null && greedyAct.value > 0) // If greedyAct.value <= 0 it implies that there is nothing left to do
	        	{
	        		CraftWorldPosition[] neighbours = currentPos.get_neighbours();
		        	
		        	for (int action = 0; action < neighbours.length; action++)
		        	{
		        		if (neighbours[action] != null)
		        		{
		        			availableDecisions.add(new Decision_RL_CW(action, false, currentPos, neighbours[action], greedyAct.value, cws.getQLearningID(Main.cw_qLearners[state.playerTurn])));
		        		}
		        	}
		        	
		        	// Give ability to craft items
		        	if (qVals[Action.CRAFT_OR_GATHER_ITEM.id] > Double.NEGATIVE_INFINITY)
		        	{
	        			availableDecisions.add(new Decision_RL_CW(Action.CRAFT_OR_GATHER_ITEM.id, false, currentPos, currentPos, greedyAct.value, cws.getQLearningID(Main.cw_qLearners[state.playerTurn])));
		        	}
		        	
			        // Give the ability for the player to pass. (Always allow it if there are no valid actions.)
			        if (include_pass)
			        {
		        		availableDecisions.add(new Decision_RL_CW(-1, availableDecisions.size() == 0, null, null, 0.0, -1));
			        }
	        	}
	        	else // Add a dummy pass action if the RL agent has left (or was never in) the environment.
	        	{
	        		availableDecisions.add(new Decision_RL_CW(-1, true, null, null, 0.0, -1));
	        	}
        	}
        }
        
    	for (Decision d : availableDecisions)
    	{
        	State new_state = state.clone();
        	d.apply_to_state(match, new_state, false, false);
        	
	        MCTS_Node new_node = new MCTS_Node(new_state, match);
	        new_node.decision = d;
	        
	        this.children.add(new_node);
    	}
    }
}
