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
 * @version 2.1
 * @author yuanyao
 *
 * updates 2019/10/14:
 * 1. add the postcondition for plans
 */
public class PlanNode extends Node
{
	// Plan -> PlanName {PlanStep ; ... ; PlanStep}
	/** planbody */
	final private ArrayList<Node> pb;
	
	/** precondition */
	final private ArrayList<Literal> pre;
	
	/** in-condition */
	final private ArrayList<Literal> inc;

	/** post-condition */
	final private ArrayList<Literal> post;



	public PlanNode(String name){
		super(name);
		this.pb = new ArrayList<>();
		this.inc = new ArrayList<>();
		this.pre = new ArrayList<>();
		this.post = new ArrayList<>();
	}

	public PlanNode(String name, ArrayList<Node> planbody, ArrayList<Literal> precondition, ArrayList<Literal> incondition,
					ArrayList<Literal> postcondition)
	{
		super(name);
		this.pb = planbody;
		this.pre = precondition;
		this.inc = incondition;
		this.post = postcondition;
	}
	
	
	/** method to return the precondition of this plan */
	public ArrayList<Literal> getPre()
	{
		return this.pre;
	}
	
	/** method to return the in-condition of this plan */
	public ArrayList<Literal> getInc()
	{
		return this.inc;
	}

	/** method to return the post-condiiton of this plan */
	public ArrayList<Literal> getPost(){ return this.post; }
	
	/** method to return its planbody */
	public ArrayList<Node> getPlanBody()
	{
		return this.pb;
	}

}
