package PuzzleRMI;

import java.io.IOException;

public class FirstPlayer extends PuzzleBoardManagerImpl{

    public FirstPlayer(int port, int row, int columns, String imagePath) throws IOException {
        super(port);
        //creazione parte server del peer
        this.createRegistry(port);

        //creazione dei parametri iniziali
        createInitParams(row, columns, imagePath);

        this.puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.createTiles();
        this.positions = puzzleBoard.getPositions();
        puzzleBoard.display(true);
    }


}
