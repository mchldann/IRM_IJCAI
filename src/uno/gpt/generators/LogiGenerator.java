package uno.gpt.generators;

import uno.gpt.structure.ActionNode;
import uno.gpt.structure.GoalNode;
import uno.gpt.structure.Literal;
import uno.gpt.structure.PlanNode;

import java.util.*;

public class LogiGenerator extends AbstractGenerator {
    /** Default values */
    static final int def_stops = 10;
    static final int def_cargo_space = 2;
    static final double def_init_shortcut_factor = 0.1d;

    /** String Bank */
    private static final String AT = "At";
    private static final String AT_STOP = AT + "Stop";
    private static final String TRANSPORT_AT_STOP = "Transport" + AT_STOP;
    private static final String PARCEL = "Parcel";
    private static final String LOADED = "Loaded";
    private static final String LOAD = "Load";
    private static final String UNLOAD = "Unload";
    private static final String TRANSPORT_PARCEL = "TransportParcel";
    private static final String SHORTCUTFROM = "ShortcutFrom";
    private static final String TO = "To";
    private static final String PARCELS_LOADED = PARCEL + "s" + LOADED;


    /** id of the tree */
    private int id;

    /** random generators */
    final private Random rm;

    /** number of stops */
    final private int num_stops;

    /** number of parcels */
    final private int num_parcels;

    /** number of parcels that can be carried */
    final private int cargo_space;

    /** shortcuts */
    private List<Integer> shortcuts;

    /** shortcut factor */
    final private double shortcut_factor;


    /**
     * Constructor
     * @param seed The random seed to be used
     * @param stops The number of stops on the loop
     * @param parcels the number of parcels to produce
     */
    LogiGenerator(int seed, int stops, int parcels, int cargo_space, double shortcut_factor){
        this.rm = new Random(seed);
        this.num_stops = stops;
        this.num_parcels = parcels;
        this.cargo_space = cargo_space;
        this.shortcut_factor = shortcut_factor;
    }

