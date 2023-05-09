package MainPack;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * used for communication among agents
 */
public class Mailer {

	// maps between agents and their mailboxes
	private HashMap<Integer, List<Message>> map = new HashMap<>();
	
	// send message @m to agent @receiver
	public void send(int receiver, Message m) {
		
		List<Message> l = map.get(receiver);
		
		synchronized (l) {
			l.add(m);
		}
	}

	// agent @receiver reads the first message from its mail box
	public Message readOne(int receiver) {
		
		List<Message> l = map.get(receiver);
		if (l.isEmpty()) {
			return null;
		}
		
		synchronized (l) {
			Message m = l.get(0);
			l.remove(0);
			return m;
		}
	}
	
	// only used for initialization
	public void put(int i) {
		List<Message> l= new ArrayList<Message>();
		this.map.put(i, l);
	}
}
