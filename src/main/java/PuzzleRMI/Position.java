package PuzzleRMI;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Comparable<Position>, Serializable {
    int originalPosition;
    int currentPosition;

    public Position(final int originalPosition, final int currentPosition) {
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public boolean isInRightPlace() {
        return currentPosition == originalPosition;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(final int newPosition) {
        currentPosition = newPosition;
    }

    @Override
    public int compareTo(Position other) {
        return this.currentPosition < other.currentPosition ? -1
                : (this.currentPosition == other.currentPosition ? 0 : 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return originalPosition == position.originalPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalPosition);
    }
}
