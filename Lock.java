import org.apache.log4j.Logger;

/**
 * Provides read-write lock functionality.
 * 
 * @author Steely Morneau
 * 
 */
public class Lock {

	private int readers = 0;
	private boolean writers = false;

	private static Logger log = Logger.getLogger(Lock.class.getName());

	public Lock() {
	}

	/**
	 * Tries to acquire the read lock, waiting if necessary for other threads to
	 * be done writing, incrementing the number of readers.
	 */
	public synchronized void acquireReadLock() {
		while (writers) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				log.error("InterruptedException");
			}
		}
		readers++;
	}

	/**
	 * Releases the read lock, decrementing the number of readers.
	 */
	public synchronized void releaseReadLock() {
		readers--;
		this.notifyAll();
	}

	/**
	 * Releases the read lock, waiting if necessary for other threads to be done
	 * reading and writing, decrementing the number of readers.
	 */
	public synchronized void acquireWriteLock() {
		while (readers > 0 || writers) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				log.error("InterruptedException");
			}
		}
		writers = true;
	}

	/**
	 * Releases the write lock, notifying the threads, decrementing the number
	 * of writers.
	 */
	public synchronized void releaseWriteLock() {
		writers = false;
		this.notifyAll();
	}

}