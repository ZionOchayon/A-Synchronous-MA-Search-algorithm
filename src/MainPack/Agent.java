package MainPack;

import java.util.ArrayList;
import java.util.HashMap;

public class Agent implements Runnable {
	
	// on this assignment we chose to perform ** Back Jumping ** algorithm
	// the variable lastConstraint represent the the last agent who's constraint and we had a conflict
	// if there is no such it will be id-1
	
	private int domainSize,agents,id,lastConstraint,assignment;
	private Mailer mailer;
	private HashMap<Integer, ConsTable> constraints;
	private ArrayList<Integer> currentDomain;
	private ArrayList<VarTuple> explanations;
	
	public Agent(int id, Mailer mailer, HashMap<Integer, ConsTable> constraints, int n, int d) {
		this.id = id;
		this.mailer = mailer;
		this.constraints = constraints;
		this.domainSize = d;
		this.agents = n;
		this.currentDomain = resetDomain();
		this.lastConstraint = -1;
		this.explanations = new ArrayList<VarTuple>();
	}
		
	@Override
	public void run() {
		if(id == 0) {
			assignment = currentDomain.remove(0);
			Message message = new CPA(new VarTuple(id,assignment),agents);
			mailer.send(id+1, message);
		}
		
		while(true) {
			Message inboxMsg = mailer.readOne(id);
			if(inboxMsg != null) {
				if(inboxMsg instanceof CPA) {
					// we reset the currentDomain and lastConstraint here because we perform Back Jumping algorithm
					lastConstraint = -1;
					currentDomain = resetDomain();
					findAssignmentAndSendMassage(inboxMsg);				
				} else if(inboxMsg instanceof BackTrack) {
					if(currentDomain.size() > 0){				
						findAssignmentAndSendMassage(inboxMsg);
					} else if(id != 0) {
						// if the current domain is empty and it was no conflicts so lastConstraint should be id-1
						if(lastConstraint == -1) {
							lastConstraint = id-1;
						}
						((BackTrack) inboxMsg).getCPA().removeAssignmentsToId(lastConstraint);
						mailer.send(lastConstraint, (Message) new BackTrack(((BackTrack) inboxMsg).getCPA()));
					} else {
						for(int i=0;i<agents;i++) {
							mailer.send(i, (Message) new Message.NoSolution());
						}
					}	
				} else if(inboxMsg instanceof Message.Solution || inboxMsg instanceof Message.NoSolution) {
					break;
				}
				
			}
		}	
	}
	
	public ArrayList<Integer> resetDomain() {
		ArrayList<Integer> domain = new ArrayList<Integer>();
		for(int i=0;i<domainSize;i++) {
			domain.add(i);
		}
		return domain;	
	}
	
	// this function uses to find assignment, send the right massage and also knows how to handle CPA messages and backtrack messages
	private void findAssignmentAndSendMassage(Message inboxMsg) {
		
		Boolean conflict = false;	
		VarTuple agentView = null;
		
		// if its backtrack extract the CPA
		if(inboxMsg instanceof BackTrack) {
			inboxMsg = ((BackTrack) inboxMsg).getCPA();
		}
		
		// find assignment
		while(!currentDomain.isEmpty()) {
			conflict = false;
			assignment = currentDomain.remove(0);
			for(Integer i : constraints.keySet()) {
				if(i<id) {
					CPA.CCs++;	
					agentView = ((CPA) inboxMsg).getAssignment(i);
					if(!constraints.get(i).getTable()[agentView.getJ()][assignment]) {
						if(agentView.getI() > lastConstraint) {
							lastConstraint = agentView.getI();
						}
						explanations.add(agentView);
						conflict = true;
					}
				}
			}
			// if there is no conflict, add assignment to the CPA assignments list and send CPA to the next agent. if its the last agent send solution massage
			if(!conflict) {
				((CPA) inboxMsg).addAssignment(id,new VarTuple(id,assignment));
				if(id != agents-1) {
					mailer.send(id+1, inboxMsg);
				}else {
					for(int i=0;i<agents;i++) {
						mailer.send(i, (Message) new Message.Solution());
					}
				}
				break;
			}
		}
		// if currentDomain is empty and there was a conflict it means that we need to send backtrack
		// we delete from the CPA assignments all assignments of agents whose id is >= then lastConstraint agent and send backtrack to lastConstraint
		if(currentDomain.isEmpty() && conflict) {
			((CPA) inboxMsg).removeAssignmentsToId(lastConstraint);
			mailer.send(lastConstraint, (Message) new BackTrack(inboxMsg));
		}
	}
}