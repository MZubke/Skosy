package hsh.skosy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrieNode {

	public ArrayList<ConceptInfo> matches;
	public String rest;

	public Map<Character,TrieNode> followers;
	
	public TrieNode(String rest,ConceptInfo c) {
		this.rest = rest;
		followers = new HashMap<Character,TrieNode>();
		matches = new ArrayList<ConceptInfo>();
		if(c != null) {
			matches.add(c);
		}
	}
	
	
}
