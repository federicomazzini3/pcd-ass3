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

    public static class Discovery implements Command {
        public final ActorRef<Collecter.Command> replyTo;

        public Discovery(ActorRef<Collecter.Command> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Finished implements Command{
        private final ActorRef<PdfAnalyzer.Command> instanceOfPdf;
        public Finished(ActorRef<PdfAnalyzer.Command> instanceOfPdf){
            this.instanceOfPdf = instanceOfPdf;
        }
    }

    private final String directoryPdf;
    private ArrayList<ActorRef<PdfAnalyzer.Command>> analyzers;
    private final ActorRef<Ignorer.Command> ignorer;
    private final ActorRef<Generator.Command> me;
    private final ActorRef<Collecter.Command> collecter;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(ActorRef<Ignorer.Command> ignorer, ActorRef<Collecter.Command> collecter, String directoryPdf) {
        return Behaviors.setup(context -> new Generator(context, ignorer, collecter, directoryPdf));
    }

    private Generator(ActorContext<Command> context, ActorRef<Ignorer.Command> ignorer, ActorRef<Collecter.Command> collecter, String directoryPdf) {
        super(context);
        this.directoryPdf = directoryPdf;
        this.analyzers = new ArrayList<>();
        this.ignorer = ignorer;
        this.me = getContext().getSelf();
        this.collecter = collecter;
        context.getSelf().tell(new Discovery(collecter));
    }

    /**
     * Receive dei messaggi
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Generator.Discovery.class, this::onStartDiscovery)
                .onMessage(Generator.Finished.class, this::onFinished)
                .build();
    }

    /**
     * Handler alla ricezione dei messaggi
     */
    private Behavior<Command> onStartDiscovery(Discovery discovery) {
        log("Inizio a esplorare la directory");
        Path path = Paths.get(this.directoryPdf);
        AtomicInteger i = new AtomicInteger();
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .filter(this::isPdf)
                    .map(this::toFile)
                    .forEach(doc -> {
                        log("CREO L'ATTORE PER IL FILE: " + doc.getName());
                        ActorRef<PdfAnalyzer.Command> analyzer = getContext().spawn(PdfAnalyzer.create(ignorer, me), "PDFAnalyzer" + i);
                        analyzers.add(analyzer);
                        analyzer.tell(new PdfAnalyzer.Pdf(doc, discovery.replyTo));
                        i.getAndIncrement();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("Finito");
        return this;
    }

    private Behavior<Command> onFinished(Finished finish) {
        analyzers.remove(finish.instanceOfPdf);
        if(analyzers.isEmpty()){ //quando la lista dei pdf è vuota manda un messaggio di terminazione al collecter
            collecter.tell(new Collecter.Finished());
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
