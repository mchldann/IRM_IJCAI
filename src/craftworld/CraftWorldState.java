package craftworld;

import java.util.ArrayList;
import java.util.List;

import beliefbase.Condition;
import main.Main;
import scheduler.State;
import util.Enums;
import util.Enums.Action;
import util.Enums.CraftWorldItem;

public class CraftWorldState {

	public class CraftWorldNeighbourInfo
	{
		CraftWorldState[] states;
		boolean canCraft;
	}
	
	public CraftWorldPosition[] position;

	public boolean[][] haveItem;
	public boolean[] treeStatus;
	public boolean[] grassStatus;
	public boolean[] ironStatus;
	public boolean[] goldStatus;
	public boolean[] gemStatus;
	
	public CraftWorldState(State s)
	{
		this.position = new CraftWorldPosition[2];
		this.position[0] = s.cw_agent_positions[0];
		this.position[1] = s.cw_agent_positions[1];
		
		haveItem = new boolean[Enums.CRAFT_WORLD_NUM_ITEMS][2];
		
		for (CraftWorldItem item : Enums.CraftWorldItem.values())
		{
			Condition cHaveItem = new Condition("Have" + item.text_rep, true);
			Condition cOpHaveItem = new Condition("OpponentHave" + item.text_rep, true);
			haveItem[item.id][0] = s.getBeliefBase().evaluate(cHaveItem);
			haveItem[item.id][1] = s.getBeliefBase().evaluate(cOpHaveItem);
		}
		
		treeStatus = new boolean[Main.cw_settings.tx.size()];
		for (int i = 0; i < treeStatus.length; i++)
		{
			int tree_x = Main.cw_settings.tx.get(i);
			int tree_y = Main.cw_settings.ty.get(i);
			Condition cTreeRemains = new Condition("TreeAtX" + tree_x + "Y" + tree_y, true);
			treeStatus[i] = s.getBeliefBase().evaluate(cTreeRemains);
		}
		
		grassStatus = new boolean[Main.cw_settings.grassx.size()];
		for (int i = 0; i < grassStatus.length; i++)
		{
			int grass_x = Main.cw_settings.grassx.get(i);
			int grass_y = Main.cw_settings.grassy.get(i);
			Condition cGrassRemains = new Condition("GrassAtX" + grass_x + "Y" + grass_y, true);
			grassStatus[i] = s.getBeliefBase().evaluate(cGrassRemains);
		}
		
		ironStatus = new boolean[Main.cw_settings.ironx.size()];
		for (int i = 0; i < ironStatus.length; i++)
		{
			int iron_x = Main.cw_settings.ironx.get(i);
			int iron_y = Main.cw_settings.irony.get(i);
			Condition cIronRemains = new Condition("IronAtX" + iron_x + "Y" + iron_y, true);
			ironStatus[i] = s.getBeliefBase().evaluate(cIronRemains);
		}
		
		goldStatus = new boolean[Main.cw_settings.goldx.size()];
		for (int i = 0; i < goldStatus.length; i++)
		{
			int gold_x = Main.cw_settings.goldx.get(i);
			int gold_y = Main.cw_settings.goldy.get(i);
			Condition cGoldRemains = new Condition("GoldAtX" + gold_x + "Y" + gold_y, true);
			goldStatus[i] = s.getBeliefBase().evaluate(cGoldRemains);
		}
		
		gemStatus = new boolean[Main.cw_settings.gemx.size()];
		for (int i = 0; i < gemStatus.length; i++)
		{
			int gem_x = Main.cw_settings.gemx.get(i);
			int gem_y = Main.cw_settings.gemy.get(i);
			Condition cGemRemains = new Condition("GemAtX" + gem_x + "Y" + gem_y, true);
			gemStatus[i] = s.getBeliefBase().evaluate(cGemRemains);
		}
	}
	
	public CraftWorldState(CraftWorldState cws)
	{
		this.position = new CraftWorldPosition[2];
		this.position[0] = (cws.position[0] == null) ? null : new CraftWorldPosition(cws.position[0]);
		this.position[1] = (cws.position[1] == null) ? null : new CraftWorldPosition(cws.position[1]);
		
		this.haveItem = new boolean[Enums.CRAFT_WORLD_NUM_ITEMS][2];
        for (int i = 0; i < this.haveItem.length; i++)
        {
        	this.haveItem[i] = cws.haveItem[i].clone();
        }

		this.treeStatus = cws.treeStatus.clone();
		this.grassStatus = cws.grassStatus.clone();
		this.ironStatus = cws.ironStatus.clone();
		this.gemStatus = cws.gemStatus.clone();
		this.goldStatus = cws.goldStatus.clone();
	}
	
