package craftworld;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import main.Main;
import scheduler.State;
import util.Enums.CraftWorldItem;
import util.Enums;
import util.Log;

public class CraftWorldRenderer {

	public static int[][] grid;
	private static int width;
	private static int height;
	
	private static int[][][] ourSprite;
	private static int[][][] otherAgentSprite;
	
	private static int[][][] treeSprite;
	private static int[][][] grassSprite;
	private static int[][][] ironSprite;
	private static int[][][] goldSprite;
	private static int[][][] gemSprite;
	private static int[][][] plankSprite;
	private static int[][][] bedSprite;
	private static int[][][] stickSprite;
	private static int[][][] ropeSprite;
	private static int[][][] bridgeSprite;
	private static int[][][] axeSprite;
	private static int[][][] clothSprite;

	private static int[][][] factorySprite;
	private static int[][][] toolshedSprite;
	private static int[][][] workbenchSprite;
	
	private static int[][][][] statusSprites;
	
	private static final int SIDE_BUFFER = 7;
	private static final int TOP_BUFFER;
	
    static {
    	
    	int num_collectable_items = Math.max(Main.cw_settings.itemsToMake.get(0).size(), Main.cw_settings.itemsToMake.get(1).size()) + 3;
    	
    	TOP_BUFFER = Math.max(0, num_collectable_items - Main.cw_settings.grid_size);
    	
	 	grid = new int[Main.cw_settings.grid_size * 6 + 1 + TOP_BUFFER * 6][Main.cw_settings.grid_size * 6 + 1  + 2 * SIDE_BUFFER];
	 	
	 	for (int y = 0; y < grid.length; y++)
	 	{
		 	for (int x = 0; x < grid[0].length; x++)
		 	{
		 		if (x < SIDE_BUFFER || x >= (grid[0].length - SIDE_BUFFER) || (y < TOP_BUFFER * 6))
		 		{
		 			grid[y][x] = 255;
		 		}
		 		else if (y % 6 == 0 || (x - SIDE_BUFFER) % 6 == 0)
		 		{
		 			grid[y][x] = 192;
		 		}
		 		else
		 		{
		 			grid[y][x] = 255;
		 		}
		 	}
	 	}
    	
    	width = grid[0].length;
    	height = grid.length;

    	ourSprite = new int[][][] {
    		{{255, 0, 0, 0, 255}, {255, 0, 255, 0, 255}, {0, 0, 0, 0, 0}, {255, 255, 0, 255, 255}, {255, 0, 255, 0, 255}},
    		{{255, 0, 0, 0, 255}, {255, 0, 255, 0, 255}, {0, 0, 0, 0, 0}, {255, 255, 0, 255, 255}, {255, 0, 255, 0, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    	};
    	
    	otherAgentSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 0, 0, 0, 255}, {255, 0, 255, 0, 255}, {0, 0, 0, 0, 0}, {255, 255, 0, 255, 255}, {255, 0, 255, 0, 255}},
    		{{255, 0, 0, 0, 255}, {255, 0, 255, 0, 255}, {0, 0, 0, 0, 0}, {255, 255, 0, 255, 255}, {255, 0, 255, 0, 255}}
    	};
    	
    	treeSprite = new int[][][] {
    		{{255, 0, 0, 0, 255}, {0, 0, 0, 0, 0}, {255, 165, 165, 165, 255}, {255, 165, 165, 165, 255}, {255, 165, 165, 165, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 42, 42, 42, 255}, {255, 42, 42, 42, 255}, {255, 42, 42, 42, 255}},
    		{{255, 0, 0, 0, 255}, {0, 0, 0, 0, 0}, {255, 42, 42, 42, 255}, {255, 42, 42, 42, 255}, {255, 42, 42, 42, 255}}
    	};
    	
    	grassSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {0, 255, 0, 255, 0}, {0, 255, 0, 255, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {0, 255, 0, 255, 0}, {0, 255, 0, 255, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}
    	};
    	
