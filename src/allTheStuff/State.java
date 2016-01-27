package allTheStuff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import controlP5.*;
import processing.core.PApplet;

public class State implements Displayable {

	private class LayerOrder implements Comparator<Displayable> {
		public int compare(Displayable d1, Displayable d2) {
			return layer.get(d1) - layer.get(d2);
		}
	}

	private LayerOrder queueOrder = new LayerOrder();

	// Could change this into a set thingy.
	private ArrayList<Displayable> contents;
	// layer 0 is rendered first, comes out on the bottom.
	private HashMap<Displayable, Integer> layer = new HashMap<Displayable, Integer>();
	private PriorityQueue<Displayable> contentsQueue = new PriorityQueue<Displayable>();
	private int bgColor;
	private String instrText;
	private String desc;
	private ControlP5 cp5;
	private boolean validState = true;

	public State(String name, ArrayList<Displayable> textBoxes, int backgroundColor, String instructionText,
			ControlP5 controller) {
		contents = textBoxes;
		bgColor = backgroundColor;
		instrText = instructionText;
		cp5 = controller;
		desc = name;
	}

	public State(String name, ArrayList<Displayable> textBoxes, int backgroundColor, String instructionText,
			PApplet parent) {
		contents = textBoxes;
		bgColor = backgroundColor;
		instrText = instructionText;
		cp5 = new ControlP5(parent);
		cp5.setVisible(false);
		desc = name;
		for (Displayable d : contents) {
			layer.put(d, 0);
		}
		if (textBoxes.size() != 0) {
			updateQueue();
		}
	}

	public State(String name, ArrayList<Displayable> textBoxes, int backgroundColor, PApplet main) {
		this(name, textBoxes, backgroundColor, " ", main);
	}

	public State(String name, int backgroundColor, String instructionText, PApplet main) {
		this(name, new ArrayList<Displayable>(), backgroundColor, instructionText, main);
	}

	public State(String name, int backgroundColor, PApplet main) {
		this(name, backgroundColor, " ", main);
	}

	/**
	 * A dummy constructor; when initializing a state, you can set it to
	 * {@code new State()} so you can reference it, even if it's invalid.
	 */
	public State() {
		validState = false;
	}

	public void addDisplayable(Displayable d) {
		contents.add(d);
		layer.put(d, 0);
		updateQueue();
	}

	public void addDisplayable(Displayable... displayables) {
		for (Displayable d : displayables) {
			addDisplayable(d);
		}
	}

	public void addDisplayable(int layerNum, Displayable d) {
		contents.add(d);
		layer.put(d, layerNum);
		updateQueue();
	}

	public void addDisplayable(int layerNum, Displayable... displayables) {
		for (Displayable d : displayables) {
			addDisplayable(layerNum, d);
		}
	}

	public boolean isValid() {
		return validState;
	}

	public void leaveState() {
		cp5.setVisible(false);
	}
	public void enterState(boolean displayWeights) {
		cp5.setVisible(true);
		if (cp5.get("displayWeights") != null){
			RadioButton rb = (RadioButton) cp5.get("displayWeights");
			rb.deactivate(0);
			if (displayWeights){
				rb.activate(0);
			}
		}
	}

	public void onClick(int x, int y) {
		for (Displayable d : contents) {
			if (d instanceof Clickable) {
				Clickable c = (Clickable) d;
				if (c.isClicked(x, y)) {
					c.onClick();
				}
			}
		}
	}

	public void updateQueue() {
		contentsQueue = new PriorityQueue<Displayable>(contents.size(), queueOrder);
		contentsQueue.addAll(contents);
	}

	public void create(PApplet main) {
		updateQueue(); // Is this efficient? If not, is there a way to copy a
						// queue in order? Is .clone() better?
		main.background(bgColor);
		Iterator<Displayable> iter = contentsQueue.iterator();
		while (iter.hasNext()) {
			((Displayable) contentsQueue.poll()).create(main);
		}

	}

	public String instructionText() {
		return instrText;
	}

	public ArrayList<Displayable> contents() {
		return contents;
	}

	public ControlP5 cp5() {
		return cp5;
	}

	public ControllerInterface<?> getCP5Part(String part) {
		return cp5.get(part);
	}

	public boolean containsPart(Displayable d) {
		return contents.contains(d);
	}

	public String getName() {
		return desc;
	}
}