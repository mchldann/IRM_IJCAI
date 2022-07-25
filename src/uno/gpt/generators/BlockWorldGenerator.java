package uno.gpt.generators;

import uno.gpt.structure.ActionNode;
import uno.gpt.structure.GoalNode;
import uno.gpt.structure.Literal;
import uno.gpt.structure.PlanNode;

import java.util.*;
import java.util.stream.Collectors;

public class BlockWorldGenerator extends AbstractGenerator{
    /** Default values */
    static final int def_num_block = 6, def_height = 4;

    /** String Bank */
    private static final String ON = "On";
    private static final String CLEAR = "Clear";
    private static final String TABLE = "Table";
    private static final String TOWER = "Tower";
    private static final String BUILD = "Build";
    private static final String PLACE = "Place";
    private static final String INSTANCE = "Instance";
    private static final String MOVE = "Move";
    private static final String DELIM = "-";
    private static final String FREE = "Free";
    private static final String FROM = "From";
    private static final int TABLE_INT = -1;


    /** random generators */
    final private Random rm;

    /** number of blocks */
    final private int num_block;


    /** height of towers */
    final private int height;


    /**
     * Constructor
     * @param seed The random seed to use
     * @param num_block The number of Blocks in the world
     * @param height The height of each Tower in Block
     */
    BlockWorldGenerator(int seed, int num_block, int height){
        this.rm = new Random(seed);
        this.num_block = num_block;
        this.height = height;
    }


    /**
     * Creates a new environment
     * This environment assumes all blocks start spread out on the table
     * @return The Environment
     */
    @Override
    public HashMap<String, Literal> genEnvironment() {
        // Create the new environment
        environment = new HashMap<>();
        // Make a Literal variable to be used
        Literal workingLiteral;
        // Then produce the relevant predicates for each block
        for (int i = 0; i < num_block; i++) {
            // Make the ClearX predicate and make it true
            workingLiteral = new Literal(CLEAR + i, true, false, false);
            // Add it to the environment
            environment.put(workingLiteral.getId(), workingLiteral);
            // Make the OnXTable predicate and make it true
            workingLiteral = new Literal(ON + i + DELIM + TABLE, true, false, false);
            // Add it to the environment
            environment.put(workingLiteral.getId(), workingLiteral);
            // Create the OnXY predicates
            for (int j = 0; j < num_block; j++) {
                // Check it isn't this block
                if (i != j){
                    // Create an OnXY predicate
                    workingLiteral = new Literal(ON + i + DELIM + j, false, false, false );
                    // Add it to the environment
                    environment.put(workingLiteral.getId(), workingLiteral);
                }
            }
        }
        return environment;
    }

    /**
     * Create the top level goal
     * @param index The index for which goal is being worked on
     * @return The goalNode to be returned
     */
    @Override
    public GoalNode genTopLevelGoal(int index) {
        // Find the stack you want to make
        List<Integer> perm = new ArrayList<>();
        // Fill it with all blocks
        for (int i = 0; i < num_block; i++) {
            // Add the ints
            perm.add(i);
        }
        // Shuffle it
        Collections.shuffle(perm, rm);
        // Make a new list as a snipped section of the old one
        List<Integer> tower = new ArrayList<>(perm.subList(0, height));

        // Build recursively
        return buildTower(tower);
    }

    /**
     * The recursive build function for towers
     * @param tower The int list representation of the tower
     * @return The appropriate goalNode
     */
    private GoalNode buildTower(List<Integer> tower) {
        // Create goalNode
        GoalNode workingGoal = new GoalNode(TOWER + makeStackString(tower));
        // Create the goalSet from the tower
        workingGoal.getGoalConds().addAll(goalSet(tower));
        // Create the plan for recursion
        PlanNode workingPlan = new PlanNode(BUILD + TOWER + makeStackString(tower));


        // Check if recursion or the base case
        if (tower.size() > 1){
            // Recursive pattern
            workingPlan.getPlanBody().add(buildTower(tower.subList(1,tower.size())));
            workingPlan.getPlanBody().add(goalMoveBlock(tower.get(0), tower.get(1)));
        } else {
            // Base Case
            workingPlan.getPlanBody().add(goalMoveBlock(tower.get(0), TABLE_INT));
        }

        // Add the plan to the goal
        workingGoal.getPlans().add(workingPlan);
        // Return the goal
        return workingGoal;
    }

