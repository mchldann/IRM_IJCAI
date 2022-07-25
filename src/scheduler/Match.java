package scheduler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import craftworld.CraftWorldRLAgent;
import craftworld.CraftWorldRenderer;
import craftworld.CraftWorldState;
import decision.Decision;
import main.Main;
import main.Main.Environment;
import officeworld.OfficeWorldRLAgent;
import officeworld.OfficeWorldRenderer;
import officeworld.OfficeWorldState;
import util.Enums;
import util.Log;
import util.Enums.CraftWorldItem;

public class Match
{
    public enum AllianceType
    {
        ADVERSARIAL,
        ALLIED,
        NEUTRAL;
    };
    
    public String[] agent_names;
    public int numGoalPlanTrees;
    public AllianceType allianceType;
    public int numAgents;
    public double assumed_politeness;
	public int drawID; // Set to -1 to disable drawing
	
	public String m_name;
	public State initialState;
	public Agent[] agents;

    private Decision decision;
	
    public Match(String m_name, int numGoalPlanTrees, AllianceType allianceType, State initialState,
    	Agent[] agents, String[] agent_names, double assumed_politeness, int drawID)
    {
    	this.m_name = m_name;
		this.numGoalPlanTrees = numGoalPlanTrees;
		this.allianceType = allianceType;
    	this.initialState = initialState.clone();
    	this.agents = agents;
		this.agent_names = agent_names;
		this.numAgents = agents.length;
		this.assumed_politeness = assumed_politeness;
		this.drawID = drawID;
    }
    
    private void getNextDecision(State s, boolean verbose, boolean is_rollout)
    {
    	boolean playerMustPass = true;

    	int iter = 0;
    	while (playerMustPass && !s.isGameOver())
    	{
    		Log.info("\n" + agent_names[s.playerTurn] +  "'s turn...", verbose);
    		
    		// This is an expensive call, hence wrapping in "if (verbose)"
    		if (verbose)
    		{
    			Log.info(s.beliefs.onPrintBB());
    		}
    		
    		if (s.actionTakenSinceReset[s.playerTurn] && !canCurrentPlayerAct(s) && !s.allTasksComplete(s.playerTurn))
    		{
    			Log.info("Resetting intentions because agent cannot act...", verbose);
    			s.resetFailedIntentions(s.playerTurn, agents[s.playerTurn].available_intentions[s.playerTurn], verbose);
    			agents[s.playerTurn].reset();
    		}
    		
    		if (s.playerTurn != agents[s.playerTurn].agent_num)
    		{
    			throw new Error("Player turn and agent number do not match!");
    		}
    		
    		this.decision = agents[s.playerTurn].getDecision(s);
    		
    		playerMustPass = this.decision.is_pass_forced();
    		
            if (playerMustPass)
            {
        		Log.info("No available action, passing...", verbose);
        		s.playerTurn = (s.playerTurn + 1) % numAgents;
        		iter++;
        		
        		if (iter >= this.numAgents)
        		{
        			if (!s.actionTakenSinceReset[0] && !s.actionTakenSinceReset[1])
        			{
        				s.notProgressible = true;
        				
        				for (int i = 0; i < numAgents; i++)
        				{
        					if (s.gameOverAge[i] < 0)
        					{
        						s.gameOverAge[i] = s.age;
        					}
        				}
        			}
        			else
        			{
	        			// All agents were forced to pass, must need a reset.
	        			Log.info("No agent can act, resetting...", verbose);
	        			s.resetAllIncompleteIntentions(verbose);
        			}
        		}
            }
    	}
    }
    
    public boolean canCurrentPlayerAct(State s)
    {
    	if (agents[s.playerTurn] instanceof OfficeWorldRLAgent
    		|| agents[s.playerTurn] instanceof CraftWorldRLAgent
    		|| agents[s.playerTurn] instanceof A_Star_Agent)
    	{
    		return true;
    	}
    	else
    	{
    		return s.getAvailableDecisions(agents[s.playerTurn].available_intentions[s.playerTurn]).size() > 0;
    	}
    }
    
