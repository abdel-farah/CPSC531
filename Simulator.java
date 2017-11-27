import java.util.*;

public class Simulator {
    public static final int ARRIVAL = 1;
	public static final int LEAVE_ELEVATOR = 2;
    public static final int STOP = 3;
    private static final Random R1 = new Random(12345);
    private static final Random R2 = new Random(54321);
	public static PriorityQueue<Event> eventList = new PriorityQueue<Event>();

    private static double expA(double lambda) {
        return -(1 / lambda) * Math.log(1 - R1.nextDouble());
    }

    private static double expD(double lambda) {
        return -(1 / lambda) * Math.log(1 - R2.nextDouble());
    }

    
    public static double getArrival( double lambda){
    	
		double arrival;
		arrival = expA(lambda);
		return arrival;
	}
    
    public static double getService(double mu){
		double service;
		service = expD(1/mu);
		return service;
	}

    public static void main(String args[]) throws Exception{
    	double clock;
    	int arrivals = 0;
    	int count = 1;
    	double eventTime;
    	Event arrivalOne = new Event(ARRIVAL, count, 0);
		Elevator elevator = new Elevator(0);
    	eventList.add(arrivalOne);
		
		Event stop = new Event(STOP, -1,  60);		
		eventList.add(stop);
		Event currentEvent = eventList.poll();
		int eventType = currentEvent.type;
		int customerID = currentEvent.id;
		int i =0;
		while (eventType != STOP){
			eventType = currentEvent.type;
			eventTime = currentEvent.time;
			customerID = currentEvent.id;
			clock = eventTime;
			if (eventType == ARRIVAL){
				System.out.println("Person arrived at " + eventTime);
				Person passenger = new Person(customerID);
				System.out.println("Current Person's ID: " + passenger.personID);
				
				elevator.addPassenger(customerID, passenger);
				count++;
				Event nextArrival = new Event(ARRIVAL, count, clock + getArrival(0.5));
				System.out.printf("Scheduling arrival event for customer %d at %f\n", count, nextArrival.time);
				eventList.add(nextArrival);
				System.out.printf("Scheduling leave elevator event for customer %d at %f\n", customerID,clock+1.0);
				Event departElevator = new Event(LEAVE_ELEVATOR, customerID, clock + 1.0);
				eventList.add(departElevator);
				
				System.out.println("Person's requested floor is " + passenger.requestedFloor);
			}
			if (eventType == LEAVE_ELEVATOR){
				System.out.println("Time for customer " + customerID + " To leave the elevator");
				Object result = elevator.passengers.get(customerID);
				System.out.println("requested Floor " + result.getClass().getField("requestedFloor").get(result));
				elevator.floor = result.getClass().getField("requestedFloor").getInt(result);
				System.out.println("Elevator is now on floor " + elevator.floor + " customer " + customerID + " is getting off");
				elevator.passengers.set(customerID, null);
			}
			
			currentEvent = eventList.poll();			
			//i++;
			
		}
		System.out.println("SIMULATION ENDED");
    }
}
