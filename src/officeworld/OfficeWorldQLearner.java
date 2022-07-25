package officeworld;

import java.util.ArrayList;
import java.util.Random;

import main.Main;
import main.Main.OfficeWorldTask;

public class OfficeWorldQLearner {

	public class StateValuePair
	{
		public int action;
		public double value;
		
		public StateValuePair(int action, double value)
		{
			this.action = action;
			this.value = value;
		}
	}
	
	public double[][] qTable;
	public OfficeWorldState initialState;
	public OfficeWorldTask task;
	public int agentNum;
	
    private static Random rm = new Random();
	
	public OfficeWorldQLearner(OfficeWorldState initialState, OfficeWorldTask task, int agentNum)
	{
		this.qTable = new double[Main.OFFICE_WORLD_NUM_STATES_BY_TASK[task.id]][4];
		this.initialState = initialState;
		this.task = task;
		this.agentNum = agentNum;
	}
	
	public void train(int numEpisodes)
	{
		for (int i = 0; i < numEpisodes; i++)
		{
			runEpisode();
		}
	}
	
	private StateValuePair getGreedyAction(OfficeWorldState ows)
	{
		if (ows.position[agentNum] == null)
		{
			return new StateValuePair(-1, Double.NEGATIVE_INFINITY);
		}
		
		OfficeWorldState[] neighbours = ows.getNeighbourStates(agentNum);
		
		double maxQ = Double.NEGATIVE_INFINITY;
		ArrayList<Integer> greedyActions = new ArrayList<Integer>();
		
        for (int action = 0; action < neighbours.length; action++)
        {
        	if (neighbours[action] == null)
        	{
        		continue;
        	}

			double qVal = qTable[ows.getQLearningID(task, agentNum)][action];
			
			if (greedyActions.size() == 0)
			{
				maxQ = qVal;
				greedyActions.add(action);
			}
			else if (qVal == maxQ)
			{
				greedyActions.add(action);
			}
			else if (qVal > maxQ)
			{
				maxQ = qVal;
				greedyActions.clear();
				greedyActions.add(action);
			}
        }
		
        int i = rm.nextInt(greedyActions.size());
        
        return new StateValuePair(greedyActions.get(i), maxQ);
	}

	private void runEpisode()
	{
		double gamma = 0.99;
		double alpha = 1.0;

		for (int qLearningID = 0; qLearningID < Main.OFFICE_WORLD_NUM_STATES_BY_TASK[task.id]; qLearningID++)
		{
			OfficeWorldState currentState = new OfficeWorldState(task, qLearningID, agentNum);
			OfficeWorldState[] neighbours = currentState.getNeighbourStates(agentNum);
				
			for (int action = 0; action < 4; action++)
			{
				if (neighbours[action] == null)
				{
					continue;
				}
				
				double reward = 0.0;
				boolean terminal = false;

				if ((task == Main.OfficeWorldTask.DELIVER_COFFEE_AND_MAIL) && neighbours[action].coffeeDelivered[agentNum] && neighbours[action].mailDelivered[agentNum])
				{
					reward += 1.0;
					terminal = true;
				}
				
				if ((task == Main.OfficeWorldTask.PATROL) && neighbours[action].nextPatrolPoint[agentNum].equals(OfficeWorldState.next_patrol_point.get(currentState.nextPatrolPoint[agentNum])))
				{
					reward += 1.0;
				}
				
				if (neighbours[action].atDecoration(agentNum))
				{
					reward += Main.OW_Q_LEARNING_DECORATION_PENALTY;
				}

				double newV = reward + gamma * (terminal? 0 : getGreedyAction(neighbours[action]).value);
				double oldV = qTable[currentState.getQLearningID(task, agentNum)][action];
				
				qTable[currentState.getQLearningID(task, agentNum)][action] = alpha * newV + (1.0 - alpha) * oldV;
			}
		}
	}
}
