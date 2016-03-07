import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Calendar;
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
		BufferedWriter out = null;
		//simulate first fit
		Simulator firstFitSimulator = new Simulator();
		System.out.println("First Fit Simulation Begins");
		try {
			out = new BufferedWriter
			         (new FileWriter("FirstFit_Result_" + Calendar.getInstance().getTimeInMillis() + ".txt"));
			firstFitSimulator.simulate(MemoryManager.FIRST_FIT, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (null != out) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("\nFirst Fit Simulation Ended\n");
		System.out.println("----------------------------------------------------------------------------");
		//simulate best fit
		Simulator bestFitSimulator = new Simulator();
		System.out.println("Best Fit Simulation Begins");
		try {
			out = new BufferedWriter
			         (new FileWriter("BestFit_Result_" + Calendar.getInstance().getTimeInMillis() + ".txt" ));
			bestFitSimulator.simulate(MemoryManager.BEST_FIT, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (null != out) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("\nBest Fit Simulation Ended\n");
		System.out.println("----------------------------------------------------------------------------");
		// simulate worst fit
		Simulator worstFitSimulator = new Simulator();
		System.out.println("Worst Fit Simulation Begins");
		try {
			out = new BufferedWriter(new FileWriter("WorstFit_Result_" + Calendar.getInstance().getTimeInMillis() + ".txt"));
			worstFitSimulator.simulate(MemoryManager.WORST_FIT, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("\nWorst Fit Simulation Ended");
	}
	
	//based on discrete time
	public void simulate(String strategy, BufferedWriter output) throws IOException{
		int jobInterval = 0, temp = 0;
		//timer start from 0 to 5000 VTUs
		while(timer < SIMULATION_TIME){
			jobInterval = RandomNumGenerator.getRandomNum(1, 10);
			//update the job in processing
			if(null == jobInProcessing){
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
				jobInProcessing.setStatus(JobStatus.PROCESSING);
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
					if ((jobInProcessing.getEndTime() - jobInProcessing.getStartTime()) < jobInProcessing.getDuration()) {
						System.out.println("Error Job-- " + jobInProcessing);
					}
					processingTimeCounter += jobInProcessing.getDuration();
				}
				//current job finished and begin to process next job 
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
					temp =  timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()); //next job start at the end of current job
					jobInProcessing = jobsList.remove(0); //run the next job in the list which has been assigned memory before
					jobInProcessing.setStatus(JobStatus.PROCESSING);
					jobInProcessing.setProcessedTime(timer + jobInterval - temp);
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
					System.out.print("\nTime: " + ((timer/100)*100 + 100));
					System.out.print(", Storage Utilization: " + Math.floor(memManager.storageUtilization()*10000)/100 + "%");
					System.out.print(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					System.out.print(", Average Hole Size: " + memManager.averageHoleSize());
					output.newLine();
					output.write("Time: " + ((timer/100)*100 + 100));
					output.write(", Storage Utilization: " + Math.floor(memManager.storageUtilization()*10000)/100 + "%");
					output.write(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					output.write(", Average Hole Size: " + memManager.averageHoleSize());
					
				}
			}
			
			//print rejected jobs at 1000, 2000, 3000, 4000, 5000 VTUs
			if (temp/1000 > timer/1000) {
				System.out.print("\nTime: " + ((timer/1000)*1000 + 1000));
				System.out.print("  Rejected jobs: " + memManager.getRejectedJobs());
				
				output.write("\nTime: " + ((timer/1000)*1000 + 1000));
				output.write("  Rejected jobs: " + memManager.getRejectedJobs());
			}
			
			//print average time at 4000 VTUs
			if (timer < 4000 && temp >= 4000) {
				if (temp != 4000) {
					System.out.print("\nTime: " + ((timer/100)*100 + 100));
	//				memManager.printHolesList();
					System.out.print(", Storage Utilization: " + Math.floor(memManager.storageUtilization()*10000)/100 + "%");
					System.out.print(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					System.out.print(", Average Hole Size (KB): " + memManager.averageHoleSize());
				}
				
				System.out.print("\nAverage turnaround time(VTUs):"+ (Math.floor(turnaroundTimeCounter*100.0/processedJobsCounter)/100));
				System.out.print(", Average waiting time(VTUs):"+ (Math.floor((turnaroundTimeCounter - processingTimeCounter)*100.0/processedJobsCounter)/100));
				System.out.print(", Average processing time(VTUs):"+ (Math.floor(processingTimeCounter*100.0/processedJobsCounter)/100));
				System.out.println("");
				
				if (temp != 4000) {
					output.newLine();
					output.write("Time: " + ((timer/100)*100 + 100));
					output.write(", Storage Utilization: " + Math.floor(memManager.storageUtilization()*10000)/100 + "%");
					output.write(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					output.write(", Average Hole Size (KB): " + memManager.averageHoleSize());
				}
				output.newLine();
				output.write("Average turnaround time(VTUs):" + (Math.floor(turnaroundTimeCounter*100.0/processedJobsCounter)/100));
				output.write(", Average waiting time(VTUs):" + (Math.floor((turnaroundTimeCounter - processingTimeCounter)*100.0/processedJobsCounter)/100));
				output.write(", Average processing time(VTUs):" + (Math.floor(processingTimeCounter*100.0/processedJobsCounter)/100));
				output.newLine();
			}
			
			//add timer
			timer += jobInterval;
			
			if (null == jobAtDoor) {
				jobAtDoor = Job.createNewJob();
			}
			memManager.assignMem(strategy, jobAtDoor);
			
			while (JobStatus.REJECTED.equals(jobAtDoor.getStatus())) {
				jobAtDoor = Job.createNewJob();
				memManager.assignMem(strategy, jobAtDoor);
			}
			
//			System.out.println(jobAtDoor);
			if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())){
				jobAtDoor.setStartTime(timer);
				jobsList.add(jobAtDoor);//add job to list
				jobAtDoor = null;
			}
//			System.out.println(jobInProcessing);
//			memManager.printHolesList();
		}
	}
	
	private Job getNextJobInMem(){
		if (jobsList.size() > 0) 
			return jobsList.remove(0);
		else
			return null;
	}

}
