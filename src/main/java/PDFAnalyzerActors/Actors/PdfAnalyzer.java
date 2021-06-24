package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PdfAnalyzer extends AbstractBehavior<PdfAnalyzer.Command> {

    public interface Command{}

    public static class Pdf implements Command{
        private File file;
        private final ActorRef<Collecter.Command> replyTo;

        public Pdf(File file, ActorRef<Collecter.Command> replyTo) {
            this.file = file;
            this.replyTo = replyTo;
        }
    }

    private ArrayList<ActorRef<TextAnalyzer.Command>> analyzers;
    private final ActorRef<Ignorer.Command> ignorer;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(ActorRef<Ignorer.Command> ignorer) {
        return Behaviors.setup(context -> new PdfAnalyzer(context, ignorer));
    }

    private PdfAnalyzer(ActorContext<Command> context, ActorRef<Ignorer.Command> ignorer) {
        super(context);
        this.analyzers = new ArrayList<>();
        this.ignorer = ignorer;
        log("Creazione");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PdfAnalyzer.Pdf.class, this::onStartAnalyze)
                .build();
    }

    private Behavior<Command> onStartAnalyze(Pdf pdf) {
        String currentFile = pdf.file.getName();
        this.log("Suddivido in ulteriori task il file " + currentFile);
        try {
            PDDocument document = PDDocument.load(pdf.file);
            Splitter splitter = new Splitter();
            PDFTextStripper stripper = new PDFTextStripper();
            int i = 0;

            for (PDDocument page : splitter.split(document)) {
                String pageText = stripper.getText(page);
                log("Splitto la pagina: " + i + " di " + currentFile);
                log("Metto in coda il task per la pagina: " + i + " del file " + currentFile);
                ActorRef<TextAnalyzer.Command> analyzer = getContext().spawn(TextAnalyzer.create(ignorer), "TEXTAnalyzer" + i);
                analyzers.add(analyzer);
                analyzer.tell(new TextAnalyzer.Text(pageText, i, currentFile, pdf.replyTo));
                log("Invio per la pagina " + i + " del file " + currentFile + " fatto");
                i++;
                page.close();
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void log(String s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[PdfAnalyzer] " + s);
    }
}
