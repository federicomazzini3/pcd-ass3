package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Generator extends AbstractBehavior<Generator.Command> {

    public interface Command {};

    public static class ToIgnoreWords implements Command {
        private HashSet toIgnoreWords;

        public ToIgnoreWords(HashSet toIgnoreWords) {
            this.toIgnoreWords = toIgnoreWords;
        }
    }

    public static class Discovery implements Command {
        private final String directoryPath;
        private final int wordsToRetrieve;
        public final ActorRef<Collecter.Command> replyTo;

        public Discovery(String directoryPath, int wordsToRetrieve, ActorRef<Collecter.Command> replyTo) {
            this.directoryPath = directoryPath;
            this.wordsToRetrieve = wordsToRetrieve;
            this.replyTo = replyTo;
        }
    }

    private ArrayList<ActorRef<PdfAnalyzer.Command>> analyzers;
    private final ActorRef<Ignorer.Command> ignorer;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(ActorRef<Ignorer.Command> ignorer) {
        return Behaviors.setup(context -> new Generator(context, ignorer));
    }

    private Generator(ActorContext<Command> context, ActorRef<Ignorer.Command> ignorer) {
        super(context);
        analyzers = new ArrayList<>();
        this.ignorer = ignorer;
    }

    /**
     * Receive dei messaggi
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Generator.Discovery.class, this::onStartDiscovery)
                .onMessage(Generator.ToIgnoreWords.class, this::onToIgnoreWords)
                .build();
    }

    /**
     * Handler alla ricezione dei messaggi
     */
    private Behavior<Command> onStartDiscovery(Discovery discovery) {
        log("Inizio a esplorare la directory");
        Path path = Paths.get(discovery.directoryPath);
        AtomicInteger i = new AtomicInteger();
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .filter(this::isPdf)
                    .map(this::toFile)
                    .forEach(doc -> {
                        log("CREO L'ATTORE PER IL FILE: " + doc.getName());
                        ActorRef<PdfAnalyzer.Command> analyzer = getContext().spawn(PdfAnalyzer.create(ignorer), "PDFAnalyzer" + i);
                        analyzers.add(analyzer);
                        analyzer.tell(new PdfAnalyzer.Pdf(doc, discovery.replyTo));
                        i.getAndIncrement();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        discovery.replyTo.tell(new Collecter.Finished());
        log("Finito");
        //return Behaviors.receive(Generator.Command.class).onMessage(Generator.ToIgnoreWords.class, this::onToIgnoreWords).build();
        return this;
    }

    private Behavior<Command> onToIgnoreWords(Generator.ToIgnoreWords toIgnoreWords) {
        //#create-actors
        /*ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.directoryPath);
        ignorer.tell(new ToIgnorer.ToIgnore(command.toIgnoreFilePath, replyTo));*/
        //#create-actors
        for(ActorRef<PdfAnalyzer.Command> analyzer : analyzers){
        }
        return this;
    }

    private File toFile(Path path) {
        return path.toFile();
    }

    private boolean isPdf(Path path) {
        boolean cond = path.getFileName().toString().toLowerCase().endsWith("pdf");
        return cond;
    }

    private void log(String s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[ Generator ]" + s);
    }

}
