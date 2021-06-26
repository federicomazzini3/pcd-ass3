package PDFAnalyzerActors.View;


import PDFAnalyzerActors.Controller.Controller;
import PDFAnalyzerActors.Model.Occurrence;

import java.util.List;

public class View {

    private ShowGUI gui;

    public View(Controller controller) {
        //this.gui = new ShowGUI(controller);
    }

    public synchronized void updateCountValue(int value) {
        gui.updateCountValue(value);
    }

    public synchronized void updateOccurrencesLabel(List<Occurrence> occurrences) {
        gui.updateOccurrencesLabel(occurrences);
    }

    public synchronized void display() {
        gui.display();
    }

    public synchronized void updateComplete(double time) {
        gui.updateComplete(time);
    }

}
