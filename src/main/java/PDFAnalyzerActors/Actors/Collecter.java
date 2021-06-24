package PDFAnalyzerActors.Actors;

import PDFAnalyzerActors.Model.Chrono;
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
        private int processedWords;

        public Collect(Map<String, Integer> occurrences, int processedWords) {
            this.occurrences = occurrences;
            this.processedWords = processedWords;
        }
    }


    public static class Finished implements Command {
        public Finished(){}
    }

    private final View view;
    private final int wordsToRetrieve;
    private Map<String, Integer> occurrences;
    private int processedWords;
    private Chrono time;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Collecter.Command> create(int wordsToRetrieve, View view, Chrono time) {
        return Behaviors.setup(context -> new Collecter(context, wordsToRetrieve, view, time));
    }

    private Collecter(ActorContext<Collecter.Command> context, int wordsToRetrieve, View view, Chrono time) {
        super(context);
        this.view = view;
        this.wordsToRetrieve = wordsToRetrieve;
        this.occurrences = new HashMap<>();
        this.processedWords = 0;
        this.time = time;
        log("Creazione");
    }

    @Override
    public Receive<Collecter.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Collecter.Collect.class, this::onCollect)
                .onMessage(Collecter.Finished.class, this::onFinished)
                .build();
    }

    private Behavior<Command> onFinished(Finished finish) {
        view.updateComplete(time.getTime());
        return this;
    }

    private Behavior<Command> onCollect(Collecter.Collect collect) {
        log("collect");
        log(collect.occurrences);
        collect.occurrences.forEach((k, v) -> this.occurrences.merge(k, v, Integer::sum));
        this.processedWords = this.processedWords + collect.processedWords;
        List<Occurrence> occ = createOccurrencesList(this.occurrences, wordsToRetrieve);
        updateView(occ, this.processedWords);
        return this;
    }

    private List<Occurrence> createOccurrencesList(Map<String, Integer> occ, int wordsNumberToRetrieve) {
        return occ.entrySet().stream()
                .map(e -> new Occurrence(e.getKey(), e.getValue()))
                .sorted()
                .limit(wordsNumberToRetrieve)
                .collect(Collectors.toList());
    }

    private void updateView(List<Occurrence> occurrences, int processedWords) {
        view.updateOccurrencesLabel(occurrences);
        view.updateCountValue(processedWords);
    }

    public void log(Object s){
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[Collecter] " + s);
    }

}