    /**
     * The catch all for making a move Goal
     * @param block The block to move
     * @param target The target block or TABLE_INT
     * @return The populate goal for the desired move
     */
    private GoalNode goalMoveBlock(int block, int target){
        // Credit to appropriate string for the target
        String sTarget = (target == TABLE_INT) ? TABLE : Integer.toString(target);
        // Create the goal
        GoalNode workingGoal = new GoalNode(PLACE + ON + block + DELIM + sTarget);
        // Add the goalCond
        workingGoal.getGoalConds().add(produceLiteral(ON + block + DELIM + sTarget, true));
        // Check if the target needs clearing
        if (target != TABLE_INT){
            // Create two plans for instancing
            PlanNode freeBlockTargetPlan = new PlanNode(
                    CLEAR + block + DELIM + sTarget + MOVE + block + DELIM + sTarget);
            PlanNode freeTargetBlockPlan = new PlanNode(
                    CLEAR + sTarget + DELIM + block + MOVE + block + DELIM + sTarget);

            // Create the two goal nodes for freeing
            GoalNode freeBlock = freeBlock(block, blockSet(block));
            GoalNode freeTarget = freeBlock(target, blockSet(target));

            // Add in correct orders
            freeBlockTargetPlan.getPlanBody().add(freeBlock);
            freeBlockTargetPlan.getPlanBody().add(freeTarget);

            freeTargetBlockPlan.getPlanBody().add(freeTarget);
            freeTargetBlockPlan.getPlanBody().add(freeBlock);

            // Create the appropriate move goal
            GoalNode instanceMoveGoal = instanceMoveBlock(block, target);

            // Add to both plans
            freeBlockTargetPlan.getPlanBody().add(instanceMoveGoal);
            freeTargetBlockPlan.getPlanBody().add(instanceMoveGoal);

            // Add both plans to the goal
            workingGoal.getPlans().add(freeBlockTargetPlan);
            workingGoal.getPlans().add(freeTargetBlockPlan);
        } else {
            // Create the plan for instancing
            PlanNode freeBlockPlan = new PlanNode(
                    CLEAR + block + MOVE + block + DELIM + sTarget);

            // Create the goal node for freeing
            freeBlockPlan.getPlanBody().add(freeBlock(block, blockSet(block)));


            // Add to the plan
            freeBlockPlan.getPlanBody().add(instanceMoveBlock(block, target));

            // Add the plan to the goal
            workingGoal.getPlans().add(freeBlockPlan);
        }

        // Return it
        return workingGoal;
    }


    /**
     * Takes a move command and creates all instances within a plan.
     * @param block The block to be moved
     * @param target The target block or TABLE_INT for the table
     * @return The populated goal with plans
     */
    private GoalNode instanceMoveBlock(int block, int target){
        // Credit to appropriate string for the target
        String sTarget = (target == TABLE_INT) ? TABLE : Integer.toString(target);
        // Create a goalNode
        GoalNode workingGoal = new GoalNode(ON + block + DELIM + sTarget);

        // If not moving to the table add the table case
        if (target != TABLE_INT){
            // Make the plan
            PlanNode workingPlan = new PlanNode(INSTANCE + MOVE + block + DELIM + TABLE + DELIM + sTarget);
            // Get the action
            ActionNode workingAction = moveBlock(block, TABLE_INT, target);

            // Propagate the pre conditions
            workingPlan.getPre().addAll(workingAction.getPreC());

            // Add the action to the plan
            workingPlan.getPlanBody().add(workingAction);

            // Add the plan to the goal
            workingGoal.getPlans().add(workingPlan);
        }

        for (int from : blockSet(block, target)) {
            // Make the plan
            PlanNode workingPlan = new PlanNode(INSTANCE + MOVE + block + DELIM + from + DELIM + sTarget);
            // Get the action
            ActionNode workingAction = moveBlock(block, from, target);

            // Propagate the pre conditions
            workingPlan.getPre().addAll(workingAction.getPreC());

            // Add the action to the plan
            workingPlan.getPlanBody().add(workingAction);

            // Add the plan to the goal
            workingGoal.getPlans().add(workingPlan);
        }

        // Return it
        return  workingGoal;
    }

    /**
     * Wrapper to make a goal for freeing a given block
     * @param block The block to be freed
     * @param blockingSet The set of blocks that might be on top of the block to free
     * @return The populated goal to achieve this
     */
    private GoalNode freeBlock(int block, Set<Integer> blockingSet){
        // 2 to create correct offset for minimal free trees
        return freeBlock(block, 2, blockingSet);
    }

