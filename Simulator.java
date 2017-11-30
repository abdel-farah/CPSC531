import java.lang.reflect.*;
import java.util.*;

public class Simulator {
    public static final int ARRIVAL = 1;
	public static final int START_MOVING = 2;
	public static final int DONE_WORKING = 3;
	public static final int MOVE_ELEVATOR = 4;
	public static final int DEPART= 5;
	public static final int STOP = 6;
	public static final int IDLE = 0;
	public static final int BUSY = 1;
	public static final int MAX_CUSTOMERS = 1000;
    private static final Random R1 = new Random(12345);
    private static final Random R2 = new Random(54321);
	public static PriorityQueue<Event> eventList = new PriorityQueue<Event>();
	public static PriorityQueue<Event> waitList = new PriorityQueue<Event>();
	public static List<Person> deliveredPassengers = new ArrayList<Person>();

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
    	int peopleOnFloor0 = 0;
    	int arrivals = 0;
    	int departures = 0;
    	int count = 1;
    	double eventTime;
    	int elevatorSize = 0;
    	Event arrivalOne = new Event(ARRIVAL, count, 0);
		Elevator elevator = new Elevator(0);
		double[] responseTimes = new double[MAX_CUSTOMERS];
		double[] responseTimesDown = new double[MAX_CUSTOMERS];

		Person test = new Person(MAX_CUSTOMERS + 1);
		/* Initialize the deliverdPassenger list, to avoid out of bounds exceptions*/
		for (int h =0 ; h < MAX_CUSTOMERS; h++){
			deliveredPassengers.add(h, test);
		}
		eventList.add(arrivalOne);
		
		Event stop = new Event(STOP, -1,  1000000);		
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
			System.out.println("Current time " + clock);
			System.out.println("Wait list size " + waitList.size());
			System.out.println("Elevator floor: " + elevator.floor);
			
			if(elevator.floor != 0 && elevatorSize == 0){
				System.out.println("ELEVATOR SIZE = " + elevatorSize);
				System.out.println("Elevator empty, going back to lobby");

				double timeTaken = ((double) elevator.floor*10)/60;

				System.out.printf("It takes %f minutes to go to floor 0\n", timeTaken);
				clock = clock +  timeTaken;
				System.out.println("Updated time " + clock);
				elevator.floor = 0;
				elevator.state = IDLE;
			}
			/* Checks if there are any passengers waiting on floor 0 (the lobby)*/
			while (waitList.size() > 0 && elevator.floor == 0){
				System.out.println("Handling wait list");
				Event waitingEvent = waitList.poll();
				System.out.println("Original arrival time " + waitingEvent.time);
				responseTimes[waitingEvent.id -1] = waitingEvent.time;
				System.out.println("Storing time in responseTimes[ " +  waitingEvent.id +" ]");

				Person waitPassenger = new Person(waitingEvent.id);
				System.out.println("Added customer " + waitingEvent.id + " to elevator");
				elevator.addPassenger(waitingEvent.id, waitPassenger);
				elevatorSize++;
				System.out.printf("Scheduling move elevator event for passenger %d at %f\n", waitingEvent.id, (clock + waitingEvent.time) -10.0);
				Event departElevator = new Event(START_MOVING, waitingEvent.id, (clock + waitingEvent.time) -10.0);
				eventList.add(departElevator);
				System.out.println("Person's requested floor is " + waitPassenger.requestedFloor);
				System.out.println("Number of people on the elevator " + elevatorSize);
			}
			
			if (eventType == ARRIVAL){
				System.out.println("ARRIVAL");
				responseTimes[customerID -1] = eventTime;
				arrivals ++;
				System.out.println("Person arrived at " + eventTime);
				Person passenger = new Person(customerID);
				System.out.println("Current Person's ID: " + passenger.personID);
				peopleOnFloor0++;
				
				/* If the elevator is on the first floor upon arrival, I just add them to the elevator*/
				
				if (elevator.floor == 0){
					elevator.addPassenger(customerID, passenger);
					elevatorSize++;
					System.out.printf("Scheduling move elevator event for passenger %d at %f\n", customerID,clock+5.0);
					Event departElevator = new Event(START_MOVING, customerID, clock + 5.0);
					eventList.add(departElevator);
					System.out.println("Person's requested floor is " + passenger.requestedFloor);
					System.out.println("Number of people on the elevator " + elevatorSize);
				}
				/* If the elevator isn't on floor 0, we just tell the passenger to wait in the lobby*/
				
				else{
					System.out.println("Waiting for Elevator");
					waitList.add(currentEvent);
					System.out.println("Waitlist size " + waitList.size());
				}
				count++;
				if (arrivals < MAX_CUSTOMERS){
					Event nextArrival = new Event(ARRIVAL, count, clock + getArrival(0.5));
					System.out.printf("Scheduling arrival event for passenger %d at %f\n", count, nextArrival.time);
					eventList.add(nextArrival);
				}
			}
		
			/* This event makes the elevator start moving*/
			
			if (eventType == START_MOVING){
				System.out.println("START_MOVING");
				elevator.state = BUSY;
				System.out.println("Customer ID " + customerID);
				
				/* Calculate the time it takes for the elevator to move to the requested floor*/
				Person currentPerson = elevator.passengers.get(customerID);
				int requestedFloor = currentPerson.requestedFloor;
				System.out.println("Requested floor" + requestedFloor);
				int difference = Math.abs(requestedFloor - elevator.floor) * 10;
				double timeTaken = ((double) difference)/60;
				System.out.println("Moving to floor " + requestedFloor + " in " + timeTaken + " minutes" );
				elevator.floor = requestedFloor;
				
				
				
				Event moveElevator = new Event(MOVE_ELEVATOR, customerID, clock + timeTaken);
				System.out.printf("Scheduling move elevator event for passenger %d at %f\n", customerID, moveElevator.time);
				eventList.add(moveElevator);
			}
			
