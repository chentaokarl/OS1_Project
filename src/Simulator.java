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
	private static final int SIMULATION_TIME = 5000; //simulation duration time
	//statistics variables
	public int processedJobsCounter = 0; //total jobs that been processed 
	public int turnaroundTimeCounter = 0; //sum of all job's turnaround time
	public int processingTimeCounter = 0; //sum of all job's processing time
	
	private int timer = 0; //in terms of VTUs 0---5000, all counters start at 1000 VTUs, period 1000---4000VTUs
	private Job jobAtDoor = null; //the only job waiting to be assigned memory
	private Job jobInProcessing = null; // current running job
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
		
		simulator.simulate(MemoryManager.FIRST_FIT);
//		simulator.simulate(MemoryManager.BEST_FIT);
//		simulator.simulate(MemoryManager.WORST_FIT);
	}
	
	//based on discrete time
	public void simulate(String strategy){
		int jobInterval = 0, temp = 0;
		
		//create a job at time 0 VTUs
		
		
		while(timer < SIMULATION_TIME){
			jobInterval = RandomNumGenerator.getRandomNum(1, 10);
			//update the job in processing
			if(null == jobInProcessing){
				if (jobsList.isEmpty()) {
					timer += jobInterval;
					if (null == jobAtDoor) {
						jobAtDoor = Job.createNewJob();
						System.out.println(jobAtDoor.toString());
					}
					memManager.assignMem(strategy, jobAtDoor);
					
					while (JobStatus.REJECTED.equals(jobAtDoor.getStatus())) {
						jobAtDoor = Job.createNewJob();
						memManager.assignMem(strategy, jobAtDoor);
					}
					
					if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())){
						jobAtDoor.setStartTime(timer);
						jobsList.add(jobAtDoor);//add job to list
						jobAtDoor = null;
					}
					continue;
				}
				jobInProcessing = jobsList.remove(0); //run the next job in the list which has been assigned memory before
			}
			//check if job in processing can be finished during this job arrive interval VTUs
			if ((jobInProcessing.getProcessedTime() + jobInterval) >= jobInProcessing.getDuration()) {
				//release memory
				memManager.releaseMem(jobInProcessing);
				jobInProcessing.setEndTime(timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()));
				
				//only count the jobs during the smapling period 1000---4000 VTUs
				if(jobInProcessing.getStartTime() >= 1000 && jobInProcessing.getEndTime() <= 4000 ){
					processedJobsCounter++;
					turnaroundTimeCounter += (jobInProcessing.getEndTime() - jobInProcessing.getStartTime());
					processingTimeCounter += jobInProcessing.getDuration();
				}
				
				if ((jobInProcessing.getProcessedTime() + jobInterval) - jobInProcessing.getDuration() > 0) {
					
					if (jobsList.isEmpty()) {
						timer += jobInterval;
						if (null == jobAtDoor) {
							jobAtDoor = Job.createNewJob();
						}
						memManager.assignMem(strategy, jobAtDoor);
						
						while (JobStatus.REJECTED.equals(jobAtDoor.getStatus())) {
							jobAtDoor = Job.createNewJob();
							memManager.assignMem(strategy, jobAtDoor);
						}
						
						if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())){
							jobAtDoor.setStartTime(timer);
							jobsList.add(jobAtDoor);//add job to list
							jobAtDoor = null;
						}
						continue;
					}
					jobInProcessing = jobsList.remove(0); //run the next job in the list which has been assigned memory before
					jobInProcessing.setStartTime(timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()));
					jobInProcessing.setStatus(JobStatus.PROCESSING);
				}else{
					jobInProcessing = null;
				}
					
					
			}else{
				jobInProcessing.setProcessedTime(jobInProcessing.getProcessedTime() + jobInterval);
			}
			
			
			//check to see if it is the time to print information during 1000 and 4000 VTUs
			temp = timer + jobInterval;
			if (temp >= 1000 && temp <= 4000) {
				if (temp/100 > timer/100) {
					System.out.println("Time: " + ((timer/100)*100 + 100));
					System.out.println("Storage Utilization: " + memManager.storageUtilization());
					System.out.println("External Fragmentation(bytes): " + memManager.getExternalFrag());
					System.out.println("Average Hole Size: " + memManager.averageHoleSize());
				}
			}
			
			//print rejected jobs at 1000, 2000, 3000, 4000, 5000 VTUs
			if (temp/1000 > timer/1000) {
				System.out.println("Time: " + ((timer/1000)*1000 + 1000));
				System.out.println("Rejected jobs: " + memManager.getRejectedJobs());
			}
			
			//print average time at 4000 VTUs
			if (timer < 4000 && temp >= 4000) {
				System.out.println("Time: " + ((timer/100)*100 + 100));
				System.out.println("Storage Utilization: " + memManager.storageUtilization());
				System.out.println("External Fragmentation(bytes): " + memManager.getExternalFrag());
				System.out.println("Average Hole Size: " + memManager.averageHoleSize());
				
				System.out.println("Average turnaround time:"+(turnaroundTimeCounter*1.0/processedJobsCounter));
				System.out.println("Average waiting time:"+((turnaroundTimeCounter - processingTimeCounter)*1.0/processedJobsCounter));
				System.out.println("Average processing time:"+(processingTimeCounter*1.0/processedJobsCounter));
			}
			
			timer += jobInterval;
			
			if (null == jobAtDoor) {
				jobAtDoor = Job.createNewJob();
			}
			memManager.assignMem(strategy, jobAtDoor);
			
			while (JobStatus.REJECTED.equals(jobAtDoor.getStatus())) {
				jobAtDoor = Job.createNewJob();
				memManager.assignMem(strategy, jobAtDoor);
			}
			
			if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())){
				jobAtDoor.setStartTime(timer);
				jobsList.add(jobAtDoor);//add job to list
				jobAtDoor = null;
			}
		}
	}
	
	private Job getNextJobInMem(){
		if (jobsList.size() > 0) 
			return jobsList.remove(0);
		else
			return null;
	}

}
