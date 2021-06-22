package PDFAnalyzerActors.Controller;

import PDFAnalyzerActors.Actors.AnalyzerMain;
import PDFAnalyzerActors.View.View;
import akka.actor.typed.ActorSystem;

public class Controller {
    private View view;
    private String directoryPdf;
    private String toIgnoreFilePath;
    private int wordsToRetrieve;
    private final ActorSystem<AnalyzerMain.Command> analyzerMain;

    public Controller() {
    	analyzerMain = ActorSystem.create(AnalyzerMain.create(), "master");
    }

    public synchronized void setView(View view) {
        this.view = view;
    }

    public synchronized void setDirectoryPdf(String directoryPdf) {
        this.directoryPdf = directoryPdf;
    }

    public synchronized void setToIgnoreFile(String toIgnoreFile) {
        this.toIgnoreFilePath = toIgnoreFile;
    }

    public synchronized void setNumberOfWords(int n) {
        this.wordsToRetrieve = n;
    }
    
    public synchronized void setSplit(boolean split) {
    }

    public synchronized void notifyStarted() {
        analyzerMain.tell(new AnalyzerMain.ToIgnore(toIgnoreFilePath));
        analyzerMain.tell(new AnalyzerMain.Analyze(directoryPdf, wordsToRetrieve));
    }

    public synchronized void notifyStopped() {
    }
}
