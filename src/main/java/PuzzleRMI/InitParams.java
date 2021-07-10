package PuzzleRMI;

import java.io.Serializable;

public class InitParams implements Serializable {
    int rows;
    int columns;
    byte[] image;

    public InitParams(int rows, int columns, byte[] image){
        this.rows = rows;
        this.columns = columns;
        this.image = image;
    }

    public int getRows(){
        return this.rows;
    }

    public int getColumns(){
        return this.columns;
    }

    public byte[] getImage(){
        return this.image;
    }

    public String toString() {
        return "InitParamsImpl{" +
                "rows=" + rows +
                ", columns=" + columns +
                '}';
    }
}
