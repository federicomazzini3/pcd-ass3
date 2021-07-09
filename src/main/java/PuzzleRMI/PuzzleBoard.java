package PuzzleRMI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class PuzzleBoard extends JFrame{
	
	final int rows, columns;
	private List<Tile> tiles = new ArrayList<>();
	
	private SelectionManager selectionManager = new SelectionManager();
	private final JPanel board;

    public PuzzleBoard(final int rows, final int columns, final byte[] imagePath) {
    	this.rows = rows;
		this.columns = columns;

    	setTitle("PuzzleCentralized");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        
        createTiles(imagePath);
        paintPuzzle();
    }

    public void display(boolean flag){
        SwingUtilities.invokeLater(() -> this.setVisible(flag));
    }

    /** Popola una lista di oggetti Tile, i quali sono composti da immagine, posizione originale immagine e posizione corrente immagine*/
    private void createTiles(final byte[] imageRaw) {
		final BufferedImage image;
        
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageRaw);
            image = ImageIO.read(bis);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
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
	}

	/** Data una collezione di Tile, inserisce al'interno del JPanel board ogni Tile e aggiunge un listener per le eventuali modifiche*/
    private void paintPuzzle() {
        SwingUtilities.invokeLater(() -> {

            board.removeAll();

            Collections.sort(tiles);

            tiles.forEach(tile -> {
                final TileButton btn = new TileButton(tile);
                board.add(btn);
                btn.setBorder(BorderFactory.createLineBorder(Color.gray));
                btn.addActionListener(actionListener -> {
                    selectionManager.selectTile(tile, () -> {
                        paintPuzzle();
                        checkSolution();
                    });
                });
            });

            pack();
            setLocationRelativeTo(null);
        });
    }

    private void checkSolution() {
    	if(tiles.stream().allMatch(Tile::isInRightPlace)) {
    	    SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
            });
    	}
    }
}
