import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


/**
 * simulator based on discrete time in terms of VTUs
 * run three simulations in once but separately
 * @author Chen
 *
 */
public class Simulator {
	//simulation duration time
	private static final int SIMULATION_TIME = 5000; 
	//the following are statistics variables
	public int processedJobsCounter = 0; //total jobs that been processed 
	//sum of all job's turnaround time
	public int turnaroundTimeCounter = 0; 
	 //sum of all job's processing time
	public int processingTimeCounter = 0;
	
	//in terms of VTUs 0---5000, all counters start
	//counting at 1000 VTUs, period 1000---4000VTUs
	private int timer = 0; 
	//the only job waiting to be assigned memory
	private Job jobAtDoor = null; 
	// current running job
	private Job jobInProcessing = null; 
	private MemoryManager memManager = new MemoryManager();
	//a list of jobs that been assigned mem 
	//but wait to be processed by cpu
	List<Job> jobsList = new LinkedList<>();
	

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
		Simulator firstFitSim = new Simulator();
		System.out.println("First Fit Simulation Begins");
		try {
			out = new BufferedWriter(new FileWriter("FirstFit_Result_" 
					 	+ Calendar.getInstance().getTimeInMillis() + ".txt"));
			firstFitSim.simulate(MemoryManager.FIRST_FIT, out);
		} catch (IOException e) {
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
		System.out.println("\nFirst Fit Simulation Ended\n");
		System.out.println("-------------------------------");
		//simulate best fit
		Simulator bestFitSim = new Simulator();
		System.out.println("Best Fit Simulation Begins");
		try {
			out = new BufferedWriter(new FileWriter("BestFit_Result_" 
						+ Calendar.getInstance().getTimeInMillis() + ".txt" ));
			bestFitSim.simulate(MemoryManager.BEST_FIT, out);
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
		System.out.println("-----------------------------");
		
		// simulate worst fit
		Simulator worstFitSim = new Simulator();
		System.out.println("Worst Fit Simulation Begins");
		try {
			out = new BufferedWriter(new FileWriter("WorstFit_Result_" 
						+ Calendar.getInstance().getTimeInMillis() + ".txt"));
			worstFitSim.simulate(MemoryManager.WORST_FIT, out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
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
					memManager.assignMem(strategy, 
										jobAtDoor);
					
					while (JobStatus.REJECTED.equals(jobAtDoor.getStatus())) {
						jobAtDoor = Job.createNewJob();
						memManager.assignMem(strategy, jobAtDoor);
					}
					
					if (JobStatus.ASSIGNED.equals(
							jobAtDoor.getStatus())){
						//jobs started when Assigned
						jobAtDoor.setStartTime(timer);
						//add job to in mem job list
						jobsList.add(jobAtDoor);
						//clean job at door
						jobAtDoor = null;
					}
					continue;
				}
				//run the next job in the list which 
				//has been assigned memory before
				jobInProcessing = jobsList.remove(0); 
				jobInProcessing.setStatus(JobStatus.PROCESSING);
			}
			//check if job in processing can be finished
			//during this job arrive interval VTUs
			if ((jobInProcessing.getProcessedTime() + jobInterval) >= jobInProcessing.getDuration()) {
				//release memory
				memManager.releaseMem(jobInProcessing);
				jobInProcessing.setEndTime(timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()));
				//only count the jobs during the 
				//smapling period 1000---4000 VTUs
				if(jobInProcessing.getStartTime() >= 1000 && jobInProcessing.getEndTime() <= 4000 ){
					processedJobsCounter++;
					turnaroundTimeCounter += (jobInProcessing.getEndTime() - jobInProcessing.getStartTime());
					if ((jobInProcessing.getEndTime() - jobInProcessing.getStartTime()) < jobInProcessing.getDuration()) {
						System.out.println("Error Job-- " + jobInProcessing);
					}
					processingTimeCounter += jobInProcessing.getDuration();
				}
				
				//current job finished and 
				//begin to process next job 
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
							//add job to list
							jobsList.add(jobAtDoor);
							jobAtDoor = null;
						}
						continue;
					}
					//next job start at the end of current job
					temp =  timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()); 
					//run the next job in the list which
					//has been assigned memory before
					jobInProcessing = jobsList.remove(0); 
					jobInProcessing.setStatus(JobStatus.PROCESSING);
					if ((timer + jobInterval - temp) < jobInProcessing.getDuration()) {
						jobInProcessing.setProcessedTime(timer + jobInterval - temp);
					}else if ((timer + jobInterval - temp) >= jobInProcessing.getDuration()) {
						//release memory
						memManager.releaseMem(jobInProcessing);
						jobInProcessing.setEndTime(timer + (jobInProcessing.getDuration() - jobInProcessing.getProcessedTime()));
						//only count the jobs during the 
						//smapling period 1000---4000 VTUs
						if(jobInProcessing.getStartTime() >= 1000 && jobInProcessing.getEndTime() <= 4000 ){
							processedJobsCounter++;
							turnaroundTimeCounter += (jobInProcessing.getEndTime() - jobInProcessing.getStartTime());
							if ((jobInProcessing.getEndTime() - jobInProcessing.getStartTime()) < jobInProcessing.getDuration()) {
								System.out.println("Error Job-- " + jobInProcessing);
							}
							processingTimeCounter += jobInProcessing.getDuration();
						}
						if ((timer + jobInterval - temp) == jobInProcessing.getDuration()) {
							jobInProcessing = null;
						}else{
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
									//add job to list
									jobsList.add(jobAtDoor);
									jobAtDoor = null;
								}
								continue;
							}
							jobInProcessing = jobsList.remove(0); 
							jobInProcessing.setStatus(JobStatus.PROCESSING);
							jobInProcessing.setProcessedTime(timer + jobInterval - temp - jobInProcessing.getDuration());
						}
					}
				}else{
					jobInProcessing = null;
				}
			}else{
				jobInProcessing.setProcessedTime(jobInProcessing.getProcessedTime() + jobInterval);
			}
			
			//discrete time to print information 
			//between 1000 and 4000 VTUs
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
			
			//print rejected jobs at 1000, 2000, 
			//3000, 4000, 5000 VTUs
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
			
			if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())){
				jobAtDoor.setStartTime(timer);
				jobsList.add(jobAtDoor);//add job to list
				jobAtDoor = null;
			}
//			System.out.println(jobInProcessing);
		}
	}
	
	private Job getNextJobInMem(){
		if (jobsList.size() > 0) 
			return jobsList.remove(0);
		else
			return null;
	}

}
