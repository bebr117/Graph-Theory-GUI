package extensibleStuff;

import java.util.ArrayList;
import controlP5.ControlP5;
import processing.core.PApplet;

class State implements Displayable {
		ArrayList<Displayable> contents;
		int bgColor;
		String instrText;
		ControlP5 cp5;
		boolean validState = true;
		int substate = 0;

		public State(ArrayList<Displayable> textBoxes, int backgroundColor, String instructionText,
				ControlP5 controller) {
			contents = textBoxes;
			bgColor = backgroundColor;
			instrText = instructionText;
			cp5 = controller;
		}

		public State(ArrayList<Displayable> textBoxes, int backgroundColor, String instructionText, PApplet main) {
			this(textBoxes, backgroundColor, instructionText, new ControlP5(main));
		}

		public State(ArrayList<Displayable> textBoxes, int backgroundColor, PApplet main) {
			this(textBoxes, backgroundColor, " ", main);
		}

		public State(int backgroundColor, String instructionText, PApplet main) {
			this(new ArrayList<Displayable>(), backgroundColor, instructionText, main);
		}

		public State(int backgroundColor, PApplet main) {
			this(backgroundColor, " ", main);
		}

		public State() {
			// So MyTextBox won't break.
			validState = false;
		}

		public void addDisplayable(Displayable d) {
			contents.add(d);
		}

		public void addDisplayable(Displayable... displayables) {
			for (Displayable d : displayables) {
				contents.add(d);
			}
		}

		public boolean isValid() {
			return validState;
		}

		private boolean cp5Contains(String name) {
			return cp5.get(name).equals(null);
		}

		public void leaveState() {
			cp5.setVisible(false);
		}

		public void enterState() {
			cp5.setVisible(true);
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

		public void create(PApplet main) {
			main.background(bgColor);
			for (Displayable d : contents) {
				d.create(main);
			}
		}
	
		public String instructionText(){
			return instrText;
		}
	}