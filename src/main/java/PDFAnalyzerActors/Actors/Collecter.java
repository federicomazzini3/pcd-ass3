package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.Model.Occurrence;
import PDFAnalyzerActors.View.View;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Collecter extends AbstractBehavior<Collecter.Command> {
    public interface Command {
    }

    public static class Collect implements Command {
        private Map<String, Integer> occurrences;

        public Collect(Map<String, Integer> occurrences) {
            this.occurrences = occurrences;
        }
    }

    private final View view;
    private final int wordsToRetrieve;
    private Map<String, Integer> occurrences;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Collecter.Command> create(int wordsToRetrieve, View view) {
        return Behaviors.setup(context -> new Collecter(context, wordsToRetrieve, view));
    }

    private Collecter(ActorContext<Collecter.Command> context, int wordsToRetrieve, View view) {
        super(context);
        this.view = view;
        this.wordsToRetrieve = wordsToRetrieve;
        this.occurrences = new HashMap<>();
        log("Creazione");
    }

    @Override
    public Receive<Collecter.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Collecter.Collect.class, this::onCollect)
                .build();
    }

    private Behavior<Command> onCollect(Collecter.Collect collect) {
        log("collect");
        log(collect.occurrences);
        collect.occurrences.forEach((k, v) -> this.occurrences.merge(k, v, Integer::sum));
        List<Occurrence> occ = createOccurrencesList(this.occurrences, wordsToRetrieve);
        updateView(occ);
        return this;
    }

    private List<Occurrence> createOccurrencesList(Map<String, Integer> occ, int wordsNumberToRetrieve) {
        return occ.entrySet().stream()
                .map(e -> new Occurrence(e.getKey(), e.getValue()))
                .sorted()
                .limit(wordsNumberToRetrieve)
                .collect(Collectors.toList());
    }

    private void updateView(List<Occurrence> occurrences) {
        view.updateOccurrencesLabel(occurrences);
    }

    public void log(Object s){
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[Collecter] " + s);
    }
}
