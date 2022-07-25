package officeworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import decision.Decision;
import decision.Decision_RL_OW;
import main.Main;
import officeworld.OfficeWorldPosition;
import officeworld.OfficeWorldState;
import scheduler.Agent;
import scheduler.State;

public class OfficeWorldRLAgent extends Agent
{
    private static Random rm = new Random();
    
	private int max_turns;
	private int turns_so_far;
	private double tau;
	
	public List<Decision> decisions_made;
	
    public OfficeWorldRLAgent(double tau)
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
		OfficeWorldPosition start_pos = state.ow_agent_positions[state.playerTurn];
    	
		// If the other agent isn't present in the environment, pass.
		if (start_pos == null)
		{
			Decision_RL_OW d = new Decision_RL_OW(-1, true, start_pos, start_pos);
			this.decisions_made.add(d);
			return d;
		}
		
		OfficeWorldPosition neighbours[] = start_pos.get_neighbours();
    	
		turns_so_far++;
		boolean forced_pass = (turns_so_far > max_turns);
		
		OfficeWorldState ows = new OfficeWorldState(state);
		double[] qVals = Main.ow_qLearners[Main.officeWorldTask.id][state.playerTurn].qTable[ows.getQLearningID(Main.officeWorldTask, state.playerTurn)];
		
		double maxQ = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < neighbours.length; i++)
		{
			if (neighbours[i] != null)
			{
				if (qVals[i] > maxQ)
				{
					maxQ = qVals[i];
				}
			}
		}
		
		double denom = 0.0;
		for (int i = 0; i < neighbours.length; i++)
		{
			if (neighbours[i] != null)
			{
				denom += Math.exp((qVals[i] - maxQ) / this.tau);
			}
		}
		
		double[] probs = new double[neighbours.length];
		for (int i = 0; i < neighbours.length; i++)
		{
			if (neighbours[i] != null)
			{
				probs[i] = Math.exp((qVals[i] - maxQ) / this.tau) / denom;
			}
		}
		
		int action = -1;
		double cumuProb = 0.0;
		double selectionPoint = rm.nextDouble();
		
		for (int i = 0; i < neighbours.length; i++)
		{
			cumuProb += probs[i];
			if (cumuProb > selectionPoint)
			{
				action = i;
				break;
			}
		}
		
    	OfficeWorldPosition end_pos = neighbours[action];
    	
		//Log.info("Moving to: " + end_pos.room_key + ", " + end_pos.cell_key);
		
    	Decision_RL_OW d = new Decision_RL_OW(action, forced_pass, start_pos, end_pos);
		this.decisions_made.add(d);
		return d;
	}
}
