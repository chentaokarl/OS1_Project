import java.awt.JobAttributes;
import java.util.ArrayList;
import java.util.List;

import org.omg.PortableInterceptor.USER_EXCEPTION;

/**
 * 
 */

/**
 * @author Tao
 *
 */
public class MemoryManager {
	public static final String FIRST_FIT= "firstfit";
	public static final String BEST_FIT= "bestfit";
	public static final String WORST_FIT= "worstfit";
	
	public int totalMem = 2000;//total memory capacity in KB, 2000KB
	public int osReservedMem = 200;//memory reserved for operating system
	public int initMem = totalMem - osReservedMem;//initial available memory for jobs
	private int memNotAssigned2Hole = initMem; // indicate memory left that not been assigned to a hole
	private List<Hole> memHolesList = new ArrayList<>();//trace memory holes
	private int rejectedJobs = 0; // count the amount of rejected jobs
	private int externalFrag = 0; //count the amount of external fragmentation in KB
	
	private static int nextHoleID = 1;
	
	/***
	 * user can specify their total memory and os reserved memory
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
	
	public void assignMem(String strategy, Job job){
		for (Hole hole : memHolesList) {
			System.out.println("Hole" + hole.id + ", " + "size: "+ hole.size + ", " + hole.job);
		} 
		if (FIRST_FIT.equals(strategy)) {
			firstFit(job);
		}else if (BEST_FIT.equals(strategy)) {
			bestFit(job);
		}else if (WORST_FIT.equals(strategy)) {
			worstFit(job);
		}
		
		for (Hole hole : memHolesList) {
			System.out.println("Hole" + hole.id + ", " + "size: "+ hole.size + ", " + hole.job);
		} 
	}

	// fisrst fit strategy
	public void firstFit(Job job){
			externalFrag = 0; //clean the counter first
			boolean greaterThanAll = true; //used to indicate if the size job requested is greater than all hole size or not 
			int firstFitHoleIndex = -1; //use to record the first fit hole index
			
			// find the first fit hole among the holes we have and also compare the size job requested with all the hole size
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
			//if the job didn't be assigned to a current hole, try to give it the mem that not be assigned as hole and create a new hole
			if (JobStatus.ASSIGNED != job.getStatus()) {
				if (job.getSize() <= memNotAssigned2Hole ) {
					createNewHole(job);
				}else if (greaterThanAll) { // greater than all current hole sizes and the available mem
					job.setStatus(JobStatus.REJECTED);
					rejectedJobs ++;
					
					// External fragmentation exists when there is enough total
					// memory space to satisfy a request but the available spaces
					// are not contiguous
					if((initMem - usedMem()) >= job.getSize())
						externalFrag = initMem - usedMem();
				}else {
					job.setStatus(JobStatus.WAIT);//wait for free hole
				}
			}else{
				//external fragmentation generates
				if (job.getSize() < memHolesList.get(firstFitHoleIndex).size) {
					createNewHole(memHolesList.get(firstFitHoleIndex).size - job.getSize());
					memHolesList.get(firstFitHoleIndex).size = job.getSize();
				}
			}
	}
	
	//best fit strategy
	public void bestFit(Job job) {
		externalFrag = 0; //clean the counter first
		boolean greaterThanAll = true; // used to indicate if the size job requested is greater than all hole size or not
		int bestFitHoleIndex = -1; //use to record the best fit hole index
		
		// find the best fit hole among the holes we have and also compare the size and are free
		// job requested with all the hole size
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
		
		//if the job didn't be assigned to a current hole, try to give it the mem that not be assigned as hole and create a new hole
		if (JobStatus.ASSIGNED != job.getStatus()) {
			if (job.getSize() <= memNotAssigned2Hole ) {
				createNewHole(job);
			}else if (greaterThanAll) { // greater than all current hole sizes and the available mem
				job.setStatus(JobStatus.REJECTED);
				rejectedJobs ++;
				// External fragmentation exists when there is enough total
				// memory space to satisfy a request but the available spaces
				// are not contiguous
				if((initMem - usedMem()) >= job.getSize())
					externalFrag = initMem - usedMem();
			}else {
				job.setStatus(JobStatus.WAIT);//wait for free hole
			}
		}else{
			//external fragmentation generates
			if (job.getSize() < memHolesList.get(bestFitHoleIndex).size) {
				createNewHole(memHolesList.get(bestFitHoleIndex).size - job.getSize());
				memHolesList.get(bestFitHoleIndex).size = job.getSize();
			}
		}
	}
	
	//worst fit strategy
	public void worstFit(Job job) {
		externalFrag = 0; //clean the counter first
		boolean greaterThanAll = true; // used to indicate if the size job requested is greater than all hole size or not
		int worstFitHoleIndex = -1; //use to record the worst fit hole index
		
		// find the best fit hole among the holes we have and also compare the
		// size
		// job requested with all the hole size
		for (Hole hole : memHolesList) {
			if (job.getSize() <= hole.size) {
				// compare to find the largest hole that fit the job size and are free
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

		// if the job didn't be assigned to a current hole, try to give it the
		// mem that not be assigned as hole and create a new hole
		if (JobStatus.ASSIGNED != job.getStatus()) {
			if (job.getSize() <= memNotAssigned2Hole) {
				createNewHole(job);
			} else if (greaterThanAll) { // greater than all current hole sizes
											// and the available mem
				job.setStatus(JobStatus.REJECTED);
				rejectedJobs ++;
				// External fragmentation exists when there is enough total
				// memory space to satisfy a request but the available spaces
				// are not contiguous
				if((initMem - usedMem()) >= job.getSize())
					externalFrag = initMem - usedMem();
			} else {
				job.setStatus(JobStatus.WAIT);// wait for free hole
			}
		}else{
			//external fragmentation generates
			if (job.getSize() < memHolesList.get(worstFitHoleIndex).size) {
				createNewHole(memHolesList.get(worstFitHoleIndex).size - job.getSize());
				memHolesList.get(worstFitHoleIndex).size = job.getSize();
			}
		}
	}
	
	public void releaseMem(Job job){
		for (Hole hole : memHolesList) {
			if (null != hole.job) {
				if (job.getId() == hole.job.getId()) {
					hole.job = null;
				}
			}
		}
	}
	
	//storage utilization = used_mem/total_mem
	public double storageUtilization(){
		return usedMem()*1.0/totalMem; 
	}
	
	//return the average hole size
	public int averageHoleSize(){
		int sum = 0;
		for (Hole hole : memHolesList) {
			sum += hole.size;
		}
		
		return sum/memHolesList.size(); 
	}
	
	
	/**
	 * @return the rejectedJobs
	 */
	public int getRejectedJobs() {
		return rejectedJobs;
	}
	

