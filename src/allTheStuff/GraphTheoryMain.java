package allTheStuff;

import processing.core.*;
import processing.data.IntDict;

import java.util.ArrayList;
import controlP5.*;

// CAN DO: Make ButtonBox a Clickable subclass of TextBox, give TextBox the create method and let it implement Displayable.
// TODO: Add "display weights" checkBox option.
// TODO: Implement edge weight modification and Dijkstra's. Then DONE!

// Bonus dux
// TODO: Make right clicking a shortcut for dragging vertices.
// TODO: Add vertex color changing option.
// This means new radioButton option, new switch clause, etc.

public final class GraphTheoryMain extends PApplet {

	class ButtonBox extends TextBox implements Clickable {
		State linksTo = new State();

		// Yes, the order of variables is weird. It's so that the different
		// constructors know which is which.
		public ButtonBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor,
				String text, int size) {
			super(boxColor, x1, y1, boxWidth, boxHeight, textColor, text, size, RECT_ROUNDING);
		}

		public ButtonBox(double x1, double y1, double boxWidth, double boxHeight, int textColor, String text,
				int size) {
			this(panelGray, x1, y1, boxWidth, boxHeight, textColor, text, size);
		}

		public ButtonBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, String text, int size) {
			this(boxColor, x1, y1, boxWidth, boxHeight, wordColor, text, size);
		}

		public ButtonBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor,
				String text) {
			this(boxColor, x1, y1, boxWidth, boxHeight, textColor, text, normalTextSize);
		}

		public ButtonBox(double x1, double y1, double boxWidth, double boxHeight, String text, int size) {
			this(x1, y1, boxWidth, boxHeight, wordColor, text, size);
		}

		public ButtonBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, String text) {
			this(boxColor, x1, y1, boxWidth, boxHeight, wordColor, text);
		}

		public ButtonBox(double x1, double y1, double boxWidth, double boxHeight, int textColor, String text) {
			this(x1, y1, boxWidth, boxHeight, textColor, text, normalTextSize);
		}

		public ButtonBox(double x1, double y1, double boxWidth, double boxHeight, String text) {
			this(x1, y1, boxWidth, boxHeight, wordColor, text);
		}

		public ButtonBox setLink(State state) {
			linksTo = state;
			return this;
		}

		public State getLink() {
			return linksTo;
		}

		public boolean isLinkButton() {
			return linksTo.isValid();
		}

		public boolean isClicked(int x, int y) {
			return super.inBox(x, y);
		}

		// Use this to determine if it's an arrow button in the Graph Viewer.
		public boolean isArrowButton() {
			return (getArrowValue() != 0);
		}

		public void onClick() {
			if (isLinkButton()) {
				changeState(linksTo);
			} else if (isArrowButton()) {
				setGraph(viewedGraphIndex + getArrowValue());
			}
			// continue as you see fit.
		}

		// should return 1 for a right arrow, -1 for a left arrow.
		// really dumb code, but I don't want to add another input to the
		// constructors.
		public int getArrowValue() {
			if (text.equals(">")) {
				return 1;
			} else if (text.equals("<")) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	// amount that TextBoxes are rounded.
	final static int RECT_ROUNDING = 10;

	// colors
	int bgBlue = color(0, 154, 205);
	int panelGreen = color(69, 139, 0);
	int panelGray = color(170, 170, 170);
	int arrowPanelGray = color(148, 148, 148);
	int wordColor = color(0, 0, 0);
	int grayedOutColor = color(17, 17, 17); // untested and unused

	// The normal text size in a text box
	static int normalTextSize = 15;

	// ControlP5 fixItLater = new ControlP5(this);

	// states
	private State MAIN_MENU_STATE = new State();
	private State GRAPH_VIEW_STATE = new State();
	private State GRAPH_EDIT_STATE = new State();

	private final int NULL_SUBSTATE = 0;
	private final int ADD_VERTEX_SUBSTATE = 1;
	private final int ADD_EDGE_SUBSTATE = 2;
	private final int DELETE_VERTEX_SUBSTATE = 3;
	private final int DELETE_EDGE_SUBSTATE = 4;
	private final int MOVE_VERTEX_SUBSTATE = 5;
	private final int CHANGE_WEIGHT_SUBSTATE = 6;

	// I believe this is how much the vertex is going to be scaled compared to
	// the screen size.
	double vtxScale = .02;
	// The index of the graph being viewed.
	int viewedGraphIndex = 0;
	// Some frequently reused boxes.
	ButtonBox backBox;
	TextBox graphBox;
	TextBox graphNumBox;
	TextBox instructionBox;
	TextBox errorBox;
	ButtonBox addGraphBox;
	// FOR displayError TESTING
	// MyTextBox derpBox;

	// A dictionary which
	IntDict vtxColors = new IntDict();

	// variables used to initialize states.
	State currentState;
	int editSubstate;
	boolean displayWeights = false;

	// Error message display.
	boolean displayError = false;
	long errorStartTime;
	int displayDuration;
	// FOR displayError TESTING
	// boolean throwError = false;

	// Check if the state was just changed, to prevent unnecessary method calls.
	boolean stateChanged = false;

	// Set to true to override the default instructionBox text from the State.
	boolean overrideInstrText = false;
	String altInstrText = " ";

	// The list of graphs. TODO: make this save between uses.
	ArrayList<Graph> graphs = new ArrayList<Graph>();

	/**
	 * A wrapper for {@code graphs.size() != 0}
	 * 
	 * @return Returns true if the list of graphs is nonempty, and false
	 *         otherwise.
	 */
	boolean hasGraphs() {
		return (graphs.size() != 0);
	}

	void addGraph() {
		graphs.add(new Graph(graphBox, vtxColors.get("Blue")));
	}

	/**
	 * A wrapper for {@code graphs.get(viewedGraphIndex)}
	 * 
	 * @return Returns the graph currently being viewed in the GUI.
	 */
	Graph viewedGraph() {
		return graphs.get(viewedGraphIndex);
	}

	/**
	 * Sets the viewed graph to the graph with the given index, accounting for
	 * wrap.
	 * 
	 * @param newGraphIndex
	 *            The index of the graph to be viewed.
	 */
	void setGraph(int newGraphIndex) {
		if (!hasGraphs()) {
			viewedGraphIndex = 0;
		} else {
			int n = graphs.size();
			viewedGraphIndex = (newGraphIndex % n) + (newGraphIndex < 0 ? n : 0);
		}
	}

	/**
	 * Gets the radius of the vertices based on the size of the graphBox.
	 * 
	 * @return The default radius of the vertices in the main graph viewing
	 *         states.
	 */
	double vtxRadius() {
		return vtxScale * (graphBox.getWidth() + graphBox.getHeight()) / 2;
	}

	/**
	 * Displays an error for a set amount of time.
	 * 
	 * @param dispDuration
	 *            The amount of time the error is displayed.
	 * @param text
	 *            The text to display for the error.
	 */
	void displayError(int dispDuration, String text) {
		displayError = true;
		errorStartTime = millis();
		displayDuration = dispDuration;
		// errorMessage = text;
		errorBox.setText(text);
	}

	/**
	 * Displays an error for 2 seconds. Simply calls
	 * {@code displayError(2000,text)}
	 * 
	 * @param text
	 *            The text to display for the error.
	 */
	void displayError(String text) {
		// We'll display it for 2 seconds.
		displayError(2000, text);
	}

	// Program start functions.

	/**
	 * Sets the different possible vertex colors.
	 */
	private void setVtxColors() {
		vtxColors.set("Red", 0xFFFF0000);
		vtxColors.set("Blue", 0xFF0000FF);
		vtxColors.set("Green", 0xFF00FF00);
		vtxColors.set("Yellow", 0xFF00FFFF);
		vtxColors.set("Purple", 0xFF9400D3);
		vtxColors.set("Pink", 0xFFFF00FF);
		vtxColors.set("Brown", 0xFF8B4513);
		vtxColors.set("Grey", 0xFFD3D3D3);
		vtxColors.set("Black", 0xFF000000);
	}

	/**
	 * Sets the text boxes in the states.
	 */
	private void setTextBoxes() {
		MAIN_MENU_STATE.addDisplayable(
				new TextBox(width * .1, height * .1, width * .8, height * .3,
						"Welcome to Graphinator!\n(Working Title)", 30),
				new ButtonBox(panelGreen, width * .1, height * .6, width * .8, height * .1, "Graph viewer")
						.setLink(GRAPH_VIEW_STATE),
				new ButtonBox(panelGreen, width * .1, height * .8, width * .8, height * .1, "Graph editor")
						.setLink(GRAPH_EDIT_STATE));

		GRAPH_VIEW_STATE.addDisplayable(1, backBox, graphNumBox, instructionBox,
				// FOR displayError TESTING
				// derpBox,
				new ButtonBox(arrowPanelGray, 0, height * .2, width * .1, height * .6, 0xFFFFFFFF, "<"),
				new ButtonBox(arrowPanelGray, width * .9, height * .2, width * .1, height * .6, 0xFFFFFFFF, ">"),
				new ButtonBox(width * .75, height * .05, width * .2, height * .05, "Edit mode", 10)
						.setLink(GRAPH_EDIT_STATE));
		GRAPH_VIEW_STATE.addDisplayable(graphBox);

		GRAPH_EDIT_STATE.addDisplayable(1, backBox, instructionBox, addGraphBox,
				new ButtonBox(arrowPanelGray, 0, height * .2, width * .1, height * .6, 0xFFFFFFFF, "<"),
				new ButtonBox(arrowPanelGray, width * .9, height * .2, width * .1, height * .6, 0xFFFFFFFF, ">"),
				new ButtonBox(width * .75, height * .05, width * .2, height * .05, "View mode", 10)
						.setLink(GRAPH_VIEW_STATE)
		/*
		 * new MyTextBox(arrowPanelGray, 0, height*.2, width*.1, height*.6,
		 * 0xFFFFFF, "<"), new MyTextBox(arrowPanelGray, width*.9, height*.2,
		 * width*.1, height*.6, 0xFFFFFF, ">"),
		 */
		);
		GRAPH_EDIT_STATE.addDisplayable(graphBox);
	}

	/**
	 * Initializes the different states, as well as any interesting cp5
	 * elements.
	 */
	private void setStates() {
		MAIN_MENU_STATE = new State("Main menu", bgBlue, this);
		GRAPH_VIEW_STATE = new State("View graphs", panelGreen, "Click the arrows to scroll between the graphs!", this);
		GRAPH_EDIT_STATE = new State("Edit graphs", panelGreen, "Use the buttons to edit the graph!", this);

		GRAPH_EDIT_STATE.cp5().addRadioButton("editSubstate").setPosition(width * .1f, height * .8f + 10)
				.setSize(width * 4 / 25, height * 3 / 100).align(CENTER, CENTER)
				.setItemsPerRow((int) ((width - 100) / 150)).setSpacingColumn(40).setSpacingRow(10)
				.addItem("Add vertices", ADD_VERTEX_SUBSTATE).addItem("Add edges", ADD_EDGE_SUBSTATE)
				.addItem("Remove vertices", DELETE_VERTEX_SUBSTATE).addItem("Remove edges", DELETE_EDGE_SUBSTATE)
				.addItem("Move vertices", MOVE_VERTEX_SUBSTATE);
		editSubstate = NULL_SUBSTATE;

		GRAPH_EDIT_STATE.cp5().addRadioButton("displayWeights").setPosition(width - 150, height - 50).setSize(75, 20)
				.align(CENTER, CENTER).addItem("Display Weights", 1)
				.setColorForeground(0xFFE5452F).setColorBackground(0xFFDC143C).setColorActive(0xFFEE7621);
		GRAPH_VIEW_STATE.cp5().addRadioButton("displayWeights").setPosition(width - 150, height - 50).setSize(75, 20)
				.align(CENTER, CENTER).addItem("Display Weights", 1)
				.setColorForeground(0xFFE5452F).setColorBackground(0xFFDC143C).setColorActive(0xFFEE7621);
		// GRAPH_VIEW_STATE.cp5().getList().add(weightOption); Maybe I can get
		// this to work at some point?
	}

	/**
	 * Initializes the MyTextBoxes not specific to given states, such as the
	 * graph box.
	 */
	private void setSpecialBoxes() {
		backBox = new ButtonBox(width * .05, height * .05, width * .2, height * .05, "Back", 10);
		backBox.setLink(MAIN_MENU_STATE);
		graphBox = new TextBox(panelGray, 0, height * .2, width, height * .6, " ");
		graphNumBox = new TextBox(panelGray, width * .4, height * .8 + 5, width * .2, height * .05, "No graphs", 10);
		instructionBox = new TextBox(panelGray, width * .2, height * .12, width * .6, height * .06,
				"Click the arrows to scroll between the graphs!", 10);
		errorBox = new TextBox(panelGray, width * .3, 0, width * .4, height * .1, " ", 8);
		addGraphBox = new ButtonBox(panelGray, width * .4, height * .05, width * .2, height * .05, "Add graph", 10);
		// FOR displayError TESTING
		// derpBox = new MyTextBox(panelGray, width*.1, height*.8+5, width*.2,
		// height*.05, "Throw Error", 10);
	}

	/**
	 * A function to draw all of the things specific to each state. I don't
	 * really know why this is a separate function from draw.
	 */
	private void drawState() {
		// Start with clearing the board.
		clear();

		// Always convenient.
		Graph g = viewedGraph();

		// Stuff to change exactly once -- mostly TextBox text.
		if (stateChanged) {
			graphNumBox.setText(hasGraphs() ? "Graph #" + (viewedGraphIndex + 1) : "No graphs");
			if (!overrideInstrText) {
				instructionBox.setText(currentState.instructionText());
			} else {
				instructionBox.setText(altInstrText);
			}
			currentState.updateQueue();
			displayError = false;
			editSubstate = (int) ((RadioButton) GRAPH_EDIT_STATE.cp5().get("editSubstate")).getValue();
			if (editSubstate < NULL_SUBSTATE) {
				editSubstate = NULL_SUBSTATE;
			}
			g.displayWeights = displayWeights;
			stateChanged = false;
		}

		display(currentState);
		// FOR displayError TESTING
		/*
		 * try { if (throwError){ throw new GraphComponentNotFoundException(
		 * "Why would you click that?"); } } catch
		 * (GraphComponentNotFoundException e){ displayError(e.getMessage()); }
		 * finally { throwError = false; }
		 */
		if (displayError) {
			display(errorBox);
		}

		if (currentState.containsPart((TextBox) g.getBox())) {
			display(g);

		}
	}

	void display(Displayable d) {
		d.create(this);
	}

	/**
	 * A function that, based on the current state, will execute functions
	 * particular to that state. I don't like subclasses, which is basically the
	 * only reason this function exists.
	 * 
	 * @param state
	 *            The state for which specialClick determines
	 * @param x
	 * @param y
	 */
	void specialClick(State state, int x, int y) {
		Graph g = viewedGraph(); // Because of course.
		boolean disableSelect = false;
		try {
			switch (state.getName()) {
				// Could get more interesting, but so far only special stuff for
				// edit state

				case "Edit graphs":
					if (graphBox.inBox(x, y)) {
						switch (editSubstate) {

							case ADD_VERTEX_SUBSTATE:
								if (!g.getVertex(x, y).isValid()) {
									g.addVertexAt(x, y);
								}
								disableSelect = true;
								break;

							case ADD_EDGE_SUBSTATE:
								if (g.selectedVertex().isValid()) {
									Graph.Vertex v = g.getVertex(x, y);
									if (v.isValid()) {
										g.addEdge(v, g.selectedVertex());
									}
									g.deselect();
									disableSelect = true;
								}
								break;

							case DELETE_VERTEX_SUBSTATE:
								if (g.getVertex(x, y).isValid()) {
									g.removeVertex(g.getVertex(x, y));
								}
								g.deselect();
								disableSelect = true;
								break;

							case DELETE_EDGE_SUBSTATE:
								if (g.selectedVertex().isValid()) {
									Graph.Vertex v = g.getVertex(x, y);
									if (v.isValid()) {
										if (v.isAdjacent(g.selectedVertex())) {
											g.removeEdge(v, g.selectedVertex());
											g.deselect();
										}
									} else {
										g.deselect();
									}
									disableSelect = true;
								}
								break;

							case CHANGE_WEIGHT_SUBSTATE:
								if (g.selectedVertex().isValid()) {
									Graph.Vertex v = g.getVertex(x, y);
									if (v.isValid()) {
										if (v.isAdjacent(g.selectedVertex())) {

										}
									} else {
										g.deselect();
									}
								}
								disableSelect = true;
								break;
						}
					}
					if (addGraphBox.inBox(x, y)) {
						addGraph();
						setGraph(-1);
					}
					if (disableSelect) {
						break;
					} // TODO make default code come after the switch statement
						// in an if(!disableSelect) block.
						// Also add breaks to the end of all switch cases.
						// Switch fall through is scary.

				default:
					if (currentState.containsPart((TextBox) g.getBox())) {
						// System.out.println("containsPart works.");
						// if (graphBox.inBox(x, y)) {
						// System.out.println("inBox works.");
						if (g.getVertex(x, y).isValid()) {
							// System.out.println("getVertex works");
							g.selectVertex(x, y);
						} else {
							g.deselect();
						}
						// }
					}
			}
		} catch (GraphComponentNotFoundException e) {
			displayError(e.getMessage());
		}
		// }
	}

	void changeState(State newState) {
		currentState.leaveState();
		currentState = newState;
		viewedGraph().deselect();
		currentState.enterState(displayWeights);
		stateChanged = true;
	}

	// Processing functions.

	/**
	 * CP5 IS FREAKING MAGIC.
	 * 
	 * @param a
	 *            The action taken. -1 if it's a deselect, otherwise it's the
	 *            index of the selected button.
	 */
	public void editSubstate(int a) {
		viewedGraph().deselect();
	}

	public void displayWeights(int a) {
		displayWeights = (a != -1);
	}

	@Override
	public void mouseClicked() {
		int x = mouseX;
		int y = mouseY;
		// specialClick(currentState, x, y);
		currentState.onClick(x, y);
		stateChanged = true;
		// FOR displayError TESTING
		/*
		 * if (tb == derpBox){ throwError = true; }
		 */
	}

	@Override
	public void mousePressed() {
		int x = mouseX;
		int y = mouseY;
		specialClick(currentState, x, y);
		stateChanged = true;
	}

	public void mouseDragged() {
		int x = mouseX;
		int y = mouseY;
		Graph g = viewedGraph();
		Graph.Vertex v = g.selectedVertex();
		if (currentState == GRAPH_EDIT_STATE && editSubstate == MOVE_VERTEX_SUBSTATE && v.isDragging()) {
			g.updatePos(v, x, y);
		}
	}

	public void mouseReleased() {
		viewedGraph().release();
	}

	public void settings() {
		size(800, 700);
	}

	@Override
	public void setup() {
		frameRate(60);

		// make sure setSpecialBoxes is before setTextBoxes, or this won't work.
		setVtxColors();
		setStates();
		setSpecialBoxes();
		setTextBoxes();
		currentState = MAIN_MENU_STATE;

		Graph sampleGraph = new Graph(graphBox, vtxColors.get("Blue"));
		sampleGraph.addVertexAt(width * .5, height * .5);
		sampleGraph.addVertexAt(width * .5, height * .7);
		sampleGraph.addEdge(0, 1);

		Graph K3 = new Graph(graphBox, vtxColors.get("Blue"));
		for (int i = 0; i < 3; i++) {
			K3.addVertexAt(width * (.3 + i * .2), height * .3);
			K3.addVertexAt(width * (.3 + i * .2), height * .7);
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				K3.addEdge(2 * i, 2 * j + 1);
			}
		}

		graphs.add(sampleGraph);
		graphs.add(K3);

		Graph forDijkstra = new Graph(graphBox, vtxColors.get("Blue"));
		forDijkstra.addVertexAt(width * .2, height * .5);
		forDijkstra.addVertexAt(width * .4, height * .7);
		forDijkstra.addVertexAt(width * .4, height * .3);
		forDijkstra.addVertexAt(width * .6, height * .7);
		forDijkstra.addVertexAt(width * .6, height * .3);
		forDijkstra.addVertexAt(width * .8, height * .5);

		forDijkstra.addEdge(0, 1);
		forDijkstra.addEdge(0, 2);
		forDijkstra.addEdge(1, 2);
		forDijkstra.addEdge(1, 3);
		forDijkstra.addEdge(1, 4);
		forDijkstra.addEdge(2, 4);
		forDijkstra.addEdge(3, 4);
		forDijkstra.addEdge(3, 5);
		forDijkstra.addEdge(4, 5);

		graphs.add(forDijkstra);
	}

	@Override
	public void draw() {
		drawState();
		if (displayError) {
			if (millis() - errorStartTime >= displayDuration) {
				displayError = false;
			}
		}
	}

	public static void main(String[] args) {
		PApplet.main("allTheStuff.GraphTheoryMain");
	}
}