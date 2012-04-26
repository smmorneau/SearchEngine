import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Creates a thread pool.
 * 
 * @author IBM
 * 
 */
public class WorkQueue {
	private final int nThreads;
	private final PoolWorker[] threads;
	private final LinkedList<Runnable> queue;
	public static final Object messenger = new Object();
	public boolean shutdown = false;
	private static WorkQueue workers = null;

	private static Logger log = Logger.getLogger(WorkQueue.class.getName());

	/**
	 * Creates a work pool of n threads.
	 * 
	 * @param nThreads
	 */
	private WorkQueue(int nThreads) {
		this.nThreads = nThreads;
		queue = new LinkedList<Runnable>();
		threads = new PoolWorker[this.nThreads];

		for (int i = 0; i < nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * Adds the new work to the queue and notifies a thread waiting in the
	 * queue.
	 * 
	 * @param r
	 */
	public void execute(Runnable r) {
		synchronized (queue) {
			queue.addLast(r);
			queue.notify();
		}
	}

	/**
	 * Checks whether the queue is empty and all threads are waiting to give the
	 * okay to shutdown.
	 * 
	 * @return true if okay to shutdown
	 */
	public boolean isDone() {
		if (!queue.isEmpty()) {
			return false;
		}
		if (allWaiting()) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether all threads are waiting in the queue.
	 * 
	 * @return true if all threads are waiting
	 */
	public boolean allWaiting() {
		for (Thread t : threads) {
			log.debug(t.getState());
			// thread is RUNNABLE if it was never used
			if (t.getState() != Thread.State.WAITING
					&& t.getState() != Thread.State.RUNNABLE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Shuts down the queue if all threads are done with work.
	 * 
	 * @throws InterruptedException
	 */
	public void stopWorkers() throws InterruptedException {
		synchronized (queue) {
			if (allWaiting()) {
				shutdown = true;
				queue.notifyAll();
			}
		}
	}

	public static WorkQueue getInstance() {
		if (workers == null) {
			// crawl 10 web pages concurrently
			workers = new WorkQueue(10);
		}
		return workers;
	}

	/**
	 * Removes work from the queue
	 * 
	 * @author IBM
	 * 
	 */
	private class PoolWorker extends Thread {
		/**
		 * The caller thread will remove the first work from the queue. If the
		 * queue is empty, it notifies the messenger and waits for all threads
		 * to finish before shutting down.
		 */
		public void run() {
			Runnable r;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							synchronized (messenger) {
								messenger.notify();
							}

							queue.wait();

							if (shutdown) {
								return;
							}

						} catch (InterruptedException ignored) {
							log.error(Thread.currentThread().getName()
									+ " stopping work");
							return;
						}
					}
					log.debug("Getting " + Thread.currentThread().getName()
							+ " from queue.");
					r = queue.removeFirst();
				}

				// If we don't catch RuntimeException,
				// the pool could leak threads
				try {
					r.run();
				} catch (RuntimeException e) {
					log.error("Runtime exception");
					e.printStackTrace();
				}
			}

		}
	}

}