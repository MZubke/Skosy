package hsh.skosy;

public class SearchResult implements Comparable{
	private ConceptInfo concept;
	private double score;
	
	public SearchResult(ConceptInfo concept, double score) {
		super();
		this.concept = concept;
		this.score = score;
	}

	public ConceptInfo getConcept() {
		return concept;
	}

	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
		
	}

	@Override
	public int compareTo(Object arg0) {
		SearchResult other = (SearchResult) arg0;
		if(this.score == other.score) {
			return 0;
		} else if (this.score < other.score) {
			return 1;
		} else {
			return -1;
		}
	}


	
	

}