	/**
	 * @return the externalFrag
	 */
	public int getExternalFrag() {
		return externalFrag*1024;
	}

	/**
	 * @param externalFrag the externalFrag to set
	 */
	public void setExternalFrag(int externalFrag) {
		this.externalFrag = externalFrag;
	}

	/**
	 * @param rejectedJobs the rejectedJobs to set
	 */
	public void setRejectedJobs(int rejectedJobs) {
		this.rejectedJobs = rejectedJobs;
	}

	private void createNewHole(Job job){
		if (null == job)
			return;

		Hole newHole = new Hole();
		newHole.size = job.getSize();
		newHole.job = job;
		
		job.setStatus(JobStatus.ASSIGNED);
		memHolesList.add(newHole);
		memNotAssigned2Hole -= job.getSize();
	}
	
	private void createNewHole(int size){
		Hole newHole = new Hole();
		newHole.size = size;
		newHole.job = null;
		
		memHolesList.add(newHole);
	}
	
	
	public void printHolesList() {
		for (Hole hole : memHolesList) {
			System.out.println("HoleID:"+hole.id +", " + "HoleSize:"+ hole.size + "Job:" + hole.job == null?"null":hole.job.getName());
		}
	}
	private int usedMem(){
		int used = 0; // calculate the current used memory
		for (Hole hole : memHolesList) {
			if (null != hole.job) 
				used += hole.job.getSize();
		}
		
		return used;
	}
	
	private class Hole{
		
		int id = 0;
		int size;//memory hole size
		Job job = null;// the job in this hole
		
		public Hole() {
			id = nextHoleID++;
		}
	}
}
