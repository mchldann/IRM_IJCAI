package decision;

import beliefbase.Condition;
import craftworld.CraftWorldPosition;
import craftworld.CraftWorldRenderer;
import craftworld.CraftWorldState;
import main.Main;
import scheduler.Match;
import scheduler.State;
import util.Enums.Action;
import util.Enums.CraftWorldItem;
import util.Log;

public class Decision_RL_CW extends Decision_RL {

	private boolean forced_pass;
	private CraftWorldPosition start_pos;
	private CraftWorldPosition end_pos;
	public double greedyQVal;
	public int initial_id;
	
    public Decision_RL_CW(int action, boolean forced_pass, CraftWorldPosition start_pos, CraftWorldPosition end_pos, double greedyQVal, int initial_id)
    {
    	this.action = action;
    	this.forced_pass = forced_pass;
    	this.start_pos = start_pos;
    	this.end_pos = end_pos;
    	this.greedyQVal = greedyQVal;
    	this.initial_id = initial_id;
    }
    
	@Override
	public boolean is_pass()
	{
		return action == -1 || forced_pass;
	}
	
	@Override
	public boolean is_pass_forced()
	{
		return forced_pass;
	}

	@Override
	public void update_state(Match m, State currentState, boolean verbose, boolean draw)
	{
        if (is_pass())
        {
        	currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
        	return;
        }
        else
        {
        	currentState.actionTakenSinceReset[currentState.playerTurn] = true;
        }
        
        CraftWorldState start_cws = new CraftWorldState(currentState);
        
        if (action == Action.CRAFT_OR_GATHER_ITEM.id)
        {
        	Log.info("Crafting or gathering", verbose);
        }
        else
        {
        	Log.info("Moving from: " + this.start_pos + " to " + this.end_pos + ", greedy Q-val = " + this.greedyQVal, verbose);
        }
        
		String agentTag = (currentState.playerTurn == 0)? "" : "Opponent";
		
		Condition unset_start_x = new Condition(agentTag + "AtX" + this.start_pos.cell_x, false);
		Condition unset_start_y = new Condition(agentTag + "AtY" + this.start_pos.cell_y, false);
		
		Condition set_end_x = new Condition(agentTag + "AtX" + this.end_pos.cell_x, true);
		Condition set_end_y = new Condition(agentTag + "AtY" + this.end_pos.cell_y, true);
		
		currentState.beliefs.apply(unset_start_x);
		currentState.beliefs.apply(unset_start_y);
		currentState.beliefs.apply(set_end_x);
		currentState.beliefs.apply(set_end_y);
		
		currentState.cw_agent_positions[currentState.playerTurn] = this.end_pos;
		
		CraftWorldState new_cws = new CraftWorldState(currentState);
		
		if (this.action == Action.CRAFT_OR_GATHER_ITEM.id)
		{
			int treeIdx = new_cws.getTreeIdx(currentState.playerTurn);
			if (treeIdx > -1 && !start_cws.haveItem[CraftWorldItem.WOOD.id][currentState.playerTurn]) // Don't waste the tree if the agent already has wood
			{
				Condition cHaveWood = new Condition(agentTag + "HaveWood", true);
				currentState.beliefs.apply(cHaveWood);
	
				Condition cRemoveTree = new Condition("TreeAtX" + Main.cw_settings.tx.get(treeIdx) + "Y" + Main.cw_settings.ty.get(treeIdx), false);
				currentState.beliefs.apply(cRemoveTree);
			}
			
			int grassIdx = new_cws.getGrassIdx(currentState.playerTurn);
			if (grassIdx > -1 && !start_cws.haveItem[CraftWorldItem.GRASS.id][currentState.playerTurn]) // Don't waste the grass if the agent already has it
			{
				Condition cHaveGrass = new Condition(agentTag + "HaveGrass", true);
				currentState.beliefs.apply(cHaveGrass);
	
				Condition cRemoveGrass = new Condition("GrassAtX" + Main.cw_settings.grassx.get(grassIdx) + "Y" + Main.cw_settings.grassy.get(grassIdx), false);
				currentState.beliefs.apply(cRemoveGrass);
			}
			
			int ironIdx = new_cws.getIronIdx(currentState.playerTurn);
			if (ironIdx > -1 && !start_cws.haveItem[CraftWorldItem.IRON.id][currentState.playerTurn]) // Don't waste the iron if the agent already has it
			{
				Condition cHaveIron = new Condition(agentTag + "HaveIron", true);
				currentState.beliefs.apply(cHaveIron);
	
				Condition cRemoveIron = new Condition("IronAtX" + Main.cw_settings.ironx.get(ironIdx) + "Y" + Main.cw_settings.irony.get(ironIdx), false);
				currentState.beliefs.apply(cRemoveIron);
			}
			
			// Craft stick
			if (new_cws.atWorkbench(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.WOOD.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.STICK.id][currentState.playerTurn] // Don't waste materials if the agent already has a stick
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.STICK)) // Only craft a stick if it's one of the items the agent is meant to craft
				
			{
				Condition cDoNotHaveWood = new Condition(agentTag + "HaveWood", false);
				currentState.beliefs.apply(cDoNotHaveWood);
				
				Condition cHaveStick = new Condition(agentTag + "HaveStick", true);
				currentState.beliefs.apply(cHaveStick);
			}
			
			// Craft cloth
			if (new_cws.atFactory(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.GRASS.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.CLOTH.id][currentState.playerTurn] // Don't waste materials if the agent already has a cloth
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.CLOTH)) // Only craft a cloth if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHaveGrass = new Condition(agentTag + "HaveGrass", false);
				currentState.beliefs.apply(cDoNotHaveGrass);
				
