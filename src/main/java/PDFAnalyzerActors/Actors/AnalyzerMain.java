package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.Model.Chrono;
import PDFAnalyzerActors.Model.Occurrence;
import PDFAnalyzerActors.View.View;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

/**
    Esegue lo spawn degli attori ignorer, generator e sink
    Invia all'ignorer un messaggio per iniziare il discovery delle parole da ignorare
    Quando l'ignorer ha trovato le parole da ignorare, le passa al generator il quale
    inizierà ad analizzare la directory contenente i pdf
 */

public class AnalyzerMain extends AbstractBehavior<AnalyzerMain.Command> {

    /** Tipo di messaggio che questo attore processa */
    public interface Command{}
    public static class Stop implements Command{}

    private final ActorRef<Ignorer.Command> ignorer;
    private final ActorRef<Generator.Command> generator;
    private final ActorRef<Collecter.Command> collecter;

    /** Factory method e costruttore */
    public static Behavior<Command> create(String directoryPdf, String toIgnoreFilePath, int wordsToRetrieve, ActorRef<ViewActor.Command> view) {
        return Behaviors.setup(context -> new AnalyzerMain(context, directoryPdf, toIgnoreFilePath, wordsToRetrieve, view));
    }

    private AnalyzerMain(ActorContext<Command> context, String directoryPdf, String toIgnoreFilePath, int wordsToRetrieve, ActorRef<ViewActor.Command> view) {
        super(context);
        this.ignorer = context.spawn(Ignorer.create(toIgnoreFilePath), "ignorer");
        this.collecter = context.spawn(Collecter.create(wordsToRetrieve, view), "collecter");
        this.generator = context.spawn(Generator.create(ignorer, collecter, directoryPdf), "generator");
    }

    /** Receive dei messaggi */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AnalyzerMain.Stop.class, this::onStop)
                .build();
    }

    private Behavior<Command> onStop(Stop stop) {
        return Behaviors.stopped();
    }
}
