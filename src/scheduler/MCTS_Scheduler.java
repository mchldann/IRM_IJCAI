package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import craftworld.CraftWorldRLAgent;
import decision.Decision;
import decision.Decision_GPT;
import decision.Decision_RL;
import main.Main;
import main.Main.Environment;
import officeworld.OfficeWorldRLAgent;
import util.Log;

public class MCTS_Scheduler extends Agent {

    public enum VisionType
    {
    	FULL,
    	UNAWARE,
    	PARTIALLY_AWARE
    }
    
    public enum ActionType
    {
    	GPT,
    	RL
    }
    
    public boolean single_player;
    
    public ActionType this_agent_action_type;
    public ActionType other_agent_action_type;
    
    public boolean other_agent_blind;
    
	public VisionType vision_type;
	
	public MCTS_Node rootNode;
	
	public int alpha, beta;
	public double c, sim_tau, rollout_stochasticity;
	public boolean allow_pass;
	private Agent rollout_schedulers[];
	private Agent blind_agent_rollout_schedulers[];
	public boolean[] gpt_visible;
	public int expansionCount = 0;
	
	private Agent rl_agent_us;
	private Agent rl_agent_opponent;
	private PassAgent pass_agent;
	
    // random
    static Random rm = new Random();
    
    // a very small value used for breaking the tie and dividing by 0
    static final double epsilon = 1e-6;
	
    // statistics
    public int nRollouts;
    
    public MCTS_Scheduler(int forest_idx, boolean single_player, ActionType this_agent_action_type, ActionType other_agent_action_type, boolean other_agent_blind, VisionType vision_type, int alpha, int beta, double c,
    	double sim_tau, double rollout_stochasticity, boolean allow_pass)
    {
    	this.forest_idx = forest_idx;
    	this.single_player = single_player;
    	this.this_agent_action_type = this_agent_action_type;
    	this.other_agent_action_type = other_agent_action_type;
    	this.other_agent_blind = other_agent_blind;
    	this.vision_type = vision_type;
    	this.alpha = alpha;
    	this.beta = beta;
    	this.c = c;
    	this.sim_tau = sim_tau;
    	this.rollout_stochasticity = rollout_stochasticity;
    	this.allow_pass = allow_pass;
    	this.expansionCount = 0;
    	
    	if (Main.environment == Environment.OFFICE_WORLD)
    	{
    		this.rl_agent_us = new OfficeWorldRLAgent(sim_tau);
    		this.rl_agent_opponent = new OfficeWorldRLAgent(sim_tau);
    	}
    	else if (Main.environment == Environment.CRAFT_WORLD)
    	{
    		this.rl_agent_us = new CraftWorldRLAgent(sim_tau);
    		this.rl_agent_opponent = new CraftWorldRLAgent(sim_tau);
    	}
    	
    	this.pass_agent = new PassAgent();
    }
    
	@Override
	public void reset()
	{
		// Nothing to reset for this scheduler
	}
    
    @Override
    public void loadMatchDetails(Match match, int agent_num, boolean mirror_match)
    {
    	super.loadMatchDetails(match, agent_num, mirror_match);
    	
		this.gpt_visible = new boolean[match.numGoalPlanTrees];
		
		for (int intentionNum = 0; intentionNum < match.numGoalPlanTrees; intentionNum++)
		{
			int agentToAssignIntention = mirror_match? ((intentionNum + 1) % match.numAgents) : (intentionNum % match.numAgents);
			
			if (agentToAssignIntention == agent_num)
			{
				gpt_visible[intentionNum] = true; // Can always see own GPTs
			}
			else
			{
				switch(vision_type)
				{
					case FULL:
						gpt_visible[intentionNum] = true;
						break;
						
					case PARTIALLY_AWARE:
						
						if ((intentionNum / match.numAgents) % 2 == 0)
						{
							gpt_visible[intentionNum] = true;
						}
						else
						{
							gpt_visible[intentionNum] = false;
						}

						break;
						
					case UNAWARE:
					default:
						gpt_visible[intentionNum] = false;
				}
			}
		}
		
    	this.rollout_schedulers = new Agent[match.numAgents];
    	this.blind_agent_rollout_schedulers = new Agent[match.numAgents];
    	
        if (this_agent_action_type == ActionType.GPT)
        {
        	this.rollout_schedulers[this.agent_num] = new Random_Scheduler(false);
        }
        else
        {
        	this.rollout_schedulers[this.agent_num] = this.rl_agent_us;
        }

        this.blind_agent_rollout_schedulers[this.agent_num] = this.pass_agent;

        if (match.numAgents > 1)
        {
        	if (single_player)
        	{
				this.rollout_schedulers[1 - this.agent_num] = this.pass_agent;
		        this.blind_agent_rollout_schedulers[1 - this.agent_num] = this.pass_agent;
        	}
        	else
        	{
				switch(other_agent_action_type)
				{
					case GPT:
						this.rollout_schedulers[1 - this.agent_num] = new Random_Scheduler(false);
				        this.blind_agent_rollout_schedulers[1 - this.agent_num] = new Random_Scheduler(false);
						break;
						
					case RL:
						this.rollout_schedulers[1 - this.agent_num] = this.rl_agent_opponent;
				        this.blind_agent_rollout_schedulers[1 - this.agent_num] = this.rl_agent_opponent;
						break;
						
					default:
						throw new Error("Unrecognised model for other agent");
				}
        	}
        }
        
        for (int i = 0; i < this.rollout_schedulers.length; i++)
        {
        	this.rollout_schedulers[i].forest_idx = this.forest_idx;
        	this.blind_agent_rollout_schedulers[i].forest_idx = this.forest_idx;
        }
    }
    
