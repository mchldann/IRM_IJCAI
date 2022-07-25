package beliefbase;

public class Condition implements Comparable<Condition>, Cloneable{

    /**
     * literal name
     */
    String literal;

    /**
     * the value of this condition
     */
    boolean value;

    public Condition(String l, boolean v){
        this.literal = l;
        this.value = v;
    }

    /**
     * @return the name of this literal
     */
    public String getLiteral(){
        return this.literal;
    }


    /**
     * @return true if this literal is positive; false, otherwise.
     */
    public boolean isPositive(){
        return this.value;
    }


    /**
     * change the value of this condition to its opposite
     * @return the value after flipping
     */
    public boolean flip(){
        this.value = !this.value;
        return this.value;
    }

    /**
     * @param condition
     * @return
     */
    public boolean isOpposite(Condition condition){
        if(condition == null)
            return false;
        if(this.literal.equals(condition.getLiteral()) && (this.value != condition.isPositive())){
            return true;
        }
        return false;
    }

    public boolean isSame(Condition condition){
        if(condition == null)
            return false;
        if(this.literal.equals(condition.getLiteral()) && (this.value == condition.isPositive())){
            return true;
        }
        return false;
    }

    /**
     * check if this condition and the given condition are describing the same thing
     * @param o the given object
     * @return true if two condition are referring to the same literal; false, otherwise.
     */
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;

        return literal.equals(condition.literal);
    }


    @Override
    public int compareTo(Condition condition){
        return this.getLiteral().compareTo(condition.getLiteral());
    }


    /**
     * @return the string representation of this literal
     */
    public String onPrintCondition(){
        return "(" + this.literal + "," + this.value + ")";
    }

    /**
     * @return the clone of this condition
     */
    @Override
    public Condition clone(){
        return new Condition(literal, value);
    }
}