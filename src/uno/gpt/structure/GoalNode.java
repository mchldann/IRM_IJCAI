/*
 * Copyright 2016 Yuan Yao
 * University of Nottingham
 * Email: yvy@cs.nott.ac.uk (yuanyao1990yy@icloud.com)
 *
 * Modified 2019 IPC Committee
 * Contact: https://www.intentionprogression.org/contact/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details 
 *  <http://www.gnu.org/licenses/gpl-3.0.html>.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uno.gpt.structure;

import java.util.ArrayList;

/**
 * @version 2.0
 */
public class GoalNode extends Node 
{
	// Goal -> GoalName {Plans}	
	/** associated plans */
	final private ArrayList<PlanNode> plans;
	
	/** in-condition */
	final private ArrayList<Literal> inc;

	/** goalConds-condition */
	final private ArrayList<Literal> goalConds;

	public GoalNode(String name, ArrayList<PlanNode> plan, ArrayList<Literal> inc, ArrayList<Literal> goalConds)
	{
		super(name);
		this.inc = inc;
		this.plans = plan;
		this.goalConds = goalConds;
	}

	public GoalNode(String name){
		super(name);
		this.inc = new ArrayList<>();
		this.plans = new ArrayList<>();
		this.goalConds = new ArrayList<>();

	}
	
	/** method to return the in-condition of this plan */
	public ArrayList<Literal> getInC()
	{
		return this.inc;
	}
	
	/** method to return the plans to achieve this goalConds*/
	public ArrayList<PlanNode> getPlans()
	{
		return this.plans;
	}

	/** method to return the plans to achieve this goalConds*/
	public ArrayList<Literal> getGoalConds()
	{
		return this.goalConds;
	}
}
