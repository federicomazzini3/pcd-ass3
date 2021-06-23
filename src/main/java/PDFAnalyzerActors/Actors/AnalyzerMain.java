package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.View.View;
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
        private final String toIgnoreFilePath;

        public ToIgnore(String toIgnoreFilePath){
            this.toIgnoreFilePath = toIgnoreFilePath;
        }
    }

    public static class Discovery implements Command {
        private final String directoryPath;
        private final int wordsToRetrieve;

        public Discovery(String directoryPath, int wordsToRetrieve) {
            this.directoryPath = directoryPath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    private final ActorRef<Ignorer.Command> ignorer;
    private final ActorRef<Generator.Command> generator;
    private final ActorRef<Collecter.Command> collecter;

    /** Factory method e costruttore */
    public static Behavior<Command> create(View view, int wordsToRetrieve) {
        //return Behaviors.setup(AnalyzerMain::new);
        return Behaviors.setup(context -> new AnalyzerMain(context, view, wordsToRetrieve));
    }

    private AnalyzerMain(ActorContext<Command> context, View view, int wordsToRetrieve) {
        super(context);
        ignorer = context.spawn(Ignorer.create(), "ignorer");
        generator = context.spawn(Generator.create(ignorer), "generator");
        collecter = context.spawn(Collecter.create(wordsToRetrieve, view), "collecter");
    }

    /** Receive dei messaggi */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AnalyzerMain.ToIgnore.class, this::onStartToIgnoreWords)
                .onMessage(AnalyzerMain.Discovery.class, this::onStartAnalyze)
                .build();
    }

    /** Handler alla ricezione dei messaggi */
    private Behavior<Command> onStartToIgnoreWords(ToIgnore toIgnore) {
        ignorer.tell(new Ignorer.GenerateToIgnoreWords(toIgnore.toIgnoreFilePath));
        return Behaviors.same();
    }

    private Behavior<Command> onStartAnalyze(Discovery discovery) {
        generator.tell(new Generator.Discovery(discovery.directoryPath, discovery.wordsToRetrieve, collecter));
        return Behaviors.same();
    }
}