				Condition cHaveCloth = new Condition(agentTag + "HaveCloth", true);
				currentState.beliefs.apply(cHaveCloth);
			}
			
			// Craft bridge
			if (new_cws.atFactory(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.WOOD.id][currentState.playerTurn] && start_cws.haveItem[CraftWorldItem.IRON.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.BRIDGE.id][currentState.playerTurn] // Don't waste materials if the agent already has a bridge
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.BRIDGE)) // Only craft a bridge if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHaveWood = new Condition(agentTag + "HaveWood", false);
				currentState.beliefs.apply(cDoNotHaveWood);
				
				Condition cDoNotHaveIron = new Condition(agentTag + "HaveIron", false);
				currentState.beliefs.apply(cDoNotHaveIron);
				
				Condition cHaveBridge = new Condition(agentTag + "HaveBridge", true);
				currentState.beliefs.apply(cHaveBridge);
			}
	
			// Craft axe
			if (new_cws.atToolshed(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.STICK.id][currentState.playerTurn] && start_cws.haveItem[CraftWorldItem.IRON.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.AXE.id][currentState.playerTurn] // Don't waste materials if the agent already has an axe
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.AXE)) // Only craft an axe if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHaveStick = new Condition(agentTag + "HaveStick", false);
				currentState.beliefs.apply(cDoNotHaveStick);
				
				Condition cDoNotHaveIron = new Condition(agentTag + "HaveIron", false);
				currentState.beliefs.apply(cDoNotHaveIron);
				
				Condition cHaveAxe = new Condition(agentTag + "HaveAxe", true);
				currentState.beliefs.apply(cHaveAxe);
			}
			
			// Craft plank
			if (new_cws.atToolshed(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.WOOD.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.PLANK.id][currentState.playerTurn] // Don't waste materials if the agent already has a plank
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.PLANK)) // Only craft a plank if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHaveWood = new Condition(agentTag + "HaveWood", false);
				currentState.beliefs.apply(cDoNotHaveWood);
				
				Condition cHavePlank = new Condition(agentTag + "HavePlank", true);
				currentState.beliefs.apply(cHavePlank);
			}
			
			// Craft rope
			if (new_cws.atToolshed(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.GRASS.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.ROPE.id][currentState.playerTurn] // Don't waste materials if the agent already has a rope
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.ROPE)) // Only craft a rope if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHaveGrass = new Condition(agentTag + "HaveGrass", false);
				currentState.beliefs.apply(cDoNotHaveGrass);
				
				Condition cHaveRope = new Condition(agentTag + "HaveRope", true);
				currentState.beliefs.apply(cHaveRope);
			}
			
			// Craft bed
			if (new_cws.atWorkbench(currentState.playerTurn) && start_cws.haveItem[CraftWorldItem.PLANK.id][currentState.playerTurn] && start_cws.haveItem[CraftWorldItem.GRASS.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.BED.id][currentState.playerTurn] // Don't waste materials if the agent already has a bed
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.BED)) // Only craft a bed if it's one of the items the agent is meant to craft
			{
				Condition cDoNotHavePlank = new Condition(agentTag + "HavePlank", false);
				currentState.beliefs.apply(cDoNotHavePlank);
				
				Condition cDoNotHaveGrass = new Condition(agentTag + "HaveGrass", false);
				currentState.beliefs.apply(cDoNotHaveGrass);
				
				Condition cHaveBed = new Condition(agentTag + "HaveBed", true);
				currentState.beliefs.apply(cHaveBed);
			}
	
			// Mine gem
			int gemIdx = new_cws.getGemIdx(currentState.playerTurn);
			if (gemIdx > -1 && start_cws.haveItem[CraftWorldItem.AXE.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.GEM.id][currentState.playerTurn] // Don't waste the gem if the agent already has it
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.GEM)) // Only take the gem if it's one of the items the agent is meant to craft
			{
				Condition cHaveGem = new Condition(agentTag + "HaveGem", true);
				currentState.beliefs.apply(cHaveGem);
	
				Condition cRemoveGem = new Condition("GemAtX" + Main.cw_settings.gemx.get(gemIdx) + "Y" + Main.cw_settings.gemy.get(gemIdx), false);
				currentState.beliefs.apply(cRemoveGem);
			}
			
			// Mine gold
			int goldIdx = new_cws.getGoldIdx(currentState.playerTurn);
			if (goldIdx > -1 && start_cws.haveItem[CraftWorldItem.BRIDGE.id][currentState.playerTurn]
				&& !start_cws.haveItem[CraftWorldItem.GOLD.id][currentState.playerTurn] // Don't waste the gold if the agent already has it
				&& Main.cw_settings.itemsToMake.get(currentState.playerTurn).contains(CraftWorldItem.GOLD)) // Only take the gold if it's one of the items the agent is meant to craft
			{
				Condition cHaveGold = new Condition(agentTag + "HaveGold", true);
				currentState.beliefs.apply(cHaveGold);
	
				Condition cRemoveGold = new Condition("GoldAtX" + Main.cw_settings.goldx.get(goldIdx) + "Y" + Main.cw_settings.goldy.get(goldIdx), false);
				currentState.beliefs.apply(cRemoveGold);
			}
		}

		currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
		
		currentState.age++;
		if (draw)
		{
	        CraftWorldRenderer renderer = new CraftWorldRenderer();
	        renderer.draw(m.m_name + "_" + String.format("%05d", m.drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
		}
	}
}