	public CraftWorldState(int qLearningID, CraftWorldQLearner learner)
	{
		// First, set default values
		this.position = new CraftWorldPosition[2];
		ironStatus = new boolean[Main.cw_settings.ironx.size()];
		grassStatus = new boolean[Main.cw_settings.grassx.size()];
		treeStatus = new boolean[Main.cw_settings.tx.size()];
		goldStatus = new boolean[Main.cw_settings.goldx.size()];
		gemStatus = new boolean[Main.cw_settings.gemx.size()];
		
		this.haveItem = new boolean[Enums.CRAFT_WORLD_NUM_ITEMS][2];
		
		int remainder = qLearningID;
		
		// Parse position of agent == agent_num
		int divisor = (int)(Math.pow(2, getInventoryPlusStatusLength(learner)) * Main.cw_settings.grid_size);
		int cellY = remainder / divisor;
		remainder -= cellY * divisor;
		
		divisor = (int)(Math.pow(2, getInventoryPlusStatusLength(learner)));
		int cellX = remainder / divisor;
		remainder -= cellX * divisor;
		
		this.position[1 - learner.agent_num] = null; // Dummy value
		this.position[learner.agent_num] = new CraftWorldPosition(cellX, cellY);
		
		// Parse iron status
		int remainingPower = getInventoryPlusStatusLength(learner) - 1;
		for (int i = ironStatus.length - 1; i >= 0; i--)
		{
			divisor = (int)(Math.pow(2, remainingPower));
			remainingPower--;
			ironStatus[i] = (remainder / divisor) == 1 ? true : false;
			remainder -= (remainder / divisor) * divisor;
		}
		
		// Parse grass status
		for (int i = grassStatus.length - 1; i >= 0; i--)
		{
			divisor = (int)(Math.pow(2, remainingPower));
			remainingPower--;
			grassStatus[i] = (remainder / divisor) == 1 ? true : false;
			remainder -= (remainder / divisor) * divisor;
		}
		
		// Parse tree status
		for (int i = treeStatus.length - 1; i >= 0; i--)
		{
			divisor = (int)(Math.pow(2, remainingPower));
			remainingPower--;
			treeStatus[i] = (remainder / divisor) == 1 ? true : false;
			remainder -= (remainder / divisor) * divisor;
		}
		
		// Parse gem status
		if (learner.allowed_inventory.contains(CraftWorldItem.GEM))
		{
			for (int i = gemStatus.length - 1; i >= 0; i--)
			{
				divisor = (int)(Math.pow(2, remainingPower));
				remainingPower--;
				gemStatus[i] = (remainder / divisor) == 1 ? true : false;
				remainder -= (remainder / divisor) * divisor;
			}
		}
		
		// Parse gold status
		if (learner.allowed_inventory.contains(CraftWorldItem.GOLD))
		{
			for (int i = goldStatus.length - 1; i >= 0; i--)
			{
				divisor = (int)(Math.pow(2, remainingPower));
				remainingPower--;
				goldStatus[i] = (remainder / divisor) == 1 ? true : false;
				remainder -= (remainder / divisor) * divisor;
			}
		}

		// Parse inventory
		for (int i = learner.allowed_inventory.size() - 1; i >= 0; i--)
		{
			divisor = (int)(Math.pow(2, remainingPower));
			remainingPower--;
			haveItem[learner.allowed_inventory.get(i).id][learner.agent_num] = (remainder / divisor) == 1 ? true : false;
			remainder -= (remainder / divisor) * divisor;
		}
	}
	
