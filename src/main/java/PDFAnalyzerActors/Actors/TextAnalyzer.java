package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class TextAnalyzer extends AbstractBehavior<TextAnalyzer.Text> {

    public class Text{
        private String text;
        private final ActorRef<Collecter.Collect> replyTo;

        public Text(String text, ActorRef<Collecter.Collect> replyTo){
            this.text = text;
            this.replyTo = replyTo;
        }
    }

    /** Factory method e costruttore */
    public static Behavior<TextAnalyzer.Text> create() {
        return Behaviors.setup(TextAnalyzer::new);
    }

    private TextAnalyzer(ActorContext<TextAnalyzer.Text> context) {
        super(context);
    }

    @Override
    public Receive<TextAnalyzer.Text> createReceive() {
        return null;
    }
}
