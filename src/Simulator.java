
/**
  * Name: Tao Chen
  * Course: CS4323
  * Assignment: CS 4323 Simulation Project, Phase I
  * Date: 03/07/2016
  * TA: Sarath Kumar Maddinani
  */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * This is the Runnable class for this project Simulator based on discrete time
 * in terms of VTUs run three simulations in once but separately
 * 
 * @author Chen
 *
 */
public class Simulator {
	// simulation duration time
	private static final int SIMULATION_TIME = 5000;
	// the following are statistics variables
	public int processedJobsCounter = 0; // total jobs that been processed
	// sum of all job's turnaround time
	public int turnaroundTimeCounter = 0;
	// sum of all job's processing time
	public int processingTimeCounter = 0;

	// in terms of VTUs 0---5000, all counters start
	// counting at 1000 VTUs, period 1000---4000VTUs
	private int timer = 0;
	// the only job waiting to be assigned memory
	private Job jobAtDoor = null;
	// current running job
	private Job jobInProcessing = null;
	private MemoryManager memManager = new MemoryManager();
	// a list of jobs that been assigned mem
	// but wait to be processed by cpu (Ready queue)
	List<Job> jobsList = new LinkedList<>();

	// phase2
	// pending list for jobs waiting to be assigned memory
	List<Job> jobsPendingList = new LinkedList<>();
	private static final int ROUNDROBIN_INTERVAL = 5;

	// use to collect info and print out at 4000 VTUs
	StringBuilder holeSizeFragInfo = new StringBuilder();

	// specify printing contents of pending list
	// for which placement strategy
	String printPlistStrategy = MemoryManager.BEST_FIT;

	// phase2

	/**
	 * constructor
	 */
	public Simulator() {
	}

