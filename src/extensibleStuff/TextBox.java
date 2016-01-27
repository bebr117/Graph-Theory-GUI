package extensibleStuff;

public class TextBox {
  double x1;
  double y1;
  double boxWidth;
  double boxHeight;
  String text;
  int size;
  int boxColor;
  int textColor;
  int rounding;

  public TextBox(int boxColor, double x1, double y1, double boxWidth, double boxHeight, int textColor, String text, int size, int rounding) {
    this.x1=x1;
    this.y1=y1;
    this.boxWidth=boxWidth;
    this.boxHeight=boxHeight;
    this.textColor=textColor;
    this.text=text;
    this.size=size;
    this.boxColor=boxColor;
    this.rounding=rounding;
  }

  public boolean inBox(int x, int y) {
    return (x1 <= x && x <= x1+boxWidth && y1 <= y && y <= y1+boxHeight);
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