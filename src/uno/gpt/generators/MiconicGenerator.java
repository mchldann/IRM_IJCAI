package uno.gpt.generators;

import uno.gpt.structure.*;

import java.util.HashMap;
import java.util.Random;

public class MiconicGenerator extends AbstractGenerator{
    /** Default values */
    static final int def_floors = 10;

    /** String Bank */
    private static final String ELEVATOR_ON_FLOOR = "ElevatorOnFloor";
    private static final String PASSENGER = "Passenger";
    private static final String IN_ELEVATOR = "InElevator";
    private static final String ON_FLOOR = "OnFloor";
    private static final String ON = "On";
    private static final String DISEMBARK = "Disembark";
    private static final String EMBARK = "Embark";


    /** id of the tree */
    private int id;

    /** random generators */
    final private Random rm;

    /** number of floors */
    final private int num_floor;

    /** number of pass */
    final private int num_pass;


    /** Constructor */
    MiconicGenerator (int seed, int num_floor, int num_pass){
        this.rm = new Random(seed);
        this.num_floor = num_floor;
        this.num_pass = num_pass;
    }


    /**
     * Build the environment, create all the predicates needed and logically assign starting positions for the elevator and passengers
     * @return The environment HashMap
     */
    @Override
    public HashMap<String, Literal> genEnvironment() {
        environment = new HashMap<>();
        Literal workingLiteral;
        // Make the floor variables for the elevator
        for (int i = 0; i < num_floor; i++) {
            workingLiteral = new Literal(ELEVATOR_ON_FLOOR + i, false, false, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // Set the initial floor of the elevator
        environment.get(ELEVATOR_ON_FLOOR + rm.nextInt(num_floor)).flip();

        // Make the passenger variables for being in the elevator
        for (int i = 0; i < num_pass; i++) {
            workingLiteral = new Literal(PASSENGER + i + IN_ELEVATOR, false,false,false);
            environment.put(workingLiteral.getId(),workingLiteral);
        }

        // Make the passenger variables for being on a floor
        // Iterate over passengers
        for (int i = 0; i < num_pass; i++) {
            // Iterate over floors
            for (int j = 0; j < num_floor; j++) {
                workingLiteral = new Literal(PASSENGER + i + ON_FLOOR + j, false,false,false);
                environment.put(workingLiteral.getId(), workingLiteral);
            }
            // Set the initial floor that the passenger is on
            environment.get("Passenger" + i + "OnFloor" + rm.nextInt(num_floor)).flip();
        }
        // Return the environment
        return environment;
    }

    /**
     * Function for creating one top level goal, which is reference to one passenger to be moved
     * @param index The index of the goal and of the passenger to be moved
     * @return The goalNode made
     */
    @Override
    public GoalNode genTopLevelGoal(int index) {
        // Make goal node
        GoalNode topLevelGoal = new GoalNode("MovePassenger" + index);
        // Set index, i.e. passenger context
        this.id = index;
        // Find where the passenger is
        int startingFloor = findPassenger(index);
        // Decide where the passenger wants to go
        int finishingFloor;
        // Roll the dice until it behaves
        do {
            finishingFloor = rm.nextInt(num_floor);
        } while (startingFloor == finishingFloor);
        // Add goal condition
        topLevelGoal.getGoalConds().add(produceLiteral(PASSENGER + index + ON_FLOOR + finishingFloor, true));
        // *Lucio Ult*
        decomposeTopLevelGoal(topLevelGoal, startingFloor, finishingFloor);
        // Return the goalNode
        return topLevelGoal;
    }

    /**
     * The worker function for creating the deterministic plans
     * @param topLevelGoal The goal node to build from
     * @param startingFloor The Floor the passenger starts on
     * @param finishingFloor The Floor the passenger desires to be on
     */
    private void decomposeTopLevelGoal(GoalNode topLevelGoal, int startingFloor, int finishingFloor) {
        // Make the monoplan
        PlanNode workingPlan = new PlanNode(
                "PlanToMovePassenger" + id + "FromFloor" + startingFloor + "ToFloor" + finishingFloor);
        // Add appropriate precondition
        workingPlan.getPre().add(produceLiteral(PASSENGER + id + ON_FLOOR + startingFloor, true));
        // Goal for going to the starting floor
        workingPlan.getPlanBody().add(genGoTo(startingFloor));
        // Goal for embarking the passenger
        workingPlan.getPlanBody().add(genEmbark(startingFloor));
        // Goal for going to the finishing floor
        workingPlan.getPlanBody().add(genGoTo(finishingFloor));
        // Goal for disembarking the passenger
        workingPlan.getPlanBody().add(genDisembark(finishingFloor));
        // Add the plan to the topLevelGoal
        topLevelGoal.getPlans().add(workingPlan);
    }

    private GoalNode genDisembark(int floor) {
        // Make a new goalNode
        GoalNode workingGoal = new GoalNode(DISEMBARK + id + ON + floor);

        // Make a new planNode
        PlanNode workingPlan = new PlanNode(DISEMBARK + "(" + id + "-" + floor + ")");
        // Make a new actionNode
        ActionNode workingAction = new ActionNode(DISEMBARK + PASSENGER + id + ON_FLOOR + floor);

        // Get the relevant precondition literal
        Literal preCond = produceLiteral(PASSENGER + id + IN_ELEVATOR,true);
        // Add to the plan and action
        workingPlan.getPre().add(preCond);
        workingAction.getPreC().add(preCond);
        // Get the relevant precondition literal
        preCond = produceLiteral(ELEVATOR_ON_FLOOR + floor,true);
        // Add to the plan and action
        workingPlan.getPre().add(preCond);
        workingAction.getPreC().add(preCond);

        // Add the relevant post condition literal
        workingAction.getPostC().add(produceLiteral(PASSENGER + id + IN_ELEVATOR,false));

        // Get the relevant goal condition
        Literal goalCond = produceLiteral(PASSENGER + id + ON_FLOOR + floor, true);
        // Add to the action and goal
        workingAction.getPostC().add(goalCond);
        workingGoal.getGoalConds().add(goalCond);

        // Add the action to the plan
        workingPlan.getPlanBody().add(workingAction);
        // Add the plan to the goal
        workingGoal.getPlans().add(workingPlan);
        // Return the goalNode
        return workingGoal;
    }

    private GoalNode genEmbark (int floor) {
        // Make a new goalNode
        GoalNode workingGoal = new GoalNode(EMBARK + id + ON + floor);

        // Make a new planNode
        PlanNode workingPlan = new PlanNode(EMBARK + "(" + id + "-" + floor + ")");
        // Make a new actionNode
        ActionNode workingAction = new ActionNode(EMBARK + PASSENGER + id + ON_FLOOR + floor);

        // Get the relevant precondition literal
        Literal preCond = produceLiteral(PASSENGER + id + ON_FLOOR + floor, true);
        // Add to the plan and action
        workingPlan.getPre().add(preCond);
        workingAction.getPreC().add(preCond);
        // Get the relevant precondition literal
        preCond = produceLiteral(ELEVATOR_ON_FLOOR + floor, true);
        // Add to the plan and action
        workingPlan.getPre().add(preCond);
        workingAction.getPreC().add(preCond);


        // Set to be false in the post condition
        Literal postCond = preCond.clone();
        // Flip it
        postCond.flip();
        // Add to the action post conditions
        workingAction.getPostC().add(postCond);

        // Get the relevant goal condition amd copy it
        Literal goalCond = produceLiteral(PASSENGER + id + IN_ELEVATOR,true);
        // Add to the action and goal
        workingAction.getPostC().add(goalCond);
        workingGoal.getGoalConds().add(goalCond);

        // Add the action to the plan
        workingPlan.getPlanBody().add(workingAction);
        // Add the plan to the goal
        workingGoal.getPlans().add(workingPlan);
        // Return the goalNode
        return workingGoal;
    }

    private GoalNode genGoTo(int floor) {
        // Make a new node
        GoalNode workingGoal = new GoalNode("GoTo" + floor);
        // Create the required move plans
        for (int i = 0; i < num_floor; i++) {
            if (i != floor){
                workingGoal.getPlans().add(genMove(i, floor));
            }
        }
        // Add the relevant goal condition
        workingGoal.getGoalConds().add(produceLiteral(ELEVATOR_ON_FLOOR + floor, true));
        // Return the goal
        return workingGoal;
    }

    private PlanNode genMove(int startFloor, int finishFloor) {
        // Make a new node
        PlanNode workingPlan = new PlanNode("Move(" + startFloor + "-" + finishFloor + ")");

        // Add the appropriate precondition
        workingPlan.getPre().add(produceLiteral(ELEVATOR_ON_FLOOR + startFloor, true));

        // Create an action
        ActionNode workingAction = new ActionNode("MoveFrom" + startFloor + "To" + finishFloor);
        // Add the appropriate precondition
        workingAction.getPreC().addAll(workingPlan.getPre());

        // Add the positive post condition
        workingAction.getPostC().add(produceLiteral(ELEVATOR_ON_FLOOR + finishFloor, true));
        // Add the negative post condition
        workingAction.getPostC().add(produceLiteral(ELEVATOR_ON_FLOOR + startFloor, false));

        // Add the action to the plan
        workingPlan.getPlanBody().add(workingAction);
        // Return the plan
        return workingPlan;
    }

    private int findPassenger(int id){
        for (int i = 0; i < num_floor; i++) {
            if (environment.get(PASSENGER + id + ON_FLOOR + i).getState()){
                return i;
            }
        }
        return -1;
    }
}
