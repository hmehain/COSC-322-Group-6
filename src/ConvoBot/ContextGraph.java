package ConvoBot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

public class ContextGraph {
	
	
	/**
	 * Explanation of (almost) all objects and classes in contextGraphREADME.txt
	 */
	
	
	// node at center of graph, all characteristic nodes connected to this automatically
	Node centerNode;
	
	// list of all characteristics/solutions that can be added to graph
	ArrayList<Characteristic> characteristicsList;
	ArrayList<Solution> solutionsList;
	
	// list of all nodes in graph
	ArrayList<Node> characteristicNodes;
	ArrayList<Node> solutionNodes;
	

	/**
	 * Constructor
	 * 	creates center node of graph
	 * 	adds all solutions in solutionsList to graph
	 * 	adds all characteristics in characteristicsList to graph
	 * 		(order [solutions, then characteristics] less important than when reading initial file, should still be maintained)
	 */
	public ContextGraph(String characteristicsFilename, String solutionsFilename) {
		
		// creates arrayLists for characteristics and solutions read from text file, to be sent to graph
		this.characteristicsList = new ArrayList<>();
		this.solutionsList = new ArrayList<>();
		
		createLists(characteristicsFilename, solutionsFilename);
		
		// center node MUST be named "centerNode" (otherwise error with edge/node weights)
		centerNode = new Node(new Subject("centerNode")); // node at center of graph, originally patient, change later?
			
		characteristicNodes = new ArrayList<>();
		solutionNodes = new ArrayList<>(); // needed to return solution at end
					
		// adds all solutions and characteristics in list to graph
		addListSolutions();
		addListCharacteristics();
	}
	