    public Decision getDecision(State state)
    {
   	 	// Reset return and age
    	State rootState = state.clone();
    	rootState.agentReturn = new double[] {0.0, 0.0};
    	rootState.age = 0;
    	
    	rootNode = new MCTS_Node(rootState, match);
		expansionCount = 0;
    	nRollouts = 0;
    	
    	run(alpha, beta);
    	
    	Decision winningDecision = null;
        int visits = -1;
        double average = Double.NEGATIVE_INFINITY;
        double other_agent_average = Double.NEGATIVE_INFINITY;
        
        Log.info("Actions available:");
        
        for(int i = 0; i < rootNode.children.size(); i++)
        {
        	Decision currentDecision = rootNode.children.get(i).decision;
        	
        	if (currentDecision instanceof Decision_GPT)
        	{
	        	if (((Decision_GPT)currentDecision).resetIntentions)
	        	{
		        	Log.info("Reset --> intention " + ((Decision_GPT)currentDecision).iChoice + ", plan " + ((Decision_GPT)currentDecision).pChoice
		        		+ ": Ave. val = " + (rootNode.children.get(i).totValue[agent_num] / rootNode.children.get(i).nVisits)
		        		+ ", visits = " + rootNode.children.get(i).nVisits);
	        	}
	        	else
	        	{
		        	Log.info("Intention " + ((Decision_GPT)currentDecision).iChoice + ", plan " + ((Decision_GPT)currentDecision).pChoice
		        		+ ": Ave. val = " + (rootNode.children.get(i).totValue[agent_num] / rootNode.children.get(i).nVisits)
		        		+ ", visits = " + rootNode.children.get(i).nVisits);
	        	}
        	}
        	else if (currentDecision instanceof Decision_RL)
        	{
	        	Log.info("Action " + ((Decision_RL)currentDecision).action
	        			+ ": Ave. val = " + (rootNode.children.get(i).totValue[agent_num] / rootNode.children.get(i).nVisits)
	        			+ ", visits = " + rootNode.children.get(i).nVisits);
        	}
        	
        	double nodeScore = rootNode.children.get(i).totValue[agent_num] / rootNode.children.get(i).nVisits
        		+ epsilon * rm.nextDouble(); // For tie-breaking
        	
            if(nodeScore > average)
            {
            	winningDecision = currentDecision;
                visits = rootNode.children.get(i).nVisits;
                average = rootNode.children.get(i).totValue[agent_num] / rootNode.children.get(i).nVisits;
                other_agent_average = rootNode.children.get(i).totValue[1 - agent_num] / rootNode.children.get(i).nVisits;
            }
        }
        
        Log.info("Action chosen:");
        
    	if (winningDecision instanceof Decision_GPT)
    	{
	        Log.info("Intention " + ((Decision_GPT)winningDecision).iChoice + ", plan " + ((Decision_GPT)winningDecision).pChoice
	        		+ " (Averaged " + average + " from " + visits + " visits)");
    	}
    	else if (winningDecision instanceof Decision_RL)
    	{
	        Log.info("Action " + ((Decision_RL)winningDecision).action
	        		+ " (Averaged " + average + " from " + visits + " visits)");
    	}
    	
        Log.info("Other agent average: " + other_agent_average);
    	
        return winningDecision;
    }
    
    /**
     * @return a node with maximum UCT value
     */
    private MCTS_Node select(MCTS_Node currentNode)
    {
        // Initialisation
        MCTS_Node selected = null;
        double bestUCT = Double.NEGATIVE_INFINITY;

        // Calculate the UCT value for each of its selected nodes
        for(int i = 0; i < currentNode.children.size(); i++)
        {
            // UCT calculation
            double uctValue = currentNode.children.get(i).totValue[currentNode.state.playerTurn] / (currentNode.children.get(i).nVisits + epsilon)
            		+ c * Math.sqrt(Math.log(nRollouts + 1) / (currentNode.children.get(i).nVisits + epsilon))
            		+ epsilon * rm.nextDouble(); // For tie-breaking
            
            // Compare with the current maximum value
            if(uctValue > bestUCT)
            {
                selected = currentNode.children.get(i);
                bestUCT = uctValue;
            }
        }
        
        // Return the nodes with maximum UCT value, null if current node is a leaf node (contains no child nodes)
        return selected;
    }
    
