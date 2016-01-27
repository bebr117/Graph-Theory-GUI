package allTheStuff;

import java.util.ArrayList;
import java.util.HashMap;
import processing.core.*;


//TODO(?): Change storage method to adjacency matrix.
/**
 * @ author Brian Reinhart
 *
 * A class for creating graphs for use in Graph Theory.
 */
public class Graph implements Displayable {
	private ArrayList<Vertex> vertices;
	private ArrayList<Edge> edges;
	private TextBox wrapBox;
	private double vtxScale = 0.02;
	public double vtxRadius;
	private Vertex badV = new Vertex(-1, -1);
	private Vertex selectedVertex = badV;
	public boolean displayWeights = false;
	private int defaultVtxColor;
	
	// TODO add private SetlikeThing<Vertex> selectedVertices;

	/**
	 * A constructor which takes an enclosing TextBox and creates a Graph which
	 * will fit in that TextBox.
	 */
	public Graph(TextBox t) {
		vertices = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		wrapBox = t; // The box containing the graph,
		vtxRadius = vtxScale * Math.min(wrapBox.getWidth(), wrapBox.getHeight());
		defaultVtxColor = 0xFF000000; // black
	}

	public Graph(TextBox t, int defaultColor) {
		this(t);
		defaultVtxColor = defaultColor;
	}

	/**
	 * Adds a vertex at the given x,y ratio in the box.
	 * 
	 * @param x
	 *            The x-coordinate of the new vertex, as a fraction of the
	 *            width.
	 * @param y
	 *            The y-coordinate of the new vertex, as a fraction of the
	 *            height.
	 */
	public void addVertex(double x, double y) {
		vertices.add((new Vertex(x, y)).setColor(defaultVtxColor));
	}

	/**
	 * Adds a vertex with the given x,y coordinates on-screen.
	 * 
	 * @param x
	 *            The x-coordinate of the new vertex.
	 * @param y
	 *            The y-coordinate of the new vertex.
	 */
	public void addVertexAt(double x, double y) {
		double newX = scaleX(x);
		double newY = scaleY(y);
		addVertex(newX, newY);
	}

	public void addEdge(Vertex v1, Vertex v2) {
		if (!v1.isAdjacent(v2)) {
			v1.addEdge(v2);
			v2.addEdge(v1);
			edges.add(new Edge(v1, v2));
		}
	}

	public void addEdge(int index1, int index2) {
		addEdge(vertices.get(index1), vertices.get(index2));
	}

	/*
	 * public void addEdge(Vertex v1, Vertex v2, boolean directed) { if
	 * (edges.contains(new Edge(v1,v2)) || edges.contains(new Edge(v1,
	 * v2,directed))) { return; } if (!edges.contains(new Edge(v2, v1))) {
	 * v1.addEdge(v2); v2.addEdge(v1); } edges.add(new Edge(v1, v2,directed)); }
	 */

	public double[][] vertexPositions() {
		double[][] ans = new double[vertices.size()][2];
		for (int i = 0; i < ans.length; i++) {
			Vertex v = vertices.get(i);
			ans[i] = new double[] { getXPos(v), getYPos(v) };
		}
		return ans;
	}

	public int[][] edgeEndpointIndices() throws GraphComponentNotFoundException {
		int[][] ans = new int[edges.size()][2];
		for (int i = 0; i < ans.length; i++) {
			Edge e = edges.get(i);
			int v1index = vertices.indexOf(e.getTail());
			int v2index = vertices.indexOf(e.getHead());
			if (v1index < 0 || v2index < 0) {
				throw new GraphComponentNotFoundException("One of the edges is not in the graph!");
			} else {
				ans[i] = new int[] { v1index, v2index };
			}
		}
		return ans;
	}

	public Edge getEdgeByIndex(int index) {
		return edges.get(index);
	}

	public boolean indexedEdgeIsDirected(int index) {
		return getEdgeByIndex(index).isDirected();
	}

	public int getIndexOfEdge(Vertex v1, Vertex v2) throws GraphComponentNotFoundException {
		if (!v1.isAdjacent(v2)) {
			throw new GraphComponentNotFoundException("No edge here!");
		} else {
			for (int i = 0; i < edges.size(); i++) {
				if (edges.get(i).endpointsAre(v1, v2)) {
					return i;
				}
			}
			return -1;
		}
	}

	public void removeVertex(Vertex v) throws GraphComponentNotFoundException {
		if (vertices.contains(v)) {
			for (Vertex v2 : v.getAdjacents().keySet()) {
				edges.remove(getIndexOfEdge(v, v2));
				v2.removeEdge(v);
			}
			vertices.remove(v);
		} else {
			return;
		}
	}

