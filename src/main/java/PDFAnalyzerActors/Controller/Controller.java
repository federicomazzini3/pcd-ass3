package PDFAnalyzerActors.Controller;

import PDFAnalyzerActors.Model.Initializer;
import PDFAnalyzerActors.View.View;

public class Controller {
    private View view;
    private Initializer init;

    public Controller() {
    	this.init = new Initializer();
    }

    public synchronized void setView(View view) {
        this.view = view;
    }

    public synchronized void setDirectoryPdf(String directoryPdf) {
        this.init.setDirectoryPath(directoryPdf);
    }

    public synchronized void setToIgnoreFile(String toIgnoreFile) {
        this.init.setToIgnoreFilePath(toIgnoreFile);
    }

    public synchronized void setNumberOfWords(int n) {
        this.init.setWordsToRetrive(n); 
    }
    
    public synchronized void setSplit(boolean split) {
    	this.init.setSplit(split);
    }

    public synchronized void notifyStarted() {
    }

    public synchronized void notifyStopped() {
    }
}