	/**
	 * main function to run the project three strategies will be simulated one
	 * by one at a time
	 */
	public static void main(String[] args) {
		String[] compStrategies = { MemoryManager.COMP_REQ_DENIED, MemoryManager.COMP_250, MemoryManager.COMP_500 };
		String[] placementStrategies = { MemoryManager.FIRST_FIT, MemoryManager.BEST_FIT, MemoryManager.WORST_FIT };
		System.out.println("---Phase2---");
		// I print infomation both on stdout(screen) and into a file
		BufferedWriter out = null;

		// 3*3 = 9 runs
		// combine three compaction scenarios with
		// each of three placement strategies
		for (int i = 0; i < compStrategies.length; i++) {
			for (int j = 0; j < placementStrategies.length; j++) {
				try {
					Simulator simulator = new Simulator();
					System.out.println("\n---Compact Scenario: " + compStrategies[i] + "; Placement strategy: "
							+ placementStrategies[j] + "----Simulation Start---");
					out = new BufferedWriter(new FileWriter(compStrategies[i] + placementStrategies[j]
							+ Calendar.getInstance().getTimeInMillis() + ".txt"));
					simulator.simulate(compStrategies[i], placementStrategies[j], out);
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
				System.out.println("-------------End------------------");
			}
		}
	}

	/**
	 * based on discrete time use the specified strategy and output file writer
	 * 
	 * @param compStratefy
	 *            : current using compact scenario
	 * @param strategy
	 *            : current using placement strategy
	 * @param output
	 *            : stream to write to a file
	 * @throws IOException
	 */
	public void simulate(String compStrategy, String strategy, BufferedWriter output) throws IOException {
		int jobInterval = 0, previousTimer = 0, RRLeftTime = ROUNDROBIN_INTERVAL;
		// timer start from 0 to 5000 VTUs
		while (timer < SIMULATION_TIME) {
			if (jobInterval == 0) {
				newArriveJob();
				jobInterval = RandomNumGenerator.getRandomNum(1, 10);
			}
			previousTimer = timer;
			// update the job in processing
			if (null == jobInProcessing) {
				if (jobsList.isEmpty()) {
					newArriveJob();
					loadJobs2Mem(compStrategy, strategy);
					jobInterval = 0;
					continue;
				}
				// run the next job in the list which
				// has been assigned memory before
				getNextJob2Run(false);
				continue;
			} else {
				// time still needed for current job
				int timeLeft = jobInProcessing.getDuration() - jobInProcessing.getProcessedTime();

				// timeLeft is next smallest time point
				// job can be finished
				if (timeLeft <= jobInterval && timeLeft <= RRLeftTime) {
					RRLeftTime = 0;
					jobInterval -= timeLeft;
					timer += timeLeft;
					jobInProcessing.setProcessedTime(jobInProcessing.getProcessedTime() + timeLeft);
					timeLeft = 0;

					// jobInterval is next smallest time point
					// there will be a new arriving job
				} else if (jobInterval <= timeLeft && jobInterval <= RRLeftTime) {
					timeLeft -= jobInterval;
					RRLeftTime -= jobInterval;
					timer += jobInterval;
					jobInProcessing.setProcessedTime(jobInProcessing.getProcessedTime() + jobInterval);
					jobInterval = 0;

					// finished this round of round-robin
					// load next job in ready queue
				} else if (RRLeftTime <= timeLeft && RRLeftTime <= jobInterval) {
					timeLeft -= RRLeftTime;
					jobInterval -= RRLeftTime;
					timer += RRLeftTime;
					jobInProcessing.setProcessedTime(jobInProcessing.getProcessedTime() + RRLeftTime);
					RRLeftTime = 0;
				} else {
					System.out.println("Error-----------****");
				}
				if (timeLeft == 0) {
					finishJob();
					getNextJob2Run(true);
					// 1. loaded after a process terminates
					loadJobs2Mem(compStrategy, strategy);
				}

				if (RRLeftTime == 0) {
					RRLeftTime = ROUNDROBIN_INTERVAL;
					if (timeLeft != 0) {
						getNextJob2Run(false);
					}
				}
			}

			// compaction at every 250 VTUs
			// only when the current strategy is compact at every 250 VTUs
			if (timer / 250 > previousTimer / 250) {
				memManager.compaction(MemoryManager.COMP_250, compStrategy);
				// 3. load jobs into memory when a call to compaction
				loadJobs2Mem(compStrategy, strategy);
			}
			// compaction at every 500 VTUs
			// only when the current strategy is compact at every 250 VTUs
			if (timer / 500 > previousTimer / 500) {
				memManager.compaction(MemoryManager.COMP_500, compStrategy);
				// 3. load jobs into memory when a call to compaction
				loadJobs2Mem(compStrategy, strategy);
			}

			// discrete time to print information
			// print info both on screen and to a file
			// between 1000 and 4000 VTUs
			if (timer >= 1000 && timer <= 4000) {
				if (timer / 100 > previousTimer / 100) {
					holeSizeFragInfo.append("\nTime: " + ((previousTimer / 100) * 100 + 100));
					holeSizeFragInfo.append(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					holeSizeFragInfo.append(", Average Hole Size: " + memManager.averageHoleSize());
				}
			}

			// print contents of pending list at 1000, 2000,
			// 3000, 4000, 5000 VTUs only for one of the placement strategy
			if ((timer / 1000 > previousTimer / 1000) && strategy.equals(printPlistStrategy)) {
				System.out.print("\nTime: " + ((previousTimer / 1000) * 1000 + 1000));
				System.out.println("  Pending list: (size: " + jobsPendingList.size());
				for (Iterator iterator = jobsPendingList.iterator(); iterator.hasNext();) {
					Job job = (Job) iterator.next();
					System.out.println(job);
				}

				output.write("\nTime: " + ((previousTimer / 1000) * 1000 + 1000));
				output.write("  Pending list: (size: " + jobsPendingList.size());
				for (Iterator iterator = jobsPendingList.iterator(); iterator.hasNext();) {
					Job job = (Job) iterator.next();
					output.newLine();
					output.write(job.toString());
				}
			}

			// print average time at 4000 VTUs
			if (previousTimer < 4000 && timer >= 4000) {
				if (timer != 4000) {
					holeSizeFragInfo.append("\nTime: " + ((previousTimer / 100) * 100 + 100));
					holeSizeFragInfo.append(", External Fragmentation(bytes): " + memManager.getExternalFrag());
					holeSizeFragInfo.append(", Average Hole Size (KB): " + memManager.averageHoleSize());
				}

				System.out.print("\nAverage turnaround time(VTUs):"
						+ (Math.floor(turnaroundTimeCounter * 100.0 / processedJobsCounter) / 100));
				System.out.print(", Average waiting time(VTUs):"
						+ (Math.floor((turnaroundTimeCounter - processingTimeCounter) * 100.0 / processedJobsCounter)
								/ 100));
				System.out.print(", Average processing time(VTUs):"
						+ (Math.floor(processingTimeCounter * 100.0 / processedJobsCounter) / 100));
				System.out.println("");

				System.out.println(holeSizeFragInfo);
				output.write(holeSizeFragInfo.toString());
			}

		}
	}

	private void newArriveJob() {
		if (null != jobAtDoor) {
			// Maximum size for pending list is 100
			if (jobsPendingList.size() < 100) {
				jobsPendingList.add(jobAtDoor);
			}
		}
		jobAtDoor = Job.createNewJob();
	}

	private void finishJob() {
		// release memory
		memManager.releaseMem(jobInProcessing);
		// jobInProcessing.setEndTime(timer + (jobInProcessing.getDuration() -
		// jobInProcessing.getProcessedTime()));
		jobInProcessing.setEndTime(timer);
		// only count the jobs during the
		// smapling period 1000---4000 VTUs
		if (jobInProcessing.getStartTime() >= 1000 && jobInProcessing.getEndTime() <= 4000) {
			processedJobsCounter++;
			turnaroundTimeCounter += (jobInProcessing.getEndTime() - jobInProcessing.getStartTime());
			if ((jobInProcessing.getEndTime() - jobInProcessing.getStartTime()) < jobInProcessing.getDuration()) {
				System.out.println("Error Job-- " + jobInProcessing);
			}
			processingTimeCounter += jobInProcessing.getDuration();
		}

	}

	/**
	 * get next job for running in roung-robin and delete the current job if it
	 * was finished
	 */
	private boolean getNextJob2Run(boolean delete) {
		if (!jobsList.isEmpty()) {
			if (null == jobInProcessing) {
				jobInProcessing = jobsList.get(0);
			} else {
				int jobIndex = jobsList.indexOf(jobInProcessing);
				int nextIndex = jobIndex + 1;// get next job in ready queue
				nextIndex = nextIndex % jobsList.size();
				jobInProcessing = jobsList.get(nextIndex);
				if (delete) {
					jobsList.remove(jobIndex);
				}
			}
			jobInProcessing.setStatus(JobStatus.PROCESSING);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * try to load jobs(in pending list and at door) into memory
	 */
	private void loadJobs2Mem(String compStrategy, String strategy) {
		// jobs in pending list have the priority over job at door
		for (Iterator iterator = jobsPendingList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			memManager.assignMem(strategy, job);

			if (JobStatus.ASSIGNED.equals(job.getStatus())) {
				// jobs started when Assigned
				job.setStartTime(timer);
				// add job to in mem job list(Ready queue)
				// insert into position before the current pointer
				if (null == jobInProcessing) {
					jobsList.add(job);
				} else {
					jobsList.add(jobsList.indexOf(jobInProcessing), job);
				}
				// if assigned, remove from pending list
				iterator.remove();
			} else if (JobStatus.WAIT.equals(job.getStatus())) {
				// compaction when 1. a memory request is denied
				memManager.compaction(MemoryManager.COMP_REQ_DENIED, compStrategy);
			}
		}

		// try assign mem to job at door after the pending list
		if (null != jobAtDoor) {
			memManager.assignMem(strategy, jobAtDoor);
			if (JobStatus.ASSIGNED.equals(jobAtDoor.getStatus())) {
				// jobs started when Assigned
				jobAtDoor.setStartTime(timer);
				// add job to in mem job list(Ready queue)
				// insert into position before the current pointer
				if (null == jobInProcessing) {
					jobsList.add(jobAtDoor);
					jobInProcessing = jobAtDoor;
				} else {
					jobsList.add(jobsList.indexOf(jobInProcessing), jobAtDoor);
				}
				jobAtDoor = null;
			} else if (JobStatus.WAIT.equals(jobAtDoor.getStatus())) {
				// Maximum size for pending list is 100
				if (jobsPendingList.size() < 100) {
					jobsPendingList.add(jobAtDoor);
					jobAtDoor = null;
				}
				// compaction when 1. a memory request is denied
				memManager.compaction(MemoryManager.COMP_REQ_DENIED, compStrategy);
			}
		}

	}
}
