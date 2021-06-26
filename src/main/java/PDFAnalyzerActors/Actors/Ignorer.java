package PDFAnalyzerActors.Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class Ignorer extends AbstractBehavior<Ignorer.Command> {

    public interface Command{}

    public static class GenerateToIgnoreWords implements Command{ }

    public static class GetToIgnoreWords implements Command{
        ActorRef<TextAnalyzer.Command> replyTo;

        public GetToIgnoreWords(ActorRef<TextAnalyzer.Command> replyTo){
            this.replyTo = replyTo;
        }
    }

    private final String toIgnoreFilePath;
    private HashSet<String> toIgnoreWords;
    private final StashBuffer<Ignorer.Command> buffer;

    /** Factory method e costruttore */
    public static Behavior<Command> create(String toIgnoreFilePath) {
        //return Behaviors.setup(Ignorer::new);
        return Behaviors.withStash(
                100,
                stash ->
                        Behaviors.setup(
                                ctx -> {
                                    return new Ignorer(ctx, stash, toIgnoreFilePath);
                                }));
    }

    private Ignorer(ActorContext<Command> context, StashBuffer<Ignorer.Command> buffer, String toIgnoreFilePath) {
        super(context);
        this.buffer = buffer;
        this.toIgnoreFilePath = toIgnoreFilePath;
        context.getSelf().tell(new GenerateToIgnoreWords());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateToIgnoreWords.class, this::onStartToIgnoreWords)
                .onMessage(Ignorer.Command.class, this::stashOtherCommand)
                .build();
    }

    private Behavior<Command> onStartToIgnoreWords(GenerateToIgnoreWords generateToIgnoreWords) {
        try {
            this.toIgnoreWords = new HashSet<>();
            log("Cerco file");
            File file = new File(this.toIgnoreFilePath);

            if (file != null) {
                Scanner input = new Scanner(file);

                while (input.hasNext()) {
                    this.toIgnoreWords.add(input.next());
                }
                input.close();
            }
        } catch (FileNotFoundException e) {
            log("Attenzione, file non trovato");
        } catch (NullPointerException ex) {
            log("Attenzione, file non inserito");
        } finally {
            log("Finito");
        }

        return buffer.unstashAll(Behaviors.receive(Ignorer.Command.class)
                .onMessage(Ignorer.GetToIgnoreWords.class, this::onGetToIgnoreWords)
                .build());
    }

    private Behavior<Command> onGetToIgnoreWords(GetToIgnoreWords getToIgnoreWords) {
        getToIgnoreWords.replyTo.tell(new TextAnalyzer.ToIgnoreWords(this.toIgnoreWords));
        return Behaviors.same();
    }

    private Behavior<Ignorer.Command> stashOtherCommand(Ignorer.Command message) {
        // stash all other messages for later processing
        buffer.stash(message);
        return Behaviors.same();
    }

    private void log(String s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + "[Ignorer] " + s);
    }

}
