package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Controller {

    PuzzleBoardManagerImpl nodeManager;

    public Controller(){
    }

    public void notifyJoin(int port, String friendAddress, int friendPort){
        new Runnable() {
            @Override
            public void run() {
                try {
                    nodeManager = new PuzzleBoardManagerImpl(port, friendAddress, friendPort);
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void notifyStart(int port, int rows, int columns, String imageUrl) {
        new Runnable() {
            @Override
            public void run() {
                try {
                    nodeManager = new PuzzleBoardManagerImpl(port, rows, columns, imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public synchronized void notifyStop() {
    }
}
