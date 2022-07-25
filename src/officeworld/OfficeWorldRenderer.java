package officeworld;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import main.Main;
import scheduler.State;
import util.Log;

public class OfficeWorldRenderer {

	private static int[][] grid;
	private static int width;
	private static int height;
	
	private static int[] ourAgentColour;
	private static int[] otherAgentColour;
	
    static {
	 	grid = new int[][] {
    		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0},
        	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    	};
    	
    	width = grid[0].length;
    	height = grid.length;
    	
    	ourAgentColour = new int[] {0, 0, 255};
    	otherAgentColour = new int[] {255, 0, 0};
    }
    
	public OfficeWorldRenderer()
	{

	}
	
	private void setObjectPixels(OfficeWorldPosition pos, int[] objectColour, int[] centreColour, int[][] r, int[][] g, int[][] b)
	{
    	int start_x = 1 + pos.room_x * 10 + pos.cell_x * 3;
    	int start_y = 29 - pos.room_y * 10 - pos.cell_y * 3;
    	
    	for (int y = start_y - 2; y <= start_y; y++)
    	{
        	for (int x = start_x; x <= start_x + 2; x++)
        	{
        		if (Main.USE_CUSTOM_OFFICEWORLD_LOCATIONS && (y == start_y - 1) && (x == start_x + 1))
        		{
	        		r[y][x] = centreColour[0];
	        		g[y][x] = centreColour[1];
	        		b[y][x] = centreColour[2];
        		}
        		else
        		{
            		r[y][x] = objectColour[0];
            		g[y][x] = objectColour[1];
            		b[y][x] = objectColour[2];
        		}
        	}
    	}
	}
	
	private int[][][] enlargeImage(int[][] r, int[][] g, int[][] b, int enlargementFactor)
	{
		int[][][] result = new int[3][r.length * enlargementFactor][r[0].length * enlargementFactor];
		
		for (int colour = 0; colour < 3; colour++)
		{
			int[][] input;
			if (colour == 0)
			{
				input = r;
			}
			else if (colour == 1)
			{
				input = g;
			}
			else
			{
				input = b;
			}
			
			for (int y = 0; y < r.length * enlargementFactor; y++)
			{
				for (int x = 0; x < r[0].length * enlargementFactor; x++)
				{
					result[colour][y][x] = input[y / enlargementFactor][x / enlargementFactor];
				}
			}
		}
		
		return result;
	}
	
	public void draw(String img_name, State state)
	{
		OfficeWorldState ows = new OfficeWorldState(state);
		
    	int[][] r = deepCopy(grid);
    	int[][] g = deepCopy(grid);
    	int[][] b = deepCopy(grid);
    	
    	// Set pixels for decorations
    	for (OfficeWorldPosition pos : ows.decorationPositions)
    	{
        	int start_x = 1 + pos.room_x * 10 + pos.cell_x * 3;
        	int start_y = 29 - pos.room_y * 10 - pos.cell_y * 3;

    		for (int colour = 0; colour < 3; colour++)
    		{
    			int[][] channel;
    			if (colour == 0)
    			{
    				channel = r;
    			}
    			else if (colour == 1)
    			{
    				channel = g;
    			}
    			else
    			{
    				channel = b;
    			}
    			
    			channel[start_y - 2][start_x] = 0;
    			channel[start_y - 1][start_x + 1] = 0;
    			channel[start_y][start_x + 2] = 0;
    			channel[start_y][start_x] = 0;
    			channel[start_y - 2][start_x + 2] = 0;
    		}
    	}
    	
    	// Set pixels for our coffee
    	for (OfficeWorldPosition pos : ows.coffeePositionsUs)
    	{
    		setObjectPixels(pos, new int[] {165, 42, 42}, ourAgentColour, r, g, b);
    	}
    	
    	// Set pixels for our office(s)
    	for (OfficeWorldPosition pos : ows.officePositionsUs)
    	{
    		setObjectPixels(pos, new int[] {255, 0, 255}, ourAgentColour, r, g, b);
    	}
    	
    	// Set pixels for our mail room(s)
    	for (OfficeWorldPosition pos : ows.mailRoomPositionsUs)
    	{
    		setObjectPixels(pos, new int[] {0, 255, 0}, ourAgentColour, r, g, b);
    	}
    	
    	// Set pixels for other agent coffee
    	for (OfficeWorldPosition pos : ows.coffeePositionsOtherAgent)
    	{
    		setObjectPixels(pos, new int[] {165, 42, 42}, otherAgentColour, r, g, b);
    	}
    	
    	// Set pixels for other agent office(s)
    	for (OfficeWorldPosition pos : ows.officePositionsOtherAgent)
    	{
    		setObjectPixels(pos, new int[] {255, 0, 255}, otherAgentColour, r, g, b);
    	}
    	
    	// Set pixels for other agent mail room(s)
    	for (OfficeWorldPosition pos : ows.mailRoomPositionsOtherAgent)
    	{
    		setObjectPixels(pos, new int[] {0, 255, 0}, otherAgentColour, r, g, b);
    	}
    	
    	// Set pixels for us
    	OfficeWorldPosition pos = ows.position[0];
    	int start_x;
    	int start_y;
    	
    	if (pos != null)
    	{
	    	start_x = 1 + pos.room_x * 10 + pos.cell_x * 3;
	    	start_y = 29 - pos.room_y * 10 - pos.cell_y * 3;
	    	
	    	for (int y = start_y - 2; y <= start_y; y++)
	    	{
	        	for (int x = start_x; x <= start_x + 2; x++)
	        	{
	        		if ((y != start_y - 1) || (x != start_x + 1))
	        		{
		        		r[y][x] = 0;
		        		g[y][x] = 0;
		        		b[y][x] = 255;
	        		}
	        	}
	    	}
    	}
    	
    	// Set pixels for opponent
    	pos = ows.position[1];
		
    	if (pos != null)
    	{
	    	start_x = 1 + pos.room_x * 10 + pos.cell_x * 3;
	    	start_y = 29 - pos.room_y * 10 - pos.cell_y * 3;
	    	
	    	for (int y = start_y - 2; y <= start_y; y++)
	    	{
	        	for (int x = start_x; x <= start_x + 2; x++)
	        	{
	        		if ((y != start_y - 1) || (x != start_x + 1))
	        		{
		        		r[y][x] = otherAgentColour[0];
		        		g[y][x] = otherAgentColour[1];
		        		b[y][x] = otherAgentColour[2];
	        		}
	        	}
	    	}
    	}
    	
    	// Create the image
    	int enlargementFactor = 8;
    	int[][][] enlargedImage = enlargeImage(r, g, b, enlargementFactor);
    	
    	BufferedImage image = new BufferedImage(width * enlargementFactor, height * enlargementFactor, BufferedImage.TYPE_INT_RGB); 
    	for (int y = 0; y < height * enlargementFactor; y++)
    	{
    		for (int x = 0; x < width * enlargementFactor; x++)
    		{
    			int rgb = enlargedImage[0][y][x];
    	        rgb = (rgb << 8) + enlargedImage[1][y][x]; 
    	        rgb = (rgb << 8) + enlargedImage[2][y][x];
    	        image.setRGB(x, y, rgb);
    		}
    	}

    	File outputFile = new File(Log.getLogDir() + "/img/" + img_name + ".png");
    	try {
			ImageIO.write(image, "png", outputFile);
		} catch (IOException e) {
			Log.info("ERROR: Could not write office world image!");
		}
	}
	
	private int[][] deepCopy(int[][] original) {
	    if (original == null) {
	        return null;
	    }

	    final int[][] result = new int[original.length][];
	    for (int i = 0; i < original.length; i++) {
	        result[i] = Arrays.copyOf(original[i], original[i].length);
	    }
	    return result;
	}
}
