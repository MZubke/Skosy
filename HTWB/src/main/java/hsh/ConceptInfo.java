package hsh;

public class ConceptInfo {
	private String term;
	private String label;
	private String uri;
	private Double relevance;
	
	public ConceptInfo(String term, String label, String uri, double relevance) {
		super();
		this.term = term;
		this.label = label;
		this.uri = uri;
		this.relevance = relevance;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public Double getRelevance() {
		return relevance;
	}
	
}
