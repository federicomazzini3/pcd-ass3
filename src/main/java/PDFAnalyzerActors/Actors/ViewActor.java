package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.Model.Chrono;
import PDFAnalyzerActors.Model.Occurrence;
import PDFAnalyzerActors.View.ShowGUI;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;


public class ViewActor extends AbstractBehavior<ViewActor.Command> {

    public interface Command {}

    public static class Start implements Command {
        private final String directoryPdf;
        private final String toIgnoreFilePath;
        private final int wordsToRetrieve;

        public Start(String directoryPdf, String toIgnoreFilePath, int wordsToRetrieve) {
            this.directoryPdf = directoryPdf;
            this.toIgnoreFilePath = toIgnoreFilePath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    public static class Stop implements Command {}

    public static class Occurrences implements Command {
        private final List<Occurrence> occurrences;

        public Occurrences(List<Occurrence> occurrences) {
            this.occurrences = occurrences;
        }
    }

    public static class ProcessedWords implements Command {
        private final int processedWords;

        public ProcessedWords(int processedWords) {
            this.processedWords = processedWords;
        }
    }

    public static class Finish implements Command {
    }

    private final ShowGUI gui;
    private final Chrono chrono;
    private ActorRef<AnalyzerMain.Command> analyzerMain;
    private int i = 0;

    /**
     * Factory method e costruttore
     */
    public static Behavior<ViewActor.Command> create() {
        return Behaviors.setup(ViewActor::new);
    }

    private ViewActor(ActorContext<ViewActor.Command> context) {
        super(context);
        gui = new ShowGUI(context.getSelf());
        gui.display();
        chrono = new Chrono();
    }

    @Override
    public Receive<ViewActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ViewActor.Start.class, this::onStartProgram)
                .onMessage(ViewActor.Stop.class, this::onStopProgram)
                .onMessage(ViewActor.Occurrences.class, this::updateOccurrencesLabel)
                .onMessage(ViewActor.ProcessedWords.class, this::updateCountValue)
                .onMessage(Finish.class, this::onFinish)
                .build();
    }

    private Behavior<Command> onStartProgram(Start start) {
        chrono.start();
        this.analyzerMain = getContext().spawn(AnalyzerMain.create(start.directoryPdf, start.toIgnoreFilePath, start.wordsToRetrieve, getContext().getSelf()), "AnalyzerMain" + i);
        this.gui.start();
        return this;
    }

    private Behavior<Command> onStopProgram(Stop stop) {
        this.analyzerMain.tell(new AnalyzerMain.Stop());
        this.i++;
        return this;
    }

    private Behavior<Command> updateCountValue(ProcessedWords processedWords) {
        this.gui.updateCountValue(processedWords.processedWords);
        return this;
    }

    private Behavior<Command> updateOccurrencesLabel(Occurrences occurrences) {
        this.gui.updateOccurrencesLabel(occurrences.occurrences);
        return this;
    }

    private Behavior<Command> onFinish(Finish finish) {
        this.analyzerMain.tell(new AnalyzerMain.Stop());
        gui.updateComplete(chrono.getTime() / 1000.00);
        return this;
    }
}
