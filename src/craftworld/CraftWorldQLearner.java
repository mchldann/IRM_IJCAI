package craftworld;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import craftworld.CraftWorldState.CraftWorldNeighbourInfo;
import main.Main;
import util.Enums;
import util.Enums.CraftWorldItem;
import util.Log;

public class CraftWorldQLearner {

	public class StateValuePair
	{
		public int action;
		public float value;
		
		public StateValuePair(int action, float value)
		{
			this.action = action;
			this.value = value;
		}
	}
	
	private int stateSpaceSize;
	public float[][][] qTable;
	public int[][] cachedSuccessor;
	public byte[][][] cachedReward; // Since all rewards are small integers this saves a bit of space
	public boolean[][] cachedTerminal;
	public CraftWorldState initialState;
	public int agent_num;
	public List<CraftWorldItem> allowed_inventory;

    private static Random rm = new Random();
	
	public CraftWorldQLearner(CraftWorldState initialState, int agent_num)
	{
		setAllowedInventory(agent_num);
		
		this.stateSpaceSize = initialState.getStateSpaceSize(this);
		this.qTable = new float[Main.cw_settings.goalItems.get(agent_num).size()][stateSpaceSize][Enums.CRAFT_WORLD_NUM_ACTIONS];
		this.cachedSuccessor = new int[stateSpaceSize][Enums.CRAFT_WORLD_NUM_ACTIONS];
		this.cachedReward = new byte[Main.cw_settings.goalItems.get(agent_num).size()][stateSpaceSize][Enums.CRAFT_WORLD_NUM_ACTIONS];
		this.cachedTerminal = new boolean[stateSpaceSize][Enums.CRAFT_WORLD_NUM_ACTIONS];
		this.initialState = initialState;
		this.agent_num = agent_num;
	}
	
	public void setAllowedInventory(int agent_num)
	{
		allowed_inventory = new ArrayList<CraftWorldItem>();
		
		// Raw materials are always allowed
		allowed_inventory.add(CraftWorldItem.WOOD);
		allowed_inventory.add(CraftWorldItem.GRASS);
		allowed_inventory.add(CraftWorldItem.IRON);
		
		for (CraftWorldItem item : Main.cw_settings.itemsToMake.get(agent_num))
		{
			allowed_inventory.add(item);
		}
	}
	
