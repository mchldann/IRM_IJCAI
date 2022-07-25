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

/**
 * @version 2.0
 */
public class Literal implements Comparable<Literal> {

	final private String id;
	private boolean state;
	final private boolean stochastic;
	final private boolean randomInit;

	final private double probability;


	public Literal(String id, boolean state, boolean stochastic, boolean randomInit, double prob) {
		this.id = id;
		this.state = state;
		this.stochastic = stochastic;
		this.randomInit = randomInit;
		this.probability = prob;
	}

	public Literal(String id, boolean state, boolean stochastic, boolean randomInit){
		this(id, state, stochastic, randomInit,0);
	}

	public String getId() {
		return this.id;
	}

	public boolean getState() {
		return state;
	}

	public boolean isStochastic() {
		return stochastic;
	}

	public boolean isRandomInit() {
		return randomInit;
	}

	public double getProbability(){return probability;}

	public void setState(boolean state) {
		this.state = state;
	}

	public boolean flip(){
		this.state = !this.state;
		return this.state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Literal literal = (Literal) o;

		return id.equals(literal.id);

	}

	@Override
	public int compareTo(Literal o) {
		return this.getId().compareTo(o.getId());
	}

	/** write the literal*/
	public String toSimpleString()
	{
		return "(" + this.id + "," + this.state + ")";
	}

	@Override
	public Literal clone(){
		return new Literal(id, state, stochastic, randomInit, probability);
	}
}
