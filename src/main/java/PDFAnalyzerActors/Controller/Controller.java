package PDFAnalyzerActors.Controller;

import ExampleAkka.GreeterMain;
import PDFAnalyzerActors.Actors.AnalyzerMain;
import PDFAnalyzerActors.Model.Initializer;
import PDFAnalyzerActors.View.View;
import akka.actor.Actor;
import akka.actor.typed.ActorSystem;

public class Controller {
    private View view;
    private String directoryPdf;
    private String toIgnoreFilePath;
    private int wordsToRetrive;
    private final ActorSystem<AnalyzerMain.Analyze> analyzerMain;

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
        this.wordsToRetrive = n;
    }
    
    public synchronized void setSplit(boolean split) {
    }

    public synchronized void notifyStarted() {
        analyzerMain.tell(new AnalyzerMain.Analyze(directoryPdf, toIgnoreFilePath, wordsToRetrive));
    }

    public synchronized void notifyStopped() {
    }
}
