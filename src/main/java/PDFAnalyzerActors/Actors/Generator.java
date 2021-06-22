package PDFAnalyzerActors.Actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;

public class Generator extends AbstractBehavior<Generator.IGenerator> {

    public interface IGenerator {};

    public static class ToIgnoreWords implements IGenerator {
        private HashSet toIgnoreWords;

        public ToIgnoreWords(HashSet toIgnoreWords){
            this.toIgnoreWords = toIgnoreWords;
        }
    }

    public static class Analyze implements IGenerator {
        private String directoryPath;

        public Analyze(String directoryPath){
            this.directoryPath = directoryPath;
        }
    }

    /** Factory method e costruttore */
    public static Behavior<IGenerator> create() {
        return Behaviors.setup(Generator::new);
    }

    private Generator(ActorContext<IGenerator> context) {
        super(context);
    }

    /** Receive dei messaggi */
    @Override
    public Receive<IGenerator> createReceive() {
        return newReceiveBuilder()
                .onMessage(Generator.Analyze.class, this::onStartAnalyze)
                .onMessage(Generator.ToIgnoreWords.class, this::onToIgnoreWords)
                .build();
    }

    /** Handler alla ricezione dei messaggi */
    private Behavior<IGenerator> onStartAnalyze(Generator.Analyze command) {
        //#create-actors
        /*ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.directoryPath);
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, replyTo));*/
        //#create-actors
        return this;
    }

    private Behavior<IGenerator> onToIgnoreWords(Generator.ToIgnoreWords command) {
        //#create-actors
        /*ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.directoryPath);
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, replyTo));*/
        //#create-actors
        return this;
    }

}
