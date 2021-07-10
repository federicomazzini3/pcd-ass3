package PuzzleRMI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PuzzleBoardManager extends Remote {

    void createRegistry(int port) throws RemoteException;

    void addManager(PuzzleBoardManager friendManager) throws RemoteException;

    void addToManagers(List<PuzzleBoardManager> friendManagers) throws RemoteException;

    PuzzleBoardManager getFirstManager() throws RemoteException;

    List<PuzzleBoardManager> getManagers() throws RemoteException;

    long getId() throws RemoteException;

    long getPort() throws RemoteException;

    InitParams getInitParams() throws RemoteException;

    List<Position> getPositions() throws RemoteException;

    void updatePosition(List<Position> positions) throws RemoteException;

    void updatePosition(Position position1, Position position2) throws RemoteException;
}
