package uno.gpt.generators;

import uno.gpt.structure.ActionNode;
import uno.gpt.structure.GoalNode;
import uno.gpt.structure.Literal;
import uno.gpt.structure.PlanNode;
import java.util.*;

import craftworld.CraftWorldGeneratorSettings;

public class MCGenerator extends AbstractGenerator {

    /** Default values */
    public static final int def_size = 39; // default size
    public static final int def_tree = 5; // default tree
    public static final int def_ts = 2; // default toolshed
    public static final int def_wb = 2; // default workbench
    public static final int def_grass = 5; // default grass
    public static final int def_fac = 2; // default factory
    public static final int def_iron = 5; // default iron
    public static final int def_gold = 2; // default gold
    public static final int def_gem = 2; // default gem

    /** Default values for gpt generation */
    public static final int gpt_tree = 3; // default tree
    public static final int gpt_ts = 1; // default toolshed
    public static final int gpt_wb = 1; // default workbench
    public static final int gpt_grass = 3; // default grass
    public static final int gpt_fac = 1; // default factory
    public static final int gpt_iron = 3; // default iron
    public static final int gpt_gold = 1; // default gold
    public static final int gpt_gem = 1; // default gem
    public static final boolean fixedorder = true;

    /** String Bank */
    public static void setStringConstants(boolean forOtherAgent)
    {
    	if (forOtherAgent)
    	{
    		ATX = OPATX;
    	    ATY = OPATY;
    	    WOOD = OPWOOD;
    	    GRASS = OPGRASS;
    	    IRON = OPIRON;
    	    GOLD = OPGOLD;
    	    GEM = OPGEM;
    	    PLANK = OPPLANK;
    	    STICK = OPSTICK;
    	    CLOTH = OPCLOTH;
    	    ROPE = OPROPE;
    	    BRIDGE = OPBRIDGE;
    	    BED = OPBED;
    	    AXE = OPAXE;
    	}
    	else
    	{
    		ATX = USATX;
    	    ATY = USATY;
    	    WOOD = USWOOD;
    	    GRASS = USGRASS;
    	    IRON = USIRON;
    	    GOLD = USGOLD;
    	    GEM = USGEM;
    	    PLANK = USPLANK;
    	    STICK = USSTICK;
    	    CLOTH = USCLOTH;
    	    ROPE = USROPE;
    	    BRIDGE = USBRIDGE;
    	    BED = USBED;
    	    AXE = USAXE;
    	}
    }
    
    private static final String AT = "At";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String OP = "Opponent";
    
    private static String ATX;
    private static String ATY;
    private static String WOOD;
    private static String GRASS;
    private static String IRON;
    private static String GOLD;
    private static String GEM;
    private static String PLANK;
    private static String STICK;
    private static String CLOTH;
    private static String ROPE;
    private static String BRIDGE;
    private static String BED;
    private static String AXE;

    private static final String USATX = AT + X;
    private static final String USATY = AT + Y;
    private static final String USWOOD = "HaveWood";
    private static final String USGRASS = "HaveGrass";
    private static final String USIRON = "HaveIron";
    private static final String USGOLD = "HaveGold";
    private static final String USGEM = "HaveGem";
    private static final String USPLANK = "HavePlank";
    private static final String USSTICK = "HaveStick";
    private static final String USCLOTH = "HaveCloth";
    private static final String USROPE = "HaveRope";
    private static final String USBRIDGE = "HaveBridge";
    private static final String USBED = "HaveBed";
    private static final String USAXE = "HaveAxe";

    private static final String OPATX = OP + USATX;
    private static final String OPATY = OP + USATY;
    private static final String OPWOOD = OP + USWOOD;
    private static final String OPGRASS = OP + USGRASS;
    private static final String OPIRON = OP + USIRON;
    private static final String OPGOLD = OP + USGOLD;
    private static final String OPGEM = OP + USGEM;
    private static final String OPPLANK = OP + USPLANK;
    private static final String OPSTICK = OP + USSTICK;
    private static final String OPCLOTH = OP + USCLOTH;
    private static final String OPROPE = OP + USROPE;
    private static final String OPBRIDGE = OP + USBRIDGE;
    private static final String OPBED = OP + USBED;
    private static final String OPAXE = OP + USAXE;
    
    private static final String MT = "MoveTo";
    private static final String UP = "MoveUp";
    private static final String LEFT ="MoveLeft";
    private static final String RIGHT = "MoveRight";
    private static final String DOWN = "MoveDown";
    private static final String DUMMY = "Dummy";
    private static final String MK = "Make";

    private CraftWorldGeneratorSettings settings;

    public int forest_index = 0;
    
    /** the position of trees */
    private List<Integer> tree_xs_trimmed;
    private List<Integer> tree_ys_trimmed;

    /** the position of grass */
    private List<Integer> grassx_trimmed;
    private List<Integer> grassy_trimmed;

    /** the position of iron */
	public List<Integer> ironx_trimmed;
	public List<Integer> irony_trimmed;
	
    /** the position of toolsheds */
	public List<Integer> tsx_trimmed;
	public List<Integer> tsy_trimmed;

    /**
     * Constructor
     */
    public MCGenerator(CraftWorldGeneratorSettings settings)
    {
    	this.settings = settings;
    }
    
	private void switchTrimmedArrays(int forest_index, int index)
    {
    	tree_xs_trimmed = settings.tx_trimmed_arr[forest_index][index];
    	tree_ys_trimmed = settings.ty_trimmed_arr[forest_index][index];
    	
    	grassx_trimmed = settings.grassx_trimmed_arr[forest_index][index];
    	grassy_trimmed = settings.grassy_trimmed_arr[forest_index][index];
    	
    	ironx_trimmed = settings.ironx_trimmed_arr[forest_index][index];
    	irony_trimmed = settings.irony_trimmed_arr[forest_index][index];
    	
    	tsx_trimmed = settings.tsx_trimmed_arr[forest_index][index];
    	tsy_trimmed = settings.tsy_trimmed_arr[forest_index][index];
    }

