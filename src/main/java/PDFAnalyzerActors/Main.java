package PDFAnalyzerActors;

import PDFAnalyzerActors.Actors.ViewActor;
import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[]args) {
        ActorSystem.create(ViewActor.create(), "ViewActor");
    }
}