	public CraftWorldNeighbourInfo getNeighbourInfo(int agent_num)
	{
		CraftWorldNeighbourInfo result = new CraftWorldNeighbourInfo();
		
		if (this.position[agent_num] == null)
		{
			return null;
		}
		
		result.states = new CraftWorldState[] {null, null, null, null, null};
		CraftWorldPosition[] neighbours = this.position[agent_num].get_neighbours();
		
		for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
		{
			if (action < 4 && neighbours[action] == null)
			{
				continue;
			}
			
			CraftWorldState newState = new CraftWorldState(this);
			
			if (action < 4)
			{
				newState.position[agent_num] = neighbours[action];
			}
			
			if (action == Action.CRAFT_OR_GATHER_ITEM.id)
			{
				int treeIdx = newState.getTreeIdx(agent_num);
				if (treeIdx > -1 && !haveItem[CraftWorldItem.WOOD.id][agent_num]) // Don't waste the tree if the agent already has wood
				{
					newState.haveItem[CraftWorldItem.WOOD.id][agent_num] = true;
					newState.treeStatus[treeIdx] = false;
					result.canCraft = true;
				}
				
				int grassIdx = newState.getGrassIdx(agent_num);
				if (grassIdx > -1 && !haveItem[CraftWorldItem.GRASS.id][agent_num]) // Don't waste the grass if the agent already has it
				{
					newState.haveItem[CraftWorldItem.GRASS.id][agent_num] = true;
					newState.grassStatus[grassIdx] = false;
					result.canCraft = true;
				}
				
				int ironIdx = newState.getIronIdx(agent_num);
				if (ironIdx > -1 && !haveItem[CraftWorldItem.IRON.id][agent_num]) // Don't waste the iron if the agent already has it
				{
					newState.haveItem[CraftWorldItem.IRON.id][agent_num] = true;
					newState.ironStatus[ironIdx] = false;
					result.canCraft = true;
				}
				
				// Craft stick
				if (newState.atWorkbench(agent_num) && haveItem[CraftWorldItem.WOOD.id][agent_num]
					&& !haveItem[CraftWorldItem.STICK.id][agent_num] // Don't waste materials if the agent already has a stick
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.STICK)) // Only craft a stick if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.WOOD.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.STICK.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft cloth
				if (newState.atFactory(agent_num) && haveItem[CraftWorldItem.GRASS.id][agent_num]
					&& !haveItem[CraftWorldItem.CLOTH.id][agent_num] // Don't waste materials if the agent already has a cloth
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.CLOTH)) // Only craft a cloth if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.GRASS.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.CLOTH.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft bridge
				if (newState.atFactory(agent_num) && haveItem[CraftWorldItem.WOOD.id][agent_num] && haveItem[CraftWorldItem.IRON.id][agent_num]
					&& !haveItem[CraftWorldItem.BRIDGE.id][agent_num] // Don't waste materials if the agent already has a bridge
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.BRIDGE)) // Only craft a bridge if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.WOOD.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.IRON.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.BRIDGE.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft axe
				if (newState.atToolshed(agent_num) && haveItem[CraftWorldItem.STICK.id][agent_num] && haveItem[CraftWorldItem.IRON.id][agent_num]
					&& !haveItem[CraftWorldItem.AXE.id][agent_num] // Don't waste materials if the agent already has an axe
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.AXE)) // Only craft an axe if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.STICK.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.IRON.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.AXE.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft plank
				if (newState.atToolshed(agent_num) && haveItem[CraftWorldItem.WOOD.id][agent_num]
					&& !haveItem[CraftWorldItem.PLANK.id][agent_num] // Don't waste materials if the agent already has a plank
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.PLANK)) // Only craft a plank if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.WOOD.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.PLANK.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft rope
				if (newState.atToolshed(agent_num) && haveItem[CraftWorldItem.GRASS.id][agent_num]
					&& !haveItem[CraftWorldItem.ROPE.id][agent_num] // Don't waste materials if the agent already has a rope
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.ROPE)) // Only craft a rope if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.GRASS.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.ROPE.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Craft bed
				if (newState.atWorkbench(agent_num) && haveItem[CraftWorldItem.PLANK.id][agent_num] && haveItem[CraftWorldItem.GRASS.id][agent_num]
					&& !haveItem[CraftWorldItem.BED.id][agent_num] // Don't waste materials if the agent already has a bed
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.BED)) // Only craft a bed if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.PLANK.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.GRASS.id][agent_num] = false;
					newState.haveItem[CraftWorldItem.BED.id][agent_num] = true;
					result.canCraft = true;
				}
				
				// Mine gem
				int gemIdx = newState.getGemIdx(agent_num);
				if (gemIdx > -1 && haveItem[CraftWorldItem.AXE.id][agent_num]
					&& !haveItem[CraftWorldItem.GEM.id][agent_num] // Don't waste the gem if the agent already has it
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.GEM)) // Only take the gem if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.GEM.id][agent_num] = true;
					newState.gemStatus[gemIdx] = false;
					result.canCraft = true;
				}
				
				// Get gold
				int goldIdx = newState.getGoldIdx(agent_num);
				if (goldIdx > -1 && haveItem[CraftWorldItem.BRIDGE.id][agent_num]
					&& !haveItem[CraftWorldItem.GOLD.id][agent_num] // Don't waste the gold if the agent already has it
					&& Main.cw_settings.itemsToMake.get(agent_num).contains(CraftWorldItem.GOLD)) // Only take the gold if it's one of the items the agent is meant to craft
				{
					newState.haveItem[CraftWorldItem.GOLD.id][agent_num] = true;
					newState.goldStatus[goldIdx] = false;
					result.canCraft = true;
				}
			}

			if (action < 4 || result.canCraft) // If it's not possible to craft, set the neighbour for the craft action to null
			{
				result.states[action] = newState;
			}
		}
		
		return result;
	}
	
	public int getQLearningID(CraftWorldQLearner learner)
	{
		List<Boolean> binaryList = new ArrayList<Boolean>();

		for (int i = 0; i < learner.allowed_inventory.size(); i++)
		{
			binaryList.add(haveItem[learner.allowed_inventory.get(i).id][learner.agent_num]);
		}
		
		if (learner.allowed_inventory.contains(CraftWorldItem.GOLD))
		{
			for (int i = 0; i < goldStatus.length; i++)
			{
				binaryList.add(goldStatus[i]);
			}
		}
		
		if (learner.allowed_inventory.contains(CraftWorldItem.GEM))
		{
			for (int i = 0; i < gemStatus.length; i++)
			{
				binaryList.add(gemStatus[i]);
			}
		}
		
		for (int i = 0; i < treeStatus.length; i++)
		{
			binaryList.add(treeStatus[i]);
		}
		
		for (int i = 0; i < grassStatus.length; i++)
		{
			binaryList.add(grassStatus[i]);
		}
		
		for (int i = 0; i < ironStatus.length; i++)
		{
			binaryList.add(ironStatus[i]);
		}
		
		int result = 0;
		for (int i = 0; i < binaryList.size(); i++)
		{
			result += (binaryList.get(i) ? 1 : 0) * Math.pow(2, i);
		}
		
		// Max value so far is 2^0 + 2^1 + ... + 2^(binaryList.size() - 1) = 2^binaryList.size() - 1
		result += position[learner.agent_num].cell_x * Math.pow(2, binaryList.size()); // Potentially adds (cw_settings.grid_size - 1) * Math.pow(2, binaryList.size())
		
		// Max value so far is Math.pow(2, binaryList.size()) + (cw_settings.grid_size - 1) * Math.pow(2, binaryList.size()) - 1
		// = Math.pow(2, binaryList.size() * cw_settings.grid_size - 1
		result += position[learner.agent_num].cell_y * (Math.pow(2, binaryList.size()) * Main.cw_settings.grid_size); // Potentially adds (cw_settings.grid_size - 1) * (Math.pow(2, binaryList.size()) * cw_settings.grid_size)
		
		return result;
	}
	
	private int getInventoryPlusStatusLength(CraftWorldQLearner learner)
	{
		return learner.allowed_inventory.size()
	        + (learner.allowed_inventory.contains(CraftWorldItem.GEM) ? gemStatus.length : 0)
	        + (learner.allowed_inventory.contains(CraftWorldItem.GOLD) ? goldStatus.length : 0)
            + treeStatus.length + grassStatus.length + ironStatus.length;
	}
	
	public int getStateSpaceSize(CraftWorldQLearner learner)
	{
		return Main.cw_settings.grid_size * Main.cw_settings.grid_size * (int)Math.pow(2, getInventoryPlusStatusLength(learner));
	}
	
	private int atEntity(List<Integer> xs, List<Integer> ys, int agent_num)
	{
		for (int i = 0; i < xs.size(); i++)
		{
			CraftWorldPosition entityPos = new CraftWorldPosition(xs.get(i), ys.get(i));
			
			if (entityPos.equals(position[agent_num]))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private int atConsumableEntity(boolean[] statusArr, List<Integer> xs, List<Integer> ys, int agent_num)
	{
		for (int i = 0; i < xs.size(); i++)
		{
			if (!statusArr[i])
			{
				continue;
			}
			
			CraftWorldPosition entityPos = new CraftWorldPosition(xs.get(i), ys.get(i));
			
			if (entityPos.equals(position[agent_num]))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	public boolean atWorkbench(int agent_num)
	{
		return atEntity(Main.cw_settings.wbx, Main.cw_settings.wby, agent_num) > -1;
	}
	
	public boolean atFactory(int agent_num)
	{
		return atEntity(Main.cw_settings.facx, Main.cw_settings.facy, agent_num) > -1;
	}
	
	public boolean atToolshed(int agent_num)
	{
		return atEntity(Main.cw_settings.tsx, Main.cw_settings.tsy, agent_num) > -1;
	}
	
	public int getTreeIdx(int agent_num)
	{
		return atConsumableEntity(treeStatus, Main.cw_settings.tx, Main.cw_settings.ty, agent_num);
	}
	
	public int getGrassIdx(int agent_num)
	{
		return atConsumableEntity(grassStatus, Main.cw_settings.grassx, Main.cw_settings.grassy, agent_num);
	}
	
	public int getIronIdx(int agent_num)
	{
		return atConsumableEntity(ironStatus, Main.cw_settings.ironx, Main.cw_settings.irony, agent_num);
	}
	
	public int getGoldIdx(int agent_num)
	{
		return atConsumableEntity(goldStatus, Main.cw_settings.goldx, Main.cw_settings.goldy, agent_num);
	}
	
	public int getGemIdx(int agent_num)
	{
		return atConsumableEntity(gemStatus, Main.cw_settings.gemx, Main.cw_settings.gemy, agent_num);
	}
}
