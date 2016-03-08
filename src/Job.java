
public class Job {

	private int id = 0;
	private String name = "";
	
	//indicate job status
	private JobStatus status; 
	//KB indicates the size of a job (50--300 in multiples of 10
	private int size = 0; 
	// indicates the processing/duration time (5--60 VTUs in multiples of 5 VTUs
	private int duration = 0;
	//count the time already be executed
	private int processedTime = 0;
	//in terms of VTUs
	private int startTime = -1; 
	//in termsof VTUs
	private int endTime = -1; 
	
	private static int nextJobID = 1;
	
	
	private Job() {
	}
	
	public static Job createNewJob(){
		Job newJob = new Job();
		
		newJob.setName("Job"+nextJobID);
		newJob.setId(nextJobID++);
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


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the processedTime
	 */
	public int getProcessedTime() {
		return processedTime;
	}

	/**
	 * @param processedTime the processedTime to set
	 */
	public void setProcessedTime(int processedTime) {
		this.processedTime = processedTime;
	}

	/**
	 * @return the startTime
	 */
	public int getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public int getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "Name:" + name + ", ID:" + id + ", Size:" + size 
				+", Duration:" + duration + ", Status:" + status
				+ ", Start:" + startTime + ", End:" + endTime;
	}
	
	
}
