/**
 * 
 */

/**
 * constants defined in this class
 * 
 * @author Tao
 *
 */
public enum JobStatus {
	
	NEW, // a job just be created

	ASSIGNED, // means a job is allocated memory 

	REJECTED, // means a job cannot be fitted in memory

	WAIT, // a job need to wait for a memory hole
	
	PROCESSING, // jobs been assigned memory and wait for executing
	
	END // job finished executing
}