    /**
     * Build the environment, create all the predicates needed and logically assign starting positions for the agent and parcels
     * @return The environment HashMap
     */
    @Override
    public HashMap<String, Literal> genEnvironment() {

        environment = new HashMap<>();
        Literal workingLiteral;

        // Make the literal for the agent's position
        for (int i = 0; i < settings.grid_size; i++) {
            boolean value = false;
            if(i == this.settings.spawn_x)
                value = true;
            workingLiteral = new Literal(USATX + i, value,true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
            value = false;
            if(i == this.settings.spawn_y)
                value = true;
            workingLiteral = new Literal(USATY + i, value,true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }

        // Make the literal for the opponent's position x
        for (int i = 0; i < settings.grid_size; i++) {
            boolean value = false;
            if(i == this.settings.op_spawn_x)
                value = true;
            workingLiteral = new Literal(OPATX + i, value,true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
            value = false;
            if(i == this.settings.op_spawn_y)
                value = true;
            workingLiteral = new Literal(OPATY + i, value,true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }

        // Make the literal for wood
        workingLiteral = new Literal(USWOOD , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for grass
        workingLiteral = new Literal(USGRASS , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for iron
        workingLiteral = new Literal(USIRON , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for gold
        workingLiteral = new Literal(USGOLD , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for gem
        workingLiteral = new Literal(USGEM , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for plank
        workingLiteral = new Literal(USPLANK , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for stick
        workingLiteral = new Literal(USSTICK , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for cloth
        workingLiteral = new Literal(USCLOTH , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for rope
        workingLiteral = new Literal(USROPE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for bridge
        workingLiteral = new Literal(USBRIDGE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for bed
        workingLiteral = new Literal(USBED , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for axe
        workingLiteral = new Literal(USAXE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        
        
        // Make the literal for opponent wood
        workingLiteral = new Literal(OPWOOD , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent grass
        workingLiteral = new Literal(OPGRASS , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent iron
        workingLiteral = new Literal(OPIRON , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent gold
        workingLiteral = new Literal(OPGOLD , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent gem
        workingLiteral = new Literal(OPGEM , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent plank
        workingLiteral = new Literal(OPPLANK , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent stick
        workingLiteral = new Literal(OPSTICK , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent cloth
        workingLiteral = new Literal(OPCLOTH , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent rope
        workingLiteral = new Literal(OPROPE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent bridge
        workingLiteral = new Literal(OPBRIDGE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent bed
        workingLiteral = new Literal(OPBED , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);
        // Make the literal for opponent axe
        workingLiteral = new Literal(OPAXE , false,true, false);
        environment.put(workingLiteral.getId(), workingLiteral);

        
        // make the tree available
        for(int i = 0; i < settings.tx.size(); i++){
            workingLiteral = new Literal("TreeAtX"+settings.tx.get(i)+"Y"+settings.ty.get(i), true, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // make the grass available
        for(int i = 0; i < settings.grassx.size(); i++){
            workingLiteral = new Literal("GrassAtX"+settings.grassx.get(i)+"Y"+settings.grassy.get(i), true, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // make the iron available
        for(int i = 0; i < settings.ironx.size(); i++){
            workingLiteral = new Literal("IronAtX"+settings.ironx.get(i)+"Y"+settings.irony.get(i), true, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // make the gold available
        for(int i = 0; i < settings.goldx.size(); i++){
            workingLiteral = new Literal("GoldAtX"+settings.goldx.get(i)+"Y"+settings.goldy.get(i), true, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // make the gem available
        for(int i = 0; i < settings.gemx.size(); i++){
            workingLiteral = new Literal("GemAtX"+settings.gemx.get(i)+"Y"+settings.gemy.get(i), true, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }

        // Return the environment
        return environment;
    }


    /**
     * Function for creating one top level goal, which is reference to one parcel to be transported
     * @param index The index of the goal and of the parcel to be moved
     * @return The populated goalNode
     */
    @Override
    public GoalNode genTopLevelGoal(int index) {

    	switchTrimmedArrays(forest_index, index);
    	
        switch (index){
            case 0: return genPlankGoal();
            case 1: return genStickGoal();
            case 2: return genClothGoal();
            case 3: return genRopeGoal();
            case 4: return genBridgeGoal();
            case 5: return genBedGoal();
            case 6: return genAxeGoal();
            case 7: return genGoldGoal();
            case 8: return genGemGoal();
            default: return null;
        }
    }

    /**
     * @return a top-level goal to make a plank
     */
    private GoalNode genPlankGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Plank");
        // make the goal-condition
        Literal goalCond = produceLiteral(PLANK, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have wood
        PlanNode workingPlan = new PlanNode(MK+"Plank1");
        // generate precondition
        Literal preCond1 = produceLiteral(WOOD, false);
        workingPlan.getPre().add(preCond1);
        // generate its planbody
        // a subgoal to move to the tree position and collect the wood
        GoalNode getWood = genGetWood();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getWood);
        // another subgoal to craft the plank
        GoalNode craftPlank = genCraftPlank();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftPlank);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has wood
        PlanNode workingPlan2 = new PlanNode(MK+"Plank2");
        // generate precondition
        Literal preCond2 = produceLiteral(WOOD, true);
        workingPlan2.getPre().add(preCond2);
        // add a single subgoal
        workingPlan2.getPlanBody().add(craftPlank);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }

    /**
     * @return a top-level goal to make a stick
     */
    private GoalNode genStickGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Stick");
        // make the goal-condition
        Literal goalCond = produceLiteral(STICK, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have wood
        PlanNode workingPlan = new PlanNode(MK+"Stick1");
        // generate precondition
        Literal preCond1 = produceLiteral(WOOD, false);
        workingPlan.getPre().add(preCond1);
        // generate its planbody
        // a subgoal to move to the tree position and collect the wood
        GoalNode getWood = genGetWood();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getWood);
        // another subgoal to craft the stick
        GoalNode craftStick = genCraftStick();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftStick);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has wood
        PlanNode workingPlan2 = new PlanNode(MK+"Stick2");
        // generate precondition
        Literal preCond2 = produceLiteral(WOOD, true);
        workingPlan2.getPre().add(preCond2);
        // add a single subgoal
        workingPlan2.getPlanBody().add(craftStick);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }

    /**
     * @return a top-level goal to make a cloth
     */
    private GoalNode genClothGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Cloth");
        // make the goal-condition
        Literal goalCond = produceLiteral(CLOTH, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have grass
        PlanNode workingPlan = new PlanNode(MK+"Cloth1");
        // generate precondition
        Literal preCond1 = produceLiteral(GRASS, false);
        workingPlan.getPre().add(preCond1);
        // generate its planbody
        // a subgoal to move to the grass position and collect the grass
        GoalNode getGrass = genGetGrass();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getGrass);
        // another subgoal to craft the cloth
        GoalNode craftCloth = genCraftCloth();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftCloth);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has grass
        PlanNode workingPlan2 = new PlanNode(MK+"Cloth2");
        // generate precondition
        Literal preCond2 = produceLiteral(GRASS, true);
        workingPlan2.getPre().add(preCond2);
        // add a single subgoal
        workingPlan2.getPlanBody().add(craftCloth);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }


    /**
     * @return a top-level goal to make a rope
     */
    private GoalNode genRopeGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Rope");
        // make the goal-condition
        Literal goalCond = produceLiteral(ROPE, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have grass
        PlanNode workingPlan = new PlanNode(MK+"Rope1");
        // generate precondition
        Literal preCond1 = produceLiteral(GRASS, false);
        workingPlan.getPre().add(preCond1);
        // generate its planbody
        // a subgoal to move to the grass position and collect the grass
        GoalNode getGrass = genGetGrass();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getGrass);
        // another subgoal to craft the rope
        GoalNode craftRope = genCraftRope();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftRope);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has grass
        PlanNode workingPlan2 = new PlanNode(MK+"Rope2");
        // generate precondition
        Literal preCond2 = produceLiteral(GRASS, true);
        workingPlan2.getPre().add(preCond2);
        // add a single subgoal
        workingPlan2.getPlanBody().add(craftRope);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }

    /**
     * @return a top-level goal to make a bridge
     */
    private GoalNode genBridgeGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Bridge");
        // make the goal-condition
        Literal goalCond = produceLiteral(BRIDGE, true);
        workingGoal.getGoalConds().add(goalCond);

        // several plans to achieve the goal
        // plan 1: if the agent does not have wood and iron
        PlanNode workingPlan = new PlanNode(MK+"Bridge1");
        // generate precondition
        Literal preCond1 = produceLiteral(WOOD, false);
        Literal niron = produceLiteral(IRON, false);
        workingPlan.getPre().add(preCond1);
        workingPlan.getPre().add(niron);
        // generate its planbody
        // a subgoal to move to the tree position and collect the wood
        GoalNode getWood = genGetWood();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getWood);
        // a subgoal to move to the iron position to collect the iron
        GoalNode getIron = genGetIron();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getIron);
        // another subgoal to craft the bridge
        GoalNode craftBridge = genCraftBridge();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftBridge);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        if(!settings.gpt_order){
            // plan 2: if the agent does not have wood and iron, but collect wood and iron in reverse order
            PlanNode workingPlan2 = new PlanNode(MK+"Bridge2");
            // generate precondition
            workingPlan2.getPre().add(preCond1);
            workingPlan2.getPre().add(niron);
            // generate its planbody
            // a subgoal to move to the iron position to collect the iron
            workingPlan2.getPlanBody().add(getIron);
            // a subgoal to move to the tree position and collect the wood
            workingPlan2.getPlanBody().add(getWood);
            // another subgoal to craft the bridge
            workingPlan2.getPlanBody().add(craftBridge);
            // add this plan 1 to the top-level goal
            workingGoal.getPlans().add(workingPlan2);
        }


        // plan 3: if the agent does not have wood, but has iron
        PlanNode workingPlan3 = new PlanNode(MK+"Bridge3");
        // generate precondition
        Literal iron = produceLiteral(IRON, true);
        workingPlan3.getPre().add(preCond1);
        workingPlan3.getPre().add(iron);
        // generate its planbody
        // a subgoal to move to the tree position and collect the wood
        workingPlan3.getPlanBody().add(getWood);
        // another subgoal to craft the bridge
        workingPlan3.getPlanBody().add(craftBridge);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan3);

        // plan 4: if the agent does not have iron, but has wood
        PlanNode workingPlan4 = new PlanNode(MK+"Bridge4");
        // generate precondition
        Literal wood = produceLiteral(WOOD, true);
        workingPlan4.getPre().add(wood);
        workingPlan4.getPre().add(niron);
        // generate its planbody
        // a subgoal to move to the iron position and collect the iron
        workingPlan4.getPlanBody().add(getIron);
        // another subgoal to craft the bridge
        workingPlan4.getPlanBody().add(craftBridge);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan4);

        // plan 5: if the agent does have both iron and wood
        PlanNode workingPlan5 = new PlanNode(MK+"Bridge5");
        // generate precondition
        workingPlan5.getPre().add(wood);
        workingPlan5.getPre().add(iron);
        // generate its planbody
        // another subgoal to craft the bridge
        workingPlan5.getPlanBody().add(craftBridge);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan5);

        return workingGoal;
    }


    /**
     * @return a top-level goal to make a bed
     */
    private GoalNode genBedGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Bed");
        // make the goal-condition
        Literal goalCond = produceLiteral(BED, true);
        workingGoal.getGoalConds().add(goalCond);

        // several plans to achieve the goal
        // plan 1: if the agent does not have plank and grass
        PlanNode workingPlan = new PlanNode(MK+"Bed1");
        // generate precondition
        Literal nplank = produceLiteral(PLANK, false);
        Literal ngrass = produceLiteral(GRASS, false);
        workingPlan.getPre().add(nplank);
        workingPlan.getPre().add(ngrass);
        // generate its planbody
        // a subgoal to make a plank
        GoalNode getPlank = genPlankGoal();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getPlank);
        // a subgoal to move to the grass position to collect the grass
        GoalNode getGrass = genGetGrass();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getGrass);
        // another subgoal to craft the bed
        GoalNode craftBed = genCraftBed();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftBed);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        if(!settings.gpt_order){
            // plan 2: if the agent does not have plank and grass, but we collect the grass first
            PlanNode workingPlan2 = new PlanNode(MK+"Bed2");
            // generate precondition
            workingPlan2.getPre().add(nplank);
            workingPlan2.getPre().add(ngrass);
            // generate its planbody
            // a subgoal to move to the grass position to collect the grass
            workingPlan2.getPlanBody().add(getGrass);
            // a subgoal to make a plank
            workingPlan2.getPlanBody().add(getPlank);
            // another subgoal to craft the bed
            workingPlan2.getPlanBody().add(craftBed);
            // add this plan 2 to the top-level goal
            workingGoal.getPlans().add(workingPlan2);

            // plan 3: if the agent does not have plank and grass, but we collect wood, then grass, and then craft plank
            PlanNode workingPlan3 = new PlanNode(MK+"Bed3");
            // generate precondition
            workingPlan3.getPre().add(nplank);
            workingPlan3.getPre().add(ngrass);
            // generate its planbody
            // a subgoal to move to the tree position and collect the wood
            GoalNode getWood = genGetWood();
            workingPlan3.getPlanBody().add(getWood);
            // a subgoal to collect grass
            workingPlan3.getPlanBody().add(getGrass);
            // a subgoal to create a plank
            GoalNode craftPlank = genCraftPlank();
            workingPlan3.getPlanBody().add(craftPlank);
            // another subgoal to craft the bed
            workingPlan3.getPlanBody().add(craftBed);
            // add this plan 1 to the top-level goal
            workingGoal.getPlans().add(workingPlan3);
        }

        // plan 4: if the agent does not have plank, but has grass
        PlanNode workingPlan4 = new PlanNode(MK+"Bed4");
        // generate precondition
        Literal grass = produceLiteral(GRASS, true);
        workingPlan4.getPre().add(nplank);
        workingPlan4.getPre().add(grass);
        // generate its planbody
        // a subgoal to create a plank
        workingPlan4.getPlanBody().add(getPlank);
        // another subgoal to craft the bed
        workingPlan4.getPlanBody().add(craftBed);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan4);

        // plan 5: if the agent does not have grass, but has plank
        PlanNode workingPlan5 = new PlanNode(MK+"Bed5");
        // generate precondition
        Literal plank = produceLiteral(PLANK, true);
        workingPlan5.getPre().add(plank);
        workingPlan5.getPre().add(ngrass);
        // generate its planbody
        // a subgoal to collect grass
        workingPlan5.getPlanBody().add(getGrass);
        // another subgoal to craft the bed
        workingPlan5.getPlanBody().add(craftBed);
        // add this plan 5 to the top-level goal
        workingGoal.getPlans().add(workingPlan5);

        // plan 6: if the agent does have both plank and grass
        PlanNode workingPlan6 = new PlanNode(MK+"Bed6");
        // generate precondition
        workingPlan6.getPre().add(plank);
        workingPlan6.getPre().add(grass);
        // generate its planbody
        // another subgoal to craft the bridge
        workingPlan6.getPlanBody().add(craftBed);
        // add this plan 6 to the top-level goal
        workingGoal.getPlans().add(workingPlan6);

        return workingGoal;
    }


    /**
     * @return a top-level goal to make an axe
     */
    private GoalNode genAxeGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Axe");
        // make the goal-condition
        Literal goalCond = produceLiteral(AXE, true);
        workingGoal.getGoalConds().add(goalCond);

        // several plans to achieve the goal
        // plan 1: if the agent does not have stick and iron
        PlanNode workingPlan = new PlanNode(MK+"Axe1");
        // generate precondition
        Literal nstick = produceLiteral(STICK, false);
        Literal niron = produceLiteral(IRON, false);
        workingPlan.getPre().add(nstick);
        workingPlan.getPre().add(niron);
        // generate its planbody
        // a subgoal to make a stick
        GoalNode getStick = genStickGoal();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getStick);
        // a subgoal to move to the iron position to collect the iron
        GoalNode getIron = genGetIron();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getIron);
        // another subgoal to craft the axe
        GoalNode craftAxe = genCraftAxe();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(craftAxe);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        if(!settings.gpt_order){
            // plan 2: if the agent does not have stick and iron, but we collect the iron first
            PlanNode workingPlan2 = new PlanNode(MK+"Axe2");
            // generate precondition
            workingPlan2.getPre().add(nstick);
            workingPlan2.getPre().add(niron);
            // generate its planbody
            // a subgoal to move to the iron position to collect the iron
            workingPlan2.getPlanBody().add(getIron);
            // a subgoal to make a stick
            workingPlan2.getPlanBody().add(getStick);
            // another subgoal to craft the axe
            workingPlan2.getPlanBody().add(craftAxe);
            // add this plan 2 to the top-level goal
            workingGoal.getPlans().add(workingPlan2);

            // plan 3: if the agent does not have stick and iron, but we collect wood, then iron, and then craft stick
            PlanNode workingPlan3 = new PlanNode(MK+"Axe3");
            // generate precondition
            workingPlan3.getPre().add(nstick);
            workingPlan3.getPre().add(niron);
            // generate its planbody
            // a subgoal to move to the tree position and collect the wood
            GoalNode getWood = genGetWood();
            workingPlan3.getPlanBody().add(getWood);
            // a subgoal to collect iron
            workingPlan3.getPlanBody().add(getIron);
            // a subgoal to create a stick
            GoalNode craftStick = genCraftStick();
            workingPlan3.getPlanBody().add(craftStick);
            // another subgoal to craft the axe
            workingPlan3.getPlanBody().add(craftAxe);
            // add this plan 3 to the top-level goal
            workingGoal.getPlans().add(workingPlan3);
        }



        // plan 4: if the agent does not have stick, but has iron
        PlanNode workingPlan4 = new PlanNode(MK+"Axe4");
        // generate precondition
        Literal iron = produceLiteral(IRON, true);
        workingPlan4.getPre().add(nstick);
        workingPlan4.getPre().add(iron);
        // generate its planbody
        // a subgoal to create a stick
        workingPlan4.getPlanBody().add(getStick);
        // another subgoal to craft the axe
        workingPlan4.getPlanBody().add(craftAxe);
        // add this plan 4 to the top-level goal
        workingGoal.getPlans().add(workingPlan4);

        // plan 5: if the agent does not have iron, but has a stick
        PlanNode workingPlan5 = new PlanNode(MK+"Axe5");
        // generate precondition
        Literal stick = produceLiteral(STICK, true);
        workingPlan5.getPre().add(stick);
        workingPlan5.getPre().add(niron);
        // generate its planbody
        // a subgoal to collect iron
        workingPlan5.getPlanBody().add(getIron);
        // another subgoal to craft the axe
        workingPlan5.getPlanBody().add(craftAxe);
        // add this plan 5 to the top-level goal
        workingGoal.getPlans().add(workingPlan5);

        // plan 6: if the agent does have both stick and iron
        PlanNode workingPlan6 = new PlanNode(MK+"Axe6");
        // generate precondition
        workingPlan6.getPre().add(stick);
        workingPlan6.getPre().add(iron);
        // generate its planbody
        // another subgoal to craft the axe
        workingPlan6.getPlanBody().add(craftAxe);
        // add this plan 6 to the top-level goal
        workingGoal.getPlans().add(workingPlan6);

        return workingGoal;
    }


    /**
     * @return a top-level goal to get a gold
     */
    private GoalNode genGoldGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Gold");
        // make the goal-condition
        Literal goalCond = produceLiteral(GOLD, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have bridge
        PlanNode workingPlan = new PlanNode(MK+"Gold1");
        // generate precondition
        Literal nbridge = produceLiteral(BRIDGE, false);
        workingPlan.getPre().add(nbridge);
        // generate its planbody
        // a subgoal to make a bridge
        GoalNode getBridge = genBridgeGoal();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getBridge);
        // another subgoal to collect the gold
        GoalNode collectGold = genGetGold();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(collectGold);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has a bridge
        PlanNode workingPlan2 = new PlanNode(MK+"Gold2");
        // generate precondition
        Literal gold = produceLiteral(BRIDGE, true); // produceLiteral(GOLD, true);
        workingPlan2.getPre().add(gold);
        // add a single subgoal
        workingPlan2.getPlanBody().add(collectGold);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }


    /**
     * @return a top-level goal to get a gem
     */
    private GoalNode genGemGoal(){
        // make a new goal node
        GoalNode workingGoal = new GoalNode(MK+"Gem");
        // make the goal-condition
        Literal goalCond = produceLiteral(GEM, true);
        workingGoal.getGoalConds().add(goalCond);

        // two plans to achieve the goal
        // plan 1: if the agent does not have axe
        PlanNode workingPlan = new PlanNode(MK+"Gem1");
        // generate precondition
        Literal naxe = produceLiteral(AXE, false);
        workingPlan.getPre().add(naxe);
        // generate its planbody
        // a subgoal to make an axe
        GoalNode getAxe = genAxeGoal();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(getAxe);
        // another subgoal to collect the gem
        GoalNode collectGem = genGetGem();
        // add the subgoal to its parent plan
        workingPlan.getPlanBody().add(collectGem);
        // add this plan 1 to the top-level goal
        workingGoal.getPlans().add(workingPlan);

        // plan 2: if the agent already has a axe
        PlanNode workingPlan2 = new PlanNode(MK+"Gem2");
        // generate precondition
        Literal gem = produceLiteral(AXE, true); // produceLiteral(GEM, true);
        workingPlan2.getPre().add(gem);
        // add a single subgoal
        workingPlan2.getPlanBody().add(collectGem);
        // add plan 2 to the top-level goal
        workingGoal.getPlans().add(workingPlan2);

        return workingGoal;
    }


    /**
     * @return a sub-goal to get wood
     */
    private GoalNode genGetWood(){
        // a subgoal to move to the tree position and collect the wood
        GoalNode getWood = new GoalNode("GetWood");
        // its goal-condition
        Literal goalC = produceLiteral(WOOD, true);
        getWood.getGoalConds().add(goalC);
        // generate the plans to achieve this subgoal
        for(int i = 0; i < tree_xs_trimmed.size(); i++){
            PlanNode treePlan =  new PlanNode("GetWoodAtX"+tree_xs_trimmed.get(i)+"Y"+tree_ys_trimmed.get(i));
            // precondition: the tree is still there
            Literal preTree = produceLiteral("TreeAtX"+tree_xs_trimmed.get(i)+"Y"+tree_ys_trimmed.get(i), true);
            treePlan.getPre().add(preTree);
            // generate its plan body, a subgoal to move to the tree position
            GoalNode subgoal = genMoveTo(tree_xs_trimmed.get(i), tree_ys_trimmed.get(i));
            // another action to collect the wood
            ActionNode action = new ActionNode("CollectWoodAt"+tree_xs_trimmed.get(i)+"Y"+tree_ys_trimmed.get(i));
            // its precondition
            Literal preActX = produceLiteral(ATX+tree_xs_trimmed.get(i), true);
            Literal preActY = produceLiteral(ATY+tree_ys_trimmed.get(i), true);
            action.getPreC().add(preTree);
            action.getPreC().add(preActX);
            action.getPreC().add(preActY);
            // its postcondition
            Literal postAct1 = produceLiteral(WOOD, true);
            Literal postAct2 = produceLiteral("TreeAtX"+tree_xs_trimmed.get(i)+"Y"+tree_ys_trimmed.get(i), false);
            action.getPostC().add(postAct1);
            action.getPostC().add(postAct2);
            // add the subgoal and action to the plan
            treePlan.getPlanBody().add(subgoal);
            treePlan.getPlanBody().add(action);
            // add this plan to the subogal of getWood
            getWood.getPlans().add(treePlan);
        }
        return getWood;
    }


    /**
     * @return a sub-goal to get grass
     */
    private GoalNode genGetGrass(){
        // a subgoal to move to the grass position and collect the grass
        GoalNode getGrass = new GoalNode("GetGrass");
        // its goal-condition
        Literal goalC = produceLiteral(GRASS, true);
        getGrass.getGoalConds().add(goalC);
        // generate the plans to achieve this subgoal
        for(int i = 0; i < grassx_trimmed.size(); i++){
            PlanNode grassPlan =  new PlanNode("GetGrassAtX"+grassx_trimmed.get(i)+"Y"+grassy_trimmed.get(i));
            // precondition: the grass is still there
            Literal preGrass = produceLiteral("GrassAtX"+grassx_trimmed.get(i)+"Y"+grassy_trimmed.get(i), true);
            grassPlan.getPre().add(preGrass);
            // generate its plan body, a subgoal to move to the grass position
            GoalNode subgoal = genMoveTo(grassx_trimmed.get(i), grassy_trimmed.get(i));
            // another action to collect the grass
            ActionNode action = new ActionNode("CollectGrassAt"+grassx_trimmed.get(i)+"Y"+grassy_trimmed.get(i));
            // its precondition
            Literal preActX = produceLiteral(ATX+grassx_trimmed.get(i), true);
            Literal preActY = produceLiteral(ATY+grassy_trimmed.get(i), true);
            action.getPreC().add(preGrass);
            action.getPreC().add(preActX);
            action.getPreC().add(preActY);
            // its postcondition
            Literal postAct1 = produceLiteral(GRASS, true);
            Literal postAct2 = produceLiteral("GrassAtX"+grassx_trimmed.get(i)+"Y"+grassy_trimmed.get(i), false);
            action.getPostC().add(postAct1);
            action.getPostC().add(postAct2);
            // add the subgoal and action to the plan
            grassPlan.getPlanBody().add(subgoal);
            grassPlan.getPlanBody().add(action);
            // add this plan to the subogal of getGrass
            getGrass.getPlans().add(grassPlan);
        }
        return getGrass;
    }


    /**
     * @return a sub-goal to get gold
     */
    private GoalNode genGetGold(){
        // a subgoal to move to the gold position and collect the gold
        GoalNode getGrass = new GoalNode("GetGold");
        // its goal-condition
        Literal goalC = produceLiteral(GOLD, true);
        getGrass.getGoalConds().add(goalC);

        // generate the plans to achieve this subgoal
        for(int i = 0; i < this.settings.goldx.size(); i++){
            PlanNode goldPlan =  new PlanNode("GetGoldAtX"+settings.goldx.get(i)+"Y"+settings.goldy.get(i));
            // precondition: the gold is still there
            Literal preGold = produceLiteral("GoldAtX"+settings.goldx.get(i)+"Y"+settings.goldy.get(i), true);
            Literal bridge = produceLiteral(BRIDGE, true);
            goldPlan.getPre().add(preGold);
            goldPlan.getPre().add(bridge);
            // generate its plan body, a subgoal to move to the gold position
            GoalNode subgoal = genMoveTo(settings.goldx.get(i), settings.goldy.get(i));
            // another action to collect the gold
            ActionNode action = new ActionNode("CollectGoldAt"+settings.goldx.get(i)+"Y"+settings.goldy.get(i));
            // its precondition
            Literal preActX = produceLiteral(ATX+settings.goldx.get(i), true);
            Literal preActY = produceLiteral(ATY+settings.goldy.get(i), true);
            action.getPreC().add(preGold);
            action.getPreC().add(bridge);
            action.getPreC().add(preActX);
            action.getPreC().add(preActY);
            // its postcondition

            Literal postAct = produceLiteral("GoldAtX"+settings.goldx.get(i)+"Y"+settings.goldy.get(i), false);
            action.getPostC().add(goalC);
            action.getPostC().add(postAct);
            // add the subgoal and action to the plan
            goldPlan.getPlanBody().add(subgoal);
            goldPlan.getPlanBody().add(action);
            // add this plan to the subogal of getGrass
            getGrass.getPlans().add(goldPlan);
        }
        return getGrass;
    }

    /**
     * @return a sub-goal to get gem
     */
    private GoalNode genGetGem(){
        // a subgoal to move to the gold position and collect the gem
        GoalNode getGrass = new GoalNode("GetGem");
        // its goal-condition
        Literal goalC = produceLiteral(GEM, true);
        getGrass.getGoalConds().add(goalC);

        // generate the plans to achieve this subgoal
        for(int i = 0; i < this.settings.gemx.size(); i++){
            PlanNode gemPlan =  new PlanNode("GetGemAtX"+settings.gemx.get(i)+"Y"+settings.gemy.get(i));
            // precondition: the gold is still there
            Literal preGem = produceLiteral("GemAtX"+settings.gemx.get(i)+"Y"+settings.gemy.get(i), true);
            Literal axe = produceLiteral(AXE, true);
            gemPlan.getPre().add(preGem);
            gemPlan.getPre().add(axe);
            // generate its plan body, a subgoal to move to the gem position
            GoalNode subgoal = genMoveTo(settings.gemx.get(i), settings.gemy.get(i));
            // another action to collect the gem
            ActionNode action = new ActionNode("CollectGemAt"+settings.gemx.get(i)+"Y"+settings.gemy.get(i));
            // its precondition
            Literal preActX = produceLiteral(ATX+settings.gemx.get(i), true);
            Literal preActY = produceLiteral(ATY+settings.gemy.get(i), true);
            action.getPreC().add(preGem);
            action.getPreC().add(axe);
            action.getPreC().add(preActX);
            action.getPreC().add(preActY);
            // its postcondition
            Literal postAct = produceLiteral("GemAtX"+settings.gemx.get(i)+"Y"+settings.gemy.get(i), false);
            action.getPostC().add(goalC);
            action.getPostC().add(postAct);
            // add the subgoal and action to the plan
            gemPlan.getPlanBody().add(subgoal);
            gemPlan.getPlanBody().add(action);
            // add this plan to the subogal of getGrass
            getGrass.getPlans().add(gemPlan);
        }
        return getGrass;
    }

    /**
     * @return a sub-goal to get iron
     */
    private GoalNode genGetIron(){
        // a subgoal to move to the iron position and collect the iron
        GoalNode getIron = new GoalNode("GetIron");
        // its goal-condition
        Literal goalC = produceLiteral(IRON, true);
        getIron.getGoalConds().add(goalC);
        // generate the plans to achieve this subgoal
        for(int i = 0; i < this.ironx_trimmed.size(); i++){
            PlanNode ironPlan =  new PlanNode("GetIronAtX"+ironx_trimmed.get(i)+"Y"+irony_trimmed.get(i));
            // precondition: the grass is still there
            Literal preIron = produceLiteral("IronAtX"+ironx_trimmed.get(i)+"Y"+irony_trimmed.get(i), true);
            ironPlan.getPre().add(preIron);
            // generate its plan body, a subgoal to move to the iron position
            GoalNode subgoal = genMoveTo(ironx_trimmed.get(i), irony_trimmed.get(i));
            // another action to collect the iron
            ActionNode action = new ActionNode("CollectIronAt"+ironx_trimmed.get(i)+"Y"+irony_trimmed.get(i));
            // its precondition
            Literal preActX = produceLiteral(ATX+ironx_trimmed.get(i), true);
            Literal preActY = produceLiteral(ATY+irony_trimmed.get(i), true);
            action.getPreC().add(preIron);
            action.getPreC().add(preActX);
            action.getPreC().add(preActY);
            // its postcondition
            Literal postAct1 = produceLiteral(IRON, true);
            Literal postAct2 = produceLiteral("IronAtX"+ironx_trimmed.get(i)+"Y"+irony_trimmed.get(i), false);
            action.getPostC().add(postAct1);
            action.getPostC().add(postAct2);
            // add the subgoal and action to the plan
            ironPlan.getPlanBody().add(subgoal);
            ironPlan.getPlanBody().add(action);
            // add this plan to the subogal of getGrass
            getIron.getPlans().add(ironPlan);
        }
        return getIron;
    }

    /**
     * @return a sub-goal to move to the toolshed to craft a plank
     */
    private GoalNode genCraftPlank(){
        // another subgoal to craft the plank
        GoalNode craftPlank = new GoalNode("CraftPlank");
        // generate its goal condition
        Literal goalCond3 = produceLiteral(PLANK, true);
        craftPlank.getGoalConds().add(goalCond3);
        // generate plans to achieve this goal
        for(int i = 0; i < tsx_trimmed.size(); i++){
            // generate plans for each position
            PlanNode tsPlan = new PlanNode("CraftPlankAtX"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal wood = produceLiteral(WOOD, true);
            tsPlan.getPre().add(wood);
            // generate its plan body
            // subgoal 1: move to the ts
            GoalNode mtts = genMoveTo(tsx_trimmed.get(i), tsy_trimmed.get(i));
            // another action to craft the plank
            ActionNode cplank = new ActionNode("CraftPlankAt"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+tsx_trimmed.get(i), true);
            Literal ypos = produceLiteral(ATY+tsy_trimmed.get(i), true);
            cplank.getPreC().add(xpos);
            cplank.getPreC().add(ypos);
            cplank.getPreC().add(wood);
            // its postcondition
            Literal nwood = produceLiteral(WOOD, false);
            Literal plank = produceLiteral(PLANK, true);
            cplank.getPostC().add(nwood);
            cplank.getPostC().add(plank);
            // add subgoal and action
            tsPlan.getPlanBody().add(mtts);
            tsPlan.getPlanBody().add(cplank);
            // add this plan to the craft goal
            craftPlank.getPlans().add(tsPlan);
        }

        return craftPlank;
    }

    /**
     * @return a sub-goal to move to the workbench to craft a stick
     */
    private GoalNode genCraftStick(){
        // a subgoal to craft a stick
        GoalNode craftStick = new GoalNode("CraftStick");
        // generate its goal condition
        Literal goalCond = produceLiteral(STICK, true);
        craftStick.getGoalConds().add(goalCond);
        // generate plans to achieve this goal
        for(int i = 0; i < settings.wbx.size(); i++){
            // generate plans for each position
            PlanNode wbPlan = new PlanNode("CraftStickAtX"+settings.wbx.get(i)+"Y"+settings.wby.get(i));
            // its precondition
            Literal wood = produceLiteral(WOOD, true);
            wbPlan.getPre().add(wood);
            // generate its plan body
            // subgoal 1: move to the wb
            GoalNode mtwb = genMoveTo(settings.wbx.get(i), settings.wby.get(i));
            // another action to craft a stick
            ActionNode cstick = new ActionNode("CraftStickAt"+settings.wbx.get(i)+"Y"+settings.wby.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+settings.wbx.get(i), true);
            Literal ypos = produceLiteral(ATY+settings.wby.get(i), true);
            cstick.getPreC().add(xpos);
            cstick.getPreC().add(ypos);
            cstick.getPreC().add(wood);
            // its postcondition
            Literal nwood = produceLiteral(WOOD, false);
            Literal stick = produceLiteral(STICK, true);
            cstick.getPostC().add(nwood);
            cstick.getPostC().add(stick);
            // add subgoal and action
            wbPlan.getPlanBody().add(mtwb);
            wbPlan.getPlanBody().add(cstick);
            // add this plan to the craft goal
            craftStick.getPlans().add(wbPlan);
        }
        return craftStick;
    }

    /**
     * @return a sub-goal to move to the factory to craft a cloth
     */
    private GoalNode genCraftCloth(){
        // a subgoal to craft a cloth
        GoalNode craftCloth = new GoalNode("CraftCloth");
        // generate its goal condition
        Literal goalCond = produceLiteral(CLOTH, true);
        craftCloth.getGoalConds().add(goalCond);

        // generate plans to achieve this goal
        for(int i = 0; i < settings.facx.size(); i++){
            // generate plans for each position
            PlanNode facPlan = new PlanNode("CraftClothAtX"+settings.facx.get(i)+"Y"+settings.facy.get(i));
            // its precondition
            Literal grass = produceLiteral(GRASS, true);
            facPlan.getPre().add(grass);
            // generate its plan body
            // subgoal 1: move to the factory
            GoalNode mtfac = genMoveTo(settings.facx.get(i), settings.facy.get(i));
            // another action to craft a cloth
            ActionNode ccloth = new ActionNode("CraftClothAt"+settings.facx.get(i)+"Y"+settings.facy.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+settings.facx.get(i), true);
            Literal ypos = produceLiteral(ATY+settings.facy.get(i), true);
            ccloth.getPreC().add(xpos);
            ccloth.getPreC().add(ypos);
            ccloth.getPreC().add(grass);
            // its postcondition
            Literal ngrass = produceLiteral(GRASS, false);
            Literal cloth = produceLiteral(CLOTH, true);
            ccloth.getPostC().add(ngrass);
            ccloth.getPostC().add(cloth);
            // add subgoal and action
            facPlan.getPlanBody().add(mtfac);
            facPlan.getPlanBody().add(ccloth);
            // add this plan to the craft goal
            craftCloth.getPlans().add(facPlan);
        }
        return craftCloth;
    }


    /**
     * @return a sub-goal to move to the workbench to craft a rope
     */
    private GoalNode genCraftRope(){
        // a subgoal to craft a rope
        GoalNode craftRope = new GoalNode("CraftRope");
        // generate its goal condition
        Literal goalCond = produceLiteral(ROPE, true);
        craftRope.getGoalConds().add(goalCond);

        // generate plans to achieve this goal
        for(int i = 0; i < tsx_trimmed.size(); i++){
            // generate plans for each position
            PlanNode tsPlan = new PlanNode("CraftRopeAtX"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal grass = produceLiteral(GRASS, true);
            tsPlan.getPre().add(grass);
            // generate its plan body
            // subgoal 1: move to the toolshed
            GoalNode mtts = genMoveTo(tsx_trimmed.get(i), tsy_trimmed.get(i));
            // another action to craft a rope
            ActionNode crope = new ActionNode("CraftRopeAt"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+tsx_trimmed.get(i), true);
            Literal ypos = produceLiteral(ATY+tsy_trimmed.get(i), true);
            crope.getPreC().add(xpos);
            crope.getPreC().add(ypos);
            crope.getPreC().add(grass);
            // its postcondition
            Literal ngrass = produceLiteral(GRASS, false);
            Literal rope = produceLiteral(ROPE, true);
            crope.getPostC().add(ngrass);
            crope.getPostC().add(rope);
            // add subgoal and action
            tsPlan.getPlanBody().add(mtts);
            tsPlan.getPlanBody().add(crope);
            // add this plan to the craft goal
            craftRope.getPlans().add(tsPlan);
        }
        return craftRope;
    }


    /**
     * @return a sub-goal to move to the factory to craft a bridge
     */
    private GoalNode genCraftBridge(){
        // a subgoal to craft a bridge
        GoalNode craftBridge = new GoalNode("CraftBridge");
        // generate its goal condition
        Literal goalCond = produceLiteral(BRIDGE, true);
        craftBridge.getGoalConds().add(goalCond);

        // generate plans to achieve this goal
        for(int i = 0; i < settings.facx.size(); i++){
            // generate plans for each position
            PlanNode facPlan = new PlanNode("CraftBridgeAtX"+settings.facx.get(i)+"Y"+settings.facy.get(i));
            // its precondition
            Literal wood = produceLiteral(WOOD, true);
            Literal iron = produceLiteral(IRON, true);
            facPlan.getPre().add(wood);
            facPlan.getPre().add(iron);

            // generate its plan body
            // subgoal 1: move to the factory
            GoalNode mtfac = genMoveTo(settings.facx.get(i), settings.facy.get(i));
            // another action to craft a bridge
            ActionNode cbridge = new ActionNode("CraftBridgeAt"+settings.facx.get(i)+"Y"+settings.facy.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+settings.facx.get(i), true);
            Literal ypos = produceLiteral(ATY+settings.facy.get(i), true);
            cbridge.getPreC().add(xpos);
            cbridge.getPreC().add(ypos);
            cbridge.getPreC().add(wood);
            cbridge.getPreC().add(iron);
            // its postcondition
            Literal nwood = produceLiteral(WOOD, false);
            Literal niron = produceLiteral(IRON, false);
            Literal bridge = produceLiteral(BRIDGE, true);
            cbridge.getPostC().add(nwood);
            cbridge.getPostC().add(niron);
            cbridge.getPostC().add(bridge);
            // add subgoal and action
            facPlan.getPlanBody().add(mtfac);
            facPlan.getPlanBody().add(cbridge);
            // add this plan to the craft goal
            craftBridge.getPlans().add(facPlan);
        }
        return craftBridge;
    }


    /**
     * @return a sub-goal to move to the workbench to craft a bed
     */
    private GoalNode genCraftBed(){
        // a subgoal to craft a bed
        GoalNode craftBed = new GoalNode("CraftBed");
        // generate its goal condition
        Literal goalCond = produceLiteral(BED, true);
        craftBed.getGoalConds().add(goalCond);

        // generate plans to achieve this goal
        for(int i = 0; i < settings.wbx.size(); i++){
            // generate plans for each position
            PlanNode wbPlan = new PlanNode("CraftBedAtX"+settings.wbx.get(i)+"Y"+settings.wby.get(i));
            // its precondition
            Literal plank = produceLiteral(PLANK, true);
            Literal grass = produceLiteral(GRASS, true);
            wbPlan.getPre().add(plank);
            wbPlan.getPre().add(grass);

            // generate its plan body
            // subgoal 1: move to the workbench
            GoalNode mtwb = genMoveTo(settings.wbx.get(i), settings.wby.get(i));
            // another action to craft a bed
            ActionNode cbed = new ActionNode("CraftBedAt"+settings.wbx.get(i)+"Y"+settings.wby.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+settings.wbx.get(i), true);
            Literal ypos = produceLiteral(ATY+settings.wby.get(i), true);
            cbed.getPreC().add(xpos);
            cbed.getPreC().add(ypos);
            cbed.getPreC().add(plank);
            cbed.getPreC().add(grass);
            // its postcondition
            Literal nplank = produceLiteral(PLANK, false);
            Literal ngrass = produceLiteral(GRASS, false);
            Literal bed = produceLiteral(BED, true);
            cbed.getPostC().add(nplank);
            cbed.getPostC().add(ngrass);
            cbed.getPostC().add(bed);
            // add subgoal and action
            wbPlan.getPlanBody().add(mtwb);
            wbPlan.getPlanBody().add(cbed);
            // add this plan to the craft goal
            craftBed.getPlans().add(wbPlan);
        }
        return craftBed;
    }

    /**
     * @return a sub-goal to move to the toolshed to craft an axe
     */
    private GoalNode genCraftAxe(){
        // a subgoal to craft a axe
        GoalNode craftAxe = new GoalNode("CraftAxe");
        // generate its goal condition
        Literal goalCond = produceLiteral(AXE, true);
        craftAxe.getGoalConds().add(goalCond);

        // generate plans to achieve this goal
        for(int i = 0; i < tsx_trimmed.size(); i++){
            // generate plans for each position
            PlanNode tsPlan = new PlanNode("CraftAxeAtX"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal stick = produceLiteral(STICK, true);
            Literal iron = produceLiteral(IRON, true);
            tsPlan.getPre().add(stick);
            tsPlan.getPre().add(iron);

            // generate its plan body
            // subgoal 1: move to the toolshed
            GoalNode mtts = genMoveTo(tsx_trimmed.get(i), tsy_trimmed.get(i));
            // another action to craft an axe
            ActionNode caxe = new ActionNode("CraftAxeAt"+tsx_trimmed.get(i)+"Y"+tsy_trimmed.get(i));
            // its precondition
            Literal xpos = produceLiteral(ATX+tsx_trimmed.get(i), true);
            Literal ypos = produceLiteral(ATY+tsy_trimmed.get(i), true);
            caxe.getPreC().add(xpos);
            caxe.getPreC().add(ypos);
            caxe.getPreC().add(stick);
            caxe.getPreC().add(iron);
            // its postcondition
            Literal nstick = produceLiteral(STICK, false);
            Literal niron = produceLiteral(IRON, false);
            Literal axe = produceLiteral(AXE, true);
            caxe.getPostC().add(nstick);
            caxe.getPostC().add(niron);
            caxe.getPostC().add(axe);
            // add subgoal and action
            tsPlan.getPlanBody().add(mtts);
            tsPlan.getPlanBody().add(caxe);
            // add this plan to the craft goal
            craftAxe.getPlans().add(tsPlan);
        }
        return craftAxe;
    }




    /**
     * @param des the y-axis value of the destination
     * @return
     */
    private GoalNode genMoveX(int des, int y) {

        // make a new goalnode
        GoalNode workingGoal = new GoalNode(MT+X+des);
        // goal condition
        Literal goalCond = produceLiteral(ATX + des, true);
        workingGoal.getGoalConds().add(goalCond);
        // create plans
        for(int i = 0; i < settings.grid_size; i++){
            int distance = des-i;
            // make a plan node
            PlanNode workingplan = new PlanNode(MT+X+des+AT+i);
            // add precondition
            Literal preCond = produceLiteral(ATX+i,true);
            workingplan.getPre().add(preCond);
            // if it is the latter subgoal
            if(y >= 0){
                workingplan.getPre().add( produceLiteral(ATY+y, true));
            }

            // if it is already at the destination
            if(distance == 0){
                // create a dummy action
                ActionNode workingAction = new ActionNode(DUMMY);
                workingplan.getPlanBody().add(workingAction);
            }
            // move up
            else if(distance > 0){
                int m = i;
                // create continuous actions
                while (distance > 0){
                    ActionNode workingAction = new ActionNode(RIGHT);
                    // precondition
                    Literal actPre = produceLiteral(ATX+m, true);
                    // postcondition
                    Literal actPost1 = produceLiteral(ATX+m, false);
                    Literal actPost2 = produceLiteral(ATX+(m+1), true);
                    // construct the action
                    workingAction.getPreC().add(actPre);
                    // if it is the latter subgoal
                    if(y >= 0){
                        workingAction.getPreC().add( produceLiteral(ATY+y, true));
                    }
                    workingAction.getPostC().add(actPost1);
                    workingAction.getPostC().add(actPost2);
                    workingplan.getPlanBody().add(workingAction);
                    m++;
                    distance = des - m;
                }
            }
            // move down
            else {
                int m = i;
                while (distance < 0){
                    ActionNode workingAction = new ActionNode(LEFT);
                    Literal actPre = produceLiteral(ATX+m, true);
                    // need the opponent's location
                    Literal actPost1 = produceLiteral(ATX+m, false);
                    Literal actPost2 = produceLiteral(ATX+(m-1), true);
                    // construct the action
                    workingAction.getPreC().add(actPre);
                    // if it is the latter subgoal
                    if(y >= 0){
                        workingAction.getPreC().add( produceLiteral(ATY+y, true));
                    }
                    workingAction.getPostC().add(actPost1);
                    workingAction.getPostC().add(actPost2);
                    workingplan.getPlanBody().add(workingAction);
                    m--;
                    distance = des - m;
                }
            }
            // add plans
            workingGoal.getPlans().add(workingplan);
        }
        return workingGoal;
    }

    /**
     * @param des the x-axis value of the destination
     * @return
     */
    private GoalNode genMoveY(int des, int x) {
        // make a new goalnode
        GoalNode workingGoal = new GoalNode(MT+Y+des);

        // goal condition
        Literal goalCond = produceLiteral(ATY + des, true);
        workingGoal.getGoalConds().add(goalCond);
        // create plans
        for(int i = 0; i < settings.grid_size; i++){
            int distance = des-i;
            // make a plan node
            PlanNode workingplan = new PlanNode(MT+Y+des+AT+i);
            // add precondition
            Literal preCond = produceLiteral(ATY+i,true);
            workingplan.getPre().add(preCond);
            // if it is the latter subgoal
            if(x >= 0){
                workingplan.getPre().add( produceLiteral(ATX+x, true));
            }
            // if it is already at the destination
            if(distance == 0){
                // create a dummy action
                ActionNode workingAction = new ActionNode(DUMMY);
                workingplan.getPlanBody().add(workingAction);
            }
            // move left
            else if(distance > 0){
                int m = i;
                // create continuous actions
                while (distance > 0){
                    ActionNode workingAction = new ActionNode(UP);
                    // precondition
                    Literal actPre = produceLiteral(ATY+m, true);
                    // postcondition
                    Literal actPost1 = produceLiteral(ATY+m, false);
                    Literal actPost2 = produceLiteral(ATY+(m+1), true);
                    // construct the action
                    workingAction.getPreC().add(actPre);
                    // if it is the latter subgoal
                    if(x >= 0){
                        workingAction.getPreC().add( produceLiteral(ATX+x, true));
                    }
                    workingAction.getPostC().add(actPost1);
                    workingAction.getPostC().add(actPost2);
                    workingplan.getPlanBody().add(workingAction);
                    m++;
                    distance = des - m;
                }
            }
            // move right
            else {
                int m = i;
                while (distance < 0){
                    ActionNode workingAction = new ActionNode(DOWN);
                    Literal actPre = produceLiteral(ATY+m, true);
                    // need the opponent's location
                    Literal actPost1 = produceLiteral(ATY+m, false);
                    Literal actPost2 = produceLiteral(ATY+(m-1), true);
                    // construct the action
                    workingAction.getPreC().add(actPre);
                    // if it is the latter subgoal
                    if(x >= 0){
                        workingAction.getPreC().add( produceLiteral(ATX+x, true));
                    }
                    workingAction.getPostC().add(actPost1);
                    workingAction.getPostC().add(actPost2);
                    workingplan.getPlanBody().add(workingAction);
                    m--;
                    distance = des - m;
                }
            }
            // add plans
            workingGoal.getPlans().add(workingplan);
        }
        return workingGoal;
    }


    private GoalNode genMoveTo(int x_value, int y_value) {
        // make a subgoal
        GoalNode workingGoal = new GoalNode(MT + "(" + x_value + "," + y_value + ")");
        // generate goal-condition
        Literal goalCond = produceLiteral(ATX+x_value, true);
        Literal goalCond2 = produceLiteral(ATX+y_value, true);
        workingGoal.getGoalConds().add(goalCond);
        workingGoal.getGoalConds().add(goalCond2);
        // a move to subgoal has 5 possible plans

        // plan 1, if the agent is already at the destination
        PlanNode workingPlan = new PlanNode(MT + "(" + x_value + "," + y_value + ")"+ AT+"(" + x_value + "," + y_value + ")");
        // generate its precondition
        Literal preCond = produceLiteral(ATX+x_value, true);
        Literal preCond2 = produceLiteral(ATY+y_value, true);
        workingPlan.getPre().add(preCond);
        workingPlan.getPre().add(preCond2);
        // generate dummy action
        ActionNode action = new ActionNode(DUMMY);
        workingPlan.getPlanBody().add(action);
        workingGoal.getPlans().add(workingPlan);

        // plan 2, if x = x_value but y != y_value
        PlanNode workingPlan2 = new PlanNode(MT + "(" + x_value + "," + y_value + ")"+ ATX + x_value );
        preCond2 = produceLiteral(ATY+y_value, false);
        workingPlan2.getPre().add(preCond);
        workingPlan2.getPre().add(preCond2);
        // generate a subgoal to move to y axis with value y_value
        GoalNode subGoal = genMoveY(y_value, x_value);
        workingPlan2.getPlanBody().add(subGoal);
        workingGoal.getPlans().add(workingPlan2);

        // plan 3, if x != x_value but y = y_value
        PlanNode workingPlan3 = new PlanNode(MT + "(" + x_value + "," + y_value + ")"+ ATY + y_value );
        preCond = produceLiteral(ATX+x_value, false);
        preCond2 = produceLiteral(ATY+y_value, true);
        workingPlan3.getPre().add(preCond);
        workingPlan3.getPre().add(preCond2);
        // generate a subgoal to move to y axis with value y_value
        GoalNode subGoal2 = genMoveX(x_value, y_value);
        workingPlan3.getPlanBody().add(subGoal2);
        workingGoal.getPlans().add(workingPlan3);

        // plan 4, if x != x_value and y != y_value
        PlanNode workingPlan4 = new PlanNode(MT + "(" + x_value + "," + y_value + ")not"+Y+X);
        preCond2 = produceLiteral(ATY+y_value, false);
        workingPlan4.getPre().add(preCond);
        workingPlan4.getPre().add(preCond2);
        // generate a subgoal to move to y axis with value y_value
        workingPlan4.getPlanBody().add(genMoveY(y_value, -1));
        workingPlan4.getPlanBody().add(subGoal2);
        workingGoal.getPlans().add(workingPlan4);

        // plan 5, if x != x_value and y != y_value
        PlanNode workingPlan5 = new PlanNode(MT + "(" + x_value + "," + y_value + ")not"+X+Y);
        workingPlan5.getPre().add(preCond);
        workingPlan5.getPre().add(preCond2);
        // generate a subgoal to move to y axis with value y_value
        workingPlan5.getPlanBody().add(genMoveX(x_value,-1));
        workingPlan5.getPlanBody().add(subGoal);
        workingGoal.getPlans().add(workingPlan5);

        return workingGoal; //workingGoal;
    }


}
