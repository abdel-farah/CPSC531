import java.util.Random;


public class Person {
	public int requestedFloor;
	public int personID;
	public Person(int id){
		this.personID = id;
		
		Random r = new Random();
		int Low = 1;
		int High = 7;
		int Result = r.nextInt(High-Low) + Low;
		this.requestedFloor = Result;
	}
}
