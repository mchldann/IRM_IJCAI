package officeworld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import beliefbase.Condition;
import main.Main;
import main.Main.OfficeWorldTask;
import scheduler.State;

public class OfficeWorldState {
	
    public static final Map<String, Integer> patrol_letter_to_int;
    public static final Map<Integer, String> int_to_patrol_letter;
    public static final Map<String, String> next_patrol_point;
    
    static {
    	Map<String, Integer> tmpMap = new HashMap<String, Integer>();
    	tmpMap.put("A", 0);
    	tmpMap.put("B", 1);
    	tmpMap.put("C", 2);
    	tmpMap.put("D", 3);
    	
    	patrol_letter_to_int = Collections.unmodifiableMap(tmpMap);
    	
    	Map<Integer, String> tmpMap2 = new HashMap<Integer, String>();
    	tmpMap2.put(0, "A");
    	tmpMap2.put(1, "B");
    	tmpMap2.put(2, "C");
    	tmpMap2.put(3, "D");
    	
    	int_to_patrol_letter = Collections.unmodifiableMap(tmpMap2);
    	
    	Map<String, String> tmpMap3 = new HashMap<String, String>();
    	tmpMap3.put("A", "B");
    	tmpMap3.put("B", "C");
    	tmpMap3.put("C", "D");
    	tmpMap3.put("D", "A");
    	
    	next_patrol_point = Collections.unmodifiableMap(tmpMap3);
    }
    
	public boolean[] coffeeDelivered;
	public boolean[] haveCoffee;
	public boolean[] mailDelivered;
	public boolean[] haveMail;
	public String[] nextPatrolPoint;
	public OfficeWorldPosition[] position;
	
	public OfficeWorldPosition[] patrolPoints;
	
	public OfficeWorldPosition[] coffeePositionsUs;
	public OfficeWorldPosition[] mailRoomPositionsUs;
	public OfficeWorldPosition[] officePositionsUs;
	
	public OfficeWorldPosition[] coffeePositionsOtherAgent;
	public OfficeWorldPosition[] mailRoomPositionsOtherAgent;
	public OfficeWorldPosition[] officePositionsOtherAgent;
	
	public OfficeWorldPosition[] decorationPositions;
	
	private void setDefaultObjectPositions()
	{
		this.patrolPoints = new OfficeWorldPosition[] {
			new OfficeWorldPosition(0, 0, 1, 1),
			new OfficeWorldPosition(3, 0, 1, 1),
			new OfficeWorldPosition(3, 2, 1, 1),
			new OfficeWorldPosition(0, 2, 1, 1)
		};
		
		this.coffeePositionsUs = new OfficeWorldPosition[] {
			new OfficeWorldPosition(1, 2, 0, 0),
			new OfficeWorldPosition(2, 0, 2, 2)
		};
		
		this.mailRoomPositionsUs = new OfficeWorldPosition[] {
			new OfficeWorldPosition(2, 1, 1, 1)
		};
			
		this.officePositionsUs = new OfficeWorldPosition[] {
			new OfficeWorldPosition(1, 1, 1, 1)
		};
		
		if (Main.USE_CUSTOM_OFFICEWORLD_LOCATIONS)
		{
			this.coffeePositionsOtherAgent = new OfficeWorldPosition[] {
				new OfficeWorldPosition(0, 2, 0, 0)
			};
			
			this.mailRoomPositionsOtherAgent = new OfficeWorldPosition[] {
				new OfficeWorldPosition(0, 1, 2, 0)
			};
				
			this.officePositionsOtherAgent = new OfficeWorldPosition[] {
				new OfficeWorldPosition(0, 0, 2, 0)
			};
		}
		else
		{
			this.coffeePositionsOtherAgent = this.coffeePositionsUs;
			this.mailRoomPositionsOtherAgent = this.mailRoomPositionsUs;
			this.officePositionsOtherAgent = this.officePositionsUs;
		}

		this.decorationPositions = new OfficeWorldPosition[] {
			new OfficeWorldPosition(1, 0, 1, 1),
			new OfficeWorldPosition(2, 0, 1, 1),
			new OfficeWorldPosition(0, 1, 1, 1),
			new OfficeWorldPosition(3, 1, 1, 1),
			new OfficeWorldPosition(1, 2, 1, 1),
			new OfficeWorldPosition(2, 2, 1, 1)
		};
	}
	
