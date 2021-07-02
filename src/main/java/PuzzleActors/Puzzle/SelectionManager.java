package PuzzleActors.Puzzle;

import PuzzleActors.BoardActor;

public class SelectionManager {

    private boolean selectionActive = false;
    private BoardActor.Tile selectedTile;

    public void selectTile(final BoardActor.Tile tile, final Listener listener) {

        if (selectionActive) {
            selectionActive = false;

            swap(selectedTile, tile);

            if (selectedTile != tile)
                listener.onSwapPerformed(selectedTile, tile);
        } else {
            selectionActive = true;
            selectedTile = tile;
        }
    }

    private void swap(final BoardActor.Tile t1, final BoardActor.Tile t2) {
        int pos = t1.getCurrentPosition();
        t1.setCurrentPosition(t2.getCurrentPosition());
        t2.setCurrentPosition(pos);
    }

    @FunctionalInterface
    interface Listener {
        void onSwapPerformed(BoardActor.Tile tile1, BoardActor.Tile tile2);
    }
}

