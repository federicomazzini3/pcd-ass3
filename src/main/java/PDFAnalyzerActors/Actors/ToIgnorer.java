package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ToIgnorer extends AbstractBehavior<ToIgnorer.ToIgnore> {

    public static class ToIgnore{
        private String directoryPath;
        public final ActorRef<Generator.Command> replyTo;

        public ToIgnore (String directoryPath, ActorRef<Generator.Command> replyTo){
            this.directoryPath = directoryPath;
            this.replyTo = replyTo;
        }
    }

    /** Factory method e costruttore */
    public static Behavior<ToIgnorer.ToIgnore> create() {
        return Behaviors.setup(ToIgnorer::new);
    }

    private ToIgnorer(ActorContext<ToIgnorer.ToIgnore> context) {
        super(context);
    }

    @Override
    public Receive<ToIgnore> createReceive() {
        return null;
    }
}
