package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.File;

public class PdfAnalyzer extends AbstractBehavior<PdfAnalyzer.Pdf> {

    public class Pdf{
        private File file;
        private final ActorRef<Collecter.Collect> replyTo;

        public Pdf(File file, ActorRef<Collecter.Collect> replyTo){
            this.file = file;
            this.replyTo = replyTo;
        }
    }

    /** Factory method e costruttore */
    public static Behavior<PdfAnalyzer.Pdf> create() {
        return Behaviors.setup(PdfAnalyzer::new);
    }

    private PdfAnalyzer(ActorContext<PdfAnalyzer.Pdf> context) {
        super(context);
    }

    @Override
    public Receive<Pdf> createReceive() {
        return null;
    }
}
