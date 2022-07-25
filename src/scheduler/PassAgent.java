package scheduler;

import decision.Decision;
import decision.Decision_RL_OW;

public class PassAgent extends Agent {

	@Override
	public void reset() {}

	@Override
	public Decision getDecision(State state)
	{
		return new Decision_RL_OW(-1, true, null, null);
	}
}
