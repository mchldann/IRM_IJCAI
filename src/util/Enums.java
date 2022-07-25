package util;

public class Enums
{
    public static final int CRAFT_WORLD_NUM_ACTIONS = 5;
    
    public enum Action
    {
    	NORTH(0),
    	SOUTH(1),
    	EAST(2),
    	WEST(3),
    	CRAFT_OR_GATHER_ITEM(4);
    	
        public final int id;

        private Action(int id) {
            this.id = id;
        }
    }

    public static final int CRAFT_WORLD_NUM_ITEMS = 12;
    
    public enum CraftWorldItem
    {
    	WOOD(0, "Wood", -1),
    	GRASS(1, "Grass", -1),
    	IRON(2, "Iron", -1),
    	GOLD(3, "Gold", 7),
    	GEM(4, "Gem", 8),
    	PLANK(5, "Plank", 0),
    	BED(6, "Bed", 5),
    	STICK(7, "Stick", 1),
    	ROPE(8, "Rope", 3),
    	BRIDGE(9, "Bridge", 4),
    	AXE(10, "Axe", 6),
    	CLOTH(11, "Cloth", 2);
    	
        public final int id;
        public final String text_rep;
        public final int objective_num;
        
        private CraftWorldItem(int id, String text_rep, int objective_num) {
            this.id = id;
            this.text_rep = text_rep;
            this.objective_num = objective_num;
        }
    }
}
