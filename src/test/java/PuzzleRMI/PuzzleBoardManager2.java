package PuzzleRMI;

import java.io.IOException;
import java.rmi.NotBoundException;

public class PuzzleBoardManager2 {

    public static void main(String[] args) throws IOException, NotBoundException {
        PuzzleBoardManager manager2 = new JoinPlayer(25252, "127.0.0.1", 25251);

        System.out.println(manager2.getInitParams());
    }
}
