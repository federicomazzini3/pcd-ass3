package PuzzleActors.Puzzle;

import java.awt.*;
import java.util.Objects;

public class Tile implements Comparable<Tile>{
	private Image image;
	private int originalPosition;
	private int currentPosition;

    public Tile(final Image image, final int originalPosition, final int currentPosition) {
        this.image = image;
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public Image getImage() {
    	return image;
    }
    
    public boolean isInRightPlace() {
    	return currentPosition == originalPosition;
    }
    
    public int getCurrentPosition() {
    	return currentPosition;
    }
    
    public void setCurrentPosition(final int newPosition) {
    	currentPosition = newPosition;
    }

	@Override
	public int compareTo(Tile other) {
		return this.currentPosition < other.currentPosition ? -1 
				: (this.currentPosition == other.currentPosition ? 0 : 1);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return originalPosition == tile.originalPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalPosition);
    }
}