	public void removeEdgeByIndex(int index) throws GraphComponentNotFoundException {
		Edge e = edges.get(index);
		e.v1.removeEdge(e.v2);
		e.v2.removeEdge(e.v1);
		edges.remove(index);
	}

	public void removeEdge(Vertex v1, Vertex v2) throws GraphComponentNotFoundException {
		removeEdgeByIndex(getIndexOfEdge(v1, v2));
	}

	/**
	 * Gets the x-position, on the screen, of the center of the vertex.
	 * 
	 * @param v
	 *            The vertex to get the x-position of.
	 * @return The x-position of the vertex.
	 */
	public double getXPos(Vertex v) {
		return wrapBox.getX() + wrapBox.getWidth() * v.getX();
	}

	/**
	 * Gets the y-position, on the screen, of the center of the vertex.
	 * 
	 * @param v
	 *            The vertex to get the y-position of.
	 * @return The x-position of the vertex.
	 */
	public double getYPos(Vertex v) {
		return wrapBox.getY() + wrapBox.getHeight() * v.getY();
	}

	/**
	 * Should mostly be used as a part of code like the following:
	 * 
	 * <pre>
	 *  
	 * Vertex v = getVertex(mouseX,mouseY); 
	 * if (v.isValid()){ 
	 * 	blah 
	 * }// else { 
	 * 	//do nothing 
	 * //}
	 * 
	 * </pre>
	 * 
	 * Gets the vertex at a given screen position. Returns badV (invalid vertex)
	 * if no vertex is found. Use for selecting vertices with the mouse. Note
	 * that the area in which vertices will be selected is square, not circular.
	 * 
	 * @param xPos
	 *            The x position of the mouse.
	 * @param yPos
	 *            The y position of the mouse.
	 * @return
	 */
	public Vertex getVertex(double xPos, double yPos) {
		for (Vertex v : vertices) {
			if (Math.abs(getXPos(v) - xPos) <= vtxRadius && Math.abs(getYPos(v) - yPos) <= vtxRadius) {
				return v;
			}
		}
		return badV;
	}

	public boolean isValid(Vertex v) {
		return v.isValid();
	}

	public void selectVertex(double xPos, double yPos) {
		selectedVertex = getVertex(xPos, yPos);
		selectedVertex.grab(xPos, yPos);
	}

	public void deselect() {
		selectedVertex = badV;
	}

	public Vertex selectedVertex() {
		return selectedVertex;
	}

	public void setBox(TextBox wrapper) {
		wrapBox = wrapper;
	}

	public TextBox getBox() {
		return wrapBox;
	}

	public void create(PApplet main) {
		main.stroke(0);
		main.strokeWeight(3);
		for (int i = 0; i < edges.size(); i++) {
			Edge e = edges.get(i);
			Vertex head = e.getHead();
			Vertex tail = e.getTail();
			main.line((float) getXPos(head), (float) getYPos(head), (float) getXPos(tail), (float) getYPos(tail));
			main.fill(0);
			if (displayWeights) {
				e.displayWeight(main);
			}
			/*
			if (this.indexedEdgeIsDirected(i)) {
				// TODO display arrow.
			}
			*/
		}
		main.strokeWeight(1);
		for (int i = 0; i < vertices.size(); i++) {
			Vertex vtx = vertices.get(i);
			main.fill(vtx.getColor());
			main.ellipse((float) getXPos(vtx), (float) getYPos(vtx), (float) (2 * this.vtxRadius),
					(float) (2 * this.vtxRadius));
		}
		if (selectedVertex.isValid()) {
			Vertex vtx = selectedVertex;
			main.noFill();
			main.stroke(0xFFFF0000);
			main.ellipse((float) getXPos(vtx), (float) getYPos(vtx), (float) (4 * this.vtxRadius),
					(float) (4 * this.vtxRadius));
		}
	}

	public void release() {
		selectedVertex.release();
	}

	public double scaleX(double x) {
		return (x - wrapBox.getX()) / wrapBox.getWidth();
	}

	public double scaleY(double y) {
		return (y - wrapBox.getY()) / wrapBox.getHeight();
	}

	public void updatePos(Vertex v, double x, double y) {
		v.setCoords(scaleX(x) - v.Xoffset, scaleY(y) - v.Yoffset);
	}

	/**
	 * @author Brian Reinhart
	 *
	 *         A class for vertices in a graph.
	 */
	class Vertex {
		// Instance variables
		private HashMap<Vertex, Integer> adjacent;
		// These should be fractions of width/height.
		private double x;
		private double y;
		private int color; 
		
		private double Xoffset;
		private double Yoffset;

		private boolean dragged = false;
		// Every vertex has these properties, regardless of
		// algorithm, so it doesn't go in the HashMap

		private HashMap<String, Object> properties = new HashMap<String, Object>();

