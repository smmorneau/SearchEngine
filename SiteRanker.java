/**
 * Represents a url and its rank based on the number of results
 * 
 * @author Steely Morneau
 * 
 */
public class SiteRanker implements Comparable {

	private int rank;
	private String site;

	/**
	 * 
	 * @param site
	 * @param rank
	 */
	public SiteRanker(String site, int rank) {
		this.rank = rank;
		this.site = site;
	}

	/**
	 * Compares two urls based on their ranks.
	 */
	public int compareTo(Object arg0) {
		int rank = ((SiteRanker) arg0).getRank();

		if (rank > this.rank) {
			return 1;
		} else if (rank < this.rank) {
			return -1;
		}
		return 0;
	}

	/**
	 * Checks to see if two urls are equal based on their ranks.
	 */
	@Override
	public boolean equals(Object fr) {
		String fileName = ((SiteRanker) fr).getSiteName();
		if (this.site.equals(fileName)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets rank of a url
	 * 
	 * @return rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Sets the rank of a url
	 * 
	 * @param rank
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Gets the name of the url.
	 * 
	 * @return
	 */
	public String getSiteName() {
		return site;
	}

	/**
	 * Sets the rank of a url.
	 * 
	 * @param filename
	 */
	public void setFileName(String filename) {
		this.site = filename;
	}

}
