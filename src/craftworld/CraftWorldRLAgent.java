package craftworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import decision.Decision;
import decision.Decision_RL_CW;
import main.Main;
import scheduler.Agent;
import scheduler.State;
import util.Enums;
import util.Enums.Action;

public class CraftWorldRLAgent extends Agent
{
    private static Random rm = new Random();
    
	private int max_turns;
	private int turns_so_far;
	private double tau;
	
	public List<Decision> decisions_made;
	
    public CraftWorldRLAgent(double tau)
    {
    	this.max_turns = 10000;
    	this.turns_so_far = 0;
    	this.tau = tau;
    	this.decisions_made = new ArrayList<Decision>();
    }
    
	@Override
	public void reset()
	{
		this.turns_so_far = 0;
    	this.decisions_made = new ArrayList<Decision>();
	}

	@Override
	public Decision getDecision(State state)
	{
		CraftWorldPosition start_pos = state.cw_agent_positions[agent_num];
    	
		turns_so_far++;
		
		// If the other agent isn't present in the environment, or max turns exceeded --> pass
		if (start_pos == null || turns_so_far > max_turns)
		{
			Decision_RL_CW d = new Decision_RL_CW(-1, true, null, null, 0.0, -1);
			this.decisions_made.add(d);
			return d;
		}
		
		CraftWorldPosition neighbours[] = start_pos.get_neighbours();

		CraftWorldState cws = new CraftWorldState(state);
		
		CraftWorldQLearner qLearner = Main.cw_qLearners[agent_num];
		float[] qVals = qLearner.getQValsForMaxGoalsAchievable(state.playerTurn, cws.getQLearningID(qLearner));
		
		float maxQ = Float.NEGATIVE_INFINITY;
		for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
		{
			if (action == Action.CRAFT_OR_GATHER_ITEM.id || neighbours[action] != null)
			{
				if (qVals[action] > maxQ)
				{
					maxQ = qVals[action];
				}
			}
		}
		
		double denom = 0.0;
		for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
		{
			if (action == Action.CRAFT_OR_GATHER_ITEM.id || neighbours[action] != null)
			{
				denom += Math.exp((qVals[action] - maxQ) / this.tau);
			}
		}
		
		double[] probs = new double[Enums.CRAFT_WORLD_NUM_ACTIONS];
		for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
		{
			if (action == Action.CRAFT_OR_GATHER_ITEM.id || neighbours[action] != null)
			{
				probs[action] = Math.exp((qVals[action] - maxQ) / this.tau) / denom;
			}
		}
		
		int chosen_action = -1;
		double cumuProb = 0.0;
		double selectionPoint = rm.nextDouble();
		
		for (int action = 0; action < Enums.CRAFT_WORLD_NUM_ACTIONS; action++)
		{
			cumuProb += probs[action];
			if (cumuProb > selectionPoint)
			{
				chosen_action = action;
				break;
			}
		}
		
    	CraftWorldPosition end_pos;
    	
    	if (maxQ > 0.0)
    	{
    		if (chosen_action == Action.CRAFT_OR_GATHER_ITEM.id)
    		{
    			end_pos = start_pos;
    		}
    		else
    		{
    			end_pos = neighbours[chosen_action];
    		}
    	}
    	else
    	{
			Decision_RL_CW d = new Decision_RL_CW(-1, true, null, null, 0.0, -1);
			this.decisions_made.add(d);
			return d;
    	}

    	Decision_RL_CW d = new Decision_RL_CW(chosen_action, false, start_pos, end_pos, qVals[chosen_action], cws.getQLearningID(Main.cw_qLearners[agent_num]));
		this.decisions_made.add(d);
		return d;
	}
}
