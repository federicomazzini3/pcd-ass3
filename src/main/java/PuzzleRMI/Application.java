package PuzzleRMI;

public class Application {

	public static void main(final String[] args) {
		Controller controller = new Controller();
		InitialView initView = new InitialView(controller);
		initView.display(true);
	}
}
