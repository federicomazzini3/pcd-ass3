package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
/**
    Esegue lo spawn degli attori ignorer, generator e sink
    Invia all'ignorer un messaggio per iniziare il discovery delle parole da ignorare
    Quando l'ignorer ha trovato le parole da ignorare, le passa al generator il quale inizierà ad analizzare la directory contenente i pdf
 */

public class AnalyzerMain extends AbstractBehavior<AnalyzerMain.Command> {

    /** Tipo di messaggio che questo attore processa */
    public interface Command{}
    public static class ToIgnore implements Command {
        private String toIgnoreFilePath;

        public ToIgnore(String toIgnoreFilePath){
            this.toIgnoreFilePath = toIgnoreFilePath;
        }
    }

    public static class Analyze implements Command {
        private String directoryPath;
        private int wordsToRetrieve;

        public Analyze(String directoryPath, int wordsToRetrieve) {
            this.directoryPath = directoryPath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    private final ActorRef<ToIgnorer.ToIgnore> ignorer;
    private final ActorRef<Generator.Command> generator;
    private final ActorRef<Collecter.Command> collecter;

    /** Factory method e costruttore */
    public static Behavior<Command> create() {
        return Behaviors.setup(AnalyzerMain::new);
    }

    private AnalyzerMain(ActorContext<Command> context) {
        super(context);
        ignorer = context.spawn(ToIgnorer.create(), "ignorer");
        generator = context.spawn(Generator.create(), "generator");
        collecter = context.spawn(Collecter.create(), "collecter");
    }

    /** Receive dei messaggi */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AnalyzerMain.ToIgnore.class, this::onStartToIgnoreWords)
                .onMessage(AnalyzerMain.Analyze.class, this::onStartAnalyze)
                .build();
    }

    /** Handler alla ricezione dei messaggi */
    private Behavior<Command> onStartToIgnoreWords(ToIgnore toIgnore) {
        ignorer.tell(new ToIgnorer.ToIgnore(toIgnore.toIgnoreFilePath, generator));
        return Behaviors.same();
    }

    private Behavior<Command> onStartAnalyze(Analyze analyze) {
        //ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, generator));
        generator.tell(new Generator.Analyze(analyze.directoryPath, analyze.wordsToRetrieve));
        return Behaviors.same();
    }
}
