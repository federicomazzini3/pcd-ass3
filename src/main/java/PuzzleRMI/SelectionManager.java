package PuzzleRMI;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;

	public void selectTile(final Tile tile, final Listener listener) {
		
		if(selectionActive) {
			selectionActive = false;
			
			selectedTile.swap(tile);
			
			listener.onSwapPerformed();
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}
	
	@FunctionalInterface
	interface Listener{
		void onSwapPerformed();
	}
}
