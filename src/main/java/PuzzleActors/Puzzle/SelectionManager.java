package PuzzleActors.Puzzle;

public class SelectionManager {

    private boolean selectionActive = false;
    private Tile selectedTile;

    public void selectTile(final Tile tile, final Listener listener) {

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

    private void swap(final Tile t1, final Tile t2) {
        int pos = t1.getCurrentPosition();
        t1.setCurrentPosition(t2.getCurrentPosition());
        t2.setCurrentPosition(pos);
    }

    public void deselection(){
        this.selectionActive = false;
    }

    @FunctionalInterface
    interface Listener {
        void onSwapPerformed(Tile tile1, Tile tile2);
    }
}

