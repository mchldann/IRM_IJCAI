package scheduler;

import java.util.Random;

import decision.Decision_GPT;

public class Random_Scheduler extends Agent {

	public MCTS_Node rootNode;
	
    private static Random rm = new Random();
    
    private boolean can_pass;
    
    public Random_Scheduler(boolean can_pass)
    {
    	this.can_pass = can_pass;
    }
    
	@Override
	public void reset()
	{
		// Nothing to reset for this scheduler
	}
    
    public Decision_GPT getDecision(State state)
    {
    	this.rootNode = new MCTS_Node(state, match);
    	
    	this.rootNode.expand(available_intentions[agent_num], this.can_pass, true, false);
    	
		int firstChildIntentionChoice = ((Decision_GPT)rootNode.children.get(0).decision).iChoice;
    	boolean playerMustPass = (rootNode.children.size() == 1) && (firstChildIntentionChoice == -1);
    	
        int i = rm.nextInt(rootNode.children.size());

        return new Decision_GPT(((Decision_GPT)rootNode.children.get(i).decision).iChoice,
        	((Decision_GPT)rootNode.children.get(i).decision).pChoice,
        	playerMustPass);
    }
}
