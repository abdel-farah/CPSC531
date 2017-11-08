import java.lang.*;
import java.util.*;
public class barberShop{
	
    public static final int ARRIVAL = 1;
    public static final int DEPARTURE = 2;
    public static final int STOP = 3;
    public static final int CUSTOMER = -1;
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int GEORGE = 2;
    public static final int FRED = 3;
    public static final int MAX_CUSTOMERS = 10;
	public static Queue<Integer> Fred = new LinkedList<Integer>();
	public static Queue<Integer> George = new LinkedList<Integer>();
	int state = IDLE;
	public static Queue<Integer> waitingRoom = new LinkedList<Integer>();
	public static PriorityQueue<Event> eventList = new PriorityQueue<Event>();
	public static double Exponential( double m)
	{
		double rand = Math.random();
		
		return ( -m * Math.log( 1.0 - rand ) );
		
	} 

	public static double getArrival( double lambda){
	
		double arrival;
		arrival = Exponential(lambda);
		return arrival;
	}
	
	public static double getService(double mu){
		double service;
		service = Exponential(mu);
		return service;
	}
	
	public static double chooseBarberAndService(int customer){
		double rand = Math.random();
		if (rand > 0.5){
			Fred.add(customer);
			return 3.0;     //mu1
			state = FRED;
		}
		else if (rand < 0.5){
			George.add(customer);
			return 2.0;   	//mu2
			state = GEORGE;
		}
		else return 0;
	}
	public static boolean checkIfDepartureIsInChair(int customerID){
		if (Fred.peek() == customerID || George.peek() == customerID)
					return true;
				
		else 
					return false;
					
		
		
	}

	
	public static void main(String[] args){
		//Queue<Integer> barberChairs = new LinkedList<Integer>();
		//Queue<Integer> waitingRoom = new LinkedList<Integer>();
		//PriorityQueue<Event> eventList = new PriorityQueue<Event>();
		double clock = 0;
		int eventType;
		double eventTime;
		//int state = IDLE;
		int arrivals = 1;
		int departures = 0;
		int customersLost = 0;
		int customerID;
		Event arrivalOne = new Event(ARRIVAL, arrivals, 0);
		eventList.add(arrivalOne);
		
		Event stop = new Event(STOP, -1,  60);		
		eventList.add(stop);
		
		/*Extract first event*/
		
		Event currentEvent = eventList.poll();
		eventType = currentEvent.type;
		customerID = currentEvent.id;
		System.out.printf("Extracting event %d of type %d from head of event list\n", customerID, eventType);
		while(eventType != STOP){
			eventType = currentEvent.type;
			eventTime = currentEvent.time;
			customerID = currentEvent.id;
			clock = eventTime;
			System.out.printf("current event is %d\n", eventType);
			System.out.printf("arrivals: %d\n", arrivals);
			if (eventType == ARRIVAL){
				/*Check if any barber chair is free*/
				arrivals++;
				customerID++;
				if ( Fred.size() < 1 && George.size() < 1){
					double mu = chooseBarberAndService(customerID);
					Event newDepart = new Event(DEPARTURE, customerID, clock + getService(mu));		//generate service time, and departure event at time clock + service time
					System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
					eventList.add(newDepart);	
					state = IDLE;
				}
				else if (Fred.size() < 1){
					Fred.add(customerID);
					Event newDepart = new Event(DEPARTURE, customerID, clock + getService(3.0));
					System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
					eventList.add(newDepart);
					state = FRED;
				}
				
				else if (George.size() < 1){
					George.add(customerID);
					Event newDepart = new Event(DEPARTURE, customerID, clock + getService(2.0));
					System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
					eventList.add(newDepart);
					state = GEORGE;
				}
				
				else    //Busy
				{
					state = BUSY;
					if ( waitingRoom.size() < 3){
						waitingRoom.add(CUSTOMER);
						Event newDepart = new Event(DEPARTURE, customerID, clock + getService(2.0));
						System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
						eventList.add(newDepart);
					}
					else{
						System.out.printf("Oh no! The waiting room is full! customer %d has left the shop", customerID);
						customersLost++;
						System.out.println(customersLost);
					}
				}
				/*Schedule next arrival time*/
				if (arrivals < MAX_CUSTOMERS){
					Event nextArrival = new Event(ARRIVAL, arrivals, clock + getArrival(0.25));
					System.out.printf("Scheduling arrival event for customer %d at %f\n", arrivals, nextArrival.time);
					eventList.add(nextArrival);
			}
			
				;
			
			}
		
		
		else if (eventType == DEPARTURE){
				
				if (checkIfDepartureIsInChair(customerId)){
						if (Fred.peek() == customerID){
							Fred.remove();
							departures++;
							printf("Customer %d left barber shop at time %f\n", customerID, clock);
					}
						else if(George.peek() == customerID){
							George.remove();
							departures++;
							printf("Customer %d left barber shop at time %f\n", customerID, clock);
					
						}
				
				}
				
				
				else{
					if(waitingRoom.size > 0 && state == IDLE){
						waitingRoom.remove();
						double mu = chooseBarberAndService(customerID);
						Event newDepart = new Event(DEPARTURE, customerID, clock + getService(mu));
						System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
						eventList.add(newDepart);
					}
					else if(waitingRoom.size > 0 && state == GEORGE){
						waitingRoom.remove();
						Fred.add(customerID);
						Event newDepart = new Event(DEPARTURE, customerID, clock + getService(3.0));
						System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
						eventList.add(newDepart);
						state = BUSY;
					}
					else if(waitingRoom.size > 0 && state == FRED){
						waitingRoom.remove();
						GEORGE.add(customerID);
						Event newDepart = new Event(DEPARTURE, customerID, clock + getService(2.0));
						System.out.printf("Scheduling departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
						eventList.add(newDepart);
						state = BUSY;
					}
					else if(waitingRoom.size > 0 && state == BUSY){
						GEORGE.add(customerID);
						Event newDepart = new Event(DEPARTURE, customerID, clock + getService(2.0));
						System.out.printf("Barber is busy!! Scheduling new departure event for a customer %d at %f\n", newDepart.id, newDepart.time);
						eventList.add(newDepart);
					}
					}
			}
		
		
		
		currentEvent = eventList.poll();
		System.out.printf("Extracting event %d of type %d from head of event list\n", customerID, eventType);
		System.out.println("finished");
		/*
		for( int i = 0; i < 10000; i++){
			arrival = Exponential(0.25);
			System.out.println("Arrival time produced:" + arrival);
		}
		*/
	}
}
}
