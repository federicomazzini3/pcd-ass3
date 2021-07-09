package PuzzleRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client implements Runnable{

    private Client(String host) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host);
        InitParamsImpl params = (InitParamsImpl) registry.lookup("initParams");
        System.out.println("Params: " + params);



        final PuzzleBoard puzzle = new PuzzleBoard(params.getRows(), params.getColumns(), params.getImage());
        puzzle.display(true);
    }

    @Override
    public void run() {

    }
}
