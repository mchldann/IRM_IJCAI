package scheduler;

import decision.Decision;
import main.Main;
import main.Main.Environment;
import util.Log;

public abstract class Agent {
	
	public Match match;
	public int agent_num;
	public boolean mirror_match;
	
	public boolean[][] available_intentions;
	
	public int forest_idx = 0;
	
	public abstract void reset();
	public abstract Decision getDecision(State state);
	
    public void loadMatchDetails(Match match, int agent_num, boolean mirror_match)
    {
    	this.match = match;
    	this.agent_num = agent_num;
    	this.mirror_match = mirror_match;
    	
		this.available_intentions = new boolean[match.agents.length][match.numGoalPlanTrees];
		
		for (int intentionNum = 0; intentionNum < match.numGoalPlanTrees; intentionNum++)
		{
			int agentToAssignIntention;
			
			if (Main.environment == Environment.OFFICE_WORLD)
			{
				if (match.numAgents == 1)
				{
					agentToAssignIntention = (intentionNum < 2) ? 0 : -1;
				}
				else
				{
					agentToAssignIntention = (intentionNum < 2) ? 0 : 1;
				}
			}
			else if (Main.environment == Environment.CRAFT_WORLD)
			{
				int intentionsPerForest = Main.cw_settings.goalItems.get(0).size() + Main.cw_settings.goalItems.get(1).size();
				
				if (intentionNum >= forest_idx * intentionsPerForest && intentionNum < (forest_idx + 1) * intentionsPerForest)
				{
					int offsetIntentionNum = intentionNum - forest_idx * intentionsPerForest;
					
					if (offsetIntentionNum < Main.cw_settings.goalItems.get(0).size())
					{
						agentToAssignIntention = 0;
					}
					else
					{
						if (match.numAgents > 1)
						{
							agentToAssignIntention = 1;
						}
						else
						{
							agentToAssignIntention = -1;
						}
					}
				}
				else
				{
					agentToAssignIntention = -1;
				}
			}
			else
			{
				Log.info("Unrecognised environment in 'loadMatchDetails'!");
				agentToAssignIntention = -1;
				System.exit(0);
			}

			for (int agentNum = 0; agentNum < match.numAgents; agentNum++)
			{
				available_intentions[agentNum][intentionNum] = (agentNum == agentToAssignIntention);
			}
		}
    }
}