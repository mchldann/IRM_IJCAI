package scheduler;

import java.util.ArrayList;
import java.util.List;

import decision.Decision;
import decision.Decision_RL_OW;
import main.Main;
import officeworld.OfficeWorldPosition;

public class A_Star_Agent extends Agent {

	public List<Integer> remainingActions;
	
	@Override
	public void reset()
	{		
		remainingActions = new ArrayList<Integer>(Main.aStarSolvers[this.agent_num].finalPathActions);
	}

	@Override
	public Decision getDecision(State state)
	{
		OfficeWorldPosition start_pos = state.ow_agent_positions[this.agent_num];
    	
		if (remainingActions.size() > 0)
		{
			int action = remainingActions.remove(0);

			OfficeWorldPosition neighbours[] = start_pos.get_neighbours();
	    	OfficeWorldPosition end_pos = neighbours[action];
			return new Decision_RL_OW(action, false, start_pos, end_pos);
		}
		else
		{
			return new Decision_RL_OW(-1, true, start_pos, start_pos);
		}
	}
}
