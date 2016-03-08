import java.awt.JobAttributes;
import java.util.ArrayList;
import java.util.List;

import org.omg.PortableInterceptor.USER_EXCEPTION;

/**
 * This class used as Memory Management unit in system
 * it assigns and trace memory holes
 * @author Tao
 *
 */
public class MemoryManager {
	// three stategies
	public static final String FIRST_FIT = "firstfit";
	public static final String BEST_FIT = "bestfit";
	public static final String WORST_FIT = "worstfit";

	// total memory capacity in KB, 2000KB
	public int totalMem = 2000;
	// memory reserved for operating system
	public int osReservedMem = 200;
	// initial available memory for jobs
	public int initMem = totalMem - osReservedMem;
	// indicate memory left that not been assigned to a hole
	private int memNotAssigned2Hole = initMem;
	// trace all memory holes
	private List<Hole> memHolesList = new ArrayList<>();
	// count the amount of rejected jobs
	private int rejectedJobs = 0;
	// count the amount of external fragmentation in KB
	private int externalFrag = 0;
	// trace fragmentation holes which generated as fragmentation happened
	private List<Hole> fragHolesList = new ArrayList<>();

	private static int nextHoleID = 1;

	/***
	 * user can specify their total memory and os reserved memory
	 * 
	 * @param totalMem
	 * @param osReservedMem
	 */
	public MemoryManager(int totalMem, int osReservedMem) {
		this.totalMem = totalMem;
		this.osReservedMem = osReservedMem;
		this.initMem = totalMem - osReservedMem;
	}

	public MemoryManager() {
	}

	/**
	 * Assign memory to the job using the strategy user specified
	 * 
	 * @param strategy
	 * @param job
	 */
	public void assignMem(String strategy, Job job) {
		if (FIRST_FIT.equals(strategy)) {
			firstFit(job);
		} else if (BEST_FIT.equals(strategy)) {
			bestFit(job);
		} else if (WORST_FIT.equals(strategy)) {
			worstFit(job);
		}
	}

