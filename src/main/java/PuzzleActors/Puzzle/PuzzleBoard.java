package PuzzleActors.Puzzle;

import PuzzleActors.BoardActor;
import akka.actor.typed.ActorRef;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class PuzzleBoard extends JFrame {

    final int rows, columns;
    String imagePath;
    byte[] imageRaw;
    private ArrayList<Tile> tiles = new ArrayList<>();
    private SelectionManager selectionManager = new SelectionManager();
    private ActorRef<BoardActor.Command> puzzleActor;
    private final JPanel board;

    public PuzzleBoard(final int rows, final int columns, String imagePath, ActorRef<BoardActor.Command> puzzleActor) {
        this.rows = rows;
        this.columns = columns;
        this.imagePath = imagePath;
        this.imageRaw = imageRaw;
        this.puzzleActor = puzzleActor;

        setTitle("PuzzleCentralized");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    /**
     * Popola una lista di oggetti Tile, i quali sono composti da immagine, posizione originale immagine e posizione corrente immagine
     */
    public void createTiles() {
        final BufferedImage image;

        try {
            //image = ImageIO.read(new ByteArrayInputStream(this.imageRaw));
            image = ImageIO.read(new URL(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows * columns).forEach(item -> {
            randomPositions.add(item);
        });
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

    public void createAndLoadTiles() {
        createTiles();
        puzzleActor.tell(new BoardActor.Tiles(createTileRaw(tiles)));
        paintPuzzle();
    }

    public void refreshTiles(BoardActor.Tiles tiles) {
        if (this.tiles.size() == 0)
            createTiles();
        for (BoardActor.TileRaw tileRaw : tiles.tiles) {
            for (Tile tile : this.tiles) {
                if (tile.getOriginalPosition() == tileRaw.originalPosition)
                    tile.setCurrentPosition(tileRaw.currentPosition);
            }
        }
        selectionManager.deselection();
        paintPuzzle();
    }

    /**
     * Data una collezione di Tile, inserisce al'interno del JPanel board ogni Tile e aggiunge un listener per le eventuali modifiche
     */
    private void paintPuzzle() {
        this.board.removeAll();

        Collections.sort(tiles);

        tiles.forEach(tile -> {
            final TileButton btn = new TileButton(tile);
            this.board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> {
                selectionManager.selectTile(tile, (tile1, tile2) -> {
                    puzzleActor.tell(new BoardActor.Swap(tile1, tile2));
                });
            });
        });
        pack();
        this.setVisible(true);
        checkSolution();
    }

    public void updateTiles(Tile tile1, Tile tile2) {
        tiles.remove(tile1);
        tiles.remove(tile2);
        tiles.add(tile1);
        tiles.add(tile2);
        this.paintPuzzle();
    }

    private void checkSolution() {
        if (tiles.stream().allMatch(Tile::isInRightPlace)) {
            JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private ArrayList<BoardActor.TileRaw> createTileRaw(ArrayList<Tile> tiles) {
        ArrayList<BoardActor.TileRaw> tilesRaw = new ArrayList<>();
        for (Tile tile : tiles) {
            tilesRaw.add(new BoardActor.TileRaw(tile.getOriginalPosition(), tile.getCurrentPosition()));
        }
        return tilesRaw;
    }
}
