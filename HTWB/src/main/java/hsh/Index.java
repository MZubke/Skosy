package hsh;

import java.util.ArrayList;

public interface Index {

	ArrayList<SearchResult> find(String searchtext, boolean selection);

	void add(String s, ConceptInfo conceptInfo);

}
