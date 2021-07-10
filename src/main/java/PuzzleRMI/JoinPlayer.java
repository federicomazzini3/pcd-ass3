package PuzzleRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JoinPlayer extends AbstractPuzzleBoardManager {

    public JoinPlayer(int port, String friendAddress, int friendPort) throws NotBoundException, RemoteException {
        super(port);

        //creazione parte server del peer
        this.createRegistry(port);

        //connessione al giocatore specificato
        this.connect(friendAddress, friendPort);

        this.retrieveInitParams();

        this.retrievePositions();

        puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.setPositions(this.positions);
        puzzleBoard.display(true);
    }

    private void connect(String friendAddress, int friendPort) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(friendAddress, friendPort);
        PuzzleBoardManager friendManager = (PuzzleBoardManager) registry.lookup("manager");

        //aggiungo tra i miei manager tutti quelli che conosce il primo manager
        this.addToManagers(friendManager.getManagers());
    }

    private void retrieveInitParams() throws RemoteException {
        PuzzleBoardManager friendManager = this.getFirstManager();
        if (friendManager != null) {
            InitParams friendParams = friendManager.getInitParams();
            InitParams params = new InitParams(friendParams.getRows(), friendParams.getColumns(), friendParams.getImage());
            this.initParams = params;
            log("Retrieve initParams: " + this.initParams.getRows() + ", " + this.initParams.getColumns() + " from Peer-" + friendManager.getId());
        } else
            System.out.println("Can't retrieve init params, i don't know manager");
    }

    private void retrievePositions() throws RemoteException {
        PuzzleBoardManager friendManager = this.getFirstManager();

        if (friendManager != null) {
            this.positions = friendManager.getPositions();
            log("Retrieve positions from Peer" + friendManager.getId());
        } else
            System.out.println("Can't retrieve positions, i don't know manager");
    }
}