	/**
	 * Reads potential solutions from file and populates solutionsList, then potential characteristics from file to characteristicsList
	 * 	(order is important, solutions added to characteristics only when they exist in solutions list)
	 * 
	 * @param characteristicsFilename = name of file where characteristics list stored
	 * 		all characteristics must be stored as "characteristic1;synonym1,synonym2;treatment1-m1,treatment2-m2,treatment3-m3;"
	 * 			m1 = multiplier (stored as int)
	 * 			only adds solutions already in solution list, searches by name
	 * @param solutionsFilename = name of file where solutions list stored
	 * 		all solutions stored as "treatment1;\ntreatment2;\n..."
	 * 			based on simple solution class (name is only attribute)
	 * @return true if no errors encountered
	 */
	private boolean createLists(String characteristicsFilename, String solutionsFilename) {
		
		try (BufferedReader cbr = new BufferedReader(new InputStreamReader(new FileInputStream(characteristicsFilename)));
				BufferedReader sbr = new BufferedReader(new InputStreamReader(new FileInputStream(solutionsFilename)));) {
		    
			String line;
		    
		    while ((line = sbr.readLine()) != null) {
		    	String[] sections = line.split(";");
		    	
		    	Solution s = new Solution(sections[0]);
		    	solutionsList.add(s);
		    }
		    
		    
		    // bad run time, small data set assumed (for demo)
		    while ((line = cbr.readLine()) != null) {
		        String[] sections = line.split(";");
		        
		        // sections[0] is the name, sections[1] is the synonyms, and sections[2] is the patientSolutions
		        String name = sections[0];
		        
		        ArrayList<String> synonyms = new ArrayList<String>(Arrays.asList(sections[1].split(",")));
		        Characteristic ch = new Characteristic(name, synonyms);
		        
		        if (sections.length > 2) {
			        ArrayList<String> solutions = new ArrayList<String>(Arrays.asList(sections[2].split(",")));
			       
			        
			        
			        for (String sm : solutions) {
			        	int separaterIndex = sm.indexOf('-');
			        	String solution = sm.substring(0, separaterIndex);
			        	double multiplier = Double.parseDouble(sm.substring(separaterIndex + 1));
			        	
			        	for (Solution s : solutionsList) {
			        		if (solution.equals(s.getName())) {
			        			ch.addSolution(s, multiplier);
			        		}
			        	}
			        	
			        }
		        }
		        characteristicsList.add(ch);
		    }
		    
		    return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//	Add Characteristic and Solution Nodes to Graph
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// some methods/functions here might be unnecessary/bloated, review later
	
	/**
	 * Adds all characteristics in characteristicsList to the graph
	 */
	public void addListCharacteristics() {
		for (Characteristic ch : characteristicsList) {
			addCharacteristic(ch);
		}
	}
	
	/**
	 * Adds a characteristic to the graph
	 * 
	 * Each characteristic being added must exist in the characteristic list and not already be in the graph
	 */
	public void addCharacteristic(Characteristic ch) {
		
		if (characteristicInGraph(ch)) { // if characteristic is already in graph it will not be added
			System.out.println("The characteristic : " + ch.toString() + " is already in the graph");
		} else if (!characteristicInList(ch)) { // characteristics must be in characteristics list to be added to graph
			System.out.println("The characteristic : " + ch.toString() + " does not exist in the characteristic list");
		} else {
			
			Node chNode = new Node(ch); // creates node for characteristic
			characteristicNodes.add(chNode); // adds node to list of characteristic nodes 
			
			connectCenterToCharacteristic(chNode); // connects characteristic node to the center node
		
			
			
			
			ArrayList<SolutionMultiplierPair> chSolutions = ch.getSolutions();
			
			for (SolutionMultiplierPair chSolution : chSolutions) { 
				Solution s = chSolution.getSolution();
				Node sNode = null;
				
				if (solutionInGraph(s)) {
					sNode = getSolutionNode(s);
				} else {
					sNode = createSolutionNode(s);
				}
				
				if (!checkNodesConnected(chNode, sNode)) {
					double m = chSolution.getMultiplier();
					connectCharacteristicToSolution(chNode, sNode, m);
				}
			}
			
			//printGraph();
		}
	}
	
	/**
	 * Creates a solution node for a given solution and adds it to solutionNodesList
	 */
	private Node createSolutionNode(Solution solution) {
		if (solutionInGraph(solution)) {
			System.out.println("The solution " + solution.getName() + " is already in the graph");
			return null;
		} else if (!solutionInList(solution)) {
			System.out.println("The solution " + solution.getName() + " is not in the list of possible solutions");
			return null;
		}
		
		Node sNode = new Node(solution);
		solutionNodes.add(sNode);
		
		return sNode;
	}
	
	/**
	 * Adds all solutions in solution list to graph
	 */
	private void addListSolutions() {
		for (Solution s : solutionsList) {
			createSolutionNode(s);
		}
	}
	
	/**
	 * Adds a solution connected to one characteristic node to the graph
	 */
	private void addSolution(Solution solution, Node chNode) {
		Node sNode = createSolutionNode(solution);
		
		// sNode = null if solution is already in graph or solution is not in the list of possible solutions
		if (sNode != null && ((Characteristic)(chNode.getSubject())).solutionInCharacteristicSolutions(solution)) {
			double multiplier = ((Characteristic)(chNode.getSubject())).getMultiplier(solution);
			connectCharacteristicToSolution(chNode, sNode, multiplier);
		} else if (sNode == null && ((Characteristic)(chNode.getSubject())).solutionInCharacteristicSolutions(solution)){
			sNode = getSolutionNode(solution);
			if (sNode != null) {
				double multiplier = ((Characteristic)(chNode.getSubject())).getMultiplier(solution);
				connectCharacteristicToSolution(chNode, sNode, multiplier);
			}
		}
		
	}
	
	/**
	 * Adds a solution connected to some number of characteristic nodes to the graph
	 */
	private void addSolution(Solution solution, ArrayList<Node> characteristicNodes) {
		for (Node chNode : characteristicNodes) {
			addSolution(solution, chNode);
		}
	}

	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//	Create edges between Nodes in Graph
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	
	/**
	 * Creates an edge between the center node and a characteristic node (chNode), weight starts at 0, multiplier = 1
	 */
	private void connectCenterToCharacteristic(Node chNode) {
		Edge edge = new Edge(centerNode, chNode, 0.0, 1); // creates edge
		centerNode.addEdgeFromNode(edge); // adds edge to center node's edge list
		chNode.addEdgeToNode(edge); // adds edge to characteristic node's edge list
	}
	
	/** 
	 *Creates an edge between a characteristic node (chNode) and a solution node (sNode) with multiplier m
	 */
	private void connectCharacteristicToSolution(Node chNode, Node sNode, double multiplier) {
		Edge edge = new Edge(chNode, sNode, 0.0, multiplier); // creates edge
		chNode.addEdgeFromNode(edge); // adds edge to characteristic node's edge list
		sNode.addEdgeToNode(edge); // adds edge to solution node's edge list
	}
	
	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// Increments characteristic sent to graph
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	
	/**
	 * increments a given characteristic by 1, returns true if successful
	 */
	public boolean incrementCharacteristic(Characteristic ch) {
		return this.incrementCharacteristic(ch, 1.0);
	}
	
	public boolean incrementCharacteristic(Characteristic ch, int weight) {
		return incrementCharacteristic(ch, 0.0 + weight);
	}
	
	/**
	 * increments a given characteristic by a double amount, returns true if successful
	 */
	public boolean incrementCharacteristic(Characteristic ch, Double weight) {
		
		if (!characteristicInGraph(ch)) {
			return false;
		}
		
		Node chNode = getCharacteristicNode(ch); 
		if (chNode == null) {
			return false;
		}
		
		chNode.incrementWeight(weight);
		centerNode.incrementWeight(weight);
		
		return true;
	}
	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// Return solution from graph
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	
	// returns solution with highest weight (does not return weight)
	public Solution getTopSolution() {
		Collections.sort(solutionNodes);
		
		return (Solution)solutionNodes.get(0).getSubject();
	}
	
	// returns array of all solutions, sorted by weight (does not return weight)
	public Solution[] getTopSolutionsArray() {
		Collections.sort(solutionNodes);
		
		Solution[] topSolutions = new Solution[solutionNodes.size()];
		
		for (int i = 0; i < topSolutions.length; i++) {
			topSolutions[i] = (Solution)solutionNodes.get(i).getSubject();
		}
		
		return topSolutions;
	}
	/*
	Set a node to be enabled or disabled (Works for Characteristics)
	*/
	public boolean checkNodeEnabled(Characteristic ch) {
		return getCharacteristicNode(ch).isEnabled();
	}
	public boolean checkNodeEnabled(Solution s) {
		return getSolutionNode(s).isEnabled();
	}
	
	public boolean checkEdgesEnabled(Characteristic ch, Solution s) {
		Node parent_node = getCharacteristicNode(ch);
		Node child_node = getSolutionNode(s);
		
		PriorityQueue<Edge> edges = parent_node.getEdgesFromNode();
		/*
		for each edge e in edges, if e.endNode == childNode
			return e.isEnabled
		*/
		
		// Creating an iterator 
        Iterator value = edges.iterator(); 
  
        // Displaying the values after iterating through the queue 
        System.out.println("The iterator values are: "); 
        while (value.hasNext()) { 
        	Edge e = (Edge) value.next();
            System.out.println(e.toString()); 
            if (((Edge) e).getEndNode().equals(child_node)) {
            	return e.isEnabled();
            }
        }
        return false; //If it does not find an edge between the characteristics and solutions
	}
	
	//Ability to enable/disable a characteristic node
	public void setNodeEnabled(Characteristic ch, boolean enabled) {
		getCharacteristicNode(ch).setEnabled(enabled);
		
	}
	
	//Ability to enable/disable a solution node
	public void setNodeEnabled(Solution s, boolean enabled) {
		getSolutionNode(s).setEnabled(enabled);
	}
	
	//Ability to enable/disable an edge
	public void setEdgeEnabled(Characteristic ch, Solution s, boolean enabled) {
		Node parent_node = getCharacteristicNode(ch);
		Node child_node = getSolutionNode(s);
		
		PriorityQueue<Edge> edges = parent_node.getEdgesFromNode();
	
        Iterator value = edges.iterator(); 
  
        // Displaying the values after iterating through the queue 
        //System.out.println("The iterator values are: "); 
        while (value.hasNext()) { 
        	Edge e = (Edge) value.next();
            //System.out.println(e.toString()); 
            if (((Edge) e).getEndNode().equals(child_node)) {
            	 e.setEnabled(enabled);
            }
        }
	}
	
	/*
	 * Set a node to be enabled or disabled (Works for solutions)
	 */
	public boolean enableOrDisable(Solution s) {
		return getSolutionNode(s).isEnabled();
	}
		
	//%%%%%%%%%%%%%%%%
	// Helper methods
	//%%%%%%%%%%%%%%%%
	
	/**
	 * Checks if a characteristic exists in the characteristic list
	 */
	private boolean characteristicInList(Characteristic ch) {
		for (Characteristic ch2 : characteristicsList) {
			if (ch.equals(ch2)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if a characteristic is already in the graph
	 * 	Assumes that all characteristics are attached to center node
	 */
	private boolean characteristicInGraph(Characteristic ch) {
		for (Edge e : centerNode.getEdgesFromNode()) {
			if (((Characteristic)(e.getEndNode().getSubject())).equals(ch)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Checks if a solution exists in the solutions list
	 */
	private boolean solutionInList(Solution s) {
		for (Solution s2 : solutionsList) {
			if (s.equals(s2)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if a solution is already in the graph
	 */
	private boolean solutionInGraph(Solution s) {
		if (getSolutionNode(s) != null) {
			System.out.println("\nSolution " + s.getName() + " is already in the graph");
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns node for characteristic from characteristicNodes list
	 */
	private Node getCharacteristicNode(Characteristic ch) {
		
		for (Node n : characteristicNodes) {
			if (((Characteristic)n.getSubject()).equals(ch)) {
				return n;
			}
		}
		
		return null;
	}
	/**
	 * Returns node for solution from solutionNodes list
	 */
	private Node getSolutionNode(Solution s) {
		for (Node node : solutionNodes) {
			if (((Solution)node.getSubject()).equals(s)) {
				return node;
			}
		}
		return null;
	}
	
	// I don't remember writing this and I don't know where it's used
	public ArrayList<Node> getCharacteristicsInGraph() {
		ArrayList<Node> chInG = new ArrayList<Node>();
		
		return chInG;
	}
	
	
	/**
	 * Returns true if node 1 is connected to node 2 in any way
	 * 	includes non-logical connections, ie node 1 has an edge to node 2, but node 2 does not have an edge to node 1
	 */
	public boolean checkNodesConnected(Node n1, Node n2) {
		
		for (Edge e : n1.getEdgesFromNode()) {
			if (e.getEndNode().equals(n2)) {
				return true;
			}
		}
		for (Edge e : n2.getEdgesFromNode()) {
			if (e.getEndNode().equals(n1)) {
				return true;
			}
		}
		for (Edge e : n1.getEdgesToNode()) {
			if (e.getStartNode().equals(n2)) {
				return true;
			}
		}
		for (Edge e : n2.getEdgesToNode()) {
			if (e.getStartNode().equals(n1)) {
				return true;
			}
		}
		
		
		return false;
	}
	
	public ArrayList<Characteristic> getCharacteristicsList(){
		return characteristicsList;
	}
	
	public ArrayList<Solution> getSolutionsList() {
		return solutionsList;
	}
	/**
	 * Returns string containing all nodes and connections
	 * 	returns centerNode and all connections to characteristic nodes, 
	 * 	characteristic nodes and all connections to solution nodes,
	 * 	and solution nodes and all connections to characteristic nodes
	 */
	public String toString() {
		
		StringBuilder s = new StringBuilder();
		
		s.append("Center node:\t[centerNode, weight : edgeWeightIn*multiplier=edgeWeightOut, enabled : characteristic, weight, enabled]\n");
		for (Edge e : centerNode.getEdgesFromNode()) {
			s.append("\t[");
			s.append(centerNode.getSubjectName());
			s.append(", ");
			s.append(centerNode.getWeight());
			s.append(" : ");
			s.append(e.getWeightIn());
			s.append("*");
			s.append(e.getMultiplier());
			s.append("=");
			s.append(e.getWeightOut());
			s.append(", ");
			s.append((e.isEnabled() ? "edge enabled" : "edge disabled"));
			s.append(" : ");
			s.append(e.getEndNode().getSubjectName());
			s.append(", ");
			s.append(e.getEndNode().getWeight());
			s.append(", ");
			s.append(e.getEndNode().isEnabled() ? "node enabled" : "node disabled");
			//s.append(e.getEndNode().isEnabled());
			s.append("]\n");
		}
		
		s.append("\nCharacteristic nodes:\t[characteristicNode, weight, enabled; solutionNode1, weight, enabled : edgeWeightIn*multiplier=edgeWeightOut, enabled; ...]\n");
		for (Node n : characteristicNodes) {

			s.append("\t[");
			s.append(n.getSubjectName());
			s.append(", ");
			s.append(n.getWeight());
			s.append(", ");
			s.append(n.isEnabled() ? "node enabled" : "node disabled");
			//s.append(n.isEnabled());
			
			for (Edge e : n.getEdgesFromNode()) {
				s.append("; ");
				
				s.append(e.getEndNode().getSubjectName());
				s.append(", ");
				s.append(e.getEndNode().getWeight());
				s.append(", ");
				s.append(e.getEndNode().isEnabled() ? "node enabled" : "node disabled");
				//s.append(e.getEndNode().isEnabled());
				s.append(" : ");
				
				
				s.append(e.getWeightIn());
				s.append("*");
				s.append(e.getMultiplier());
				s.append("=");
				s.append(e.getWeightOut());
				s.append(", ");
				s.append(e.isEnabled() ? "edge enabled" : "edge disabled");
				//s.append(e.isEnabled());
		
			}
			
			s.append("]\n");
		}
		s.append("\nSolution nodes:\t[solutionNode, weight, enabled; characteristicNode1, weight, enabled : edgeWeightIn*multiplier=edgeWeightOut, enabled; ...]\n");
		for (Node n : solutionNodes) {
			
			s.append("\t[");
			s.append(n.getSubjectName());
			s.append(", ");
			s.append(n.getWeight());
			s.append(", ");
			s.append(n.isEnabled() ? "node enabled" : "node disabled");
			//s.append(n.isEnabled());
			
			for (Edge e : n.getEdgesToNode()) {
				s.append("; ");
				
				s.append(e.getStartNode().getSubjectName());
				s.append(", ");
				s.append(e.getStartNode().getWeight());
				s.append(", ");
				s.append(e.getStartNode().isEnabled() ? "node enabled" : "node disabled");
				s.append(" : ");
				
				
				
				s.append(e.getWeightIn());
				s.append("*");
				s.append(e.getMultiplier());
				s.append("=");
				s.append(e.getWeightOut());
				s.append(", ");
				//s.append(e.isEnabled());
				s.append(e.isEnabled() ? "edge enabled" : "edge disabled");
			}
			
			s.append("]\n");
		}
		
		return s.toString();
	}		
	
}
