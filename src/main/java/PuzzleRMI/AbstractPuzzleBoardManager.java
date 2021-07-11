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

public abstract class AbstractPuzzleBoardManager implements PuzzleBoardManager {

    Registry registry;
    int port;
    List<PuzzleBoardManager> managers;
    List<PuzzleBoardManager> toRemoveManagers;
    InitParams initParams;
    List<Position> positions;
    int id;
    PuzzleBoard puzzleBoard;

    public AbstractPuzzleBoardManager(int port){
        this.id = new Random().nextInt();
        this.port = port;
        managers = new ArrayList<>();
        toRemoveManagers = new ArrayList<>();
        log("Create");
    }

    @Override
    public void createRegistry(int port) throws RemoteException {
        registry = LocateRegistry.createRegistry(port);
        PuzzleBoardManager manager = (PuzzleBoardManager) UnicastRemoteObject.exportObject(this, port);
        registry.rebind("manager", manager);
        this.addManager(manager);
    }

    @Override
    public void addToManagers(List<PuzzleBoardManager> friendManagers) throws RemoteException {
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
                log("Peer-" + friendManager.getId() + " added");
            } catch (RemoteException e) {
                addToRemoveManager(friendManager);
                log("Peer died, I'll remove it");
            }
        }
    }

    public void addToRemoveManager(PuzzleBoardManager manager) {
        this.toRemoveManagers.add(manager);
    }

    public void removeManager() {
        if (toRemoveManagers.size() > 0) {
            log("Delete manager from managers list");
            this.managers.removeAll(toRemoveManagers);
            toRemoveManagers.clear();
        }
    }

    @Override
    public List<PuzzleBoardManager> getManagers() throws RemoteException {
        return this.managers;
    }

    @Override
    public PuzzleBoardManager getFirstManager() throws RemoteException {
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

    @Override
    public InitParams getInitParams() {
        return this.initParams;
    }

    @Override
    public List<Position> getPositions() {
        return this.positions;
    }

    //update di tutte le posizioni
    @Override
    public synchronized void updatePosition(List<Position> positions) {
        this.positions = new ArrayList<>(positions);
        this.puzzleBoard.setPositions(positions);
        log("Update position");
    }

    //update delle sole due tiles swappate
    @Override
    public synchronized void updatePosition(Position position1, Position position2) {
        this.updateInternalPosition(position1, position2);
        this.puzzleBoard.setPositions(this.positions);
        log("Update position");
    }

    protected void swap(Position position1, Position position2) {
        this.updateInternalPosition(position1, position2);
        log("Internal swap");
        this.spreadSwap();
    }

    private void spreadSwap(){
        for (PuzzleBoardManager manager : this.managers) {
            try {
                log("Spread swap to other peer");
                //manager.updatePosition(position1, position2);
                manager.updatePosition(this.positions);
            } catch (RemoteException e) {
                log("Can't spread swap, peer is down");
                toRemoveManagers.add(manager);
            }
        }
        removeManager();
    }

    private void updateInternalPosition(Position position1, Position position2){
        this.positions.remove(position1);
        this.positions.remove(position2);
        this.positions.add(position1);
        this.positions.add(position2);
    }

    public void log(String s) {
        System.out.println("[Peer-" + this.id + "]: " + s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPuzzleBoardManager that = (AbstractPuzzleBoardManager) o;
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