	public OfficeWorldState(State s)
	{
		this.position = new OfficeWorldPosition[2];
		this.position[0] = s.ow_agent_positions[0];
		this.position[1] = s.ow_agent_positions[1];
		
		this.coffeeDelivered = new boolean[2];
		Condition cCoffeeDelivered = new Condition("CoffeeDelivered", true);
		Condition cOpCoffeeDelivered = new Condition("OpCoffeeDelivered", true);
		this.coffeeDelivered[0] = s.getBeliefBase().evaluate(cCoffeeDelivered);
		this.coffeeDelivered[1] = s.getBeliefBase().evaluate(cOpCoffeeDelivered);
		
		this.haveCoffee = new boolean[2];
		Condition cHaveCoffee = new Condition("HaveCoffee", true);
		Condition cOpHaveCoffee = new Condition("OpHaveCoffee", true);
		this.haveCoffee[0] = s.getBeliefBase().evaluate(cHaveCoffee);
		this.haveCoffee[1] = s.getBeliefBase().evaluate(cOpHaveCoffee);

		this.mailDelivered = new boolean[2];
		Condition cMailDelivered = new Condition("MailDelivered", true);
		Condition cOpMailDelivered = new Condition("OpMailDelivered", true);
		this.mailDelivered[0] = s.getBeliefBase().evaluate(cMailDelivered);
		this.mailDelivered[1] = s.getBeliefBase().evaluate(cOpMailDelivered);	
		
		this.haveMail = new boolean[2];
		Condition cHaveMail = new Condition("HaveMail", true);
		Condition cOpHaveMail = new Condition("OpHaveMail", true);
		this.haveMail[0] = s.getBeliefBase().evaluate(cHaveMail);
		this.haveMail[1] = s.getBeliefBase().evaluate(cOpHaveMail);
		
		this.nextPatrolPoint = new String[2];
		this.nextPatrolPoint[0] = s.getNextPatrolPoint(0);
		this.nextPatrolPoint[1] = s.getNextPatrolPoint(1);
		
		setDefaultObjectPositions();
	}
	
	public OfficeWorldState(OfficeWorldState ows)
	{
		this.coffeeDelivered = ows.coffeeDelivered.clone();
		this.haveCoffee = ows.haveCoffee.clone();
		this.mailDelivered = ows.mailDelivered.clone();
		this.haveMail = ows.haveMail.clone();
		this.nextPatrolPoint = ows.nextPatrolPoint.clone();
		
		this.position = new OfficeWorldPosition[] {
			new OfficeWorldPosition(ows.position[0]),
			new OfficeWorldPosition(ows.position[1])
		};
		
		// TODO: Might be necessary to change these to deep copies later on, but these elements are fixed for now.
		this.patrolPoints = ows.patrolPoints;
		
		this.coffeePositionsUs = ows.coffeePositionsUs;
		this.mailRoomPositionsUs = ows.mailRoomPositionsUs;
		this.officePositionsUs = ows.officePositionsUs;
		
		this.coffeePositionsOtherAgent = ows.coffeePositionsOtherAgent;
		this.mailRoomPositionsOtherAgent = ows.mailRoomPositionsOtherAgent;
		this.officePositionsOtherAgent = ows.officePositionsOtherAgent;
		
		this.decorationPositions = ows.decorationPositions;
	}
	
	public OfficeWorldState(OfficeWorldTask task, int qLearningID, int agent_num)
	{
		int remainder, roomX, roomY, cellX, cellY, coffeeDelivered, haveCoffee, mailDelivered, haveMail, nextPatrolPoint;
		
		if (task == OfficeWorldTask.DELIVER_COFFEE_AND_MAIL)
		{
			remainder = qLearningID;
			
			cellY = remainder / 576;
			remainder -= cellY * 576;
			
			cellX = remainder / 192;
			remainder -= cellX * 192;	
			
			roomY = remainder / 64;
			remainder -= roomY * 64;
			
			roomX = remainder / 16;
			remainder -= roomX * 16;	
			
			haveMail = remainder / 8;
			remainder -= haveMail * 8;
			
			mailDelivered = remainder / 4;
			remainder -= mailDelivered * 4;
			
			haveCoffee = remainder / 2;
			remainder -= haveCoffee * 2;
			
			coffeeDelivered = remainder;
			
			// Dummy values
			nextPatrolPoint = 0;
		}
		else if (task == OfficeWorldTask.PATROL)
		{
			remainder = qLearningID;
			
			cellY = remainder / 144;
			remainder -= cellY * 144;
			
			cellX = remainder / 48;
			remainder -= cellX * 48;	
			
			roomY = remainder / 16;
			remainder -= roomY * 16;
			
			roomX = remainder / 4;
			remainder -= roomX * 4;	
			
			nextPatrolPoint = remainder;
			
			// Dummy values
			haveMail = 0;
			mailDelivered = 0;
			haveCoffee = 0;
			coffeeDelivered = 0;
		}
		else
		{
			return;
		}
		
		this.coffeeDelivered = new boolean[2];
		this.coffeeDelivered[1 - agent_num] = false; // Dummy value
		this.coffeeDelivered[agent_num] = (coffeeDelivered == 1? true : false);
		
		this.haveCoffee = new boolean[2];
		this.haveCoffee[1 - agent_num] = false; // Dummy value
		this.haveCoffee[agent_num] = (haveCoffee == 1? true : false);
		
		this.mailDelivered = new boolean[2];
		this.mailDelivered[1 - agent_num] = false; // Dummy value
		this.mailDelivered[agent_num] = (mailDelivered == 1? true : false);
		
		this.haveMail = new boolean[2];
		this.haveMail[1 - agent_num] = false; // Dummy value
		this.haveMail[agent_num] = (haveMail == 1? true : false);
		
		this.position = new OfficeWorldPosition[2];
		this.position[1 - agent_num] = new OfficeWorldPosition(0, 0, 0, 0); // Dummy value
		this.position[agent_num] = new OfficeWorldPosition(roomX, roomY, cellX, cellY);
		
		this.nextPatrolPoint = new String[2];
		this.nextPatrolPoint[1 - agent_num] = "A"; // Dummy value
		this.nextPatrolPoint[agent_num] = int_to_patrol_letter.get(nextPatrolPoint);
		
		setDefaultObjectPositions();
	}
	
