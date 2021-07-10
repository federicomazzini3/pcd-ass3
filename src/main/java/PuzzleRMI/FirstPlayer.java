package PuzzleRMI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FirstPlayer extends AbstractPuzzleBoardManager {

    public FirstPlayer(int port, int row, int columns, String imagePath) throws IOException {
        super(port);
        //creazione parte server del peer
        this.createRegistry(port);

        //creazione dei parametri iniziali
        this.createInitParams(row, columns, imagePath);

        this.puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.createTiles();
        puzzleBoard.display(true);
        this.createPositions(puzzleBoard.getPositions());
    }

    private void createInitParams(int rows, int columns, String image) throws IOException {
        byte[] imageRaw = Files.readAllBytes(new File(image).toPath());
        this.initParams = new InitParams(rows, columns, imageRaw);
        log("Create initParams: " + this.initParams.getRows() + ", " + this.initParams.getColumns());
    }

    private void createPositions(List<Position> positions){
        this.positions = new ArrayList<>(positions);
        log("Create first tile's positions");
    }
}