    // TODO: Need to fix the image and video logging for two-sided series if this is ever reinstated.
    /*
    public void run_two_sided_series(boolean verbose, boolean write_results)
    {
    	run(verbose, write_results, false, false);
    	
    	// Alternate first agent to act for mirror match
    	State temp_state = initialState.clone();
    	initialState.playerTurn = (initialState.playerTurn + 1) % numAgents;
    	
    	run(verbose, write_results, true, false);
    	
    	initialState = temp_state;
    }
    */
    
    public State run(boolean verbose, boolean write_results, boolean mirror_match, boolean is_rollout)
    {
    	for (int i = 0; i < numAgents; i++)
    	{
			agents[i].loadMatchDetails(this, i, mirror_match);
			agents[i].reset();
    	}
    	
		// Reset to the initial state
    	State currentState = initialState.clone();
    	
		if (drawID >= 0)
		{
			if (Main.environment == Environment.OFFICE_WORLD)
			{
				OfficeWorldRenderer renderer = new OfficeWorldRenderer();
				renderer.draw(m_name + "_" + String.format("%05d", drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
			}
			else if (Main.environment == Environment.CRAFT_WORLD)
			{
				CraftWorldRenderer renderer = new CraftWorldRenderer();
				renderer.draw(m_name + "_" + String.format("%05d", drawID) + "_img_" + String.format("%05d", currentState.age), currentState);
			}
		}

    	Log.info("MATCH COMMENCED", verbose);
        long startTime = System.currentTimeMillis();
    	
        getNextDecision(currentState, verbose, is_rollout);
        
        while (!currentState.isGameOver())
        {
        	/*
        	int intendedSuccessor = -1;
        	int agent_num_pre_apply = -1;
        	CraftWorldQLearner qLearner = null;
        	
        	if (decision instanceof Decision_RL_CW)
        	{
        		intendedSuccessor = ((Decision_RL_CW)decision).intended_successor;
        		agent_num_pre_apply = currentState.playerTurn;
        		qLearner = Main.cw_qLearners[currentState.playerTurn];
        		Log.info("Intended successor = " + intendedSuccessor, verbose);
        	}
        	*/
        	
        	decision.apply_to_state(this, currentState, verbose, drawID >= 0);
        	
        	/*
        	if (intendedSuccessor != -1)
        	{
        		CraftWorldState cws = new CraftWorldState(currentState);
        		int actualSuccessor = cws.getQLearningID(qLearner);

        		if (actualSuccessor != intendedSuccessor)
        		{
            		Log.info("Actual successor = " + actualSuccessor, verbose);
        			throw new Error("Actual successor (" +  actualSuccessor + ") for agent " + agent_names[agent_num_pre_apply] + " does not match intended (" + intendedSuccessor + ")!");
        		}
        	}
        	*/
        	
            getNextDecision(currentState, verbose, is_rollout);
        }
        
        if (currentState.notProgressible)
        {
        	Log.info("\nGame over -- no remaining intentions are progressible!", verbose);
        }
        else if (currentState.allTasksComplete(0) && currentState.allTasksComplete(1))
        {
        	Log.info("\nGame over -- all intentions are complete!", verbose);
        }
        else if (Main.environment == Environment.OFFICE_WORLD && currentState.age > Main.OFFICE_WORLD_TIMEOUT)
        {
        	Log.info("\nGame over due to time out!", verbose);
        }
        else if (Main.environment == Environment.CRAFT_WORLD && currentState.age > Main.CRAFT_WORLD_TIMEOUT)
        {
        	Log.info("\nGame over due to time out!", verbose);
        }
        
        /*
        if (currentState.consecutive_passes >= State.MAX_CONSECUTIVE_PASSES)
        {
        	Log.info("Game over due to repetition!", verbose);
        }
        */

        long total_match_time = System.currentTimeMillis() - startTime;
        
        // This is an expensive call, hence wrapping in "if (verbose)"
        if (verbose)
        {
        	Log.info(currentState.beliefs.onPrintBB());
        }
        
        for (int i = 0; i < numAgents; i++)
        {
    		agents[i].match = null; // Free match memory in case the scheduler is still referenced in the main method
    		Log.info(agent_names[i] + "'s score = " + currentState.getStateScore(i), verbose);
    		Log.info(agent_names[i] + "'s return = " + currentState.agentReturn[i], verbose);
        }
        Log.info("", verbose);
        
        if (write_results)
        {
        	writeResults(currentState, total_match_time);
        }
        
        if (!is_rollout)
        {
        	if (Main.environment == Environment.CRAFT_WORLD)
        	{
		        CraftWorldState cws = new CraftWorldState(currentState);
		        Log.info("-== ENVIRONMENT STATUS ==-");
		        for (int i = 0; i < cws.treeStatus.length; i++)
		        {
		        	Log.info("Tree " + i + " remains = " + cws.treeStatus[i]);
		        }
		        for (int i = 0; i < cws.grassStatus.length; i++)
		        {
		        	Log.info("Grass " + i + " remains = " + cws.grassStatus[i]);
		        }
		        for (int i = 0; i < cws.ironStatus.length; i++)
		        {
		        	Log.info("Iron " + i + " remains = " + cws.ironStatus[i]);
		        }
		        Log.info("");
		        
		        Log.info("-== AGENT A STATUS ==-");
		        Log.info("Position = " + cws.position[0]);
		        
				for (CraftWorldItem item : Enums.CraftWorldItem.values())
				{
					Log.info("Have " + item.text_rep.toLowerCase() + " = " + cws.haveItem[item.id][0]);
				}
		        Log.info("");
		        
		        Log.info("-== AGENT B STATUS ==-");
		        Log.info("Op position = " + cws.position[1]);
		        
				for (CraftWorldItem item : Enums.CraftWorldItem.values())
		        {
		        	Log.info("Op have " + item.text_rep.toLowerCase() + " = " + cws.haveItem[item.id][1]);
		        }
		        Log.info("");
		        
		        // UNCOMMENT TO TEST GENERATING/PARSING Q-LEARNING ID
		        /*
		        Log.info("State space size = " + cws.getStateSpaceSize());
		        
		        int qID = cws.getQLearningID(0);
		        Log.info("Q learning ID (agent 0) = " + qID);
		        
		        CraftWorldState cws_2 = new CraftWorldState(qID, 0);
		        int qID2 = cws_2.getQLearningID(0);
		        Log.info("Q learning ID (agent 0 ***test***) = " + qID2);
		        
		        CraftWorldState cws_3 = new CraftWorldState(cws_2);
		        int qID3 = cws_3.getQLearningID(0);
		        Log.info("Q learning ID (agent 0 ***test 3***) = " + qID3);
		        */
		        
        	}
        	else if (Main.environment == Environment.OFFICE_WORLD)
        	{
		        OfficeWorldState ows = new OfficeWorldState(currentState);
		        Log.info("-== AGENT A STATUS ==-");
		        Log.info("Position = " + ows.position[0]);
		        Log.info("Have coffee = " + ows.haveCoffee[0]);
		        Log.info("Have mail = " + ows.haveMail[0]);
		        Log.info("Coffee delivered = " + ows.coffeeDelivered[0]);
		        Log.info("Mail delivered = " + ows.mailDelivered[0]);
		        Log.info("");
		        
		        Log.info("-== AGENT B STATUS ==-");
		        Log.info("Op position = " + ows.position[1]);
		        Log.info("Op have coffee = " + ows.haveCoffee[1]);
		        Log.info("Op have mail = " + ows.haveMail[1]);
		        Log.info("Op coffee delivered = " + ows.coffeeDelivered[1]);
		        Log.info("Op mail delivered = " + ows.mailDelivered[1]);
		        Log.info("");
        	}
        }

        if (drawID >= 0)
        {
	        Log.info("Exporting video...");
	        
	        Runtime rt = Runtime.getRuntime();
	        String videoFile = Log.getLogDir() + "/" + m_name + "_" + String.format("%05d", drawID) + "_scenario_" + Main.SCENARIO_NUM + ".mp4";
	        String[] commands = null;
	        
	        if (Main.environment == Environment.CRAFT_WORLD)
	        {
	        	commands = new String[] {"ffmpeg", "-r", "10", "-i", Log.getLogDir() + "/img/" + m_name + "_" + String.format("%05d", drawID) + "_img_%05d" + ".png", "-c:v", "libx264", "-vf", "scale=" + (CraftWorldRenderer.grid[0].length * 4) + ":" + (CraftWorldRenderer.grid.length * 4) + ", fps=25", "-pix_fmt", "yuv420p", videoFile};
	        }
	        else if (Main.environment == Environment.OFFICE_WORLD)
	        {
	        	commands = new String[] {"ffmpeg", "-r", "10", "-i", Log.getLogDir() + "/img/" + m_name + "_" + String.format("%05d", drawID) + "_img_%05d" + ".png", "-c:v", "libx264", "-vf", "scale=328:248, fps=25", "-pix_fmt", "yuv420p", videoFile};
	        }

	        Process proc;
			try {
				proc = rt.exec(commands);
				BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = input.readLine()) != null)
				{
					Log.info(line);
				}
		        if (!proc.waitFor(10, TimeUnit.SECONDS))
		        {
		        	proc.destroy();
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
	
	        // Clean up the image files that the video was created from
	        if (!Main.KEEP_PNG_FILES)
	        {
		        File folder = new File(Log.getLogDir() + "/img");
		        for (File f : folder.listFiles()) {
		            if (f.getName().endsWith(".png")) {
		                f.delete();
		            }
		        }
	        }
	        
	        Log.info("Done.");
        }
        
        return currentState;
    }
    
    public void writeResults(State s, long total_match_time)
    {
		try
		{
			String csv_file = Log.getLogDir() + "/match_results.csv";
			File f = new File(csv_file);
			boolean firstWrite = !f.exists();
			
			FileWriter fw = new FileWriter(csv_file, true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        PrintWriter out = new PrintWriter(bw);
	        
	        // Output the header row if this is the first record we're writing
	        if (firstWrite)
	        {
		        StringBuilder str = new StringBuilder();
		        
		        str.append("MatchName,ScenarioNum,ForestName,Perspective,AssumedPoliteness,MatchAge,MatchTimeMillis,P1Name,P1Score,P1Return,P2Name,P2Score,P2Return");
		        
	        	if (Main.environment == Environment.CRAFT_WORLD)
	        	{
			        for (int i = 0; i < numAgents; i++)
			        {
						for (CraftWorldItem item : Enums.CraftWorldItem.values())
				        {
				    		str.append(",A" + i + "_Assign_" + item.text_rep);
				        }
						
						for (CraftWorldItem item : Enums.CraftWorldItem.values())
				        {
				    		str.append(",A" + i + "_Collect_" + item.text_rep);
				        }
			        }
	        	}
		        
	        	out.println(str.toString());
	        }
	        
	        StringBuilder str = new StringBuilder();
            str.append(m_name);
            str.append("," + Main.SCENARIO_NUM);
            str.append("," + initialState.forest_name);
            str.append(",DUMMY");
            str.append("," + assumed_politeness);
            str.append("," + s.age);
            str.append("," + total_match_time);
            
	        for (int i = 0; i < numAgents; i++)
	        {
	        	str.append("," + agent_names[i].replace("_clone", ""));
	    		str.append("," + s.getStateScore(i));
	    		str.append("," + s.agentReturn[i]);
	        }
	        
        	if (Main.environment == Environment.CRAFT_WORLD)
        	{
        		CraftWorldState cws = new CraftWorldState(s);
        		
		        for (int i = 0; i < numAgents; i++)
		        {
					for (CraftWorldItem item : Enums.CraftWorldItem.values())
			        {
			    		str.append("," + (Main.cw_settings.goalItems.get(i).contains(item) ? 1 : 0));
			        }
					
					for (CraftWorldItem item : Enums.CraftWorldItem.values())
			        {
			    		str.append("," +  (cws.haveItem[item.id][i] ? 1 : 0));
			        }
		        }
	        }

	        out.println(str.toString());
    	    out.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
