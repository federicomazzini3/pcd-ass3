package PDFAnalyzerActors.Actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;

public class Generator extends AbstractBehavior<Generator.Command> {

    public interface Command {};

    public static class ToIgnoreWords implements Command {
        private HashSet toIgnoreWords;

        public ToIgnoreWords(HashSet toIgnoreWords){
            this.toIgnoreWords = toIgnoreWords;
        }
    }

    public static class Analyze implements Command {
        private String directoryPath;
        private int wordsToRetrieve;

        public Analyze(String directoryPath, int wordsToRetrieve){
            this.directoryPath = directoryPath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    /** Factory method e costruttore */
    public static Behavior<Command> create() {
        return Behaviors.setup(Generator::new);
    }

    private Generator(ActorContext<Command> context) {
        super(context);
    }

    /** Receive dei messaggi */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Generator.Analyze.class, this::onStartAnalyze)
                .onMessage(Generator.ToIgnoreWords.class, this::onToIgnoreWords)
                .build();
    }

    /** Handler alla ricezione dei messaggi */
    private Behavior<Command> onStartAnalyze(Generator.Analyze command) {
        //#create-actors
        /*ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.directoryPath);
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, replyTo));*/
        //#create-actors
        return this;
    }

    private Behavior<Command> onToIgnoreWords(Generator.ToIgnoreWords command) {
        //#create-actors
        /*ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.directoryPath);
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, replyTo));*/
        //#create-actors
        return this;
    }

}
