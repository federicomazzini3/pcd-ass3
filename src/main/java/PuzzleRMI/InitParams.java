package PuzzleRMI;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InitParams extends Serializable {

    int getRows();

    int getColumns();

    byte[] getImage();
}
