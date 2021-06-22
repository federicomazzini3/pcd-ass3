package PDFAnalyzerActors.Actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Collecter  extends AbstractBehavior<Collecter.Command> {
    public interface Command{}
    public class Collect{

    }

    /** Factory method e costruttore */
    public static Behavior<Collecter.Command> create() {
        return Behaviors.setup(Collecter::new);
    }

    private Collecter(ActorContext<Collecter.Command> context) {
        super(context);
    }

    @Override
    public Receive<Collecter.Command> createReceive() {
        return null;
    }
}
