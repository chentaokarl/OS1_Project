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
	private static final int SIMULATION_TIME = 5000;
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
		Simulator simulator = new Simulator();
		
		for (int i = 0; i < 10; i++) {
			simulator.jobsList.add(Job.createNewJob());
		}
		
		for (Job job : simulator.jobsList) {
			System.out.println(job.toString());
		}
	}
	
	//based on discrete time
	public void simulate(){
		int jobInterval = RandomNumGenerator.getRandomNum(1, 10);
		timer += jobInterval; // first time to generate a job
		Job jobAtDoor = Job.createNewJob();
		jobsList.add(jobAtDoor);//add job to job list
		
		while(timer < SIMULATION_TIME){
			
		}
	}
	
	private Job getNextJobInMem(){
		if (jobsList.size() > 0) 
			return jobsList.remove(0);
		else
			return null;
	}

}