    /**
     * The main MCTS process
     * @param alpha number of iterations
     * @param beta number of simulation per iteration
     */
    private void run(int alpha, int beta)
    {	 
        long startTime = System.currentTimeMillis();
        
        // Record the list of nodes that has been visited
        List<MCTS_Node> visited = new ArrayList<MCTS_Node>();
        
        // Run alpha iterations
        for(int i = 0; i < alpha; i++)
        {
            visited.clear();
            
            // Set the current node to this node
            MCTS_Node currentNode = this.rootNode;
            boolean firstExpansion = true;
            
            // Add this node to the list of visited node
            visited.add(currentNode);
            
            // Find the leaf node which has the largest UCT value
            while ((currentNode != null) && !currentNode.isLeaf())
            {
                currentNode = select(currentNode);
                
                if (currentNode != null)
                {
                    visited.add(currentNode);
                }
            }

            if (single_player)
            {
            	currentNode.state.playerTurn = this.agent_num;
            }
            	
            if (firstExpansion || !currentNode.state.isGameOver()) // Always need to expand once so that 'select' has children to choose from.
            {
            	boolean expand_as_gpt_agent = (currentNode.state.playerTurn == (1 - this.agent_num) && other_agent_action_type == ActionType.GPT)
            		|| (currentNode.state.playerTurn == this.agent_num && this_agent_action_type == ActionType.GPT);
            	
            	currentNode.expand(available_intentions[currentNode.state.playerTurn], allow_pass, expand_as_gpt_agent, firstExpansion);
            	this.expansionCount++;
            	firstExpansion = false;
            	
            	// Select a node for simulation
            	currentNode = select(currentNode);
            }
            
            visited.add(currentNode);
            
            // TODO: Tidy this logic later
            int otherAgentNum = 1 - agent_num;
            
            // Simulation
            for (int j = 0; j < beta; j++)
            {
            	State rolloutStartState = currentNode.state.clone();
            	
            	State blindAgentRolloutStartState = null;
            	if (other_agent_blind)
            	{
            		blindAgentRolloutStartState = currentNode.state.clone();
            		blindAgentRolloutStartState.removeAgent(agent_num);
            	}
            	
            	State endOfGame;
            	if (rolloutStartState.isGameOver())
                {
            		endOfGame = rolloutStartState;
                }
            	else
            	{
                    Match m = new Match("MCTS_rollout", match.numGoalPlanTrees, match.allianceType, rolloutStartState,
                    	rollout_schedulers, new String[] {"rollout_a1", "rollout_a2"}, match.assumed_politeness, -1);
                    
	                endOfGame = m.run(false, false, mirror_match, true);
            	}
            	
            	State blindAgentEndOfGame = null;
            	if (other_agent_blind)
            	{
                	if (blindAgentRolloutStartState.isGameOver())
                    {
                		blindAgentEndOfGame = blindAgentRolloutStartState;
                    }
                	else
                	{
                        Match m_blind = new Match("MCTS_rollout", match.numGoalPlanTrees, match.allianceType, blindAgentRolloutStartState,
                        	blind_agent_rollout_schedulers, new String[] {"rollout_a1", "rollout_a2"}, match.assumed_politeness, -1);
                        
                        blindAgentEndOfGame = m_blind.run(false, false, mirror_match, true);
                	}
            	}

                nRollouts++;
                
                // Back-propagation
                // TODO: Fix this logic later so that it caters for more than two agents. It's hacky right now.
                
                for (int t = 0; t < visited.size(); t++)
                {
                	MCTS_Node node = visited.get(visited.size() - t - 1);

                    node.nVisits++;
                    
                    // Update value for this scheduler
                    node.totValue[agent_num] += endOfGame.agentReturn[agent_num];
                    node.totValue[agent_num] += Main.POLITENESS * endOfGame.agentReturn[otherAgentNum];
                    
                    // Update value for other agent
        			if (match.agents.length > 1)
        			{
        				if (other_agent_blind)
        				{
        					node.totValue[otherAgentNum] += blindAgentEndOfGame.agentReturn[otherAgentNum];
        				}
        				else
        				{
        					node.totValue[otherAgentNum] += endOfGame.agentReturn[otherAgentNum];
                            node.totValue[otherAgentNum] += Main.POLITENESS * endOfGame.agentReturn[agent_num];
        				}
        			}
                }
            }
        }

        Log.info("MCTS calculation time = " + (System.currentTimeMillis() - startTime) + "ms");
        Log.info("Expansion count: " + this.expansionCount);
    }
}
