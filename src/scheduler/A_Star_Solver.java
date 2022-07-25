package scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import main.Main;
import main.Main.OfficeWorldTask;
import officeworld.OfficeWorldState;
import util.Log;

public class A_Star_Solver {

	public class A_Star_Node
	{
		public OfficeWorldState state;
		public double fScore;
		
		public A_Star_Node(OfficeWorldState state)
		{
			this.state = state;
			this.fScore = Double.MIN_VALUE;
		}
		
		public boolean goalComplete(int agentNum)
		{
			return state.coffeeDelivered[agentNum] && state.mailDelivered[agentNum];
		}
		
		public A_Star_Node[] getNeighbours(int agentNum)
		{
			A_Star_Node[] result = new A_Star_Node[4];
			OfficeWorldState[] neighbourStates = state.getNeighbourStates(agentNum);
			
			for (int i = 0; i < 4; i++)
			{
				if (neighbourStates[i] != null)
				{
					result[i] = new A_Star_Node(neighbourStates[i]);
				}
			}
			
			return result;
		}
	}
	
	public class A_Star_Node_Comparator implements Comparator<A_Star_Node>
	{
	    @Override
	    public int compare(A_Star_Node nodeA, A_Star_Node nodeB)
	    {
	       return Double.compare(nodeA.fScore, nodeB.fScore);
	    }
	}
	
	public OfficeWorldState initialState;
	public OfficeWorldTask task;
	public int agentNum;
	
	private PriorityQueue<A_Star_Node> openSet;
	private Map<Integer, Double> gScore;
	private Map<Integer, A_Star_Node> cameFrom;
	private Map<Integer, Integer> cameFromDirection;
	public List<A_Star_Node> finalPath;
	public List<Integer> finalPathActions;
	
	public A_Star_Solver(OfficeWorldState initialState, OfficeWorldTask task, int agentNum)
	{
		this.initialState = initialState;
		this.task = task;
		this.agentNum = agentNum;
		
		CalculatePath();
	}
	
	private double get_h_score(A_Star_Node node)
	{
		return 0.0;
	}
	
	// TODO: This is quite inefficient, fix later.
	private void remove_from_open_set(A_Star_Node node, int agentNum)
	{
	      Iterator<A_Star_Node> it = openSet.iterator();
	      
	      while(it.hasNext())
	      {
	    	 A_Star_Node element = it.next();
	         if (element.state.getQLearningID(task, agentNum) == node.state.getQLearningID(task, agentNum))
	         {
	        	 openSet.remove(element);
	        	 break;
	         }
	      }
	}
	
	private void ReconstructPath(A_Star_Node finalNode)
	{
		Log.info("Reconstructing path...");
		
		this.finalPath = new ArrayList<A_Star_Node>();
		this.finalPathActions = new ArrayList<Integer>();
		
		this.finalPath.add(finalNode);
		this.finalPathActions.add(cameFromDirection.get(finalNode.state.getQLearningID(task, agentNum)));
		
		while (cameFrom.containsKey(finalNode.state.getQLearningID(task, agentNum)))
		{
			finalNode = cameFrom.get(finalNode.state.getQLearningID(task, agentNum));
			this.finalPath.add(0, finalNode);
			this.finalPathActions.add(0, cameFromDirection.get(finalNode.state.getQLearningID(task, agentNum)));
		}
		
		this.finalPathActions.remove(0);
		
		Log.info("Done!");
	}
		
	private void CalculatePath()
	{
		Log.info("Calculating A* path for agent " + agentNum + "...");
		
		openSet = new PriorityQueue<A_Star_Node>(new A_Star_Node_Comparator());
		gScore = new HashMap<Integer, Double>();
		cameFrom = new HashMap<Integer, A_Star_Node>();
		cameFromDirection = new HashMap<Integer, Integer>();
		
		A_Star_Node initialNode = new A_Star_Node(initialState);
		gScore.put(initialNode.state.getQLearningID(task, agentNum), 0.0);
		initialNode.fScore = gScore.get(initialNode.state.getQLearningID(task, agentNum)) + get_h_score(initialNode);
		openSet.add(initialNode);
		
		while (openSet.size() > 0)
		{
			A_Star_Node currentNode = openSet.poll();
			
			if (currentNode.goalComplete(agentNum))
			{
				ReconstructPath(currentNode);
				return;
			}
			
			A_Star_Node[] neighbours = currentNode.getNeighbours(agentNum);
			
			//for (A_Star_Node neighbour : neighbours)
			for (int i = 0; i < neighbours.length; i++)
			{
				if (neighbours[i] == null)
				{
					continue;
				}
				
				double step_cost = neighbours[i].state.atDecoration(agentNum) ? -Main.OW_Q_LEARNING_DECORATION_PENALTY : 1.0;
				
				double tentative_g_score = gScore.get(currentNode.state.getQLearningID(task, agentNum)) + step_cost;
				
				if (!gScore.containsKey(neighbours[i].state.getQLearningID(task, agentNum))
					|| (tentative_g_score < gScore.get(neighbours[i].state.getQLearningID(task, agentNum))))
				{
					cameFrom.put(neighbours[i].state.getQLearningID(task, agentNum), currentNode);
					cameFromDirection.put(neighbours[i].state.getQLearningID(task, agentNum), i);
					gScore.put(neighbours[i].state.getQLearningID(task, agentNum), tentative_g_score);
					neighbours[i].fScore = gScore.get(neighbours[i].state.getQLearningID(task, agentNum)) + get_h_score(neighbours[i]);
					
					remove_from_open_set(neighbours[i], agentNum);
					
					openSet.add(neighbours[i]);
				}
			}
		}
		
		throw new Error("A* could not find a solution for the task!");
	}
}
