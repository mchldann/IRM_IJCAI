package scheduler;

import beliefbase.BeliefBase;
import beliefbase.Condition;
import craftworld.CraftWorldPosition;
import decision.Decision;
import decision.Decision_GPT;
import goalplantree.ActionNode;
import goalplantree.GoalNode;
import goalplantree.PlanNode;
import goalplantree.TreeNode;
import main.Main;
import main.Main.Environment;
import officeworld.OfficeWorldPosition;
import officeworld.OfficeWorldState;
import util.Log;
import util.Enums.CraftWorldItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class State {

	public String forest_name;
	
    // current belief base
    public BeliefBase beliefs;

    // previous belief base
    public BeliefBase beliefsPrevious;
    
    // the list of intentions
    public ArrayList<TreeNode> intentions;
    
    public int playerTurn;
    public int age;
    public int[] gameOverAge;
    public double[] agentReturn;
    public boolean[] actionTakenSinceReset;
    public boolean notProgressible;
    
    public CraftWorldPosition[] cw_agent_positions;
    public OfficeWorldPosition[] ow_agent_positions;
    
    // Note: This constructor is private and only used by clone()
    private State(String forest_name, BeliefBase beliefs, BeliefBase beliefsPrevious, ArrayList<TreeNode> intentions, int playerTurn, int age, int[] gameOverAge, double agentReturn[],
    	boolean[] actionTakenSinceReset, boolean notProgressible, CraftWorldPosition[] cw_agent_positions, OfficeWorldPosition[] ow_agent_positions)
    {
    	this.forest_name = forest_name;
        this.beliefs = beliefs;
        this.beliefsPrevious = beliefsPrevious;
        this.intentions = new ArrayList<TreeNode>(intentions);
        this.playerTurn = playerTurn;
        this.age = age;
        this.gameOverAge = gameOverAge.clone();
        this.agentReturn = agentReturn.clone();
        this.actionTakenSinceReset = actionTakenSinceReset;
        this.cw_agent_positions = cw_agent_positions;
        this.ow_agent_positions = ow_agent_positions;
    }
    
    public State(String forest_name, BeliefBase beliefs, BeliefBase beliefsPrevious, ArrayList<TreeNode> intentions, int playerTurn, int age, boolean[] actionTakenSinceReset)
    {
    	this.forest_name = forest_name;
        this.beliefs = beliefs;
        this.beliefsPrevious = beliefsPrevious;
        this.intentions = new ArrayList<TreeNode>(intentions);
        this.playerTurn = playerTurn;
        this.age = age;
        this.actionTakenSinceReset = actionTakenSinceReset;
        this.notProgressible = false;
        
        this.gameOverAge = new int[2];
        this.gameOverAge[0] = Integer.MIN_VALUE;
        this.gameOverAge[1] = Integer.MIN_VALUE;
        
        this.agentReturn = new double[2];
        this.agentReturn[0] = 0.0;
        this.agentReturn[1] = 0.0;
        
        this.cw_agent_positions = new CraftWorldPosition[2];
        this.ow_agent_positions = new OfficeWorldPosition[2];
        refreshAgentPosition(0);
        refreshAgentPosition(1);
    }
    
    public void refreshAgentPosition(int agent_num)
    {
    	if (Main.environment == Environment.CRAFT_WORLD)
    	{
	    	String agentTag = (agent_num == 0) ? "" : "Opponent";
	        int[] agent_pos_arr = cw_parse_pos_array(agentTag);
	        
	        if (agent_pos_arr == null)
	        {
	        	this.cw_agent_positions[agent_num] = null;
	        }
	        else
	        {
	            this.cw_agent_positions[agent_num] = new CraftWorldPosition(agent_pos_arr[0], agent_pos_arr[1]);
	        }
    	}
    	else if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	String agentTag = (agent_num == 0) ? "" : "Op";
	        int[] agent_pos_arr = ow_parse_pos_array(agentTag);
	        
	        if (agent_pos_arr == null)
	        {
	        	this.ow_agent_positions[agent_num] = null;
	        }
	        else
	        {
	            this.ow_agent_positions[agent_num] = new OfficeWorldPosition(agent_pos_arr[0], agent_pos_arr[1], agent_pos_arr[2], agent_pos_arr[3]);
	        }
    	}
    }
    
	private int[] cw_parse_pos_array(String entity_tag)
	{
		int cell_x = -1;
		int cell_y = -1;
		
        Iterator<Map.Entry<String, Boolean>> itr = beliefs.iterator();
        String xStr = entity_tag + "AtX.*";
        String yStr = entity_tag + "AtY.*";
        
        while(itr.hasNext() && (cell_x == -1 || cell_y == -1))
        {
             Map.Entry<String, Boolean> entry = itr.next();
             
             // Note: Best to evaluate 'entry.getValue() == true' first because the string matching is more expensive
             if (entry.getValue() == true && entry.getKey().matches(xStr))
             {
            	 String tmp = entry.getKey().substring((entity_tag + "AtX").length());
            	 cell_x = Integer.parseInt(tmp);
             }
             else if (entry.getValue() == true && entry.getKey().matches(yStr))
             {
            	 String tmp = entry.getKey().substring((entity_tag + "AtY").length());
            	 cell_y = Integer.parseInt(tmp);
             }
        }
        
        if (cell_x == -1)
        {
        	return null;
        }
        else
        {
        	return new int[] {cell_x, cell_y};
        }
	}
	
	private int[] ow_parse_pos_array(String entity_tag)
	{
		int room_x = -1;
		int room_y = -1;
		int cell_x = -1;
		int cell_y = -1;
		
        Iterator<Map.Entry<String, Boolean>> itr = beliefs.iterator();
        String roomStr = entity_tag + "AtRoom.*";
        String cellStr = entity_tag + "At.*";
        
        while(itr.hasNext() && (room_x == -1 || cell_x == -1))
        {
             Map.Entry<String, Boolean> entry = itr.next();
             
             // Note: Best to evaluate 'entry.getValue() == true' first because the string matching is more expensive
             if (entry.getValue() == true && entry.getKey().matches(roomStr))
             {
            	 String room = entry.getKey().substring((entity_tag + "AtRoom").length());
            	 String[] room_split = room.split("-");
            	 
            	 room_x = Integer.parseInt(room_split[0]);
            	 room_y = Integer.parseInt(room_split[1]);
             }
             else if (entry.getValue() == true && entry.getKey().matches(cellStr))
             {
            	 String position_in_room = entry.getKey().substring((entity_tag + "At").length());
            	 Integer[] position_in_room_arr = OfficeWorldPosition.cell_key_to_ints(position_in_room);
            	 
            	 cell_x = position_in_room_arr[0];
            	 cell_y = position_in_room_arr[1];
             }
        }
        
        if (room_x == -1)
        {
        	return null;
        }
        else
        {
        	return new int[] {room_x, room_y, cell_x, cell_y};
        }
	}
    
    public boolean isGameOver()
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	return notProgressible
	    		|| (allTasksComplete(0) && allTasksComplete(1))
	    		|| agentsInSameRoom()
	    		|| age > Main.OFFICE_WORLD_TIMEOUT;
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
    		return notProgressible
    	    	|| age > Main.CRAFT_WORLD_TIMEOUT;
    		//	|| allTasksComplete(agent_num);
    	}
    	else
    	{
    		return true;
    	}
    }
    
    public boolean agentsInSameRoom()
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	if (ow_agent_positions[0] == null || ow_agent_positions[1] == null)
	    	{
	    		return false;
	    	}
	    	
	    	return (ow_agent_positions[0].room_x == ow_agent_positions[1].room_x) && (ow_agent_positions[0].room_y == ow_agent_positions[1].room_y);
    	}
    	else
    	{
    		return false;
    	}
    }
    
    public void removeDoneAgents()
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	for (int agent = 0; agent < 2; agent++)
	    	{
	    		if (ow_agent_positions[agent] == null)
	    		{
	    			continue;
	    		}
	    		
	    		String agentTag = (agent == 0) ? "" : "Op";
	    		
				// Check if RL agent has completed its tasks
				Condition opCoffeeDelivered = new Condition(agentTag + "CoffeeDelivered", true);
				boolean coffeeDelivered = beliefs.evaluate(opCoffeeDelivered);
				
				Condition opMailDelivered = new Condition(agentTag + "MailDelivered", true);
				boolean mailDelivered = beliefs.evaluate(opMailDelivered);
				
				if (coffeeDelivered && mailDelivered)
				{
					removeAgent(agent);
				}
	    	}
    	}
    }
    
    public void updateReturns()
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
    		if (!agentsInSameRoom())
    		{
		    	for (int agent_num = 0; agent_num < 2; agent_num++)
		    	{
			    	String agentTag = (agent_num == 0)? "" : "Op";
			    	
			    	Condition cCoffeeDelivered = new Condition(agentTag + "CoffeeDelivered", true);
			    	Condition cMailDelivered = new Condition(agentTag + "MailDelivered", true);
			    	
			    	boolean both_delivered_now = beliefs.evaluate(cCoffeeDelivered) && beliefs.evaluate(cMailDelivered);
			    	boolean both_delivered_previously = beliefsPrevious.evaluate(cCoffeeDelivered) && beliefsPrevious.evaluate(cMailDelivered);
			    	
			    	if (both_delivered_now && !both_delivered_previously)
			    	{
			    		agentReturn[agent_num] = Math.pow(Main.GAMMA, age);
			    	}
		    	}
    		}
    		else
    		{
    			agentReturn[0] = Math.pow(Main.GAMMA, age) * Main.FAIL_SCORE;
    			agentReturn[1] = Math.pow(Main.GAMMA, age) * Main.FAIL_SCORE;
    		}
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
	    	for (int agent_num = 0; agent_num < 2; agent_num++)
	    	{
	    		if (cw_agent_positions[agent_num] == null)
	    		{
	    			continue;
	    		}
	    		
	    		int previousNumGoals = 0;
	    		int currentNumGoals = 0;
	    		
	    		for (CraftWorldItem item : Main.cw_settings.goalItems.get(agent_num))
	    		{
    		    	String agentTag = (agent_num == 0)? "" : "Opponent";
    		    	Condition cHaveItem = new Condition(agentTag + "Have" + item.text_rep, true);

    		    	if (beliefs.evaluate(cHaveItem))
    		    	{
    		    		currentNumGoals++;
    		    	}
    		    	
    		    	if (beliefsPrevious.evaluate(cHaveItem))
    		    	{
    		    		previousNumGoals++;
    		    	}
	    		}
	    		
	    		agentReturn[agent_num] +=  Math.pow(Main.GAMMA, age) * (double)(currentNumGoals - previousNumGoals);
	    	}
    	}
    	
    	beliefsPrevious = beliefs.clone();
    }
    
    public void removeAgent(int agent_num)
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
			String agentTag = (agent_num == 0) ? "" : "Op";
			OfficeWorldPosition agentPos = ow_agent_positions[agent_num];
			
			if (agentPos == null)
			{
				return;
			}
			
			Condition unset_end_pos_room = new Condition(agentTag + "AtRoom" + agentPos.roomID(), false);
			Condition unset_end_pos_cell = new Condition(agentTag + "At" + agentPos.cellID(), false);
			beliefs.apply(unset_end_pos_room);
			beliefs.apply(unset_end_pos_cell);
			
			ow_agent_positions[agent_num] = null;
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
			String agentTag = (agent_num == 0) ? "" : "Opponent";
			CraftWorldPosition agentPos = cw_agent_positions[agent_num];
			
			if (agentPos == null)
			{
				return;
			}
			
			Condition unset_cell_x = new Condition(agentTag + "AtX" + agentPos.cell_x, false);
			Condition unset_cell_y = new Condition(agentTag + "AtY" + agentPos.cell_y, false);
			beliefs.apply(unset_cell_x);
			beliefs.apply(unset_cell_y);
			
			cw_agent_positions[agent_num] = null;
    	}
    }
    
    public void placeAgent_OW(int agent_num, OfficeWorldPosition newPos)
    {
    	removeAgent(agent_num);
    	
		String agentTag = (agent_num == 0) ? "" : "Op";
		
		Condition set_room = new Condition(agentTag + "AtRoom" + newPos.roomID(), true);
		Condition set_cell = new Condition(agentTag + "At" + newPos.cellID(), true);
		beliefs.apply(set_room);
		beliefs.apply(set_cell);
		
		ow_agent_positions[agent_num] = newPos;
    }
    
    public void placeAgent_CW(int agent_num, CraftWorldPosition newPos)
    {
    	removeAgent(agent_num);
    	
		String agentTag = (agent_num == 0) ? "" : "Opponent";
		
		Condition set_cell_x = new Condition(agentTag + "AtX" + newPos.cell_x, true);
		Condition set_cell_y = new Condition(agentTag + "AtY" + newPos.cell_y, true);
		
		beliefs.apply(set_cell_x);
		beliefs.apply(set_cell_y);
		
		cw_agent_positions[agent_num] = newPos;
    }
    
    public List<Decision> getAvailableDecisions(boolean[] intentionAvailable)
    {
    	List<Decision> result = new ArrayList<Decision>();
    	
        for (int i = 0; i < intentions.size(); i++)
        {
        	if (!intentionAvailable[i])
        	{
        		continue;
        	}
        	
            // ignore the intention which already has been achieved
            if (intentions.get(i) != null)
            {
                if (intentions.get(i) instanceof ActionNode)
                {
                    ActionNode action = (ActionNode)intentions.get(i);
                    Condition[] prec = action.getPrec();

                    if(beliefs.evaluate(prec))
                    {
                    	result.add(new Decision_GPT(i, -1, false));
                    }
                }
                else if (intentions.get(i) instanceof GoalNode)
                {
                    GoalNode goal = (GoalNode)intentions.get(i);
                    PlanNode[] pls = goal.getPlans();

                    for(int j = 0; j < pls.length; j++)
                    {
                        Condition[] prec = pls[j].getPrec();

                        if(beliefs.evaluate(prec))
                        {
                        	result.add(new Decision_GPT(i, j, false));
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public BeliefBase getBeliefBase()
    {
        return this.beliefs;
    }

    public ArrayList<TreeNode> getIntentions()
    {
        return this.intentions;
    }
    
    public int getTotalNumberOfGPTs()
    {
    	return this.intentions.size();
    }
    
    public String getNextPatrolPoint(int agent_num)
    {
    	String agentTag = (agent_num == 0)? "" : "Op";
    	
    	for (int i = 0; i < 4; i++)
    	{
    		String letter = OfficeWorldState.int_to_patrol_letter.get(i);
    		Condition cIsCurrentPatrolPoint = new Condition(agentTag + "Patrol" + letter, true);
    		
    		if (beliefs.evaluate(cIsCurrentPatrolPoint))
    		{
    			return letter;
    		}
    	}
    	
    	return null;
    }
    
    public boolean allTasksComplete(int agent_num)
    {
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	String agentTag = (agent_num == 0)? "" : "Op";
	    	
	    	Condition cCoffeeDelivered = new Condition(agentTag + "CoffeeDelivered", true);
	    	Condition cMailDelivered = new Condition(agentTag + "MailDelivered", true);
	    	
	    	if (!beliefs.evaluate(cCoffeeDelivered) || !beliefs.evaluate(cMailDelivered))
	    	{
	    		return false;
	    	}
    		
    		return true;
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
	    	String agentTag = (agent_num == 0)? "" : "Opponent";
	    	
    		for (CraftWorldItem item : Main.cw_settings.goalItems.get(agent_num))
    		{
		    	Condition cHaveItem = new Condition(agentTag + "Have" + item.text_rep, true);

		    	if (!beliefs.evaluate(cHaveItem))
		    	{
		    		return false;
		    	}
    		}
    		
    		return true;
    	}
    	else
    	{
    		return true;
    	}
    }
    
    public boolean intentionComplete(int intention_idx)
    {
    	return intentions.get(intention_idx) == null;
    }
    
    public int getNumIntentionsCompleted(boolean[] intention_available)
    {
    	int intentions_completed = 0;
    	
        for (int i = 0; i < intentions.size(); i++)
        {
        	if (intention_available[i] && intentionComplete(i))
        	{
                intentions_completed++;
        	}
        }

        return intentions_completed;
    }
    
    private void resetSpecifiedIntentions(boolean[] doReset, boolean verbose)
    {
    	for (int i = 0; i < doReset.length; i++)
        {
    		if (doReset[i])
    		{
    			if (verbose)
    			{
    				Log.info("Resetting intention " + i + "...");
    			}
    			
    			TreeNode topNode = intentions.get(i);
    			while (topNode.getParent() != null)
    			{
    				topNode = topNode.getParent();
    			}
    			
    			intentions.set(i, topNode);
    		}
        }
    }
    
    public void resetIncompleteIntentions(int agent_num, boolean[] intentionAvailable, boolean verbose)
    {
    	boolean[] incomplete_intentions = new boolean[intentions.size()];
    	
    	for (int i = 0; i < intentions.size(); i++)
        {
    		if (intentionAvailable[i])
    		{
    			incomplete_intentions[i] = !intentionComplete(i);
    		}
        }
    	
    	resetSpecifiedIntentions(incomplete_intentions, verbose);
    	actionTakenSinceReset[agent_num] = false;
    }
    
    public void resetAllIncompleteIntentions(boolean verbose)
    {
    	boolean[] incomplete_intentions = new boolean[intentions.size()];
    	
    	for (int i = 0; i < intentions.size(); i++)
        {
			incomplete_intentions[i] = !intentionComplete(i);
			
			if (incomplete_intentions[i])
			{
				Log.info("Resetting intention " + i + "...", verbose);
			}
			else
			{
				Log.info("Not resetting intention " + i + ", since it is complete.", verbose);
			}
        }
    	
    	resetSpecifiedIntentions(incomplete_intentions, verbose);
    	actionTakenSinceReset[0] = false;
    	actionTakenSinceReset[1] = false;
    }
    
    public void resetFailedIntentions(int agent_num, boolean[] intentionAvailable, boolean verbose)
    {
    	boolean[] failed_intentions = new boolean[intentions.size()];
    	
    	for (int i = 0; i < intentions.size(); i++)
        {
    		if (intentionAvailable[i])
    		{
            	// If an intention is complete then it isn't failed.
        		// Default to true for incomplete intentions, but override below if it is still progressable.
    			failed_intentions[i] = !intentionComplete(i);
    		}
        }
    	
    	// See which intentions are still available.
    	List<Decision> availableDecisions = getAvailableDecisions(intentionAvailable);
    	
    	for (Decision decision : availableDecisions)
    	{
    		Decision_GPT decision_gpt = (Decision_GPT)decision;
    		
    		// If an intention is available for progression then it isn't failed.
    		if (decision_gpt.iChoice != -1)
    		{
    			failed_intentions[decision_gpt.iChoice] = false;
    		}
    	}
    	
    	resetSpecifiedIntentions(failed_intentions, verbose);
    	actionTakenSinceReset[agent_num] = false;
    }
    
    public double getStateScore(int agent_num)
    {
    	double score = 0.0;
    	
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
	    	if (agentsInSameRoom())
	    	{
	    		return Main.FAIL_SCORE;
	    	}
	    	
	    	// TODO: The below logic will no longer work for partial vision -- will need to be revisited if this work is extended to the partial vision case.
	    	String agentTag = (agent_num == 0)? "" : "Op";
	    	
	    	Condition cCoffeeDelivered = new Condition(agentTag + "CoffeeDelivered", true);
	    	Condition cMailDelivered = new Condition(agentTag + "MailDelivered", true);
	    	
	    	score = (beliefs.evaluate(cCoffeeDelivered) && beliefs.evaluate(cMailDelivered)) ? 1.0 : 0.0;
	        
	        return score;
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
    		for (CraftWorldItem item : Main.cw_settings.goalItems.get(agent_num))
    		{
		    	String agentTag = (agent_num == 0)? "" : "Opponent";
		    	Condition cHaveItem = new Condition(agentTag + "Have" + item.text_rep, true);

		    	if (beliefs.evaluate(cHaveItem))
		    	{
		    		score += 1.0;
		    	}
    		}
    		
    		return score;
    	}
    	else
    	{
    		return score;
    	}
    }

    /**
     * @return a copy of the current state
     */
    @Override
    public State clone()
    {
    	CraftWorldPosition cw_pos_0_clone = (cw_agent_positions[0] == null) ? null : new CraftWorldPosition(cw_agent_positions[0]);
    	CraftWorldPosition cw_pos_1_clone = (cw_agent_positions[1] == null) ? null : new CraftWorldPosition(cw_agent_positions[1]);
    	
    	OfficeWorldPosition ow_pos_0_clone = (ow_agent_positions[0] == null) ? null : new OfficeWorldPosition(ow_agent_positions[0]);
    	OfficeWorldPosition ow_pos_1_clone = (ow_agent_positions[1] == null) ? null : new OfficeWorldPosition(ow_agent_positions[1]);
    	
        // generate the new state
        return new State(forest_name, beliefs.clone(), beliefsPrevious.clone(), new ArrayList<TreeNode>(intentions), playerTurn, age, gameOverAge, agentReturn, actionTakenSinceReset.clone(),
        	notProgressible, new CraftWorldPosition[] {cw_pos_0_clone, cw_pos_1_clone}, new OfficeWorldPosition[] {ow_pos_0_clone, ow_pos_1_clone});
    }
}
