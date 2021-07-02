package PuzzleActors.Puzzle;
import PuzzleActors.BoardActor;
import akka.actor.typed.ActorRef;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class PuzzleBoard extends JFrame {
	
	final int rows, columns;
	private List<Tile> tiles = new ArrayList<>();
	private SelectionManager selectionManager = new SelectionManager();
	private ActorRef<BoardActor.Command> puzzleActor;
	private final JPanel board;

    public PuzzleBoard(final int rows, final int columns, ActorRef<BoardActor.Command> puzzleActor) {
    	this.rows = rows;
		this.columns = columns;
		this.puzzleActor = puzzleActor;
    	
    	setTitle("PuzzleCentralized");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
    }

    /** Popola una lista di oggetti Tile, i quali sono composti da immagine, posizione originale immagine e posizione corrente immagine*/
    public List<Tile> createTiles(final String imagePath) {
		final BufferedImage image;
        
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;
        
        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows*columns).forEach(item -> { randomPositions.add(item); }); 
        Collections.shuffle(randomPositions);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
            	final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns, 
                        					i * imageHeight / rows, 
                        					(imageWidth / columns), 
                        					imageHeight / rows)));

                tiles.add(new Tile(imagePortion, position, randomPositions.get(position)));
                position++;
            }
        }
        paintPuzzle();
        return tiles;
	}

	public void initTiles(List<Tile> tile){
        this.tiles = tiles;
    }

	/** Data una collezione di Tile, inserisce al'interno del JPanel board ogni Tile e aggiunge un listener per le eventuali modifiche*/
    private void paintPuzzle() {
    	this.board.removeAll();
    	
    	Collections.sort(tiles);
    	
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton(tile);
            this.board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> {
            	selectionManager.selectTile(tile, (tile1, tile2) -> {
            		//paintPuzzle();
                    puzzleActor.tell(new BoardActor.Swap(tile1,tile2));
                	checkSolution();
            	});
            });
    	});
    	
    	pack();
        setLocationRelativeTo(null);
    }

    public void updateTiles(Tile tile1, Tile tile2){
        tiles.remove(tile1);
        tiles.remove(tile2);
        tiles.add(tile1);
        tiles.add(tile2);
        this.paintPuzzle();
    }

    private void checkSolution() {
    	if(tiles.stream().allMatch(Tile::isInRightPlace)) {
    		JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    	}
    }
}
