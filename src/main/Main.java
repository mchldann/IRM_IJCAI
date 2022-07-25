package main;

import scheduler.A_Star_Agent;
import scheduler.A_Star_Solver;
import scheduler.Agent;
import scheduler.MCTS_Scheduler;
import scheduler.MCTS_Scheduler.ActionType;
import scheduler.MCTS_Scheduler.VisionType;
import scheduler.Match;
import scheduler.Match.AllianceType;
import scheduler.State;
import xml2bdi.XMLReader;
import uno.gpt.generators.*;
import uno.gpt.structure.GoalNode;
import uno.gpt.structure.Literal;
import util.Log;
import util.Enums.CraftWorldItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import craftworld.CraftWorldGeneratorSettings;
import craftworld.CraftWorldQLearner;
import craftworld.CraftWorldRLAgent;
import craftworld.CraftWorldState;
import officeworld.OfficeWorldPosition;
import officeworld.OfficeWorldQLearner;
import officeworld.OfficeWorldRLAgent;
import officeworld.OfficeWorldState;

public class Main
{
    public static Environment environment = Environment.CRAFT_WORLD;
 
    // MCTS settings
    public static int MCTS_ALPHA = 100;
    public static int MCTS_BETA = 10;
    public static double MCTS_C = Math.sqrt(2.0); // Theoretical value is sqrt(2), but that's for two-player zero sum games.
    public static boolean MCTS_ALLOW_PASS = false;
    
    // The score assigned by MCTS when an episode fails. Note that this is different to the "infinite negative reward states" used in the calculation of the tactic set.
    public static final double FAIL_SCORE = -1.0;
    
    public static final double RL_AGENT_SIM_TAU = 0.015;
    public static final double RL_AGENT_ALT_SIM_TAU = 0.01;
    public static final double RL_AGENT_ACTUAL_TAU = 0.01;
    public static final double RL_AGENT_ERRATIC_TAU = 0.02;
    
    // The discount factor used in all experiments.
    public static final double GAMMA = 0.99;
    
    public enum Environment
    {
        OFFICE_WORLD(0),
        CRAFT_WORLD(1);
        
        public final int id;

        private Environment(int id) {
            this.id = id;
        }
    }
    
    public enum OfficeWorldTask
    {
        DELIVER_COFFEE(0),
        DELIVER_MAIL(1),
        DELIVER_COFFEE_AND_MAIL(2),
        PATROL(3);
        
        public final int id;

        private OfficeWorldTask(int id) {
            this.id = id;
        }
    }
    
    public static final int[] OFFICE_WORLD_NUM_STATES_BY_TASK = new int[] {-1, -1, 1728, 432};
    
    private static Random rm = new Random();
    
    public static final int OFFICE_WORLD_TIMEOUT = 1000;
    public static final int CRAFT_WORLD_TIMEOUT = 250;
    
    public static OfficeWorldTask officeWorldTask = OfficeWorldTask.DELIVER_COFFEE_AND_MAIL;
    
    public static final boolean DELETE_CLOSEST_RESOURCES = false;

    public static CraftWorldGeneratorSettings cw_settings;

    public static int[] SCENARIOS;
    public static double[] POLITENESS_BY_SCENARIO;
    
    public static int SCENARIO_NUM = 0;
    public static double POLITENESS = 0;
    
    public static final int CW_ENVIRONMENT_REPS = 1;
    
    public static OfficeWorldQLearner[][] ow_qLearners;
    public static CraftWorldQLearner[] cw_qLearners;
    public static A_Star_Solver[] aStarSolvers;
    public static boolean USE_CUSTOM_OFFICEWORLD_LOCATIONS = false;
    
    // This is the "infinite" negative reward used in the calculation of the tactic set. Setting it to a large negative number has basically the same effect under a softmax policy.
    public static final double OW_Q_LEARNING_DECORATION_PENALTY = -1000.0;
    
    // Can turn this on for debugging purposes if the videos aren't rendering properly.
    public static final boolean KEEP_PNG_FILES = false;
    
    // Craft World generator settings
    public static final int GRID_SIZE = 5;
    
    public static final int FAC_NUM = 1;
    public static final int GPT_FAC_NUM = 1; // TODO: Need to add extra code if != FAC_NUM
    
    public static final int WB_NUM = 1;
    public static final int GPT_WB_NUM = 1; // TODO: Need to add extra code if != WB_NUM
    
