package PuzzleRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class JoinPlayer extends PuzzleBoardManagerImpl{

    public JoinPlayer(int port, String friendAddress, int friendPort) throws NotBoundException, RemoteException {
        super(port);

        //creazione parte server del peer
        createRegistry(port);

        //connessione al giocatore specificato
        this.connect(friendAddress, friendPort);

        this.retrieveInitParams();

        this.retrievePositions();

        puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.setPositions(this.positions);
        puzzleBoard.display(true);
    }
}
