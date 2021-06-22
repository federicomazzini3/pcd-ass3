package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class AnalyzerMain extends AbstractBehavior<AnalyzerMain.Analyze> {

    /** Tipo di messaggio che questo attore processa */
    public static class Analyze {
        private String directoryPath;
        private String toIgnoreFilePath;
        private int wordsToRetrieve;

        public Analyze(String directoryPath, String toIgnoreFilePath, int wordsToRetrieve) {
            this.directoryPath = directoryPath;
            this.toIgnoreFilePath = toIgnoreFilePath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    private final ActorRef<ToIgnorer.ToIgnore> ignorer;
    private final ActorRef<Generator.IGenerator> generator;

    /** Factory method e costruttore */
    public static Behavior<Analyze> create() {
        return Behaviors.setup(AnalyzerMain::new);
    }

    private AnalyzerMain(ActorContext<Analyze> context) {
        super(context);
        ignorer = context.spawn(ToIgnorer.create(), "ignorer");

        generator = context.spawn(Generator.create(), "generator");
        //spawn sinkActor
    }

    /** Receive dei messaggi */
    @Override
    public Receive<Analyze> createReceive() {
        return newReceiveBuilder()
                .onMessage(Analyze.class, this::onStartAnalyze)
                //.onMessage(Generator.Analyze, this::onStartAnalyze)
                .build();
    }

    /** Handler alla ricezione dei messaggi */
    private Behavior<AnalyzerMain.Analyze> onStartAnalyze(Analyze command) {
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, generator));

        return this;
    }
}
