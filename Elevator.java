import java.util.ArrayList;


public class Elevator {
	public int floor;
	public int state;
	ArrayList passengers = new ArrayList();
	public Elevator(int floor){
		this.floor = floor;
		passengers.add(null);
	}
	public void addPassenger(int id, Person passenger){
		passengers.add(id, passenger);;
	}
}
