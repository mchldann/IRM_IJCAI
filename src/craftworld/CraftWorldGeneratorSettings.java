package craftworld;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import uno.gpt.generators.MCGenerator;
import util.Enums.CraftWorldItem;

public class CraftWorldGeneratorSettings
{
    public List<List<CraftWorldItem>> goalItems;
    public List<List<CraftWorldItem>> itemsToMake;
    
	public int spawn_x;
	public int spawn_y;
	public int op_spawn_x;
	public int op_spawn_y;
	
	public int grid_size = MCGenerator.def_size;   // 39
	public int tree_num = MCGenerator.def_tree;    // 5
	public int ts_num = MCGenerator.def_ts;        // 2 (toolsheds)
	public int wb_num = MCGenerator.def_wb;        // 2 (workbenches)
	public int grass_num = MCGenerator.def_grass;  // 5
	public int fac_num = MCGenerator.def_fac;      // 2 (factories)
	public int iron_num = MCGenerator.def_iron;    // 5
	public int gold_num = MCGenerator.def_gold;    // 2
	public int gem_num = MCGenerator.def_gem;      // 2

	public int gpt_tree_num = MCGenerator.gpt_tree;
	public int gpt_ts_num = MCGenerator.gpt_ts;
	public int gpt_wb_num = MCGenerator.gpt_wb;
	public int gpt_grass_num = MCGenerator.gpt_grass;
	public int gpt_fac_num = MCGenerator.gpt_fac;
	public int gpt_iron_num = MCGenerator.gpt_iron;
	public int gpt_gold_num = MCGenerator.gpt_gold;
	public int gpt_gem_num = MCGenerator.gpt_gem;
	
	public boolean gpt_order = MCGenerator.fixedorder;
	
	public ArrayList<Integer> tx = new ArrayList<>();      // Tree x
	public ArrayList<Integer> ty = new ArrayList<>();      // Tree y
	public ArrayList<Integer> grassx = new ArrayList<>();  // Grass x
	public ArrayList<Integer> grassy = new ArrayList<>();  // Grass y
	public ArrayList<Integer> ironx = new ArrayList<>();   // Iron x
	public ArrayList<Integer> irony = new ArrayList<>();   // Iron y
	public ArrayList<Integer> goldx = new ArrayList<>();
	public ArrayList<Integer> goldy = new ArrayList<>();
	public ArrayList<Integer> gemx = new ArrayList<>();    // Gem x
	public ArrayList<Integer> gemy = new ArrayList<>();    // Gem y
	
	public ArrayList<Integer> tsx = new ArrayList<>();     // Toolshed x
	public ArrayList<Integer> tsy = new ArrayList<>();     // Toolshed y
	public ArrayList<Integer> wbx = new ArrayList<>();     // Workbench x
	public ArrayList<Integer> wby = new ArrayList<>();     // Workbench y
	public ArrayList<Integer> facx = new ArrayList<>();    // Factory x
	public ArrayList<Integer> facy = new ArrayList<>();    // Factory y
	
	public List<Integer>[][] tx_trimmed_arr;
	public List<Integer>[][] ty_trimmed_arr;
	
	public List<Integer>[][] grassx_trimmed_arr;
	public List<Integer>[][] grassy_trimmed_arr;
	
	public List<Integer>[][] ironx_trimmed_arr;
	public List<Integer>[][] irony_trimmed_arr;
	
	public List<Integer>[][] tsx_trimmed_arr;
	public List<Integer>[][] tsy_trimmed_arr;
	
	public void serialise_to_xml(String out_xml_file)
	{
		XMLEncoder encoder= null;
		
		try
		{
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(out_xml_file)));
		}
		catch(FileNotFoundException fileNotFound)
		{
			System.out.println("ERROR: While creating or opening the file " + out_xml_file);
		}
		
		encoder.writeObject(this);
		encoder.close();
	}
	
	public static CraftWorldGeneratorSettings load_from_xml(String in_xml_file)
	{
		XMLDecoder decoder = null;
		
		try
		{
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(in_xml_file)));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("ERROR: File " + in_xml_file + " not found");
		}
		
		CraftWorldGeneratorSettings settings = (CraftWorldGeneratorSettings)decoder.readObject();
		
		return settings;
	}
}
