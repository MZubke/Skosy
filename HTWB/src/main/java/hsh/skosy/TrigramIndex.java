package hsh.skosy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TrigramIndex implements Index {

	private Map<String,Set<String>> ngramindex;
	private Map<String,Set<ConceptInfo>> termindex;

	public TrigramIndex() {
		ngramindex = new HashMap<String,Set<String>>();
		termindex = new HashMap<String,Set<ConceptInfo>>();
	}

	@Override
	public void add(String s, ConceptInfo conceptInfo) {
		String base = "$" + s.toUpperCase() + "$";
		Set<ConceptInfo> concepts = termindex.get(base);
		if(concepts == null) {
			concepts = new HashSet<ConceptInfo>();
			termindex.put(base, concepts);
		}
		if(!concepts.contains(s)) {
			concepts.add(conceptInfo);
			for(int p = 0; p < base.length()-2; p++) {
				String tgram = base.substring(p, p+2);
				Set<String> terms = ngramindex.get(tgram);
				if(terms == null) {
					terms = new HashSet<String>();
					ngramindex.put(tgram, terms);
				}
				terms.add(base);
			}
		}
	}
	

	
	
	private ArrayList<SearchResult> findexact(String term) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		String searchterm = "$" + term.toUpperCase() + "$";
		
		Set<ConceptInfo> matchingconcepts = termindex.get(searchterm);
		if(matchingconcepts != null) {
			for(ConceptInfo c:matchingconcepts) {
				double score = c.getRelevance();
				results.add(new SearchResult(c,score));
			}
		}
		
		return results;
	}

	private ArrayList<SearchResult> findfuzzy(String term) {	
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		
		Map<String,Integer> conceptcounts = new HashMap<String,Integer>();
		String searchterm = "$" + term.toUpperCase() + "$";
		
		for(int p = 0; p < searchterm.length()-2; p++) {
			String tgram = searchterm.substring(p, p+2);
			Set<String> terms = ngramindex.get(tgram);

			if(terms != null) {
				for(String t:terms) {
					Integer n = conceptcounts.get(t);
					if(n == null) n = 0;
					n++;
					conceptcounts.put(t, n);
				}
			}
		}
		
		int maxoverlap = 0;
		for(int overlap:conceptcounts.values()) {
			if(overlap > maxoverlap) {
				maxoverlap = overlap;
			}
		}

		int minoverlap = maxoverlap - 1;
		if(minoverlap < 2) {
			minoverlap = maxoverlap;
		}
		
		for(Entry<String,Integer> e:conceptcounts.entrySet()) {
			if(e.getValue() < minoverlap) {
				continue;
			}

			double ngIntersection = (double) e.getValue();
			double ngST = (double) searchterm.length() - 2;
			double ngMT = (double) e.getKey().length() - 2;
			double jaccard = ngIntersection / (ngST+ngMT-ngIntersection);
			Set<ConceptInfo> matchingconcepts = termindex.get(e.getKey());
			for(ConceptInfo c:matchingconcepts) {
				double score = jaccard * c.getRelevance();
				results.add(new SearchResult(c,score));
			}
		}
		
		Collections.sort(results);
		
		return results;
	}
	
	@Override
	public ArrayList<SearchResult> find(String term, boolean fuzzy) {

		String[] terms = term.split("[ -]");
		ArrayList<SearchResult> result = null;

		if(fuzzy) {
			result = findfuzzy(terms[0]);
			for(int i = 1; i< terms.length; i++) {
				result = addResults(result,findfuzzy(terms[i]));
			}
		} else {
			result = findexact(terms[0]);
			for(int i = 1; i< terms.length; i++) {
				result = intersectResults(result,findexact(terms[i]));
			}
		}
		return result;
	}

	private ArrayList<SearchResult> intersectResults(ArrayList<SearchResult> resultA, ArrayList<SearchResult> resultB) {
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		for(SearchResult sr:resultA) {
			String conceptURI1 = sr.getConcept().getUri();
			String term1 = sr.getConcept().getTerm();
			for(SearchResult sr2:resultB) {
				String term2 = sr2.getConcept().getTerm();
				String conceptURI2 = sr2.getConcept().getUri();
				if(conceptURI1.equals(conceptURI2) && term1.equals(term2)) { //TODO Pointer to concepts would be easier to compare
					result.add(sr);
					resultB.remove(sr2);
					break;
				}
			}
		}
		
		return result;
	}

	private ArrayList<SearchResult> addResults(ArrayList<SearchResult> resultA,	ArrayList<SearchResult> resultB) {
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		for(SearchResult sr:resultA) {
			boolean found = false;
			String conceptURI1 = sr.getConcept().getUri();
			String term1 = sr.getConcept().getTerm();
			for(SearchResult sr2:resultB) {
				String term2 = sr2.getConcept().getTerm();
				String conceptURI2 = sr2.getConcept().getUri();
				if(conceptURI1.equals(conceptURI2) && term1.equals(term2))  { //TODO Pointer to concepts would be easier to compare
					sr.setScore(sr.getScore()+sr2.getScore());
					result.add(sr);
					resultB.remove(sr2);
					found = true;
					break;
				}
			}
			if(!found) {
				result.add(sr);
			}
		}
		result.addAll(resultB);
		
		Collections.sort(result);
		
		return result;
	}

	
	
}
