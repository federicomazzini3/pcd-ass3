package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;

public class PuzzleBoardManager4 {
    public static void main(String[] args) throws IOException, NotBoundException {
        PuzzleBoardManager manager3 = new PuzzleBoardManagerImpl(25254, "127.0.0.1", 25251);


        System.out.println(manager3.getInitParams());
    }
}
