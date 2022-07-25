package decision;

import beliefbase.Condition;
import officeworld.OfficeWorldPosition;
import officeworld.OfficeWorldRenderer;
import officeworld.OfficeWorldState;
import scheduler.Match;
import scheduler.State;
import util.Log;

public class Decision_RL_OW extends Decision_RL {

	public int action;
	private boolean forced_pass;
	private OfficeWorldPosition start_pos;
	private OfficeWorldPosition end_pos;
	
    public Decision_RL_OW(int action, boolean forced_pass, OfficeWorldPosition start_pos, OfficeWorldPosition end_pos)
    {
    	this.action = action;
    	this.forced_pass = forced_pass;
    	this.start_pos = start_pos;
    	this.end_pos = end_pos;
    }
    
	@Override
	public boolean is_pass()
	{
		return action == -1 || forced_pass;
	}
	
	@Override
	public boolean is_pass_forced()
	{
		return forced_pass;
	}

	@Override
	public void update_state(Match m, State currentState, boolean verbose, boolean draw)
	{
        if (is_pass())
        {
        	currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
        	return;
        }
        else
        {
        	currentState.actionTakenSinceReset[currentState.playerTurn] = true;
        }
 
		Log.info("Moving from: " + this.start_pos + " to " + this.end_pos, verbose);
		
		String agentTag = (currentState.playerTurn == 0)? "" : "Op";
		
		Condition unset_start_pos_room = new Condition(agentTag + "AtRoom" + this.start_pos.roomID(), false);
		Condition unset_start_pos_cell = new Condition(agentTag + "At" + this.start_pos.cellID(), false);
		
		Condition set_end_pos_room = new Condition(agentTag + "AtRoom" + this.end_pos.roomID(), true);
		Condition set_end_pos_cell = new Condition(agentTag + "At" + this.end_pos.cellID(), true);
		
		currentState.beliefs.apply(unset_start_pos_room);
		currentState.beliefs.apply(unset_start_pos_cell);
		currentState.beliefs.apply(set_end_pos_room);
		currentState.beliefs.apply(set_end_pos_cell);
		
		currentState.ow_agent_positions[currentState.playerTurn] = this.end_pos;
		
		OfficeWorldState ows = new OfficeWorldState(currentState);
		
		// Get coffee
		if (ows.atCoffee(currentState.playerTurn))
		{
			Condition cHaveCoffee = new Condition(agentTag + "HaveCoffee", true);
			currentState.beliefs.apply(cHaveCoffee);
		}
		
		// Get mail
		if (ows.atMailRoom(currentState.playerTurn))
		{
			Condition cHaveMail = new Condition(agentTag + "HaveMail", true);
			currentState.beliefs.apply(cHaveMail);
		}
		
		// Deliver coffee
		// TODO: Is it possible to grab more coffee after it has been delivered?
		Condition cHaveCoffee = new Condition(agentTag + "HaveCoffee", true);
		boolean haveCoffee = currentState.getBeliefBase().evaluate(cHaveCoffee);
		if (haveCoffee && ows.atOffice(currentState.playerTurn))
		{
			Condition cCoffeeDelivered = new Condition(agentTag + "CoffeeDelivered", true);
			Condition cNoLongerHaveCoffee = new Condition(agentTag + "HaveCoffee", false);
			currentState.beliefs.apply(cCoffeeDelivered);
			currentState.beliefs.apply(cNoLongerHaveCoffee);
		}
		
		// Deliver mail
		// TODO: Is it possible to grab more mail after it has been delivered?
		Condition cHaveMail = new Condition(agentTag + "HaveMail", true);
		boolean haveMail = currentState.getBeliefBase().evaluate(cHaveMail);
		if (haveMail && ows.atOffice(currentState.playerTurn))
		{
			Condition cMailDelivered = new Condition(agentTag + "MailDelivered", true);
			Condition cNoLongerHaveMail = new Condition(agentTag + "HaveMail", false);
			currentState.beliefs.apply(cMailDelivered);
			currentState.beliefs.apply(cNoLongerHaveMail);
		}
		
		// Move to next patrol point
		/*
		String currentPatrolPoint = currentState.getNextPatrolPoint(currentState.playerTurn);
		if (ows.atPatrolPoint(currentPatrolPoint, currentState.playerTurn))
		{
			String newPatrolPoint = OfficeWorldState.next_patrol_point.get(currentPatrolPoint);
			Condition cSetNewPatrolPoint = new Condition(agentTag + "Patrol" + newPatrolPoint, true);
			Condition cUnsetOldPatrolPoint = new Condition(agentTag + "Patrol" + currentPatrolPoint, false);
			currentState.beliefs.apply(cSetNewPatrolPoint);
			currentState.beliefs.apply(cUnsetOldPatrolPoint);
		}
		*/
		
		currentState.playerTurn = (currentState.playerTurn + 1) % m.numAgents;
		
		currentState.age++;
		if (draw)
		{
	        OfficeWorldRenderer renderer = new OfficeWorldRenderer();
	        renderer.draw(m.m_name + "_" + String.format("%05d", m.drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
		}
	}
}