    	ironSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {241, 241, 201, 201, 201}, {241, 241, 201, 201, 201}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {13, 13, 33, 33, 33}, {13, 13, 33, 33, 33}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {12, 12, 30, 30, 30}, {12, 12, 30, 30, 30}, {255, 255, 255, 255, 255}}
    	};
    	
    	factorySprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{0, 0, 0, 0, 0}, {0, 255, 255, 255, 255}, {0, 0, 0, 0, 0}, {0, 255, 255, 255, 255}, {0, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}}
    	};
    	
    	toolshedSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{0, 0, 0, 0, 0}, {255, 255, 0, 255, 255}, {255, 255, 0, 255, 255}, {255, 255, 0, 255, 255}, {255, 255, 0, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}}
    	};
    	
    	workbenchSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{0, 255, 255, 255, 0}, {0, 255, 255, 255, 0}, {0, 255, 0, 255, 0}, {255, 0, 0, 0, 255}, {255, 0, 255, 0, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}}
    	};
    	
    	goldSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {191, 191, 191, 191, 191}, {191, 255, 255, 255, 191}, {191, 191, 191, 191, 191}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {255, 255, 255, 255, 255}}
    	};
    	
    	gemSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 255, 56, 255, 255}, {255, 56, 166, 56, 255}, {255, 56, 166, 56, 255}, {255, 56, 166, 56, 255}, {255, 255, 56, 255, 255}},
    		{{255, 255, 56, 255, 255}, {255, 56, 166, 56, 255}, {255, 56, 166, 56, 255}, {255, 56, 166, 56, 255}, {255, 255, 56, 255, 255}}
    	};
    	
    	plankSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {180, 180, 123, 123, 123}, {180, 180, 123, 123, 123}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {120, 120, 61, 61, 61}, {120, 120, 61, 61, 61}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {4, 4, 0, 0, 0}, {4, 4, 0, 0, 0}, {255, 255, 255, 255, 255}}
    	};
    	
    	bedSprite = new int[][][] {
    		{{42, 42, 255, 255, 255}, {180, 180, 180, 180, 180}, {180, 180, 180, 180, 180}, {123, 255, 255, 255, 123}, {123, 255, 255, 255, 123}},
    		{{96, 96, 255, 255, 255}, {199, 199, 199, 199, 199}, {199, 199, 199, 199, 199}, {61, 255, 255, 255, 61}, {61, 255, 255, 255, 61}},
    		{{153, 153, 255, 255, 255}, {220, 220, 220, 220, 220}, {220, 220, 220, 220, 220}, {0, 255, 255, 255, 0}, {0, 255, 255, 255, 0}}
    	};
    	
    	stickSprite = new int[][][] {
    		{{255, 255, 180, 255, 255}, {255, 255, 180, 129, 255}, {255, 255, 180, 255, 255}, {255, 255, 180, 255, 255}, {255, 255, 180, 255, 255}},
    		{{255, 255, 120, 255, 255}, {255, 255, 120, 212, 255}, {255, 255, 120, 255, 255}, {255, 255, 120, 255, 255}, {255, 255, 120, 255, 255}},
    		{{255, 255, 4, 255, 255}, {255, 255, 4, 26, 255}, {255, 255, 4, 255, 255}, {255, 255, 4, 255, 255}, {255, 255, 4, 255, 255}}
    	};
    	
    	ropeSprite = new int[][][] {
    		{{255, 255, 255, 255, 180}, {255, 180, 255, 255, 180}, {180, 255, 255, 180, 255}, {180, 255, 255, 180, 255}, {255, 180, 180, 255, 255}},
    		{{255, 255, 255, 255, 120}, {255, 120, 255, 255, 120}, {120, 255, 255, 120, 255}, {120, 255, 255, 120, 255}, {255, 120, 120, 255, 255}},
    		{{255, 255, 255, 255, 4}, {255, 4, 255, 255, 4}, {4, 255, 255, 4, 255}, {4, 255, 255, 4, 255}, {255, 4, 4, 255, 255}}
    	};
    	
    	bridgeSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {120, 120, 120, 120, 120}, {120, 120, 120, 120, 120}, {180, 255, 180, 255, 180}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {75, 75, 75, 75, 75}, {75, 75, 75, 75, 75}, {120, 255, 120, 255, 120}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {4, 4, 4, 4, 4}, {4, 4, 4, 4, 4}, {4, 255, 4, 255, 4}}
    	};
    	
    	axeSprite = new int[][][] {
    		{{255, 180, 204, 204, 255}, {255, 180, 204, 204, 255}, {255, 180, 180, 204, 255}, {255, 180, 180, 255, 255}, {255, 180, 180, 255, 255}},
    		{{255, 120, 204, 204, 255}, {255, 120, 204, 204, 255}, {255, 120, 120, 204, 255}, {255, 120, 120, 255, 255}, {255, 120, 120, 255, 255}},
    		{{255, 4, 204, 204, 255}, {255, 4, 204, 204, 255}, {255, 4, 4, 204, 255}, {255, 4, 4, 255, 255}, {255, 4, 4, 255, 255}}
    	};
    	
    	clothSprite = new int[][][] {
    		{{221, 221, 221, 221, 221}, {212, 212, 212, 212, 212}, {221, 221, 221, 221, 221}, {212, 212, 212, 212, 212}, {221, 221, 221, 221, 221}},
    		{{232, 232, 232, 232, 232}, {234, 234, 234, 234, 234}, {232, 232, 232, 232, 232}, {234, 234, 234, 234, 234}, {232, 232, 232, 232, 232}},
    		{{203, 203, 203, 203, 203}, {107, 107, 107, 107, 107}, {203, 203, 203, 203, 203}, {107, 107, 107, 107, 107}, {203, 203, 203, 203, 203}}
    	};
    	
    	statusSprites = new int[][][][] {treeSprite, grassSprite, ironSprite, goldSprite, gemSprite, plankSprite, bedSprite, stickSprite, ropeSprite, bridgeSprite, axeSprite, clothSprite};
    	
    	/*
    	allWhiteSprite = new int[][][] {
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}},
    		{{255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}, {255, 255, 255, 255, 255}}
    	};
    	
    	allBlackSprite = new int[][][] {
    		{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}},
    		{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}},
    		{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}
    	};
    	*/
    }
    
	public CraftWorldRenderer()
	{

	}
	
	private void drawStatusSprite(CraftWorldItem item, int item_no, int agent_num, int[][] r, int[][] g, int[][] b)
	{
		CraftWorldPosition pos;
		
		if (agent_num == 0)
		{
			pos = new CraftWorldPosition(-1, TOP_BUFFER + Main.cw_settings.grid_size - 1 - item_no);
			setObjectPixels(pos, statusSprites[item.id], r, g, b, -1);
		}
		else
		{
			pos = new CraftWorldPosition(Main.cw_settings.grid_size, TOP_BUFFER + Main.cw_settings.grid_size - 1 - item_no);
			setObjectPixels(pos, statusSprites[item.id], r, g, b, 1);
		}
	}
	
	private void setObjectPixels(CraftWorldPosition pos, int[][][] objectSprite, int[][] r, int[][] g, int[][] b)
	{
		setObjectPixels(pos, objectSprite, r, g, b, 0);
	}
	
	private void setObjectPixels(CraftWorldPosition pos, int[][][] objectSprite, int[][] r, int[][] g, int[][] b, int additional_x_offset)
	{
    	int start_x = SIDE_BUFFER + 1 + pos.cell_x * 6 + additional_x_offset;
    	int start_y = grid.length - (1 + pos.cell_y * 6) - 5;
    	
    	for (int y_offset = 0; y_offset <= 4; y_offset++)
    	{
    		int y = start_y + y_offset;
    				
    		for (int x_offset = 0; x_offset <= 4; x_offset++)
        	{
    			int x = start_x + x_offset;

        		r[y][x] = objectSprite[0][y_offset][x_offset];
        		g[y][x] = objectSprite[1][y_offset][x_offset];
        		b[y][x] = objectSprite[2][y_offset][x_offset];
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
		CraftWorldState cws = new CraftWorldState(state);
		
    	int[][] r = deepCopy(grid);
    	int[][] g = deepCopy(grid);
    	int[][] b = deepCopy(grid);
    	
    	CraftWorldPosition pos;
    	
    	// Set pixels for trees
    	for (int i = 0; i < Main.cw_settings.tx.size(); i++)
    	{
    		if (cws.treeStatus[i])
    		{
    			pos = new CraftWorldPosition(Main.cw_settings.tx.get(i), Main.cw_settings.ty.get(i));
    			setObjectPixels(pos, treeSprite, r, g, b);
    		}
    	}
    	
    	// Set pixels for grass
    	for (int i = 0; i < Main.cw_settings.grassx.size(); i++)
    	{
    		if (cws.grassStatus[i])
    		{
    			pos = new CraftWorldPosition(Main.cw_settings.grassx.get(i), Main.cw_settings.grassy.get(i));
    			setObjectPixels(pos, grassSprite, r, g, b);
    		}
    	}
    	
    	// Set pixels for iron
    	for (int i = 0; i < Main.cw_settings.ironx.size(); i++)
    	{
    		if (cws.ironStatus[i])
    		{
    			pos = new CraftWorldPosition(Main.cw_settings.ironx.get(i), Main.cw_settings.irony.get(i));
    			setObjectPixels(pos, ironSprite, r, g, b);
    		}
    	}
    	
    	// Set pixels for gold
    	for (int i = 0; i < Main.cw_settings.goldx.size(); i++)
    	{
    		if (cws.goldStatus[i])
    		{
    			pos = new CraftWorldPosition(Main.cw_settings.goldx.get(i), Main.cw_settings.goldy.get(i));
    			setObjectPixels(pos, goldSprite, r, g, b);
    		}
    	}
    	
    	// Set pixels for gems
    	for (int i = 0; i < Main.cw_settings.gemx.size(); i++)
    	{
    		if (cws.gemStatus[i])
    		{
    			pos = new CraftWorldPosition(Main.cw_settings.gemx.get(i), Main.cw_settings.gemy.get(i));
    			setObjectPixels(pos, gemSprite, r, g, b);
    		}
    	}
    	
    	// Set pixels for factories
    	for (int i = 0; i < Main.cw_settings.facx.size(); i++)
    	{
			pos = new CraftWorldPosition(Main.cw_settings.facx.get(i), Main.cw_settings.facy.get(i));
			setObjectPixels(pos, factorySprite, r, g, b);
    	}
    	
    	// Set pixels for toolsheds
    	for (int i = 0; i < Main.cw_settings.tsx.size(); i++)
    	{
			pos = new CraftWorldPosition(Main.cw_settings.tsx.get(i), Main.cw_settings.tsy.get(i));
			setObjectPixels(pos, toolshedSprite, r, g, b);
    	}
    	
    	// Set pixels for workbenches
    	for (int i = 0; i < Main.cw_settings.wbx.size(); i++)
    	{
			pos = new CraftWorldPosition(Main.cw_settings.wbx.get(i), Main.cw_settings.wby.get(i));
			setObjectPixels(pos, workbenchSprite, r, g, b);
    	}
    	
    	// Set pixels for us
    	pos = cws.position[0];
    	if (pos != null)
    	{
    		setObjectPixels(pos, ourSprite, r, g, b);
    	}
    	
    	// Set pixels for opponent
    	pos = cws.position[1];
    	if (pos != null)
    	{
    		setObjectPixels(pos, otherAgentSprite, r, g, b);
    	}
    	
    	// Draw agent inventories
		for (int agent_num = 0; agent_num < 2; agent_num++)
		{
	    	int item_no = 0;
			for (CraftWorldItem item : Enums.CraftWorldItem.values())
			{
				if (cws.haveItem[item.id][agent_num])
				{
					drawStatusSprite(item, item_no, agent_num, r, g, b);
					item_no++;
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
