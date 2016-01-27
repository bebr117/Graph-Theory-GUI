package allTheStuff;

import processing.core.PApplet;

public class TextBox implements Displayable {
	double x1;
	double y1;
	double boxWidth;
	double boxHeight;
	String text;
	int size;
	int boxColor;
	int textColor;
	int rounding;

	public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor, String text,
			int size, int rounding) {
		this.x1 = x1;
		this.y1 = y1;
		this.boxWidth = boxWidth;
		this.boxHeight = boxHeight;
		this.textColor = textColor;
		this.text = text;
		this.size = size;
		this.boxColor = boxColor;
		this.rounding = rounding;
	}

	static int panelGreen = 0xFF458B00;
	
	public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor, String text,
			int size) {
		this(boxColor, x1, y1, boxWidth, boxHeight, textColor, text, size, 10);
	}

	public TextBox(double x1, double y1, double boxWidth, double boxHeight, int textColor, String text, int size) {
		this(panelGreen, x1, y1, boxWidth, boxHeight, textColor, text, size);
	}

	public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, String text, int size) {
		this(boxColor, x1, y1, boxWidth, boxHeight, 0xFF000000, text, size);
	}

	public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor, String text) {
		this(boxColor, x1, y1, boxWidth, boxHeight, textColor, text, 15);
	}

	public TextBox(double x1, double y1, double boxWidth, double boxHeight, String text, int size) {
		this(x1, y1, boxWidth, boxHeight, 0xFF000000, text, size);
	}

	public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, String text) {
		this(boxColor, x1, y1, boxWidth, boxHeight, 0xFF000000, text);
	}

	public TextBox(double x1, double y1, double boxWidth, double boxHeight, int textColor, String text) {
		this(x1, y1, boxWidth, boxHeight, textColor, text, 15);
	}

	public TextBox(double x1, double y1, double boxWidth, double boxHeight, String text) {
		this(x1, y1, boxWidth, boxHeight, 0xFF000000, text);
	}

	public void create(PApplet main) {
		main.rectMode(PApplet.CORNER);
		main.fill(boxColor);
		main.stroke(boxColor);
		main.rect((float) x1, (float) y1, (float) boxWidth, (float) boxHeight, rounding);
		main.textAlign(PApplet.CENTER, PApplet.CENTER);
		main.fill(textColor);
		main.textSize(size);
		main.text(text, (float) x1, (float) y1, (float) boxWidth, (float) boxHeight);
	}

	public boolean inBox(int x, int y) {
		return (x1 <= x && x <= x1 + boxWidth && y1 <= y && y <= y1 + boxHeight);
	}

	public void setText(String newText) {
		text = newText;
	}

	public double getWidth() {
		return boxWidth;
	}

	public double getHeight() {
		return boxHeight;
	}

	public double getX() {
		return x1;
	}

	public double getY() {
		return y1;
	}
}