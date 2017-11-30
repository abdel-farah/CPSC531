import java.util.ArrayList;
import java.util.List;


public class Elevator {
	public int floor;
	public int state;
	List<Person> passengers = new ArrayList<Person>();
	public Elevator(int floor){
		this.floor = floor;
		state = 0;
		passengers.add(null);
		//passengers.ensureCapacity(1000000);
	}
	public void addPassenger(int id, Person passenger){
		passengers.add(id, passenger);;
	}
}
