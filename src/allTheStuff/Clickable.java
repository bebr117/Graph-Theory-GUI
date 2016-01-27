package allTheStuff;

public interface Clickable extends Displayable{
	
	public boolean isClicked(int xPos, int yPos);
	
	public void onClick();

}
