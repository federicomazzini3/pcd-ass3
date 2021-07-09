package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Controller {

    PuzzleBoardManagerImpl nodeManager;

    public Controller() {
    }

    public void notifyJoin(int port, String friendAddress, int friendPort) {
        ((Runnable) () -> {
            try {
                nodeManager = new JoinPlayer(port, friendAddress, friendPort);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }).run();
    }

    public void notifyStart(int port, int rows, int columns, String imageUrl) {
        ((Runnable) () -> {
            try {
                nodeManager = new FirstPlayer(port, rows, columns, imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();
    }
}
