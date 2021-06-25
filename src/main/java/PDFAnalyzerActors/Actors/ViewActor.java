package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.Model.Chrono;
import PDFAnalyzerActors.View.ShowGUI;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class ViewActor extends AbstractBehavior<ViewActor.Command> {

    private ShowGUI gui;

    public interface Command{}

    public static class Start implements Command{
        private final String directoryPdf;
        private final String toIgnoreFilePath;
        private final int wordsToRetrieve;

        public Start(String directoryPdf, String toIgnoreFilePath, int wordsToRetrieve){
            this.directoryPdf = directoryPdf;
            this.toIgnoreFilePath = toIgnoreFilePath;
            this.wordsToRetrieve = wordsToRetrieve;
        }
    }

    public static class Stop implements Command{
        public Stop(){}
    }

    /**
     * Factory method e costruttore
     */
    public static Behavior<ViewActor.Command> create() {
        return Behaviors.setup(context -> new ViewActor(context));
    }

    private ViewActor(ActorContext<ViewActor.Command> context) {
        super(context);
    }

    @Override
    public Receive<ViewActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ViewActor.Start.class, this::onStartProgram)
                //.onMessage(ViewActor.Stop.class, this::onStopProgram)
                .build();
    }

    private Behavior<Command> onStartProgram(Start start) {
        Chrono time = new Chrono();
        time.start();
        //ActorRef<AnalyzerMain.Command> analyzerMain = getContext().spawn(AnalyzerMain.create(view, start.wordsToRetrieve, time), "master");
        return this;
    }
}
