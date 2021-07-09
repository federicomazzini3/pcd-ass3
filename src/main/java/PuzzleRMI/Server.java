package PuzzleRMI;

import ch.qos.logback.core.util.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Runnable{

    private int port;
    private int rows;
    private int columns;
    private String image;

    public Server(int port, int rows, int columns, String image){
        this.port = port;
        this.rows = rows;
        this.columns = columns;
        this.image = image;
    }

    @Override
    public void run() {
        try {
            byte[] imageRaw = Files.readAllBytes((new File(image)).toPath());
            InitParams params = new InitParamsImpl(rows, columns, imageRaw);
            //InitParams paramsStub = (InitParams) UnicastRemoteObject.exportObject(params, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(5555);

            //registry.rebind("initParams", paramsStub);

            System.out.println("Objects registered.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
