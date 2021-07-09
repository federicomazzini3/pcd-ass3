package PuzzleRMI;

import java.rmi.RemoteException;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;

	public void selectTile(final Tile tile, final Listener listener){
		
		if(selectionActive) {
			selectionActive = false;
			
			selectedTile.swap(tile);

			if (selectedTile != tile)
				listener.onSwapPerformed(selectedTile.getPositions(), tile.getPositions());
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}

	public void deselection(){
		this.selectionActive = false;
	}
	
	@FunctionalInterface
	interface Listener{
		void onSwapPerformed(Position position1, Position position2);
	}
}
