package PuzzleRMI;


import java.io.IOException;

class PuzzleBoardManager1 {

    public static void main(String[] args) throws IOException {
        PuzzleBoardManager manager1 = new PuzzleBoardManagerImpl(25251, 3, 5, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg");
    }
}