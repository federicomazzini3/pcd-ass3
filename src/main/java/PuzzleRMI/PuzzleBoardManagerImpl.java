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
    private List<PuzzleBoardManager> toRemoveManagers;
    private InitParams initParams;
    private List<Position> positions;
    private Long id;
    private PuzzleBoard puzzleBoard;

    public PuzzleBoardManagerImpl(int port, int row, int columns, String imagePath) throws IOException {
        this.id = new Random().nextLong();
        this.port = port;
        managers = new ArrayList<>();
        toRemoveManagers = new ArrayList<>();
        log("Create");

        //creazione parte server del peer
        createRegistry(port);

        //creazione dei parametri iniziali
        createInitParams(row, columns, imagePath);

        puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.createTiles();
        this.positions = puzzleBoard.getPositions();
        puzzleBoard.display(true);
    }

    public PuzzleBoardManagerImpl(int port, String friendAddress, int friendPort) throws NotBoundException, RemoteException {
        this.id = new Random().nextLong();
        this.port = port;
        managers = new ArrayList<>();
        toRemoveManagers = new ArrayList<>();
        log("Create");

        //creazione parte server del peer
        createRegistry(port);

        //connessione al giocatore specificato
        this.connect(friendAddress, friendPort);

        this.retrieveInitParams();

        this.retrievePositions();


        puzzleBoard = new PuzzleBoard(this.initParams.getRows(), this.initParams.getColumns(), this.initParams.getImage(), this);
        puzzleBoard.setPositions(this.positions);
        puzzleBoard.display(true);
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
    public void addManager(PuzzleBoardManager friendManager) {
        if (!this.managers.contains(friendManager)) {
            //aggiunta in locale del friendManager
            this.managers.add(friendManager);
            //aggiunta in remoto di questo manager
            try {
                friendManager.addManager(this);
                log("Manager of Peer-" + friendManager.getId() + " added");
            } catch (RemoteException e) {
                addToRemoveManager(friendManager);
                log("Manager died, I'll remove it");
            }
        }
    }

    private void addToRemoveManager(PuzzleBoardManager manager) {
        this.toRemoveManagers.add(manager);
    }

    private void removeManager() {
        if(toRemoveManagers.size() > 0){
            log("Delete manager from managers list");
            this.managers.removeAll(toRemoveManagers);
            toRemoveManagers.clear();
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

    @Override
    public long getPort() {
        return this.port;
    }

    private void retrieveInitParams() throws RemoteException {
        PuzzleBoardManager friendManager = this.getFirstManager();
        if (friendManager != null) {
            InitParams friendParams = friendManager.getInitParams();
            InitParams params = new InitParamsImpl(friendParams.getRows(), friendParams.getColumns(), friendParams.getImage());
            this.initParams = params;
            log("Retrieve initParams: " + this.initParams.getRows() + ", " + this.initParams.getColumns());
        } else
            System.out.println("Can't retrieve init params, i don't know manager");
    }

    private void retrievePositions() throws RemoteException {
        PuzzleBoardManager friendManager = this.getFirstManager();

        if (friendManager != null) {
            this.positions = friendManager.getPositions();
            log("Retrieve positions from " + friendManager.getId());
        } else
            System.out.println("Can't retrieve positions, i don't know manager");
    }

    @Override
    public void createInitParams(int rows, int columns, String image) throws IOException {
        byte[] imageRaw = Files.readAllBytes(new File(image).toPath());
        this.initParams = new InitParamsImpl(rows, columns, imageRaw);
        log("Create initParams: " + this.initParams.getRows() + ", " + this.initParams.getColumns());
    }

    @Override
    public InitParams getInitParams() {
        return this.initParams;
    }

    @Override
    public List<Position> getPositions() {
        return this.positions;
    }

    @Override
    public void updatePosition(List<Position> positions) {
        this.positions = new ArrayList<>(positions);
        this.puzzleBoard.setPositions(positions);
    }

    @Override
    public void swap(Position position1, Position position2) {
        this.positions.remove(position1);
        this.positions.remove(position2);
        this.positions.add(position1);
        this.positions.add(position2);
        for (PuzzleBoardManager manager : this.managers) {
            try {
                log("update position");
                manager.updatePosition(this.positions);
            } catch (RemoteException e) {
                log("not update position");
                toRemoveManagers.add(manager);
            }
        }
        removeManager();
    }

    private void log(String s) {
        System.out.println("[Peer-" + this.id + "]: " + s);
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
                ", tiles=" + positions +
                ", id=" + id +
                '}';
    }
}
