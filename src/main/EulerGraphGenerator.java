package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EulerGraphGenerator {
	/** Adjacency matrix. First index denote 'u' vertex, second index is 'v' vertex. And if (u, v) exists, then edge[u][v] != 0.*/
	private int edge[][];
	/** Graph density. It is value between 0 and 1. Zero is not allowed. One means graph will be complete.
	 *  Value d means that graph will have q edges, where d = q/ec. Where ec is number of edges in complete graph = n(n-1)/2 */
	private double density;
	/** Number of vertices */
	private int vSize;
	/** Flag set to true if graph should be directed */
	private boolean isDirected = false;
	
	public EulerGraphGenerator(String[] parameters) {
		parseParameters(parameters);
		edge = new int[vSize][vSize];
	}
	
	/**
	 * This function fills edge matrix with values correspond to Eulerian Graph.
	 */
	public void generateEuler() {
		final int division = isDirected ? 1 : 2; // When graph is indirected, total amount of edges should be divide by 2.
		final int KnEdgesNumber = vSize * (vSize - 1) / division; //edge total number in complete graph with vSize vertices
		final int edgesThreshold = (int) (density * KnEdgesNumber); //number of edges, which should be created
		int currEdgesNumber = 0;
		// Start vertex:
		final int start = 0; //rand.nextInt(vSize);
		// Edge from u to v means: (u, v)
		int u = start, v = getNext(u, start);
		while (currEdgesNumber++ <= edgesThreshold && v != start) {
			addEdge(u, v);
			u = v;
			v = getNext(u, start); //could return 'start' vertex (when algorithm should stop)
		}
		
		finishCycle(u, start);
	}
	
	/**
	 * Getter for size of graph in number of vertices.
	 * @return size of graph = number of vertices
	 */
	public int graphSize() {
		return vSize;
	}
	
	/**
	 * Adding new edge (u, v) to the graph. Deals with direct and indirect graphs.
	 * @param u is index of begin of new edge. It is value from [0, graphSize()). 
	 * @param v is index of end of new edge. It's value from [0, graphSize()).
	 */
	private void addEdge(int u, int v) {
		System.out.println("Edge (" + u +", " + v + ").");
		edge[u][v] = 1;
		if (isDirected == false) 
			edge[v][u] = 1;
	}
	
	/**
	 * This method takes last vertex 'u' on Euler route and make path to 'start' vertex to close route and make Euler cycle.
	 * @param u is index of last vertex on Euler route (from 'start' to 'u')
	 * @param start is index of begin of route
	 */
	private void addFinishEdge(int u, int start) {
		if (edge[u][start] == 0)
			addEdge(u, start);
		else { // We cannot directly connect last vertex 'u' to the start point. THIS IS POSSIBLE ONLY FOR INDIRECT GRAPHS.
			// First we try to find any vertex which is not connected with 'u'.
			int vert = getNext(u, start); 
			// It is highly impossible - so method getNext() almost always return 'start' point (it is signal "'u' has already all connections")
			if (vert == start) //Vertex 'u' has all possible edges just created. 
				//Select any existing edge. But not bridge to startPoint:
				vert = Integer.max(start+1, u + 1) % vSize; //either vertex next to startPoint or next to U-point
			//Change edge (u, vert) into route: (u, start), (start, vert):
			removeEdge(u, vert);
			//edge (u, start) exists, so there lack edge from 'start' to somewhere:
			addEdge(start, vert);
		}
	}
	
	/**
	 * Removes edge (u, v) - either if exists or not.
	 * @param u index of begin of edge to remove
	 * @param v index of end of edge to remove
	 */
	private void removeEdge(int u, int v) {
		edge[u][v] = 0;
		if (isDirected == false) 
			edge[v][u] = 0;
	}
	
	/**
	 * This method search for any free connection from vertex 'u' to any other vertex except startPoint.
	 * Generally it use random number generator to draw vertex of end of free edge.
	 * If there is no possibility to choose any edge, then this method returns startPoint which means
	 * that only possible edge is return to start point (and consequently close the Euler path).
	 * It is assumed that start point is reachable from any vertex (it is not truth for first created edge in Euler path).
	 * @param u is index of begin of new edge
	 * @param startPoint is index of start vertex
	 * @return index of vertex which could be the end of new edge
	 */
	private int getNext(int u, int startPoint) {
		int v = u; // index of second vertex
		int count = 0;
		Random rand = new Random();
		// Looking for free edge (excluding self-edge) - draw only finite number of times
		while ((v == u || edge[u][v] == 1 || v == startPoint) && count++ < vSize) 
			v = rand.nextInt(vSize);
		
		// return vertex if drawing was finished because of selection free edge
		if (count < vSize)
			return v;
		
		// else, iterate throw all edges from u and search for free one (not self and not starting point)
		v = startPoint; //starting point maneuver - this value will be returned when all iteration will fail
		for (int i = 0; i < vSize; ++i)
			if (edge[u][i] == 0 && u != i && i != startPoint) 
				v = i;
		
		// Apply starting point maneuver - if all others edges was created before, then return starting point - close cycle.
		return v;
	}
	
	/**
	 * Finish cycle procedure cares about all condition while closing Euler path to make Euler cycle.
	 * In this step if there are any isolated vertices, then all of them will be connected and included to Euler path in this method.
	 * @param u last connected vertex to Euler path 
	 * @param startPoint index of vertex which begins Euler path
	 */
	private void finishCycle(int u, int startPoint) {
		// Search for isolated vertices. This graph should be one strongly connected component. So this vertices should be on Euler cycle.
		List<Integer> isolatedPoint = new ArrayList<>();
		for(int j = 0; j < vSize; ++j) {
			int sum = 0;
			for(int i = 0; i < vSize; ++i)
				sum += edge[i][j];
			if (sum == 0)
				isolatedPoint.add(j);
		}
		
		//All points on isolatedPoint list have no edges, so they can be connected in any way:
		for(int v: isolatedPoint) {
			addEdge(u, v);
			u = v;
		}
		
		// Last step: Finish cycle (if necessary):
		if (u != startPoint)
			addFinishEdge(u, startPoint);
	}
	

	/**
	 * Save graph in Vertex Format.
	 * @param fileName which will contain graph structure
	 */
	public void saveInVertexForm(String fileName) {
		PrintWriter file;
		try {
			file = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("Cannot open file \"" + fileName + "\" to read.");
			System.err.println(e.getMessage());
			return;
		}
		for (int i = 0; i < vSize; ++i) {
			file.print("" + i + " : ");
			for (int j = 0; j < vSize; ++j) {
				if (edge[i][j] != 0)
					file.print("" + j + " ");
			}
			file.println();
		}
		file.close();
	}
	
	/**
	 * Message, which should be presented in the case of any error in typing command-line parameters.
	 */
	public static void printAbout() {
		System.out.println("This application creates new graph (Euler or NotEuler).");
		System.out.println("Usage: ");
		System.out.println("java EulerGraphGenerator verticesAmount density [direct|indirect] [NotEuler]");
		System.out.println("Where: ");
		System.out.println("  - 'verticesAmount' is a positive number of vertices in new graph. This should be value greater than 3.");
		System.out.println("  - 'density' denote fraction of created edges to all possible edges in complete graph. It is value from (0; 1]. 0 is not allowed.");
		System.out.println("  - [direct|indirect] it is two flags (switches) which enable creating direct or indirect graphs.");
		System.out.println();
	}
	
	/**
	 * Reads and parse all command-line arguments to find parameters required to generate new Euler graph.
	 * @param args - command-line arguments
	 */
	private void parseParameters(String[] args) {
		try {
			if (args.length < 2)
				throw new NumberFormatException("Too few parameters.");

			int size = Integer.parseInt(args[0]);
			double density = Double.parseDouble(args[1]);
			this.isDirected = (args.length >= 3) ? args[2].toLowerCase().equals("direct") : false;
			
			if (size < 3)
				throw new NumberFormatException("Too low value for number of vertices");
			this.vSize = size;
			
			if (density <= 0 || density > 1)
				throw new NumberFormatException("Wrong density value.");
			this.density = density;
			
		} catch (NumberFormatException e) {
			System.out.println("Parameter format exception");
			printAbout();
			throw new IllegalArgumentException(e);
		}
	}
	
	public static void main(String[] args) {
		EulerGraphGenerator egen = new EulerGraphGenerator(args);
		egen.generateEuler();
		egen.saveInVertexForm("pliczeczek");
	}

}