    /**
     * Creates a goal for freeing a given block
     * @param block The block to be freed
     * @param depth The current recursive depth to control recursion
     * @param blockingSet The set of blocks that might be on top of the block to free
     *                    Key to recursion
     * @return The populated goal to achieve this
     */
    private GoalNode freeBlock(int block, int depth, Set<Integer> blockingSet){
        // Create the goalNode
        GoalNode workingGoal = new GoalNode(CLEAR + block);
        // Add the goalCond
        workingGoal.getGoalConds().add(produceLiteral(CLEAR + block, true));

        // For each potential blocker
        for (int blocker: blockingSet) {
            // Make a new plan
            PlanNode workingPlan = new PlanNode(FREE + block + FROM + blocker);

            // Add the main precondition
            workingPlan.getPre().add(produceLiteral(ON + blocker + DELIM + block, true));

            // Check for recursion
            if (depth < height && blockingSet.size() > 1) {
                // Create the new BlockingSet
                Set<Integer> reducedBlockingSet = new HashSet<>(blockingSet);
                // Remove this block from it
                reducedBlockingSet.remove(blocker);
                // Recurse
                workingPlan.getPlanBody().add(freeBlock(blocker, depth + 1, reducedBlockingSet));
            } else {
                // If you can't recurse this has stricter preconditions
                workingPlan.getPre().add(produceLiteral(CLEAR + blocker, true));
            }

            // Create the freeing action
            workingPlan.getPlanBody().add(moveBlock(blocker, block, TABLE_INT));

            // Add the plan to the goal
            workingGoal.getPlans().add(workingPlan);
        }

        // Return it
        return workingGoal;
    }

    /**
     * Catch all function for moving blocks.
     * @param block the block to be moved
     * @param from The origin, a block or TABLE_INT for table
     * @param to The target, a block or TABLE_INT for table
     * @return The populated action
     */
    private ActionNode moveBlock(int block, int from, int to){
        // Set the string to use
        String sFrom = (from == TABLE_INT) ? TABLE : Integer.toString(from);
        String sTo = (to == TABLE_INT) ? TABLE : Integer.toString(to);

        // Make new action node
        ActionNode workingAction = new ActionNode(MOVE + block + DELIM + sFrom + DELIM + sTo);

        // Add the from clear precondition
        workingAction.getPreC().add(produceLiteral(CLEAR + block, true));
        // Add the to clear precondition, if applicable
        if (to != TABLE_INT){
            workingAction.getPreC().add(produceLiteral(CLEAR + sTo, true));
        }
        // Add the on precondition
        workingAction.getPreC().add(produceLiteral(ON + block + DELIM + sFrom, true));

        // Add the clear positive postcondition, if applicable
        if (from != TABLE_INT){
            workingAction.getPostC().add(produceLiteral(CLEAR + sFrom, true));
        }
        // Add the clear negative postcondition, if applicable
        if (to != TABLE_INT){
            workingAction.getPostC().add(produceLiteral(CLEAR + sTo, false));
        }
        // Add the on positive postcondition
        workingAction.getPostC().add(produceLiteral(ON + block + DELIM + sTo, true));
        // Add the on negative postcondition
        workingAction.getPostC().add(produceLiteral(ON + block + DELIM + sFrom, false));

        // Return
        return workingAction;
    }

    /**
     * Constructs a set of all blocks excluding those listed
     * @param exclude The blocks to be excluded
     * @return The set of blocks
     */
    private Set<Integer> blockSet(int... exclude){
        // Make set
        Set<Integer> workingSet = new HashSet<>();
        // Iterate through the blocks
        for (int i = 0; i < num_block; i++) {
            // Add them to the list
            workingSet.add(i);
        }
        // Remove the excluded
        for (int i : exclude) {
            workingSet.remove(i);
        }
        // Return the set
        return workingSet;
    }

    /**
     * The goalSet build function for towers
     * @param tower The int list representation of the tower
     * @return The appropriate set of Literals to represent the goal
     */
    private List<Literal> goalSet(List<Integer> tower) {
        // Make the set to be returned
        List<Literal> goalList = new ArrayList<>();
        // Declare a literal to work with
        Literal workingLiteral;
        // Iterate through the list
        for (int i = 0; i < tower.size(); i++) {
            // Check if it is the final element
            if (i < tower.size() - 1){
                // Take the element and it's previous and use that to search
                workingLiteral = environment.get(ON + tower.get(i) + DELIM + tower.get(i + 1)).clone();
                // Set to be true
                workingLiteral.setState(true);
                // Add to the final set
                goalList.add(workingLiteral);
            }else{
                // Find the element on the table
                workingLiteral  = environment.get(ON + tower.get(i) + DELIM + TABLE ).clone();
                // Set to be true
                workingLiteral.setState(true);
                // Add to the final set
                goalList.add(workingLiteral);
            }
        }
        // Return the goalList
        return goalList;
    }

    /**
     * Makes a tidy string from the List
     * @param tower The List representing the
     * @return A string representation
     */
    private String makeStackString(List<Integer> tower) {
        return tower.stream().map(Object::toString).collect(Collectors.joining(DELIM));
    }
}
