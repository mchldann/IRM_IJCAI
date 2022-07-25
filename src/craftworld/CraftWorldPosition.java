package craftworld;

import main.Main;

public class CraftWorldPosition {

	public int cell_x;
	public int cell_y;
	
	public CraftWorldPosition(int cell_x, int cell_y)
	{
		this.cell_x = cell_x;
		this.cell_y = cell_y;
	}
    
	public CraftWorldPosition(CraftWorldPosition toClone)
	{
		this.cell_x = toClone.cell_x;
		this.cell_y = toClone.cell_y;
	}
	
	@Override
    public boolean equals(Object o)
	{
        if (o == this) {
            return true;
        }
 
        if (!(o instanceof CraftWorldPosition)) {
            return false;
        }
         
        CraftWorldPosition cwp = (CraftWorldPosition)o;
         
        return (cwp.cell_x == this.cell_x) && (cwp.cell_y == this.cell_y);
    }
	
	public String toString() { 
	    return "cell (" + this.cell_x + ", " + this.cell_y + ")";
	}
	
	public CraftWorldPosition[] get_neighbours()
	{
		CraftWorldPosition[] result = new CraftWorldPosition[] {null, null, null, null};
		
		for (int action = 0; action < 4; action++)
		{
			int xOffset = 0;
			int yOffset = 0;
			
			if (action == 0) // North
			{
				xOffset = 0;
				yOffset = 1;
			}
			else if (action == 1) // South
			{
				xOffset = 0;
				yOffset = -1;
			}
			else if (action == 2) // East
			{
				xOffset = 1;
				yOffset = 0;
			}
			else if (action == 3) // West
			{
				xOffset = -1;
				yOffset = 0;
			}
			
			CraftWorldPosition neighbourPos = new CraftWorldPosition(cell_x + xOffset, cell_y + yOffset);
			
			if (neighbourPos.cell_x < 0 || neighbourPos.cell_y < 0 || neighbourPos.cell_x >= Main.cw_settings.grid_size || neighbourPos.cell_y >= Main.cw_settings.grid_size)
			{
				continue;
			}
			
			result[action] = neighbourPos;
		}
		
		return result;
	}
}
