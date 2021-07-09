package PuzzleRMI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PuzzleBoardManager extends Remote {

    void addManager(PuzzleBoardManager friendManager) throws RemoteException;

    List<PuzzleBoardManager> getManagers() throws RemoteException;

    long getId() throws RemoteException;

    void createInitParams(int rows, int columns, String image) throws IOException, RemoteException;

    InitParams getInitParams() throws RemoteException;

    void updateTiles(List<Tile> tiles) throws RemoteException;

    void swap(Tile tile1, Tile tile2) throws RemoteException;
}