			/* This event moves the elevator to the requested floor, and gets the passenger off the elevator*/
			if (eventType == MOVE_ELEVATOR){
				System.out.println("MOVE_ELEVATOR");
				System.out.println("Passenger ID " + customerID);

				Person currentPerson = elevator.passengers.get(customerID);
				int requestedFloor = currentPerson.requestedFloor;
				System.out.printf("Person %d arrived at floor %d\n", customerID, requestedFloor);
				System.out.println("Time for passenger " + customerID + " To leave the elevator"); 
				elevator.floor = requestedFloor;
				currentPerson.finalFloor = requestedFloor;
				deliveredPassengers.add(customerID, currentPerson);
				Event doneWorking = new Event(DONE_WORKING, customerID, clock + getService(10));
				System.out.printf("Scheduling done working event for passenger %d at %f\n", customerID, doneWorking.time);
				eventList.add(doneWorking);
				System.out.println("Elevator array size " + elevator.passengers.size());
				elevatorSize--;
				
				double updatedResponseTime = clock - responseTimes[customerID-1] ;
				responseTimes[customerID -1] = updatedResponseTime;
				System.out.println("Updated response time: " + updatedResponseTime);
				System.out.println("Number of people on the elevator " + elevatorSize);
			}
			if (eventType == DONE_WORKING){
				System.out.println("DONE WORKING");
				Person person = deliveredPassengers.get(customerID);
				responseTimesDown[customerID-1] = clock;
				System.out.println("Passenger ID " + customerID);
				System.out.printf("Elevator is on floor %d, and has %d people in it\n", elevator.floor, elevatorSize);
				System.out.println("Person is on floor " + person.finalFloor);
				if (elevatorSize != 0 ){
				//	if (elevator.floor == person.finalFloor ){
						int difference = Math.abs(person.finalFloor - elevator.floor) * 10;
						double timeTaken = ((double) difference)/60;
						System.out.println("Moving to floor " + person.finalFloor + " in " + timeTaken + " minutes" );
						elevator.floor = person.finalFloor;
						clock = clock +  timeTaken;
						System.out.println("Updated time " + clock);
						elevator.addPassenger(customerID, person);
						elevatorSize++;
						System.out.printf("Scheduling depart event for passenger %d at %f\n", customerID,clock+1.0);
						Event departElevator = new Event(DEPART, customerID, clock + 3.0);
						eventList.add(departElevator);
				//	}
				}
				else if ( elevator.floor == 0 && elevatorSize == 0){
					

					System.out.println("Elevator is free");
					 person = deliveredPassengers.get(customerID);

					elevator.state = BUSY;
					System.out.println("Customer ID " + customerID);

					int requestedFloor = 0;
					int difference = Math.abs(person.finalFloor) * 10;
					double timeTaken = ((double) difference)/60;
					System.out.println("Moving to floor " + person.finalFloor + " in " + timeTaken + " minutes" );
					clock = clock+timeTaken;
					System.out.println("Updated time " + clock);
					elevator.floor = person.finalFloor;
					System.out.println("Elevator arrived on floor " + elevator.floor);
					
					elevator.addPassenger(customerID, person);
					elevatorSize++;
					System.out.printf("Scheduling depart event for customer %d at %f\n", customerID,clock+1.0);
					Event departElevator = new Event(DEPART, customerID, clock + 1.0);
					eventList.add(departElevator);
					
					
				}
			}
			if(eventType == DEPART){
				System.out.println("DEPART");
				System.out.println("Customer ID: " + customerID);

				int cFloor = elevator.floor*10;
				double timeTaken = ((double) cFloor)/60;
				System.out.println("Moving to floor 0" +  " in " + timeTaken + " minutes" );
				clock = clock + timeTaken;
				elevator.floor = 0;
				System.out.println("Updated time " + clock);
				System.out.println("Elevator arrived on floor " + elevator.floor);
				System.out.println("Person getting out of elevator");
				double updatedResponseTimeDown = clock  - responseTimesDown[customerID-1];
				responseTimesDown[customerID -1] = updatedResponseTimeDown;
				departures++;
				elevatorSize--;
				System.out.println("Number of people in elevator " + elevatorSize);
			}
			
			
			System.out.println();
			currentEvent = eventList.poll();			
			//i++;
			
		}
		System.out.println("Arrivals: " + arrivals);
		System.out.println("Departures: " + departures);
		double totalResponseTime = 0;
		double totalResponseTime2 = 0;

		for (int q =0; q < responseTimes.length; q++){
			totalResponseTime = totalResponseTime + responseTimes[q];
		}
		for (int w =0; w < responseTimesDown.length; w++){
			totalResponseTime2 = totalResponseTime2 + responseTimesDown[w];
		}
		double responseTime1 =  totalResponseTime/MAX_CUSTOMERS;
		double responseTime2 =  totalResponseTime2/MAX_CUSTOMERS;
		System.out.println("Total response time 1" + totalResponseTime);
		System.out.println("Response time going up " + responseTime1 );
		System.out.println("Response time going down " + responseTime2 );

		System.out.println("SIMULATION ENDED");
    }
}