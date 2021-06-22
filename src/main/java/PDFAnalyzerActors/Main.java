package PDFAnalyzerActors;

import PDFAnalyzerActors.Controller.Controller;
import PDFAnalyzerActors.View.View;

public class Main {
    public static void main(String[]args) {
        Controller controller = new Controller();
        View view = new View(controller);
        controller.setView(view);
        view.display();
    }
}
