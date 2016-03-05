
public class Job {

	private JobStatus status;
	private int size = 0; //KB indicates the size of a job (50--300 in multiples of 10
	private int duration = 0;// indicates the processing time (5--60 VTUs in multiples of 5 VTUs
	private int processedTime = 0; //count the time already be executed
	private int startTime = 0; //in terms of VTUs
	private int endTime = 0; //in termsof VTUs
	
	
	private Job() {
	}
	
	public static Job createNewJob(){
		Job newJob = new Job();
		
		newJob.setStatus(JobStatus.NEW);
		newJob.setSize(RandomNumGenerator.getRandomNum(5, 30)*10);
		newJob.setDuration(RandomNumGenerator.getRandomNum(1, 12)*5);
		
		return newJob;
	}

	
	
	/**
	 * @return the status
	 */
	public JobStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(JobStatus status) {
		this.status = status;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	
}
