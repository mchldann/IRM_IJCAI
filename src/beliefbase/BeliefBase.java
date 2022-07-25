package beliefbase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author yuanyao
 *
 * closed world assumption.
 *
 * a more general belief can be positive or negative
 * and contains a functor together with 0 or n parameters
 */

public class BeliefBase implements Cloneable, Iterable<Map.Entry<String, Boolean>> {

    /**
     * a belief base is a map of functor value pairs,
     */
    private Map<String, Boolean> bbMap;

    /**
     * initial capacity of this belief base
     */
    private int initialCapacity;

    /**
     * identity of this belief base
     */
    private String id;


    /**
     * initialisation
     * @param identity
     * @param capacity
     */
    public BeliefBase(String identity, int capacity){
        id = identity;
        initialCapacity = capacity;
        init();
    }

    /**
     *
     * @param par input parameter
     */
    public BeliefBase(String par){
        // generate a new environment with a specified id
        id = par;
        init();
    }

    public BeliefBase(int capacity){
        initialCapacity = capacity;
        init();
    }

    /**
     * initialisation
     */
    private void init(){
        // if the initial capacity is not given, then assign it to 32 as default
        if(initialCapacity == 0)
            initialCapacity = 32;
        // create the hash map to maintain agent's belief base
        bbMap = new HashMap<>(initialCapacity);
    }

    /**
     * @return the size of this belief base
     */
    public int getSize(){
        return bbMap.size();
    }

    /**
     * @return the id of this belief base
     */
    public String getId(){
        return id;
    }

    /**
     * add a belief to the belief base or update the value of a literal in the belief base
     * @return true if a belief is added to the belief base; false if the value of an existing belief is updated
     */
    public boolean apply(Condition condition){
        return bbMap.put(condition.getLiteral(), condition.isPositive()) == null;
    }

    /**
     * apply a list of conditions to this belief base
     * @param conditions
     */
    public void apply(Condition[] conditions){
        if(conditions == null)
            return;

        for(int i = 0; i < conditions.length; i++){
            apply(conditions[i]);
        }
    }

    /**
     * evaluate if a specified literal holds or doesn't hold in the current belief base
     * a literal holds if it appears in the belief base and its corresponding value is true.
     * a literal does not hold if it didn't appear in the belief base or its value in the belief base if false.
     * @param condition the given condition;
     * @return true, if the given condition is positive and the corresponding literal holds in the environment, or if
     * the given condition is false and the corresponding literal does not hold; false, otherwise.
     */
    public boolean evaluate(Condition condition){
        // get the value of a specified literal in the belief base(null if it does not exist)
        Boolean value = bbMap.get(condition.getLiteral());
        if(value == null){
            return !condition.isPositive();
        }
        return (value == condition.isPositive()) ? true : false;
    }


    /**
     * evaluate a list of literal
     * @param conditions
     * @return
     */
    public boolean evaluate(Condition[] conditions){
        if(conditions != null){
            for(int i = 0; i < conditions.length; i++){
                if(!this.evaluate(conditions[i])){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * clone
     * @return
     */
    @Override
    public BeliefBase clone(){
        BeliefBase beliefBaseImp = new BeliefBase(id, getSize());
        beliefBaseImp.bbMap = new HashMap<String, Boolean>(this.bbMap);
        return beliefBaseImp;
    }

    /**
     * iterator
     * @return
     */
    @Override
    public Iterator<Map.Entry<String, Boolean>> iterator(){
        return bbMap.entrySet().iterator();
    }

    /**
     * @return the string representation of belief base
     */
    public String onPrintBB() {
        String result = "Belief Base = {";
        for(Map.Entry<String, Boolean> b : this) {
            result += "(" + b.getKey() + "," + b.getValue() + ");";
        }
        result += " }.";
        return result;
    }
}
