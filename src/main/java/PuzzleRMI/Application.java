package PuzzleRMI;

import javax.naming.ldap.Control;

/**
 * 
 * Simple Puzzle Game - Centralized version.
 * 
 * By A. Croatti 
 * 
 * @author acroatti
 *
 */
public class Application {

	public static void main(final String[] args) {
		final int n = 3;
		final int m = 5;
		
		final String imagePath = "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg";
		
		//final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
        //puzzle.display(true);
		Controller controller = new Controller();
		InitialView initView = new InitialView(controller);
		initView.display(true);
	}
}
