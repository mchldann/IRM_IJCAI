package officeworld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import util.Enums.Action;

public class OfficeWorldPosition {

    private static final Map<String, Integer[]> cell_key_to_ints_map;
    private static final Map<String, Boolean[]> room_neighbours;
    
    static {
    	Map<String, Integer[]> tmpMap = new HashMap<String, Integer[]>();
    	tmpMap.put("SW", new Integer[] {0, 0});
    	tmpMap.put("S", new Integer[] {1, 0});
    	tmpMap.put("SE", new Integer[] {2, 0});
    	
    	tmpMap.put("W", new Integer[] {0, 1});
    	tmpMap.put("M", new Integer[] {1, 1});
    	tmpMap.put("E", new Integer[] {2, 1});
    	
    	tmpMap.put("NW", new Integer[] {0, 2});
    	tmpMap.put("N", new Integer[] {1, 2});
    	tmpMap.put("NE", new Integer[] {2, 2});
    	
    	cell_key_to_ints_map = Collections.unmodifiableMap(tmpMap);
    	
    	Map<String, Boolean[]> tmpMap2 = new HashMap<String, Boolean[]>(); // [north, south, east, west]
    	tmpMap2.put("0-0", new Boolean[] {true, false, true, false});
    	tmpMap2.put("1-0", new Boolean[] {false, false, true, true});
    	tmpMap2.put("2-0", new Boolean[] {false, false, true, true});
    	tmpMap2.put("3-0", new Boolean[] {true, false, false, true});
    	
    	tmpMap2.put("0-1", new Boolean[] {true, true, false, false});
    	tmpMap2.put("1-1", new Boolean[] {true, false, false, false});
    	tmpMap2.put("2-1", new Boolean[] {true, false, false, false});
    	tmpMap2.put("3-1", new Boolean[] {true, true, false, false});
    	
    	tmpMap2.put("0-2", new Boolean[] {false, true, true, false});
    	tmpMap2.put("1-2", new Boolean[] {false, true, true, true});
    	tmpMap2.put("2-2", new Boolean[] {false, true, true, true});
    	tmpMap2.put("3-2", new Boolean[] {false, true, false, true});
    	
    	room_neighbours = Collections.unmodifiableMap(tmpMap2);
    }
    
	public int room_x;
	public int room_y;
	public int cell_x;
	public int cell_y;
	
	public OfficeWorldPosition(int room_x, int room_y, int cell_x, int cell_y)
	{
		this.room_x = room_x;
		this.room_y = room_y;
		this.cell_x = cell_x;
		this.cell_y = cell_y;
	}
    
	public OfficeWorldPosition(OfficeWorldPosition toClone)
	{
		this.room_x = toClone.room_x;
		this.room_y = toClone.room_y;
		this.cell_x = toClone.cell_x;
		this.cell_y = toClone.cell_y;
	}
	
	public int[] getWorldCoordinates()
	{
		return new int[] {this.room_x * 3 + this.cell_x, this.room_y * 3 + this.cell_y};
	}
	
	@Override
    public boolean equals(Object o)
	{
        if (o == this) {
            return true;
        }
 
        if (!(o instanceof OfficeWorldPosition)) {
            return false;
        }
         
        OfficeWorldPosition owp = (OfficeWorldPosition)o;
         
        return (owp.room_x == this.room_x) && (owp.room_y == this.room_y) && (owp.cell_x == this.cell_x) && (owp.cell_y == this.cell_y);
    }
	
	public String toString() { 
	    return "room (" + this.room_x + ", " + this.room_y + "), cell (" + this.cell_x + ", " + this.cell_y + ")";
	}
	
	public String roomID() {
		return this.room_x + "-" + this.room_y;
	}
	
	public String cellID()
	{
		if (this.cell_x == 0 && this.cell_y == 0)
		{
			return "SW";
		}
		if (this.cell_x == 0 && this.cell_y == 1)
		{
			return "W";
		}
		if (this.cell_x == 0 && this.cell_y == 2)
		{
			return "NW";
		}
		if (this.cell_x == 1 && this.cell_y == 0)
		{
			return "S";
		}
		if (this.cell_x == 1 && this.cell_y == 1)
		{
			return "M";
		}
		if (this.cell_x == 1 && this.cell_y == 2)
		{
			return "N";
		}
		if (this.cell_x == 2 && this.cell_y == 0)
		{
			return "SE";
		}
		if (this.cell_x == 2 && this.cell_y == 1)
		{
			return "E";
		}
		if (this.cell_x == 2 && this.cell_y == 2)
		{
			return "NE";
		}
		else
		{
			return null;
		}
	}
	
	public OfficeWorldPosition[] get_neighbours()
	{
		OfficeWorldPosition[] result = new OfficeWorldPosition[] {null, null, null, null};
		
		Boolean[] neighbours_connected = room_neighbours.get(roomID());
		
		// If this room isn't in the 'room_neighbours' map then it's not a valid room.
		if (neighbours_connected == null)
		{
			return result;
		}
		
		for (int x_offset = -1; x_offset <= 1; x_offset++)
		{
			for (int y_offset = -1; y_offset <= 1; y_offset++)
			{
				if (x_offset == 0 && y_offset == 0)
				{
					continue;
				}
				
				// Only allow movements along cardinal Actions (no diagonals)
				if (x_offset != 0 && y_offset != 0)
				{
					continue;
				}
				
				int dir;
				if (x_offset == -1)
				{
					dir = Action.WEST.id;
				}
				else if (x_offset == 1)
				{
					dir = Action.EAST.id;
				}
				else if (y_offset == -1)
				{
					dir = Action.SOUTH.id;
				}
				else if (y_offset == 1)
				{
					dir = Action.NORTH.id;
				}
				else
				{
					throw new Error("Invalid Action generated.");
				}
				
				if ((cell_x == 0) && (dir == Action.WEST.id))
				{
					if ((cell_y != 1) || !neighbours_connected[Action.WEST.id])
					{
						continue;
					}
				}
				
				if ((cell_x == 2) && (dir == Action.EAST.id))
				{
					if ((cell_y != 1) || !neighbours_connected[Action.EAST.id])
					{
						continue;
					}
				}
				
				if ((cell_y == 0) && (dir == Action.SOUTH.id))
				{
					if ((cell_x != 1) || !neighbours_connected[Action.SOUTH.id])
					{
						continue;
					}
				}
				
				if ((cell_y == 2) && (dir == Action.NORTH.id))
				{
					if ((cell_x != 1) || !neighbours_connected[Action.NORTH.id])
					{
						continue;
					}
				}
				
				int new_room_x = room_x;
				int new_room_y = room_y;
				int new_cell_x = cell_x + x_offset;
				int new_cell_y = cell_y + y_offset;
				
				if (new_cell_x < 0)
				{
					new_cell_x += 3;
					new_room_x -= 1;
				}
				
				if (new_cell_x > 2)
				{
					new_cell_x -= 3;
					new_room_x += 1;
				}
				
				if (new_cell_y < 0)
				{
					new_cell_y += 3;
					new_room_y -= 1;
				}
				
				if (new_cell_y > 2)
				{
					new_cell_y -= 3;
					new_room_y += 1;
				}
				
				result[dir] = new OfficeWorldPosition(new_room_x, new_room_y, new_cell_x, new_cell_y);
			}
		}
		
		return result;
	}

	public static Integer[] cell_key_to_ints(String cell_key)
	{
		return cell_key_to_ints_map.get(cell_key);
	}
	
	public String get_room_key(String entity_tag)
	{
		return entity_tag + "AtRoom" + this.room_x + "-" + this.room_y;
	}
}
