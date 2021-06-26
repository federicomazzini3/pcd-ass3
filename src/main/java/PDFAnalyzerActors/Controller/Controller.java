package PDFAnalyzerActors.Controller;

import PDFAnalyzerActors.Actors.AnalyzerMain;
import PDFAnalyzerActors.Model.Chrono;
import PDFAnalyzerActors.View.View;
import akka.actor.typed.ActorSystem;

public class Controller {
    private View view;
    private String directoryPdf;
    private String toIgnoreFilePath;
    private int wordsToRetrieve;
    private ActorSystem<AnalyzerMain.Command> analyzerMain;
    private Chrono time;

    public Controller() {
        time = new Chrono();
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
    
    public synchronized void notifyStarted() {
        time.start();
        //analyzerMain = ActorSystem.create(AnalyzerMain.create(view, wordsToRetrieve, time), "master");
        //analyzerMain.tell(new AnalyzerMain.ToIgnore(toIgnoreFilePath));
        //analyzerMain.tell(new AnalyzerMain.Discovery(directoryPdf, wordsToRetrieve));
    }

    public synchronized void notifyStopped() {
        time.stop();
        analyzerMain.terminate();
    }
}
