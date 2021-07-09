package PuzzleRMI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PuzzleBoardManager extends Remote {

    void addManager(PuzzleBoardManager friendManager) throws RemoteException;

    List<PuzzleBoardManager> getManagers() throws RemoteException;

    long getId() throws RemoteException;

    long getPort() throws RemoteException;

    void createInitParams(int rows, int columns, String image) throws IOException, RemoteException;

    InitParams getInitParams() throws RemoteException;

    List<Position> getPositions() throws RemoteException;

    void updatePosition(List<Position> positions) throws RemoteException;

    void swap(Position position1, Position position2) throws RemoteException;
}
