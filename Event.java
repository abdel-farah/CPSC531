import java.util.Comparator;
public class Event implements Comparable<Event> {

	int type;
	double time;
	int id;
    public int getType(){
	return type;
    }
    public double getTime(){
	return time;
    }
    public Event(int type, int id, double time){
		this.type = type;
		this.time = time;
		this.id = id;
	}
    
    public int compareTo(Event e){
	if (this.getTime() > e.getTime()){
	    return 1;
	}else if(this.getTime() < e.getTime()){
	    return -1;
	}
	return 0;
	
    }

}