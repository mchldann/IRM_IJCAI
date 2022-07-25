package decision;

import scheduler.Match;
import scheduler.State;

public abstract class Decision {

	public static final int PASS = -1;
	
	public abstract boolean is_pass();
	
	public abstract boolean is_pass_forced();
	
	public abstract void update_state(Match m, State currentState, boolean verbose, boolean draw);
	
	public void apply_to_state(Match m, State currentState, boolean verbose, boolean draw)
	{
		update_state(m, currentState, verbose, draw);
		currentState.removeDoneAgents();
		currentState.updateReturns();
		
		for (int i = 0; i < m.numAgents; i++)
		{
			if (currentState.gameOverAge[i] < 0 && currentState.isGameOver())
			{
				currentState.gameOverAge[i] = currentState.age;
			}
		}
	}
}