    public static final int TS_NUM = 2;
    public static final int GPT_TS_NUM = 1;
    
    public static final int TREE_NUM = 3;
    public static final int GPT_TREE_NUM = 2;
    
    public static final int GRASS_NUM = 3;
    public static final int GPT_GRASS_NUM = 2;
    
    public static final int IRON_NUM = 3;
    public static final int GPT_IRON_NUM = 2;

    public static final int GOLD_NUM = 1;
    public static final int GEM_NUM = 1;
    
    public static void main(String[] args) throws Exception
    {
        SCENARIOS = (environment == Environment.OFFICE_WORLD)? new int[] {0} : new int[] {1, 2, 3, 4};
        POLITENESS_BY_SCENARIO = (environment == Environment.OFFICE_WORLD)? new double[] {1.0} : new double[] {0.0, 0.0, -1.0, -1.0};
        
        Log.log_to_file = false;
        
        int experiment_repetitions = 1000;
        
        String xml_file;
        if (environment == Environment.OFFICE_WORLD)
        {
            xml_file = "officeworld.xml";
        }
        else if (environment == Environment.CRAFT_WORLD)
        {
            xml_file = null; // Use the generator
        }
        else
        {
            throw new Exception("Unrecognised environment!");
        }
        
        // Agents
        ArrayList<String> agent_names = new ArrayList<String>();
        ArrayList<Agent> agents = new ArrayList<Agent>();
        ArrayList<Agent> agent_clones = new ArrayList<Agent>(); // For mirror matches
        
        
        ////////// Office World agents //////////
        
        // Agent 0: Office World A*
        agent_names.add("OW_A_star");
        agents.add(new A_Star_Agent());
        agent_clones.add(new A_Star_Agent());
        
        // Agent 1: Office World RL Agent (tau = 0.01)
        agent_names.add("OW_RL_agent");
        agents.add(new OfficeWorldRLAgent(RL_AGENT_ACTUAL_TAU));
        agent_clones.add(new OfficeWorldRLAgent(RL_AGENT_ACTUAL_TAU));
        
        // Agent 2: Office World RL Agent (tau = 0.02)
        agent_names.add("Erratic_RL_agent");
        agents.add(new OfficeWorldRLAgent(RL_AGENT_ERRATIC_TAU));
        agent_clones.add(new OfficeWorldRLAgent(RL_AGENT_ERRATIC_TAU));
        
        
        ////////// Craft World agents //////////
        
        // Fully co-designed agents
        
        // Agent 3: SP_MCTS
        agent_names.add("SP_MCTS");
        agents.add(new MCTS_Scheduler(0, true, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(0, true, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        
        // Agent 4: I_B
        agent_names.add("I_B");
        agents.add(new MCTS_Scheduler(0, false, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(0, false, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        
        // Agent 5: I_RM
        agent_names.add("I_RM");
        agents.add(new MCTS_Scheduler(0, false, ActionType.GPT, ActionType.RL, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(0, false, ActionType.GPT, ActionType.RL, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));

        // Not co-designed agents
        
        // Agent 6: Craft World A* (equivalent to following the Q-learning greedy policy, hence set up as a CraftWorldRLAgent)
        agent_names.add("A*");
        agents.add(new CraftWorldRLAgent(0.000001));
        agent_clones.add(new CraftWorldRLAgent(0.000001));
        
        // Agent 7: Craft World RL Agent (tau = 0.01)
        agent_names.add("Craft_world_RL_agent");
        agents.add(new CraftWorldRLAgent(RL_AGENT_ACTUAL_TAU));
        agent_clones.add(new CraftWorldRLAgent(RL_AGENT_ACTUAL_TAU));
        
        // Agent 8: Craft World RL Agent (tau = 0.02)
        agent_names.add("Erratic_craft_world_RL_agent");
        agents.add(new CraftWorldRLAgent(RL_AGENT_ERRATIC_TAU));
        agent_clones.add(new CraftWorldRLAgent(RL_AGENT_ERRATIC_TAU));
        
        // Partially co-designed agents
        
        // Agent 9: SP_MCTS with different GPT generation seed
        agent_names.add("SP_MCTS_alt_forest");
        agents.add(new MCTS_Scheduler(1, true, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(1, true, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        
        // Agent 10: I_B with different GPT generation seed
        agent_names.add("I_B_alt_forest");
        agents.add(new MCTS_Scheduler(1, false, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(1, false, ActionType.GPT, ActionType.GPT, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        
        // Agent 11: I_RM with different GPT generation seed
        agent_names.add("I_RM_alt_forest");
        agents.add(new MCTS_Scheduler(1, false, ActionType.GPT, ActionType.RL, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));
        agent_clones.add(new MCTS_Scheduler(1, false, ActionType.GPT, ActionType.RL, false, VisionType.FULL, MCTS_ALPHA, MCTS_BETA, MCTS_C, RL_AGENT_SIM_TAU, 1.0, MCTS_ALLOW_PASS));

        
        int[] evaluation_agents = new int[] {3, 4, 5};
        
        for (int experiment_num = 0; experiment_num < experiment_repetitions; experiment_num++)
        {
            for (int scenario_idx = 0; scenario_idx < SCENARIOS.length; scenario_idx++)
            {
                SCENARIO_NUM = SCENARIOS[scenario_idx];
                POLITENESS = POLITENESS_BY_SCENARIO[scenario_idx];

                String full_filename = null;
                
                if (environment == Environment.OFFICE_WORLD)
                {
                    full_filename = xml_file;
                }
                else if (environment == Environment.CRAFT_WORLD)
                {
                    if (experiment_num % CW_ENVIRONMENT_REPS == 0)
                    {
                        xml_file = "experiment_" + experiment_num + "_scenario_" + SCENARIO_NUM + ".xml";
                        randomiseCraftWorldSettings();
                    }
                    
                    full_filename = Log.getLogDir() + "/" + xml_file;
    
                    generateCraftWorldXML(full_filename);
                    
                    cw_settings.serialise_to_xml(Log.getLogDir() + "/" + "experiment_" + experiment_num + "_scenario_" + SCENARIO_NUM + "_settings.xml");
                }
                
                XMLReader reader = new XMLReader(full_filename);
    
                // Read the initial state from the XML file
                State currentState = new State(xml_file, reader.getBeliefs(), reader.getBeliefs().clone(), reader.getIntentions(), 0, 0, new boolean[] {false, false});
    
                // Update numGoalPlanTrees in case we loaded from a pre-generated xml file
                int numGoalPlanTrees = currentState.intentions.size();
                
                int[][] agent_pairings = null;
                
                if (environment == Environment.OFFICE_WORLD)
                {
                    agent_pairings = new int[evaluation_agents.length * 3][2];
                            
                    for (int i = 0; i < evaluation_agents.length; i++)
                    {
                        // Not co-designed
                        agent_pairings[i * 3] = new int[] {evaluation_agents[i], 0};
                        agent_pairings[i * 3 + 1] = new int[] {evaluation_agents[i], 1};
                        agent_pairings[i * 3 + 2] = new int[] {evaluation_agents[i], 2};
                    }
                }
                else if (environment == Environment.CRAFT_WORLD)
                {
                    agent_pairings = new int[evaluation_agents.length * 9][2];
                    
                    for (int i = 0; i < evaluation_agents.length; i++)
                    {
                        // Co-designed
                        agent_pairings[i * 9] = new int[] {evaluation_agents[i], 3};
                        agent_pairings[i * 9 + 1] = new int[] {evaluation_agents[i], 4};
                        agent_pairings[i * 9 + 2] = new int[] {evaluation_agents[i], 5};
                        
                        // Not co-designed
                        agent_pairings[i * 9 + 3] = new int[] {evaluation_agents[i], 6};
                        agent_pairings[i * 9 + 4] = new int[] {evaluation_agents[i], 7};
                        agent_pairings[i * 9 + 5] = new int[] {evaluation_agents[i], 8};
                        
                        // Partially co-designed
                        agent_pairings[i * 9 + 6] = new int[] {evaluation_agents[i], 9};
                        agent_pairings[i * 9 + 7] = new int[] {evaluation_agents[i], 10};
                        agent_pairings[i * 9 + 8] = new int[] {evaluation_agents[i], 11};
                    }
                }
                
                State randomStartState = currentState.clone();
                boolean validSpawns = false;
                while (!validSpawns)
                {
                    if (environment == Environment.OFFICE_WORLD)
                    {
                        OfficeWorldPosition agent_0_pos = new OfficeWorldPosition(rm.nextInt(4), rm.nextInt(3), rm.nextInt(3), rm.nextInt(3));
                        OfficeWorldPosition agent_1_pos = new OfficeWorldPosition(rm.nextInt(4), rm.nextInt(3), rm.nextInt(3), rm.nextInt(3));
                        
                        randomStartState.placeAgent_OW(0, agent_0_pos);
                        randomStartState.placeAgent_OW(1, agent_1_pos);
                        
                        OfficeWorldState ows = new OfficeWorldState(randomStartState);
                        
                        int manhattanDist = Math.abs(agent_0_pos.room_x - agent_1_pos.room_x) + Math.abs(agent_0_pos.room_y - agent_1_pos.room_y);
                        
                        boolean acceptableManhattanDistOfOne = (agent_0_pos.roomID().equals("1-1") && agent_1_pos.roomID().equals("0-1"))
                            || (agent_0_pos.roomID().equals("2-1") && agent_1_pos.roomID().equals("3-1"))
                            || (agent_1_pos.roomID().equals("1-1") && agent_0_pos.roomID().equals("0-1"))
                            || (agent_1_pos.roomID().equals("2-1") && agent_0_pos.roomID().equals("3-1"));
                        
                        validSpawns = !randomStartState.agentsInSameRoom()
                            && !ows.atDecoration(0)
                            && !ows.atDecoration(1)
                            && (manhattanDist > 1 || (manhattanDist == 1 && acceptableManhattanDistOfOne));
                        
                        // Correct for bug in the xml file where there are no plans if an agent spawns at the middle of room (0, 2)
                        if ((agent_0_pos.room_x == 0 && agent_0_pos.room_y == 2 && agent_0_pos.cell_x == 1 && agent_0_pos.cell_y == 1)
                            || (agent_1_pos.room_x == 0 && agent_1_pos.room_y == 2 && agent_1_pos.cell_x == 1 && agent_1_pos.cell_y == 1))
                        {
                            validSpawns = false;
                        }
                    }
                    else if (environment == Environment.CRAFT_WORLD)
                    {
                        validSpawns = true;
                    }
                }
                
                // Calculate the optimal RL policy for the current environment.
                // Note: For tasks where the only non-zero reward is paid upon success, the heuristic value, 'h', of the tactic set is identical to the optimal action-value calculated by Q-learning.
                // Therefore, for efficiency, we just reuse the action-values of the RL baselines, rather than calculating the tactic sets separately.
                
                aStarSolvers = new A_Star_Solver[2];
                ow_qLearners = new OfficeWorldQLearner[4][2];
                
                if (environment == Environment.OFFICE_WORLD)
                {
                    for (int agent_num = 0; agent_num < 2; agent_num++)
                    {
                        if (officeWorldTask == OfficeWorldTask.DELIVER_COFFEE_AND_MAIL)
                        {
                            aStarSolvers[agent_num] = new A_Star_Solver(new OfficeWorldState(randomStartState), officeWorldTask, agent_num);
                            Log.info("Solution length = " + aStarSolvers[agent_num].finalPath.size() + "\n");
                        }
                        
                        Log.info("Running Q-learning for agent " + agent_num + "...\n");
                        ow_qLearners[officeWorldTask.id][agent_num] = new OfficeWorldQLearner(new OfficeWorldState(randomStartState), officeWorldTask, agent_num);
                        ow_qLearners[officeWorldTask.id][agent_num].train(1000);
                    }
                }
                else if (environment == Environment.CRAFT_WORLD)
                {
                    Log.info("Running Q-learning for craft world...\n");
                    cw_qLearners = new CraftWorldQLearner[2];
                    
                    cw_qLearners[0] = new CraftWorldQLearner(new CraftWorldState(randomStartState), 0);
                    cw_qLearners[0].train(1000, 0.000001f, false);
                    
                    cw_qLearners[1] = new CraftWorldQLearner(new CraftWorldState(randomStartState), 1);
                    cw_qLearners[1].train(1000, 0.000001f, false);
                }
                
                Log.info("Q-learning done.\n");
            

                for (int[] pairing : agent_pairings)
                {
                    int agent_1 = pairing[0];
                    int agent_2 = pairing[1];
                    
                    new Match(agent_names.get(agent_1) + "_and_" + agent_names.get(agent_2) + (agent_1 == agent_2? "_clone" : ""),
                            numGoalPlanTrees,
                            AllianceType.NEUTRAL,
                            randomStartState.clone(),
                            new Agent[] {agents.get(agent_1), agent_1 == agent_2? agent_clones.get(agent_2) : agents.get(agent_2)},
                            new String[] {agent_names.get(agent_1), agent_names.get(agent_2)},
                            POLITENESS, experiment_num).run(true,  true,  false, false);
                    
                    // Mirror
                    new Match(agent_names.get(agent_1) + "_and_" + agent_names.get(agent_2) + (agent_1 == agent_2? "_clone" : "") + "_mirror",
                            numGoalPlanTrees,
                            AllianceType.NEUTRAL,
                            randomStartState.clone(),
                            new Agent[] {agents.get(agent_2), agent_2 == agent_1? agent_clones.get(agent_1) : agents.get(agent_1)},
                            new String[] {agent_names.get(agent_2), agent_names.get(agent_1)},
                            POLITENESS, experiment_num).run(true,  true,  false, false);
                }
            }
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public static void randomiseCraftWorldSettings()
    {
        cw_settings = new CraftWorldGeneratorSettings();
        
        cw_settings.goalItems = new ArrayList<List<CraftWorldItem>>();
        cw_settings.itemsToMake = new ArrayList<List<CraftWorldItem>>();
        
        for (int agent_num = 0; agent_num < 2; agent_num++)
        {
            cw_settings.goalItems.add(new ArrayList<CraftWorldItem>());
            cw_settings.itemsToMake.add(new ArrayList<CraftWorldItem>());
            
            if (SCENARIO_NUM == 1)
            {
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.GOLD);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.BED);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.AXE);

                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.GOLD);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.BED);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.AXE);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.STICK);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.PLANK);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.BRIDGE);
            }
            else if (SCENARIO_NUM == 2)
            {
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.GEM);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.BRIDGE);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.AXE);
                
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.GEM);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.BRIDGE);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.AXE);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.STICK);
                
                if (agent_num == 0)
                {
                    cw_settings.goalItems.get(agent_num).add(CraftWorldItem.ROPE);
                    cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.ROPE);
                }
                else
                {
                    cw_settings.goalItems.get(agent_num).add(CraftWorldItem.CLOTH);
                    cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.CLOTH);
                }
            }
            else if (SCENARIO_NUM == 3)
            {
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.STICK);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.PLANK);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.CLOTH);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.ROPE);
                
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.STICK);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.PLANK);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.CLOTH);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.ROPE);
            }
            else if (SCENARIO_NUM == 4)
            {
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.GOLD);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.BRIDGE);
                cw_settings.goalItems.get(agent_num).add(CraftWorldItem.CLOTH);
                
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.GOLD);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.BRIDGE);
                cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.CLOTH);
                
                if (agent_num == 0)
                {
                    cw_settings.goalItems.get(agent_num).add(CraftWorldItem.ROPE);
                    cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.ROPE);
                }
                else
                {
                    cw_settings.goalItems.get(agent_num).add(CraftWorldItem.PLANK);
                    cw_settings.itemsToMake.get(agent_num).add(CraftWorldItem.PLANK);
                }
            }
        }
        
        cw_settings.grid_size = GRID_SIZE;
        
        cw_settings.fac_num = FAC_NUM;
        cw_settings.gpt_fac_num = GPT_FAC_NUM;
        
        cw_settings.wb_num = WB_NUM;
        cw_settings.gpt_wb_num = GPT_WB_NUM;
        
        cw_settings.ts_num = TS_NUM;
        cw_settings.gpt_ts_num = GPT_TS_NUM;
        
        cw_settings.tree_num = TREE_NUM;
        cw_settings.gpt_tree_num = GPT_TREE_NUM;
        
        cw_settings.grass_num = GRASS_NUM;
        cw_settings.gpt_grass_num = GPT_GRASS_NUM;
        
        cw_settings.iron_num = IRON_NUM;
        cw_settings.gpt_iron_num = GPT_IRON_NUM;
        
        cw_settings.gold_num = GOLD_NUM;
        cw_settings.gem_num = GEM_NUM;
        
        ArrayList<Integer> positions = new ArrayList<>();

        // randomly generate positions for tree
        int counter = 0;
        while (counter < cw_settings.tree_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.tx.add(x);
            cw_settings.ty.add(y);
            counter++;
        }
        
        // randomly generate positions for toolshed
        counter = 0;
        while (counter < cw_settings.ts_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.tsx.add(x);
            cw_settings.tsy.add(y);
            counter++;
        }
        
        // randomly generate positions for workbench
        counter = 0;
        while (counter < cw_settings.wb_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.wbx.add(x);
            cw_settings.wby.add(y);
            counter++;
        }
        
        // randomly generate positions for grass
        counter = 0;
        while (counter < cw_settings.grass_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.grassx.add(x);
            cw_settings.grassy.add(y);
            counter++;
        }
        
        // randomly generate positions for factory
        counter = 0;
        while (counter < cw_settings.fac_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.facx.add(x);
            cw_settings.facy.add(y);
            counter++;
        }
        
        // randomly generate positions for iron
        counter = 0;
        while (counter < cw_settings.iron_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.ironx.add(x);
            cw_settings.irony.add(y);
            counter++;
        }
        
        // randomly generate positions for gold
        counter = 0;
        while (counter < cw_settings.gold_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.goldx.add(x);
            cw_settings.goldy.add(y);
            counter++;
        }
        
        // randomly generate positions for gem
        counter = 0;
        while (counter < cw_settings.gem_num){
            int n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            while (positions.contains(n)){
                n = rm.nextInt(cw_settings.grid_size * cw_settings.grid_size);
            }
            int x = n / cw_settings.grid_size;
            int y = n % cw_settings.grid_size;
            positions.add(n);
            cw_settings.gemx.add(x);
            cw_settings.gemy.add(y);
            counter++;
        }
        
        // agent's location
        cw_settings.spawn_x = rm.nextInt(cw_settings.grid_size);
        cw_settings.spawn_y = rm.nextInt(cw_settings.grid_size);

        // opponent's location
        cw_settings.op_spawn_x = rm.nextInt(cw_settings.grid_size);
        cw_settings.op_spawn_y = rm.nextInt(cw_settings.grid_size);
        while (cw_settings.spawn_x == cw_settings.op_spawn_x && cw_settings.spawn_y == cw_settings.op_spawn_y) {
            cw_settings.op_spawn_x = rm.nextInt(cw_settings.grid_size);
            cw_settings.op_spawn_y = rm.nextInt(cw_settings.grid_size);
        }
        
        // Set up trimmed arrays for each goal
        int num_top_level_goals = 9;
        
        cw_settings.tx_trimmed_arr = new ArrayList[2][num_top_level_goals];
        cw_settings.ty_trimmed_arr = new ArrayList[2][num_top_level_goals];

        cw_settings.grassx_trimmed_arr = new ArrayList[2][num_top_level_goals];
        cw_settings.grassy_trimmed_arr = new ArrayList[2][num_top_level_goals];

        cw_settings.ironx_trimmed_arr = new ArrayList[2][num_top_level_goals];
        cw_settings.irony_trimmed_arr = new ArrayList[2][num_top_level_goals];
        
        cw_settings.tsx_trimmed_arr = new ArrayList[2][num_top_level_goals];
        cw_settings.tsy_trimmed_arr = new ArrayList[2][num_top_level_goals];
        
        for (int forest_idx = 0; forest_idx < 2; forest_idx++)
        {
            cw_settings.tx_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            cw_settings.ty_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            
            cw_settings.grassx_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            cw_settings.grassy_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            
            cw_settings.ironx_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            cw_settings.irony_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            
            cw_settings.tsx_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            cw_settings.tsy_trimmed_arr[forest_idx] = new ArrayList[num_top_level_goals];
            
            for (int index = 0; index < num_top_level_goals; index++)
            {
                cw_settings.tx_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.tx);
                cw_settings.ty_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.ty);
                
                cw_settings.grassx_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.grassx);
                cw_settings.grassy_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.grassy);
                
                cw_settings.ironx_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.ironx);
                cw_settings.irony_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.irony);
                
                cw_settings.tsx_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.tsx);
                cw_settings.tsy_trimmed_arr[forest_idx][index] = new ArrayList<Integer>(cw_settings.tsy);
                
                // delete possible resource for gpt generation
                deletePositions(cw_settings.tx_trimmed_arr[forest_idx][index], cw_settings.ty_trimmed_arr[forest_idx][index], cw_settings.gpt_tree_num);
                deletePositions(cw_settings.grassx_trimmed_arr[forest_idx][index], cw_settings.grassy_trimmed_arr[forest_idx][index], cw_settings.gpt_grass_num);
                deletePositions(cw_settings.ironx_trimmed_arr[forest_idx][index], cw_settings.irony_trimmed_arr[forest_idx][index], cw_settings.gpt_iron_num);
                deletePositions(cw_settings.tsx_trimmed_arr[forest_idx][index], cw_settings.tsy_trimmed_arr[forest_idx][index], cw_settings.gpt_ts_num);
            }
        }

        Log.info("Tree:");
        for(int m = 0; m < cw_settings.tx.size(); m++){
            Log.info("("+cw_settings.tx.get(m)+","+cw_settings.ty.get(m)+")");
        }
        Log.info("Toolshed:");
        for(int m = 0; m < cw_settings.tsx.size(); m++){
            Log.info("("+cw_settings.tsx.get(m)+","+cw_settings.tsy.get(m)+")");
        }
        Log.info("Workbench:");
        for(int m = 0; m < cw_settings.wbx.size(); m++){
            Log.info("("+cw_settings.wbx.get(m)+","+cw_settings.wby.get(m)+")");
        }
        Log.info("Grass:");
        for(int m = 0; m < cw_settings.grassx.size(); m++){
            Log.info("("+cw_settings.grassx.get(m)+","+cw_settings.grassy.get(m)+")");
        }
        Log.info("Factory:");
        for(int m = 0; m < cw_settings.facx.size(); m++){
            Log.info("("+cw_settings.facx.get(m)+","+cw_settings.facy.get(m)+")");
        }
        Log.info("Iron:");
        for(int m = 0; m < cw_settings.ironx.size(); m++){
            Log.info("("+cw_settings.ironx.get(m)+","+cw_settings.irony.get(m)+")");
        }
        Log.info("Gold:");
        for(int m = 0; m < cw_settings.goldx.size(); m++){
            Log.info("("+cw_settings.goldx.get(m)+","+cw_settings.goldy.get(m)+")");
        }
        Log.info("Gem:");
        for(int m = 0; m < cw_settings.gemx.size(); m++){
            Log.info("("+cw_settings.gemx.get(m)+","+cw_settings.gemy.get(m)+")");
        }
        Log.info("Agent:");
        Log.info("("+cw_settings.spawn_x+","+cw_settings.spawn_y+")");
        Log.info("Opponent:");
        Log.info("("+cw_settings.op_spawn_x+","+cw_settings.op_spawn_y+")");
    }

    private static void deletePositions(List<Integer> xs, List<Integer> ys, int n)
    {
        if (DELETE_CLOSEST_RESOURCES)
        {
            while (xs.size() > n)
            {
                double min_dist = Double.MAX_VALUE;
                int min_idx = -1;
                
                for (int i = 0; i < xs.size(); i++)
                {
                    double dist = Math.pow(xs.get(i) - cw_settings.spawn_x, 2.0) + Math.pow(ys.get(i) - cw_settings.spawn_y, 2.0)
                        + Math.pow(xs.get(i) - cw_settings.op_spawn_x, 2.0) + Math.pow(ys.get(i) - cw_settings.op_spawn_y, 2.0);
                    
                    if (dist < min_dist)
                    {
                        min_dist = dist;
                        min_idx = i;
                    }
                }
                
                xs.remove(min_idx);
                ys.remove(min_idx);
            }
        }
        else
        {
            while (xs.size() > n)
            {
                int m = rm.nextInt(xs.size());
                xs.remove(m);
                ys.remove(m);
            }
        }
    }
    
    public static void generateCraftWorldXML(String outputFile)
    {
        MCGenerator gen = new MCGenerator(cw_settings);

        HashMap<String, Literal> environment = gen.genEnvironment();

        // generate the tree
        ArrayList<GoalNode> goalForests = new ArrayList<>();

        for (int forest_index = 0; forest_index < 2; forest_index++)
        {
            gen.forest_index = forest_index;
            
            for (int agent = 0; agent < 2; agent++)
            {
                MCGenerator.setStringConstants(agent == 1);
     
                ArrayList<CraftWorldItem> itemsToGenGPTsFor = (ArrayList<CraftWorldItem>)cw_settings.goalItems.get(agent);
    
                for (CraftWorldItem item : itemsToGenGPTsFor)
                {
                    goalForests.add(gen.genTopLevelGoal(item.objective_num));
                }
            }
        }

        // write the set of goal plan tree to an XML file
        XMLWriter wxf = new XMLWriter();
        wxf.CreateXML(environment, goalForests, outputFile);
    }
}