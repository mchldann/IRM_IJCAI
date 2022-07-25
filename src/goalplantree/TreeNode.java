package goalplantree;

public abstract class TreeNode {

    /**
     * definition of the state
     */
    public enum Status
    {
        // enumerate status
        SUCCESS("success"),
        FAILURE("failure"),
        ACTIVE("active"),
        DEFAULT("default");

        private String name;

        private Status(String name){
            this.name = name;
        }

        public boolean isDone(){
            return this == SUCCESS || this == FAILURE;
        }

        @Override
        public String toString(){
            return this.name;
        }
    };

    /**
     * the parent node
     */
    protected TreeNode parent;

    /**
     * the unique identifier
     */
    protected String id;

    /**
     * the type name
     */
    protected String type;

    /**
     * the execution status
     */
    protected Status status;
    /**
     * the next step of this node
     */
    protected TreeNode next;

    /**
     * indentation
     */
    final String indent = "    ";


    public TreeNode(String id, String type){
        // name
        this.id = id;
        this.type = type;
        init();
    }

    /**
     * initialisation
     */
    private void init(){
        // initially the execution status is set to be default
        this.status = Status.DEFAULT;
    }

    /**
     * @return the name of this node
     */
    public String getId(){
        return this.id;
    }

    /**
     * @return the type name of this goal/plan/action
     */
    public String getType(){
        return this.type;
    }

    /**
     * @return the parent of this node
     */
    public TreeNode getParent(){
        return this.parent;
    }

    public void setParent(TreeNode node){
        this.parent = node;
    }

    /**
     * @return the current status
     */
    public Status getStatus(){
        return this.status;
    }

    /**
     *
     */
    public void setStatus(Status state){
        this.status = state;
    }


    /**
     * @return the next step in the gpt
     */
    public TreeNode getNext(){
        return this.next;
    }

    /**
     * set the next goal/action
     * @param node
     */
    public void setNext(TreeNode node){
        this.next = node;
    }

//    public abstract TreeNode Fail();

    public abstract String onPrintNode(int num);
    
    /**
     * @return the next step of an action in the given intention
     */
    public TreeNode nextIstep()
    {
        // if it is not the last step in a plan
        if(next != null) {
            return next;
        }
        // if it is
        else{
            // if it is the top-level goal
            if(parent == null) {
                return null;   
            } else {
                // get the goal it is going to achieve
                GoalNode gn = (GoalNode)parent.getParent();
                return gn.nextIstep();
            }
        }
    }
}
