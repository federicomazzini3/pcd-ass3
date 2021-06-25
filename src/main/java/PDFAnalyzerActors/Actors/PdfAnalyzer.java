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

    public static class Finished implements Command{
        private ActorRef<TextAnalyzer.Command> textAnalyzerToRemove;
        public Finished(ActorRef<TextAnalyzer.Command>  textAnalyzerToRemove){
            this. textAnalyzerToRemove =  textAnalyzerToRemove;
        }
    }

    private ArrayList<ActorRef<TextAnalyzer.Command>> analyzers;
    private final ActorRef<Ignorer.Command> ignorer;
    private final ActorRef<PdfAnalyzer.Command> me;
    private final ActorRef<Generator.Command> gen;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(ActorRef<Ignorer.Command> ignorer, ActorRef<Generator.Command> gen) {
        return Behaviors.setup(context -> new PdfAnalyzer(context, ignorer, gen));
    }

    private PdfAnalyzer(ActorContext<Command> context, ActorRef<Ignorer.Command> ignorer, ActorRef<Generator.Command> gen) {
        super(context);
        this.analyzers = new ArrayList<>();
        this.ignorer = ignorer;
        log("Creazione");
        this.me = getContext().getSelf();
        this.gen = gen;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PdfAnalyzer.Pdf.class, this::onStartAnalyze)
                .onMessage(PdfAnalyzer.Finished.class, this::onFinishedTextAnalyzer)
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
                ActorRef<TextAnalyzer.Command> analyzer = getContext().spawn(TextAnalyzer.create(ignorer, me), "TEXTAnalyzer" + i);
                analyzers.add(analyzer);
                analyzer.tell(new TextAnalyzer.Text(pageText, i, currentFile, pdf.replyTo, analyzer));
                log("Invio per la pagina " + i + " del file " + currentFile + " fatto");
                i++;
                page.close();
            }
            document.close();
           // gen.tell(new Generator.Finished(pdf.istanceOfPdfAnalyzer));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private Behavior<Command> onFinishedTextAnalyzer(Finished finished) {
        analyzers.remove(finished.textAnalyzerToRemove);
        if(analyzers.isEmpty()){
            gen.tell(new Generator.Finished(me));
        }
        return this;
    }

    public void log(String s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[PdfAnalyzer] " + s);
    }
}