    /**
     * Build the environment, create all the predicates needed and logically assign starting positions for the agent and parcels
     * @return The environment HashMap
     */
    @Override
    public HashMap<String, Literal> genEnvironment() {
        environment = new HashMap<>();
        shortcuts = new ArrayList<>();



        int jump;
        Literal workingLiteral;
        // Make the shortcuts
        for (int i = 0; i < num_stops; i++) {
            jump = rm.nextInt(num_stops - 4) + 2;
            shortcuts.add(i, (i+jump)%num_stops);
            workingLiteral = new Literal(SHORTCUTFROM + i + TO + shortcuts.get(i),
                rm.nextDouble() < shortcut_factor, true, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }

        // Make the stop variables for the transport
        for (int i = 0; i < num_stops; i++) {
            workingLiteral = new Literal(TRANSPORT_AT_STOP + i, false, false, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        // Set the initial stop of the transport
        environment.get(TRANSPORT_AT_STOP + rm.nextInt(num_stops)).flip();

        // Make the parcel variables for being loaded
        for (int i = 0; i < num_parcels; i++) {
            workingLiteral = new Literal(PARCEL + i + LOADED, false,false,false);
            environment.put(workingLiteral.getId(),workingLiteral);
        }

        // Make the parcel variables for being at a stop
        // Iterate over parcels
        for (int i = 0; i < num_parcels; i++) {
            // Iterate over stops
            for (int j = 0; j < num_stops; j++) {
                workingLiteral = new Literal(PARCEL + i + AT_STOP + j, false,false,false);
                environment.put(workingLiteral.getId(), workingLiteral);
            }
            // Set the initial floor that the parcel is on
            environment.get(PARCEL + i + AT_STOP + rm.nextInt(num_stops)).flip();
        }

        // Create the cargo
        for (int i = 0; i <= cargo_space; i++) {
            workingLiteral = new Literal(i + PARCELS_LOADED, false, false, false);
            environment.put(workingLiteral.getId(), workingLiteral);
        }
        environment.get(0 + PARCELS_LOADED).flip();

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
        // Make goal node
        GoalNode topLevelGoal = new GoalNode(TRANSPORT_PARCEL + index);
        // Set index, i.e. parcel context
        this.id = index;
        // Find where the parcel is
        int pickUp = findParcel(index);
        // Decide where the parcel wants to go
        int dropOff;
        // Roll the dice until it behaves
        do {
            dropOff = rm.nextInt(num_stops);
        } while (pickUp == dropOff);
        // Add goal condition
        topLevelGoal.getGoalConds().add(produceLiteral(PARCEL + index + AT_STOP + dropOff, true));
        // *Lucio Ult*
        decomposeTopLevelGoal(topLevelGoal, pickUp, dropOff);
        // Return the goalNode
        return topLevelGoal;
    }

    /**
     * The worker function for creating the deterministic plans
     * @param topLevelGoal The goal node to build from
     * @param origin The Stop the parcel starts on
     * @param destination The Floor the parcel desires to be on
     */
    private void decomposeTopLevelGoal(GoalNode topLevelGoal, int origin, int destination) {
        // Make the monoplan
        PlanNode workingPlan = new PlanNode(
                "PlanTo" + TRANSPORT_PARCEL + id + "From" + origin + "To" + destination);
        // Add appropriate precondition
        workingPlan.getPre().add(produceLiteral(PARCEL + id + AT_STOP + origin, true));
        // Goal for going to the starting floor
        workingPlan.getPlanBody().add(genGoTo(origin));
        // Goal for embarking the passenger
        workingPlan.getPlanBody().add(genLoad(origin));
        // Goal for going to the finishing floor
        workingPlan.getPlanBody().add(genGoTo(destination));
        // Goal for disembarking the passenger
        workingPlan.getPlanBody().add(genUnload(destination));
        // Add the plan to the topLevelGoal
        topLevelGoal.getPlans().add(workingPlan);
    }

    private GoalNode genUnload(int stop) {
        // Make a new goalNode
        GoalNode workingGoal = new GoalNode(UNLOAD + id + AT + stop);
        Literal goalCond = produceLiteral(PARCEL + id + AT_STOP + stop, true);
        Literal loadCond = produceLiteral(PARCEL + id + LOADED, true);
        Literal stopCond = produceLiteral(TRANSPORT_AT_STOP+ stop, true);
        workingGoal.getGoalConds().add(goalCond);
        for (int i = 1; i <= cargo_space; i++) {
            // Make a new planNode
            PlanNode workingPlan = new PlanNode(UNLOAD + "(" + id + "-" + stop + ")");
            // Make a new actionNode
            ActionNode workingAction = new ActionNode(UNLOAD + PARCEL + id + AT_STOP + stop);

            // Add the loaded and stop precondition literals
            workingPlan.getPre().add(loadCond);
            workingAction.getPreC().add(loadCond);
            workingPlan.getPre().add(stopCond);
            workingAction.getPreC().add(stopCond);
            // Get the cargo precondition literal
            Literal carCond = produceLiteral(i + PARCELS_LOADED, true);
            // Add to the plan and action
            workingPlan.getPre().add(carCond);
            workingAction.getPreC().add(carCond);

            // Add the relevant post condition literal
            workingAction.getPostC().add(produceLiteral(PARCEL + id + LOADED, false));
            // Do the same for the cargo
            workingAction.getPostC().add(produceLiteral(i + PARCELS_LOADED, false));
            // Make true the next cargo state
            workingAction.getPostC().add(produceLiteral((i - 1) + PARCELS_LOADED, true));

            // Add the goal condition
            workingAction.getPostC().add(goalCond);

            // Add the action to the plan
            workingPlan.getPlanBody().add(workingAction);
            // Add the plan to the goal
            workingGoal.getPlans().add(workingPlan);
        }
        // Return the goalNode
        return workingGoal;
    }

    private GoalNode genLoad(int stop) {
        // Make a new goalNode
        GoalNode workingGoal = new GoalNode(LOAD + id + AT + stop);
        Literal goalCond = produceLiteral(PARCEL + id + LOADED, true);
        Literal posiCond = produceLiteral(PARCEL + id + AT_STOP + stop, true);
        Literal stopCond = produceLiteral(TRANSPORT_AT_STOP+ stop, true);
        workingGoal.getGoalConds().add(goalCond);
        for (int i = 0; i < cargo_space; i++) {
            // Make a new planNode
            PlanNode workingPlan = new PlanNode(LOAD + i + "(" + id + "-" + stop + ")");
            // Make a new actionNode
            ActionNode workingAction = new ActionNode(LOAD + i + PARCEL + id + AT_STOP + stop);

            // Add the position and stop preconditions to the plan and action
            workingPlan.getPre().add(posiCond);
            workingAction.getPreC().add(posiCond);
            workingPlan.getPre().add(stopCond);
            workingAction.getPreC().add(stopCond);
            // Get the cargo precondition literal
            Literal carCond = produceLiteral(i + PARCELS_LOADED, true);
            // Add to the plan and action
            workingPlan.getPre().add(carCond);
            workingAction.getPreC().add(carCond);


            // Set to be false in the post condition
            workingAction.getPostC().add(produceLiteral(PARCEL + id + AT_STOP + stop, false));
            // Do the same for the cargo
            workingAction.getPostC().add(produceLiteral(i + PARCELS_LOADED, false));
            // Make true the next cargo state
            workingAction.getPostC().add(produceLiteral((i + 1) + PARCELS_LOADED, true));


            // Add the goal condition
            workingAction.getPostC().add(goalCond);

            // Add the action to the plan
            workingPlan.getPlanBody().add(workingAction);
            // Add the plan to the goal
            workingGoal.getPlans().add(workingPlan);
        }
        // Return the goalNode
        return workingGoal;
    }

    private GoalNode genGoTo(int destination) {
        // Make a new node
        GoalNode workingGoal = new GoalNode("GoTo" + destination);
        // Create the required move plans
        for (int i = 0; i < num_stops; i++) {
            if (i != destination){
                workingGoal.getPlans().addAll(genRoutes(i, destination));
            }
        }
        // Add the relevant goal condition
        workingGoal.getGoalConds().add(produceLiteral(TRANSPORT_AT_STOP + destination, true));
        // Return the goal
        return workingGoal;
    }

    private List<PlanNode> genRoutes(int origin, int destination){
        // Make a new node
        List<PlanNode> result = new ArrayList<>();

        // Set up the lists and origin
        List<List<Integer>> routes = new ArrayList<>();
        routes.add(new ArrayList<>());
        routes.get(0).add(origin);

        // Get max iters
        int iterationLimit = Math.min(Math.abs(destination - origin), num_stops - Math.abs(destination - origin));

        for (int i = 0; i < iterationLimit; i++) {
            expandRoutes(routes, destination);
            reduceRoutes(routes, destination);
        }

        // Remove those that don't reach the destination
        validateRoutes(routes, destination);

        for (int i = 0; i < routes.size() ; i++) {
            result.add(genMoves(i, routes.get(i)));
        }
        return result;
    }

    private void expandRoutes(List<List<Integer>> routes, int destination){
        List<Integer> route, clockwise, anticlockwise;
        int tail, nextstep, initalRoutesCount = routes.size();
        for (int i = 0; i < initalRoutesCount; i++) {
            route = routes.get(i);
            tail = route.get(route.size()-1);
            if(tail == destination){
                continue;
            }
            // Add the clockwise
            clockwise = new ArrayList<>(route);
            nextstep = (tail + 1)% num_stops;
            if (!clockwise.contains(nextstep)) {
                clockwise.add(nextstep);
                routes.add(clockwise);
            }
            // The anticlockwise
            anticlockwise = new ArrayList<>(route);
            nextstep = Math.floorMod(tail - 1, num_stops);
            if (!anticlockwise.contains(nextstep)) {
                anticlockwise.add(nextstep);
                routes.add(anticlockwise);
            }
            // The shortcut
            nextstep = shortcuts.get(tail);
            if (!route.contains(nextstep)){
                route.add(nextstep);
            }
        }

    }

    private void reduceRoutes(List<List<Integer>> routes, int destination){
        int lengthM = 0;
        List<Integer> route;
        // Prune duplicates
        for (int i = 0; i < routes.size(); i++) {
            route = routes.get(i);
            lengthM = Math.max(lengthM, route.size());
            for (int j = i+1; j < routes.size(); j++) {
                if(route.equals(routes.get(j))){
                    routes.remove(j);
                    j--;
                }
            }
        }
        // Prune dead ends
        for (int i = 0; i < routes.size(); i++) {
            route = routes.get(i);
            if (route.size() < lengthM && route.get(route.size()-1) != destination){
                routes.remove(route);
                i--;
            }
        }
    }

    private void validateRoutes(List<List<Integer>> routes, int destination){
        for (int i = 0; i < routes.size(); i++) {
            List<Integer> route = routes.get(i);
            if (route.get(route.size()-1) != destination){
                routes.remove(route);
                i--;
            }
        }
    }


    private PlanNode genMoves(int identifier, List<Integer> route){
        // Get the origin and dest
        int origin = route.get(0), dest = route.get(route.size() - 1);
        // Make a new node
        PlanNode workingPlan = new PlanNode("Route" + identifier +
                "("+ origin + "-" + dest + ")");
        workingPlan.getPre().add(produceLiteral(TRANSPORT_AT_STOP + origin, true));
        int from, to;
        Set<Literal> preconditions;
        for (int i = 1; i < route.size(); i++) {
            from = route.get(i-1);
            to = route.get(i);
            preconditions = new HashSet<>();
            if (Math.abs(from - to) != 1 && Math.abs(from-to) != num_stops - 1){
                preconditions.add(produceLiteral(SHORTCUTFROM + from + TO + to, true));
            }
            workingPlan.getPlanBody().add(genMove(from, to, preconditions));
            for (Literal condition:preconditions) {
                if (!workingPlan.getPre().contains(condition)){
                    workingPlan.getPre().add(condition);
                }
            }
        }
        // Return the plan
        return workingPlan;
    }

    private ActionNode genMove(int origin, int destination, Set<Literal> conditions) {
        // Create an action
        ActionNode workingAction = new ActionNode("MoveFrom" + origin + "To" + destination);
        // Add the appropriate precondition
        workingAction.getPreC().add(produceLiteral(TRANSPORT_AT_STOP + origin, true));
        // Add the prescribed conditions
        workingAction.getPreC().addAll(conditions);

        // Add the positive post condition
        workingAction.getPostC().add(produceLiteral(TRANSPORT_AT_STOP + destination, true));
        // Add the negative post condition
        workingAction.getPostC().add(produceLiteral(TRANSPORT_AT_STOP + origin, false));

        return workingAction;
    }

    private int findParcel(int id){
        for (int i = 0; i < num_stops; i++) {
            if (environment.get(PARCEL + id + AT_STOP + i).getState()){
                return i;
            }
        }
        return -1;
    }
}
