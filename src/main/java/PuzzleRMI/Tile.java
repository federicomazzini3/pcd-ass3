package PuzzleRMI;

import java.awt.*;

public class Tile implements Comparable<Tile>{
	private Image image;
	private Position positions;

    public Tile(final Image image, final int originalPosition, final int currentPosition) {
        this.image = image;
        this.positions = new Position(originalPosition, currentPosition);
    }
    
    public Image getImage() {
    	return image;
    }
    
    public boolean isInRightPlace() {
    	return positions.isInRightPlace();
    }

    public int getOriginalPosition() {
        return positions.getOriginalPosition();
    }

    public int getCurrentPosition() {
        return positions.getCurrentPosition();
    }
    
    public void setCurrentPosition(final int newPosition) {
    	this.positions.setCurrentPosition(newPosition);
    }

    public void swap(final Tile otherTile){
        int oldPosition = this.positions.getCurrentPosition();
        this.positions.setCurrentPosition(otherTile.getCurrentPosition());
        otherTile.setCurrentPosition(oldPosition);
    }

    public Position getPositions(){
        return positions;
    }

	@Override
	public int compareTo(Tile other) {
		return this.getCurrentPosition() < other.getCurrentPosition() ? -1
				: (this.getCurrentPosition() == other.getCurrentPosition() ? 0 : 1);
	}
}
