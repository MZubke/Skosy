package hsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;


public class Trie implements Index{
	
	
	

	//private ArrayList<TrieNode> nodes = new ArrayList<TrieNode>();
	
	private TrieNode root = null;
	
	
	
	public Trie() {
		root = new TrieNode("",null);
	}

	public void add(String term, ConceptInfo concept) {
		//String term = concept.getTerm().toUpperCase();
		term = term.toUpperCase();
			
		TrieNode prev = null;
		TrieNode akt = null;
		TrieNode next = root;
	
		int pos = 0;
		
		do {
			prev = akt;
			akt = next;
			Character c = term.charAt(pos);
			next = akt.followers.get(c);
			pos++;
		} while(next !=null && pos < term.length());
		if(next == null) {
			pos--;
		} else {
			prev = akt;
			akt = next;
		}
		
		while(akt.rest.length() > 0 && pos < term.length() && term.charAt(pos) == akt.rest.charAt(0)) {
			akt.rest = akt.rest.substring(1);
			TrieNode n1 = new TrieNode("",null);
			n1.followers.put(term.charAt(pos), akt);
			prev.followers.put(term.charAt(pos-1), n1);
			prev = n1;
			pos++;
		}
		
		if(pos >= term.length()) {
			if(akt.rest.isEmpty()) {
				akt.matches.add(concept);
			} else {
				TrieNode n1 = new TrieNode("",concept);
				n1.followers.put(akt.rest.charAt(0), akt);
				akt.rest = akt.rest.substring(1);
				prev.followers.put(term.charAt(pos-1), n1);
			}
		} else if(akt.rest.length() > 0) {	
			TrieNode n1 = new TrieNode("",null);
			n1.followers.put(akt.rest.charAt(0), akt);
			akt.rest = akt.rest.substring(1);
			prev.followers.put(term.charAt(pos-1), n1);
			TrieNode n2 = new TrieNode(term.substring(pos+1),concept);
			n1.followers.put(term.charAt(pos), n2);		
		} else {
			TrieNode n2 = new TrieNode(term.substring(pos+1),concept);
			akt.followers.put(term.charAt(pos), n2);	
		}
	}
	
	
	
	private ArrayList<SearchResult> get(String term) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		term = term.toUpperCase();	
		TrieNode node = root;
		int pos = 0;
		
		do {
			Character c = term.charAt(pos);
			node = node.followers.get(c);
			pos++;
		} while(node !=null && pos < term.length() && node.rest.isEmpty());
		
		if (node != null && node.rest.equals(term.substring(pos)) && !node.matches.isEmpty()) {
			addToResults(results,node,0);
		}
		
		return results;
	}
	
	private ArrayList<SearchResult> findfuzzy(String term) {
		//TODO Weights for prefixes and suffixes
		//TODO Treatment of non coded rests!
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		
		term = term.toUpperCase();	
		int maxPen = 4;

		ArrayList<searchnode> activenodes = new ArrayList<searchnode>();
		activenodes.add(new searchnode(root));
		ArrayList<TrieNode>[] visited = new ArrayList[term.length()+1]; 
		for(int i = 0; i < visited.length; i++) {
			visited[i] = new ArrayList<TrieNode>();
		}
		
		
		while(!activenodes.isEmpty()) {
			Collections.sort(activenodes);
			searchnode best = activenodes.get(0);
			double penalty = best.getPenalty();
			searchnode n;
			
			if(penalty < maxPen) {
				
				for(Entry<Character, TrieNode> e:best.node.followers.entrySet()) {
					Character c = e.getKey();
					TrieNode t = e.getValue();
					penalty = best.getPenalty();
					if(best.getPosition() < term.length()) {
						if(c == term.charAt(best.getPosition())) {
							//match
							n = new searchnode(t,best.getPosition()+1,penalty);
						} else {
							//substitute
							n = new searchnode(t,best.getPosition()+1,penalty+1);
						}
						if(!visited[n.getPosition()].contains(n.getNode())) {
							activenodes.add(n);
							visited[n.getPosition()].add(n.getNode());

							if(n.getPosition() == term.length() && !n.getNode().matches.isEmpty() && n.getNode().rest.isEmpty()) {
								addToResults(results,n.getNode(),n.getPenalty());
								if(results.size() == 1) {
									maxPen = (int) Math.max(maxPen, n.getPenalty()+2);
								} else if(results.size() > 10) {
									maxPen = (int) Math.max(maxPen, n.getPenalty());
								}
							}
						}
					}
					
					
					//delete
					if(best.getPosition() < term.length()) {
						penalty = best.getPenalty()+1;
						n = new searchnode(best.getNode(),best.getPosition()+1,penalty);
						if(!visited[n.getPosition()].contains(n.getNode())) {
							activenodes.add(n);
							visited[n.getPosition()].add(n.getNode());

							if(n.getPosition() == term.length() && !n.getNode().matches.isEmpty() && n.getNode().rest.isEmpty()) {
								addToResults(results,n.getNode(),n.getPenalty());
								if(results.size() == 1) {
									maxPen = (int) Math.max(maxPen, n.getPenalty()+2);
								} else if(results.size() > 10) {
									maxPen = (int) Math.max(maxPen, n.getPenalty());
								}
							}
						}
					}
					
					
					
					//insert
					penalty = best.getPenalty();
					if(best.getPosition() == 0 || best.getPosition() > term.length() ) { //prefix && postfix
						penalty += 0.2;
					} else {
						penalty += 1;
					}
					
					n = new searchnode(t,best.getPosition(),penalty);
					if(!visited[n.getPosition()].contains(n.getNode())) {
						activenodes.add(n);
						visited[n.getPosition()].add(n.getNode());
						if(n.getPosition() == term.length() && !n.getNode().matches.isEmpty() && n.getNode().rest.isEmpty()) {
							addToResults(results,n.getNode(),n.getPenalty());
							if(results.size() == 1) {
								maxPen = (int) Math.max(maxPen, n.getPenalty()+2);
							} else if(results.size() > 10) {
								maxPen = (int) Math.max(maxPen, n.getPenalty());
							}
						}
					} 
				}
			}
			activenodes.remove(best);
		}	
		return results;
	}
	
	private void addToResults(ArrayList<SearchResult> results, TrieNode n, double penalty) {
		for(ConceptInfo c:n.matches) {
			results.add(new SearchResult(c,c.getRelevance()/(1+penalty)));
		}
		
	}

	public ArrayList<SearchResult> find(String term, boolean fuzzy) {
		ArrayList<SearchResult> results;
		
		if(fuzzy) {
			results = findfuzzy(term);	
		} else {
			results = get(term);
		}
		
		Collections.sort(results);
		return results;
	}
	
	protected class searchnode implements Comparable{

		private TrieNode node;
		private int position;
		private double penalty;		
		
		public searchnode(TrieNode node, int position, double penalty) {
			super();
			this.node = node;
			this.position = position;
			this.penalty = penalty;
		}

		public searchnode(TrieNode node) {
			super();
			this.node = node;
			this.penalty = 0;
			this.position = 0;
		}

		public double getPenalty() {
			return penalty;
		}


		public void setPenalty(double penalty) {
			this.penalty = penalty;
		}


		public TrieNode getNode() {
			return node;
		}
		
		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public int compareTo(Object arg0) {
			searchnode other = (searchnode) arg0;
			if(this.penalty == other.penalty) {
				return 0;
			} else if (this.penalty < other.penalty) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