	public OfficeWorldState[] getNeighbourStates(int agent_num)
	{
		if (this.position[agent_num] == null)
		{
			return null;
		}
		
		OfficeWorldState[] result = new OfficeWorldState[] {null, null, null, null};
		OfficeWorldPosition[] neighbours = this.position[agent_num].get_neighbours();
		
		for (int i = 0; i < neighbours.length; i++)
		{
			if (neighbours[i] == null)
			{
				continue;
			}
			
			OfficeWorldState newState = new OfficeWorldState(this);
			newState.position[agent_num] = neighbours[i];

			// Get coffee
			if (newState.atCoffee(agent_num))
			{
				newState.haveCoffee[agent_num] = true;
			}
			
			// Get mail
			if (newState.atMailRoom(agent_num))
			{
				newState.haveMail[agent_num] = true;
			}
			
			// Deliver coffee
			// TODO: Is it possible to grab more coffee after it has been delivered?
			if (this.haveCoffee[agent_num] && newState.atOffice(agent_num))
			{
				newState.coffeeDelivered[agent_num] = true;
				newState.haveCoffee[agent_num] = false;
			}
			
			// Deliver mail
			// TODO: Is it possible to grab more mail after it has been delivered?
			if (this.haveMail[agent_num] && newState.atOffice(agent_num))
			{
				newState.mailDelivered[agent_num] = true;
				newState.haveMail[agent_num] = false;
			}
			
			// Move to next patrol point
			/*
			if (newState.atPatrolPoint(this.nextPatrolPoint[agent_num], agent_num))
			{
				newState.nextPatrolPoint[agent_num] = next_patrol_point.get(this.nextPatrolPoint[agent_num]);
			}
			*/
			
			result[i] = newState;
		}
		
		return result;
	}

	public int getQLearningID(OfficeWorldTask task, int agent_num)
	{
		int result = 0;
		
		if (task == OfficeWorldTask.DELIVER_COFFEE_AND_MAIL)
		{
			result += coffeeDelivered[agent_num]? 1 : 0;
			result += haveCoffee[agent_num]? 2 : 0;
			result += mailDelivered[agent_num]? 4 : 0;
			result += haveMail[agent_num]? 8 : 0;
			
			// Max value is 15 so far
			result += position[agent_num].room_x * 16; // Potentially adds 3 * 16 = 48
			
			// Max value is 63 so far
			result += position[agent_num].room_y * 64; // Potentially adds 2 * 64 = 128
			
			// Max value is 191 so far
			result += position[agent_num].cell_x * 192; // Potentially adds 2 * 192 = 384
			
			// Max value is 575 so far
			result += position[agent_num].cell_y * 576; // Potentially adds 2 * 576 = 1152
			
			// Max possible value is 1727 --> 1728 possible states
			return result;
		}
		else if (task == OfficeWorldTask.PATROL)
		{
			result += patrol_letter_to_int.get(nextPatrolPoint[agent_num]);
			
			// Max value is 3 so far
			result += position[agent_num].room_x * 4; // Potentially adds 3 * 4 = 12
			
			// Max value is 15 so far
			result += position[agent_num].room_y * 16; // Potentially adds 2 * 16 = 32
			
			// Max value is 47 so far
			result += position[agent_num].cell_x * 48; // Potentially adds 2 * 48 = 96
			
			// Max value is 143 so far
			result += position[agent_num].cell_y * 144; // Potentially adds 2 * 144 = 288
			
			// Max possible value is 431 --> 432 possible states
			return result;
		}
		else
		{
			return -1;
		}
	}
	
	private boolean atEntity(OfficeWorldPosition[] entity_positions, int agent_num)
	{
		for (OfficeWorldPosition pos : entity_positions)
		{
			if (pos.equals(position[agent_num]))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean atCoffee(int agent_num)
	{
		return atEntity(coffeePositionsOtherAgent, agent_num);
	}
	
	public boolean atMailRoom(int agent_num)
	{
		return atEntity(mailRoomPositionsOtherAgent, agent_num);
	}
	
	public boolean atOffice(int agent_num)
	{
		return atEntity(officePositionsOtherAgent, agent_num);
	}
	
	public boolean atDecoration(int agent_num)
	{
		return atEntity(decorationPositions, agent_num);
	}
	
	/*
	public boolean atPatrolPoint(String letter, int agent_num)
	{
		return atEntity(new OfficeWorldPosition[] {patrolPoints[patrol_letter_to_int.get(letter)]}, agent_num);
	}
	*/
}