	public void writeQValues()
	{
		try
		{
			String csv_file = Log.getLogDir() + "/q_table_agent_" + agent_num + ".csv";
			
			FileWriter fw = new FileWriter(csv_file, false);
	        BufferedWriter bw = new BufferedWriter(fw);
	        PrintWriter out = new PrintWriter(bw);

	        for (int i = 0; i < qTable.length; i++)
	        {
	        	out.println(qTable[i][0] + "," + qTable[i][1] + "," + qTable[i][2] + "," + qTable[i][3]);
	        }
	        
    	    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void train(int maxIter, float stoppingPoint, boolean writeQ)
	{
		for (int i = 0; i < maxIter; i++)
		{
			Log.info("Running training episode " + i + "...");
			float maxDelta = runEpisode(i == 0);
			
			if (maxDelta < stoppingPoint)
			{
				Log.info("Stopping point reached.");
				
				if (writeQ)
				{
					Log.info("Writing Q-values...");
					writeQValues();
				}
				
				break;
			}
		}
		
		// Free as much memory as possible
		cachedSuccessor = null;
		cachedReward = null;
		cachedTerminal = null;
	}
	
	public StateValuePair getGreedyAction(float[] qVals)
	{
		float maxQ = Float.NEGATIVE_INFINITY;
		ArrayList<Integer> greedyActions = new ArrayList<Integer>();
		
        for (int action = 0; action < qVals.length; action++)
        {
			if (greedyActions.size() == 0)
			{
				maxQ = qVals[action];
				greedyActions.add(action);
			}
			else if (qVals[action] == maxQ)
			{
				greedyActions.add(action);
			}
			else if (qVals[action] > maxQ)
			{
				maxQ = qVals[action];
				greedyActions.clear();
				greedyActions.add(action);
			}
        }
		
        int i = rm.nextInt(greedyActions.size());
        
        return new StateValuePair(greedyActions.get(i), maxQ);
	}
	
	private float runEpisode(boolean firstRun)
	{
		float maxDelta = 0.0f;
		float maxQ = Float.NEGATIVE_INFINITY;
		int maxQ_id = -1;
		
		float gamma = (float)Main.GAMMA;
		float alpha = 1.0f;

		for (int qLearningID = 0; qLearningID < stateSpaceSize; qLearningID++)
		{
			if (firstRun && qLearningID % 100000 == 0)
			{
				Log.info("qLearningID = " + qLearningID);
			}
			
			CraftWorldState currentState = null;
			CraftWorldNeighbourInfo neighbour_info = null;
			
			if (firstRun)
			{
				currentState = new CraftWorldState(qLearningID, this);
				neighbour_info = currentState.getNeighbourInfo(agent_num);
			}
				
			for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
			{
				if (firstRun && neighbour_info.states[action] == null)
				{
					for (int num_g_idx = 0; num_g_idx < Main.cw_settings.goalItems.get(agent_num).size(); num_g_idx++)
					{
						qTable[num_g_idx][qLearningID][action] = Float.NEGATIVE_INFINITY;
						cachedSuccessor[qLearningID][action] = -1;
					}
					
					continue;
				}
				
				if (cachedSuccessor[qLearningID][action] == -1 && !firstRun)
				{
					continue;
				}
				
				byte[] reward = new byte[Main.cw_settings.goalItems.get(agent_num).size()];
				boolean terminal = false;

				if (firstRun)
				{
					int previousItemsHeld = 0;
					int newItemsHeld = 0;
					
					for (CraftWorldItem item : Main.cw_settings.goalItems.get(agent_num))
					{
						if (currentState.haveItem[item.id][agent_num])
						{
							previousItemsHeld++;
						}
						
						if (neighbour_info.states[action].haveItem[item.id][agent_num])
						{
							newItemsHeld++;
						}
					}
					
					for (int num_g_idx = 0; num_g_idx < Main.cw_settings.goalItems.get(agent_num).size(); num_g_idx++)
					{
						if (newItemsHeld > previousItemsHeld && newItemsHeld == (num_g_idx + 1))
						{
							reward[num_g_idx] = (byte)newItemsHeld;
						}
						
						cachedReward[num_g_idx][qLearningID][action] = reward[num_g_idx];
					}
					
					cachedTerminal[qLearningID][action] = terminal;
				}
				else
				{
					for (int num_g_idx = 0; num_g_idx < Main.cw_settings.goalItems.get(agent_num).size(); num_g_idx++)
					{
						reward[num_g_idx] = cachedReward[num_g_idx][qLearningID][action];
					}
					
					terminal = cachedTerminal[qLearningID][action];
				}

				if (firstRun)
				{
					cachedSuccessor[qLearningID][action] = neighbour_info.states[action].getQLearningID(this);
				}
				
				for (int num_g_idx = 0; num_g_idx < Main.cw_settings.goalItems.get(agent_num).size(); num_g_idx++)
				{
					float[] nextStateQValues = qTable[num_g_idx][cachedSuccessor[qLearningID][action]];
					
					float newV = reward[num_g_idx] + gamma * (terminal? 0 : getGreedyAction(nextStateQValues).value);
					float oldV = qTable[num_g_idx][qLearningID][action];
					
					float delta = Math.abs(oldV - newV);
					if (delta > maxDelta)
					{
						maxDelta = delta;
					}
	
					qTable[num_g_idx][qLearningID][action] = alpha * newV + (1.0f - alpha) * oldV;
					
					if (qTable[num_g_idx][qLearningID][action] > maxQ)
					{
						maxQ = qTable[num_g_idx][qLearningID][action];
						maxQ_id = qLearningID;
					}
				}
			}
		}
		
		Log.info("Max Q = " + maxQ);
		Log.info("Max Q id = " + maxQ_id);
		Log.info("Max delta = " + maxDelta);
		
		return maxDelta;
	}
	
	public float[] getQValsForMaxGoalsAchievable(int playerTurn, int qLearningID)
	{
		int max_idx = 0;
		for (int num_g_idx = qTable.length - 1; num_g_idx >= 1; num_g_idx--)
		{
			float[] qVals = qTable[num_g_idx][qLearningID];
			
			for (float q : qVals)
			{
				if (q > 0.0)
				{
					max_idx = num_g_idx;
				}
			}
		}
		
		return qTable[max_idx][qLearningID];
	}
}
