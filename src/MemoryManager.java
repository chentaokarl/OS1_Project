import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class likes the Memory Management Unit in system It will in charge of
 * assigning and tracing memory info
 * 
 * @author Tao
 *
 */
public class MemoryManager {
	// three stategies
	public static final String FIRST_FIT = "Firstfit";
	public static final String BEST_FIT = "Bestfit";
	public static final String WORST_FIT = "Worstfit";
	// Scenarios 1: compact when a memory reqeust is denied
	public static final String COMP_REQ_DENIED = "compReqDenied";
	// Scenarios 2: compact at every 250 VTUs
	public static final String COMP_250 = "compAtEvery250";
	// Scenarios 2: compact at every 250 VTUs
	public static final String COMP_500 = "compAtEvery500";

	// total memory capacity in KB, 2000KB
	public int totalMem = 2000;
	// memory reserved for operating system
	public int osReservedMem = 200;
	// initial available memory for jobs
	public int initMem = totalMem - osReservedMem;
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
	 * user can specify their total memory and os reserved memory if not
	 * specified, use the default mem 2000KB and 200 KB
	 * 
	 * @param totalMem
	 * @param osReservedMem
	 */
	public MemoryManager(int totalMem, int osReservedMem) {
		this.totalMem = totalMem;
		this.osReservedMem = osReservedMem;
		this.initMem = totalMem - osReservedMem;
		// At the beginning, only one hole (block) contain all memory
		createNewHole(this.initMem);
	}

	public MemoryManager() {
		// At the beginning, only one hole (block) contain all memory
		createNewHole(this.initMem);
	}

	/**
	 * Assign memory to the job using the strategy user specified
	 * 
	 * @param strategy
	 * @param job
	 */
	public void assignMem(String strategy, Job job) {
		// reject if job needed more than the total memory
		if (initMem < job.getSize()) {
			job.setStatus(JobStatus.REJECTED);
		}

		// assign memory hole to job based on strategies.
		if (FIRST_FIT.equals(strategy)) {
			firstFit(job);
		} else if (BEST_FIT.equals(strategy)) {
			bestFit(job);
		} else if (WORST_FIT.equals(strategy)) {
			worstFit(job);
		}
	}

