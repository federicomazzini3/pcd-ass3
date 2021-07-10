package PuzzleRMI;

public class Application {

	public static void main(final String[] args) {
		InitialController initialController = new InitialController();
		InitialView initView = new InitialView(initialController);
		initView.display(true);
	}
}
