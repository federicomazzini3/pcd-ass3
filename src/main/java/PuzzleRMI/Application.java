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
		Controller controller = new Controller();
		InitialView initView = new InitialView(controller);
		initView.display(true);
	}
}
