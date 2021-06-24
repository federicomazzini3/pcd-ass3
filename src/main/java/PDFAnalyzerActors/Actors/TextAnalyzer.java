package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

public class TextAnalyzer extends AbstractBehavior<TextAnalyzer.Command> {

    public interface Command{}

    public static class Text implements Command {
        private String text;
        private int currentPage;
        private String currentFile;
        private final ActorRef<Collecter.Command> replyTo;

        public Text(String text, int currentPage, String currentFile, ActorRef<Collecter.Command> replyTo){
            this.text = text;
            this.currentPage = currentPage;
            this.currentFile = currentFile;
            this.replyTo = replyTo;
        }
    }

    public static class ToIgnoreWords implements Command {
        private HashSet<String> toIgnoreWords;
        public ToIgnoreWords (HashSet<String> toIgnoreWords){
            this.toIgnoreWords = toIgnoreWords;
        }
    }

    private HashSet<String> toIgnoreWords;
    private final StashBuffer<Command> buffer;

    /** Factory method e costruttore */
    public static Behavior<TextAnalyzer.Command> create(ActorRef<Ignorer.Command> ignorer) {
        //return Behaviors.setup(context -> new TextAnalyzer(context, ignorer));
        return Behaviors.withStash(
                100,
                stash ->
                        Behaviors.setup(
                                ctx -> {
                                    return new TextAnalyzer(ctx, stash, ignorer);
                                }));
    }

    private TextAnalyzer(ActorContext<TextAnalyzer.Command> context, StashBuffer<Command> buffer, ActorRef<Ignorer.Command> ignorer) {
        super(context);
        this.buffer = buffer;
        ignorer.tell(new Ignorer.GetToIgnoreWords(context.getSelf()));
        log("Creazione TextAnalyzer");
    }

    @Override
    public Receive<TextAnalyzer.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TextAnalyzer.ToIgnoreWords.class, this::onGetToIgnoreWords)
                .onMessage(TextAnalyzer.Command.class, this::stashOtherCommand)
                .build();
    }

    private Behavior<Command> onGetToIgnoreWords(TextAnalyzer.ToIgnoreWords toIgnoreWords) {
        this.toIgnoreWords = toIgnoreWords.toIgnoreWords;
        //return Behaviors.receive(TextAnalyzer.Command.class).onMessage(TextAnalyzer.Text.class, this::onStartAnalyze).build();
        return buffer.unstashAll(Behaviors.receive(TextAnalyzer.Command.class).
                onMessage(TextAnalyzer.Text.class, this::onStartAnalyze)
                .build());
    }

    private Behavior<Command> stashOtherCommand(Command message) {
        // stash all other messages for later processing
        buffer.stash(message);
        return Behaviors.same();
    }

    private Behavior<Command> onStartAnalyze(TextAnalyzer.Text text) {
        log("Analizzo la pagina " + text.currentPage + " di " + text.currentFile);
        Map<String, Integer> localCounter = new HashMap<>();
        int processedWords = 0;

        String filteredDocument = text.text.replaceAll("[|;:,_.*=?!/<()'\"<-].", " ");
        StringTokenizer doc = new StringTokenizer(filteredDocument);
        while (doc.hasMoreTokens()) {
            processedWords = processedWords + 1;

            String word = doc.nextToken().toLowerCase();
            if (!toIgnoreWords.contains(word))
                localCounter.merge(word, 1, Integer::sum);
        }
        log("Finito di analizzare il testo della pagina");
        log("Mando i risultati al collecter");
        //counter.mergeOccurrence(localCounter, processedWords);
        //ResultAnalyzeTask task = new ResultAnalyzeTask(counter, wordsToRetrieve, view, stopFlag);
        text.replyTo.tell(new Collecter.Collect(localCounter, processedWords));
        return this;
    }

    public void log(String s){
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[Text Analyzer] " + s);
    }

}
