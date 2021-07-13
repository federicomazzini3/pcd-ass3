package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;

public class InitialController {

    public void notifyJoin(int port, String friendAddress, int friendPort) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                new JoinPlayer(port, friendAddress, friendPort);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void notifyStart(int port, int rows, int columns, String imageUrl) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                new FirstPlayer(port, rows, columns, imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
