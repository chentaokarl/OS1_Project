import java.security.PrivilegedActionException;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */

/**
 * simulator based on discrete time in terms of VTUs
 * @author Chen
 *
 */
public class Simulator {
	//statistics variables
	public int jobsCount = 0; //total jobs that been processed 
	public int turnaroundTimeCounter = 0; //sum of all job's turnaround time
	public int processingTimeCounter = 0; //sum of all job's processing time
	
	private int timer = 0; //in terms of VTUs 0---5000, all counters start at 1000 VTUs, period 1000---4000VTUs
	private Job jobAtDoor = null; //the only job waiting to be assigned memory
	private Job jobProcessing = null; // current running job
	private MemoryManager memManager = new MemoryManager();
	List<Job> jobsList = new LinkedList<>();//a list of jobs that been assigned mem but wait to be processed by cpu
	

	/**
	 * 
	 */
	public Simulator() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	//based on discrete time
	public void simulate(){
		//
	}
	
	private Job getNextJobInMem(){
		if (jobsList.size() > 0) 
			return jobsList.remove(0);
		else
			return null;
	}

}
