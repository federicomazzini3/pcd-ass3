package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;

public class PuzzleBoardManager3 {
    public static void main(String[] args) throws IOException, NotBoundException {
        PuzzleBoardManager manager3 = new JoinPlayer(25253, "127.0.0.1", 25252);


        System.out.println(manager3.getInitParams());
    }
}