		// Constructors
		public Vertex() {
			adjacent = new HashMap<Vertex, Integer>();
		}

		public Vertex(Vertex... adjacents) {
			adjacent = new HashMap<Vertex, Integer>();
			for (Vertex v : adjacents) {
				adjacent.put(v, 1);
			}
		}

		public Vertex(double myX, double myY) {
			this();
			x = myX;
			y = myY;
		}

		public Vertex(double myX, double myY, Vertex... adjacents) {
			this(adjacents);
			x = myX;
			y = myY;
		}

		/**
		 * Adds an adjacent vertex. Named for convenience in the Graph addEdge
		 * method.
		 * 
		 * @param v
		 *            The vertex which we are adding an edge to.
		 */
		public void addEdge(Vertex v) {
			adjacent.put(v, 1);
		}

		public void addEdge(Vertex v, int weight) {
			adjacent.put(v, weight);
		}

		public void removeEdge(Vertex v) throws GraphComponentNotFoundException {
			if (isAdjacent(v)) {
				adjacent.remove(v);
			} else {
				throw new GraphComponentNotFoundException("No edge to remove!");
			}
		}

		public boolean isAdjacent(Vertex v) {
			return adjacent.containsKey(v);
		}

		public boolean isValid() {
			return !equals(badV);
		}

		public HashMap<Vertex, Integer> getAdjacents() {
			return adjacent;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double snapToRange(double num, double lBound, double uBound){
			if (num < lBound){
				return lBound;
			} else if (num > uBound){
				return uBound;
			} else {
				return num;
			}
		}
		
		public void setCoords(double x2, double y2) {
			x = snapToRange(x2,0,1);
			y = snapToRange(y2,0,1);

		}

		public Vertex setColor(int c) {
			color = c;
			return this;
		}

		public int getColor() {
			return color;
		}

		public void grab(double x, double y) {
			dragged = true;
			Xoffset = scaleX(x) - this.x;
			Yoffset = scaleY(y) - this.y;
		}

		public void release() {
			dragged = false;
		}

		public boolean isDragging() {
			return dragged;
		}
	}

	/**
	 * 
	 * @author Brian Reinhart
	 *
	 *         A class for edges in a graph.
	 */
	class Edge {
		private Vertex v1;
		private Vertex v2;
		private int weight;
		
		public Edge(Vertex one, Vertex two) {
			v1 = one;
			v2 = two;
			weight = 1;
		}

		public Edge(Vertex one, Vertex two, int myWeight) {
			v1 = one;
			v2 = two;
			weight = myWeight;
		}

		public Edge(Vertex one, Vertex two, boolean directed) {
			this(one, two);
			this.directed = directed;
		}

		public Edge(Vertex one, Vertex two, int myWeight, boolean directed) {
			this(one, two, directed);
			weight = myWeight;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int myWeight) {
			weight = myWeight;
		}

		public Vertex getTail() {
			return v1;
		}

		public Vertex getHead() {
			return v2;
		}

		public boolean isDirected() {
			return directed;
		}

		public boolean endpointsAre(Vertex a, Vertex b) {
			return (a == v1 && b == v2) || (a == v2 && b == v1);
		}

		private PVector getMidpoint() {
			return new PVector((float) ((getXPos(v1) + getXPos(v2)) / 2), (float) ((getYPos(v1) + getYPos(v2)) / 2));
		}

		private double getSlope() {
			if (v2.getX() - v1.getX() != 0) {
				return (v2.getY() - v1.getY()) / (v2.getX() - v1.getX());
			} else {
				return Integer.MAX_VALUE;
			}
		}

		public PVector getWeightLocation() {
			double perpSlope = -1 / getSlope();
			float angle = PApplet.atan((float) perpSlope);
			// Here, we get displacement by an amount of .01 in a perpendicular
			// direction
			float xDisp = 15 * PApplet.cos(angle);
			float yDisp = 15 * PApplet.sin(angle);
			return new PVector(getMidpoint().x + xDisp, getMidpoint().y + yDisp);
		}

		public void displayWeight(PApplet main) {
			main.text("" + weight, getWeightLocation().x, getWeightLocation().y);
		}
		/*
		 * public boolean equals(Edge e) { // Sorry for the long return
		 * statements, just checks that all of the // properties (except weight)
		 * are the same, letting v1 and v2 be interchangeable if // directed is
		 * false.
		 */
		/*
		 * if (directed) { return (e.getTail() == v1 && e.getHead() == v2 &&
		 * e.isDirected() == directed); } else { return ((e.getTail() == v1 &&
		 * e.getHead() == v2) || (e.getTail() == v2 && e.getHead() == v1)) &&
		 * e.directed == directed; } }
		 */
		private boolean directed = false;
	}
}