	// fisrst fit strategy
	public void firstFit(Job job) {
		/**
		 * greaterThanAll used to indicate if the size job requested is greater
		 * than all hole size or not
		 */
		boolean greaterThanAll = true;
		// use to record the first fit hole index
		int firstFitHoleIndex = -1;

		// find the first fit hole among the holes we have and
		// also compare the size job requested with all the hole size
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				greaterThanAll = false;
				if (null == hole.job) {
					firstFitHoleIndex = memHolesList.indexOf(hole);
					job.setStatus(JobStatus.ASSIGNED);
					hole.job = job;
					break;
				}
			}
		}
		// if the job didn't be assigned to a current hole, try to give it
		// the mem that not be assigned as hole and create a new hole
		if (JobStatus.ASSIGNED != job.getStatus()) {
			if (job.getSize() <= memNotAssigned2Hole) {
				createNewHole(job);
			} else if (greaterThanAll) {
				// Rejected when greater than all current hole sizes
				// and the available memory
				job.setStatus(JobStatus.REJECTED);
				rejectedJobs++;
			} else {
				// wait for free hole
				job.setStatus(JobStatus.WAIT);
			}
		} else {
			/**
			 * External fragmentation exists when there is enough 
			 * total memory space to satisfy a request but
			 * available spaces are not contiguous
			 */
			if (job.getSize() < memHolesList.get(firstFitHoleIndex).size) {
				// external fragmentation happened
				Hole tempNewHole = createNewHole(memHolesList.get(firstFitHoleIndex).size - job.getSize());
				memHolesList.get(firstFitHoleIndex).size = job.getSize();
				fragHolesList.add(tempNewHole);
			}
		}
	}

	// best fit strategy
	public void bestFit(Job job) {
		/**
		 * greaterThanAll used to indicate if the size job requested is greater
		 * than all hole size or not
		 */
		boolean greaterThanAll = true;
		// use to record the best fit hole index
		int bestFitHoleIndex = -1;

		/**
		 * find the best fit hole among the holes we have and also compare the
		 * size and are free job requested with all the hole size
		 */
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				// compare to find the smallest hole that fit the job size
				if (null == hole.job) {
					if (bestFitHoleIndex == -1 || memHolesList.get(bestFitHoleIndex).size > hole.size)
						bestFitHoleIndex = memHolesList.indexOf(hole);
				}
				greaterThanAll = false;
			}
		}

		if (bestFitHoleIndex != -1) {
			memHolesList.get(bestFitHoleIndex).job = job;
			memHolesList.get(bestFitHoleIndex).job.setStatus(JobStatus.ASSIGNED);
		}

		/**
		 * if the job didn't be assigned to a current hole, try to give it the
		 * mem that not be assigned as hole and create a new hole
		 */
		if (JobStatus.ASSIGNED != job.getStatus()) {
			if (job.getSize() <= memNotAssigned2Hole) {
				createNewHole(job);
			} else if (greaterThanAll) {
				// Rejected when greater than all current hole sizes
				// and the available memory
				job.setStatus(JobStatus.REJECTED);
				rejectedJobs++;
			} else {
				job.setStatus(JobStatus.WAIT);// wait for free hole
			}
		} else {
			/**
			 * External fragmentation exists when there is enough 
			 * total memory space to satisfy a request but
			 * available spaces are not contiguous
			 */
			if (job.getSize() < memHolesList.get(bestFitHoleIndex).size) {
				// external fragmentation happened
				Hole tempNewHole = createNewHole(memHolesList.get(bestFitHoleIndex).size - job.getSize());
				memHolesList.get(bestFitHoleIndex).size = job.getSize();
				fragHolesList.add(tempNewHole);
			}
		}
	}

	// worst fit strategy
	public void worstFit(Job job) {
		/**
		 * greaterThanAll used to indicate if the size job requested is greater
		 * than all hole size or not
		 */
		boolean greaterThanAll = true; 
		// use to record the worst fit hole index
		int worstFitHoleIndex = -1; 

		/**
		 * find the worst fit hole among the holes we have and also compare the
		 * size job requested with all the hole size
		 */
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				// compare to find the largest hole 
				//that fit the job size and are free
				if (null == hole.job) {
					if (worstFitHoleIndex == -1 || memHolesList.get(worstFitHoleIndex).size < hole.size)
						worstFitHoleIndex = memHolesList.indexOf(hole);
				}
				greaterThanAll = false;
			}
		}

		if (worstFitHoleIndex != -1) {
			memHolesList.get(worstFitHoleIndex).job = job;
			memHolesList.get(worstFitHoleIndex).job.setStatus(JobStatus.ASSIGNED);
		}

		/**
		 *  if the job didn't be assigned to a current hole, 
		 *  try to give it the mem that not be assigned 
		 *  as a hole and create a new hole
		 */
		if (JobStatus.ASSIGNED != job.getStatus()) {
			if (job.getSize() <= memNotAssigned2Hole) {
				createNewHole(job);
			} else if (greaterThanAll) { 
				// Rejected when greater than all current hole sizes
				// and the available memory
				job.setStatus(JobStatus.REJECTED);
				rejectedJobs++;
			} else {
				// wait for free hole
				job.setStatus(JobStatus.WAIT);
			}
		} else {
			/**
			 * External fragmentation exists when there is enough 
			 * total memory space to satisfy a request but
			 * available spaces are not contiguous
			 */
			if (job.getSize() < memHolesList.get(worstFitHoleIndex).size) {
				// external fragmentation happened
				Hole tempNewHole = createNewHole(memHolesList.get(worstFitHoleIndex).size - job.getSize());
				memHolesList.get(worstFitHoleIndex).size = job.getSize();
				fragHolesList.add(tempNewHole);
			}
		}
	}

	// release memory after the job been processed
	public void releaseMem(Job job) {
		for (Hole hole : memHolesList) {
			if (null != hole.job) {
				if (job.getId() == hole.job.getId()) {
					hole.job = null;
				}
			}
		}
	}

	// storage utilization = used_mem/initMem
	public double storageUtilization() {
		return usedMem() * 1.0 / initMem;
	}

	// return the average hole size
	public int averageHoleSize() {
		int sum = 0;
		for (Hole hole : memHolesList) {
			sum += hole.size;
		}

		return sum / memHolesList.size();
	}

	/**
	 * @return the rejectedJobs
	 */
	public int getRejectedJobs() {
		return rejectedJobs;
	}

	/**
	 * get the sum of all external fragmentation generated so far
	 * 
	 * @return the externalFrag
	 */
	public int getExternalFrag() {
		externalFrag = 0;
		for (Hole hole : fragHolesList) {
			if (null == hole.job) {
				externalFrag += hole.size;
			}
		}
		return externalFrag * 1024;
	}

	/**
	 * @param externalFrag
	 *            the externalFrag to set
	 */
	public void setExternalFrag(int externalFrag) {
		this.externalFrag = externalFrag;
	}

	/**
	 * @param rejectedJobs
	 *            the rejectedJobs to set
	 */
	public void setRejectedJobs(int rejectedJobs) {
		this.rejectedJobs = rejectedJobs;
	}

	// create new hole in terms of job as the same size of the job
	private void createNewHole(Job job) {
		if (null == job)
			return;

		Hole newHole = new Hole();
		newHole.size = job.getSize();
		newHole.job = job;

		job.setStatus(JobStatus.ASSIGNED);
		memHolesList.add(newHole);
		memNotAssigned2Hole -= job.getSize();
	}

	// create new hole as specified size
	private Hole createNewHole(int size) {
		Hole newHole = new Hole();
		newHole.size = size;
		newHole.job = null;

		memHolesList.add(newHole);
		return newHole;
	}

	// calculate the memory been used currently
	private int usedMem() {
		int used = 0; // calculate the current used memory
		for (Hole hole : memHolesList) {
			if (null != hole.job)
				used += hole.job.getSize();
		}

		return used;
	}

	// memory hole class
	private class Hole {
		int id = 0;
		int size;// memory hole size
		Job job = null;// the job in this hole

		public Hole() {
			id = nextHoleID++;
		}
	}
}
