package PuzzleRMI;

import java.rmi.RemoteException;
import java.util.Arrays;

public class InitParamsImpl implements InitParams {
    int rows;
    int columns;
    byte[] image;

    public InitParamsImpl(int rows, int columns, byte[] image){
        this.rows = rows;
        this.columns = columns;
        this.image = image;
    }

    @Override
    public int getRows(){
        return this.rows;
    }

    @Override
    public int getColumns(){
        return this.columns;
    }

    @Override
    public byte[] getImage(){
        return this.image;
    }

    @Override
    public String toString() {
        return "InitParamsImpl{" +
                "rows=" + rows +
                ", columns=" + columns +
                '}';
    }
}
