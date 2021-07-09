package PuzzleRMI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PuzzleBoardManagerImpl implements PuzzleBoardManager {

    private Registry registry;
    private int port;
    private List<PuzzleBoardManager> managers;
    private InitParams initParams;
    private List<Tile> tiles;
    private Long id;

    public PuzzleBoardManagerImpl(int port, int row, int columns, String imagePath) throws IOException {
        this.id = new Random().nextLong();
        this.port = port;
        managers = new ArrayList<>();
        log("Create");

        //creazione parte server del peer
        createRegistry(port);

        //creazione dei parametri iniziali
        createInitParams(row, columns, imagePath);
    }

    public PuzzleBoardManagerImpl(int port, String friendAddress, int friendPort) throws NotBoundException, RemoteException {
        this.id = new Random().nextLong();
        this.port = port;
        managers = new ArrayList<>();
        log("Create");

        //creazione parte server del peer
        createRegistry(port);

        //connessione al giocatore specificato
        this.connect(friendAddress, friendPort);

        this.retrieveInitParams();
    }

    private void createRegistry(int port) throws RemoteException {
        registry = LocateRegistry.createRegistry(port);
        PuzzleBoardManager manager = (PuzzleBoardManager) UnicastRemoteObject.exportObject(this, port);
        registry.rebind("manager", manager);
        this.addManager(manager);
    }

    private void connect(String friendAddress, int friendPort) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(friendAddress, friendPort);
        PuzzleBoardManager friendManager = (PuzzleBoardManager) registry.lookup("manager");

        //aggiungo tra i miei manager tutti quelli che conosce il primo manager
        this.addToManagers(friendManager.getManagers());
    }

    private void addToManagers(List<PuzzleBoardManager> friendManagers) throws RemoteException {
        for (PuzzleBoardManager friendManager : friendManagers) {
            this.addManager(friendManager);
        }
    }

    @Override
    public void addManager(PuzzleBoardManager friendManager) throws RemoteException {
        if (!this.managers.contains(friendManager)) {
            this.managers.add(friendManager);
            friendManager.addManager(this);
            log("manager " + friendManager.getId() + " added");
        }
    }

    @Override
    public List<PuzzleBoardManager> getManagers() throws RemoteException {
        return this.managers;
    }

    private PuzzleBoardManager getFirstManager() throws RemoteException {
        for (PuzzleBoardManager manager : this.managers) {
            if (manager.getId() != this.getId())
                return manager;
        }
        System.out.println("No manager found");
        return null;
    }

    @Override
    public long getId() {
        return id;
    }

    private void retrieveInitParams() throws RemoteException {
        PuzzleBoardManager friendManager = this.getFirstManager();
        if (friendManager != null) {
            InitParams friendParams = friendManager.getInitParams();
            InitParams params = new InitParamsImpl(friendParams.getRows(), friendParams.getColumns(), friendParams.getImage());
            this.initParams = params;
        } else
            System.out.println("Can't retrieve init params, i don't know manager");
    }

    @Override
    public void createInitParams(int rows, int columns, String image) throws IOException {
        byte[] imageRaw = Files.readAllBytes(new File(image).toPath());
        this.initParams = new InitParamsImpl(rows, columns, imageRaw);
        log("Create initParams");
    }

    @Override
    public InitParams getInitParams() {
        return this.initParams;
    }

    @Override
    public void updateTiles(List<Tile> tiles) {

    }

    @Override
    public void swap(Tile tile1, Tile tile2) {

    }

    private void log(String s) {
        System.out.println(this.id + " " + s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuzzleBoardManagerImpl that = (PuzzleBoardManagerImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PuzzleBoardManagerImpl{" +
                "registry=" + registry +
                ", port=" + port +
                ", managers=" + managers +
                ", initParams=" + initParams +
                ", tiles=" + tiles +
                ", id=" + id +
                '}';
    }
}