	/**
	 * fisrst fit strategy
	 * 
	 * @param job
	 */
	public void firstFit(Job job) {
		// use to record the first fit hole index
		int firstFitHoleIndex = -1;

		// find the first fit hole among the holes we have and
		// also compare the size job requested with all the hole size
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				if (null == hole.job) {
					firstFitHoleIndex = memHolesList.indexOf(hole);
					job.setStatus(JobStatus.ASSIGNED);
					hole.job = job;
					break;
				}
			}
		}
		// if the job didn't be assigned to a current hole,
		// try to give it the mem that not be assigned as hole
		// and create a new hole
		if (JobStatus.ASSIGNED != job.getStatus()) {
			// Not current holes can fit the job
			// put the job into waiting
			// wait for free hole
			job.setStatus(JobStatus.WAIT);
		} else {
			/**
			 * External fragmentation exists when there is enough total memory
			 * space to satisfy a request but available spaces are not
			 * contiguous
			 */
			if (job.getSize() < memHolesList.get(firstFitHoleIndex).size) {
				// external fragmentation happened when
				// job didn't take the whole size of a hole
				Hole tempNewHole = createNewHole(memHolesList.get(firstFitHoleIndex).size - job.getSize());
				memHolesList.get(firstFitHoleIndex).size = job.getSize();
				coalescence();
				if (null != tempNewHole && tempNewHole.size <= job.getSize()) {
					fragHolesList.add(tempNewHole);// mark
				}
			}
		}
	}

	/**
	 * best fit strategy
	 * 
	 * @param job
	 */
	public void bestFit(Job job) {
		// use to record the best fit hole index
		int bestFitHoleIndex = -1;

		/**
		 * find the best fit hole among the holes we have and also compare the
		 * size job requested with all the hole sizes
		 */
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				// compare to find the smallest hole that fit the job size
				if (null == hole.job) {
					if (bestFitHoleIndex == -1 || memHolesList.get(bestFitHoleIndex).size > hole.size)
						bestFitHoleIndex = memHolesList.indexOf(hole);
				}
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
			// Not current holes can fit the job
			// put the job into waiting
			// wait for the free hole fits the job
			job.setStatus(JobStatus.WAIT);
		} else {
			/**
			 * External fragmentation exists when there is enough total memory
			 * space to satisfy a request but available spaces are not
			 * contiguous
			 */
			if (job.getSize() < memHolesList.get(bestFitHoleIndex).size) {
				// external fragmentation happened when
				// job didn't take the whole size of a hole
				Hole tempNewHole = createNewHole(memHolesList.get(bestFitHoleIndex).size - job.getSize());
				memHolesList.get(bestFitHoleIndex).size = job.getSize();
				coalescence();
				if (null != tempNewHole && tempNewHole.size <= job.getSize()) {
					fragHolesList.add(tempNewHole);// mark
				}
			}
		}
	}

	/**
	 * worst fit strategy
	 * 
	 * @param job
	 */
	public void worstFit(Job job) {
		// use to record the worst fit hole index
		int worstFitHoleIndex = -1;

		/**
		 * find the worst fit hole among the holes we have and also compare the
		 * size job requested with all the hole size
		 */
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				// compare to find the largest hole
				// that fit the job size and are free
				if (null == hole.job) {
					if (worstFitHoleIndex == -1 || memHolesList.get(worstFitHoleIndex).size < hole.size)
						worstFitHoleIndex = memHolesList.indexOf(hole);
				}
			}
		}

		if (worstFitHoleIndex != -1) {
			memHolesList.get(worstFitHoleIndex).job = job;
			memHolesList.get(worstFitHoleIndex).job.setStatus(JobStatus.ASSIGNED);
		}

		/**
		 * if the job didn't be assigned to a current hole, try to give it the
		 * mem that not be assigned as a hole and create a new hole
		 */
		if (JobStatus.ASSIGNED != job.getStatus()) {
			// Not current holes can fit the job
			// put the job into waiting
			// wait for free hole
			job.setStatus(JobStatus.WAIT);
		} else {
			/**
			 * External fragmentation exists when there is enough total memory
			 * space to satisfy a request but available spaces are not
			 * contiguous
			 */
			if (job.getSize() < memHolesList.get(worstFitHoleIndex).size) {
				// external fragmentation happened when
				// job didn't take the whole size of a hole
				Hole tempNewHole = createNewHole(memHolesList.get(worstFitHoleIndex).size - job.getSize());
				memHolesList.get(worstFitHoleIndex).size = job.getSize();
				coalescence();
				if (null != tempNewHole && tempNewHole.size <= job.getSize()) {
					fragHolesList.add(tempNewHole);// mark
				}
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
		coalescence();
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
		// return in bytes (externalFrag is KB here)
		return externalFrag * 1024;
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
		// memNotAssigned2Hole -= job.getSize();
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

	// phase2 coalescence and compaction

	/**
	 * coalescence the mem holes
	 */
	protected void coalescence() {
		Hole temp = null;
		for (Iterator iterator = memHolesList.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			if (null == hole.job) {
				if (null != temp) {
					// collapse two holes
					temp.size += hole.size;
					iterator.remove();
					// after coalescence, remove hole from fragment 
					fragHolesList.remove(temp);
					fragHolesList.remove(hole);
				} else {
					temp = hole;
				}
			} else {
				temp = null;
			}
		}
//		fragHolesList.clear();
	}

	/**
	 * compact the mem holes
	 * compact only when compL which is the current invoke
	 * location and the current strategy matched. 
	 */
	protected void compaction(String compL, String strategy) {
		if (compL.equals(strategy)) {

			int temp = 0;
			for (Iterator iterator = memHolesList.iterator(); iterator.hasNext();) {
				Hole hole = (Hole) iterator.next();
				if (null != hole.job) {
					temp += hole.size;
				} else {
					iterator.remove();
				}
			}
			// compact all free mem as a large hole
			createNewHole(initMem - temp);
			// after compaction, there is no fragment
			fragHolesList.clear();
		}
	}

}
