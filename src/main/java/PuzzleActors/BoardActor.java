package PuzzleActors;

import PuzzleActors.Puzzle.PuzzleBoard;
import PuzzleActors.Puzzle.Tile;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class BoardActor extends AbstractBehavior<BoardActor.Command> {

    public interface Command{}

    public static class Swap implements Command{
        private Tile tile1;
        private Tile tile2;

        public Swap(Tile tile1, Tile tile2){
            this.tile1 = tile1;
            this.tile2 = tile2;
        }
    }

    private final PuzzleBoard puzzle;

    /** Factory method e costruttore */
    public static Behavior<Command> create(int n, int m, String imagePath) {
        return Behaviors.setup(context -> new BoardActor(context, n, m, imagePath));
    }

    public static Behavior<Command> create(int n, int m) {
        return Behaviors.setup(context -> new BoardActor(context, n, m));
    }

    private BoardActor(ActorContext<Command> context, int n, int m, String imagePath) {
        super(context);
        this.puzzle = new PuzzleBoard(n, m, this.getContext().getSelf());
        this.puzzle.createTiles(imagePath);
        puzzle.setVisible(true);
    }

    private BoardActor(ActorContext<Command> context, int n, int m) {
        super(context);
        this.puzzle = new PuzzleBoard(n, m, this.getContext().getSelf());
        puzzle.setVisible(true);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BoardActor.Swap.class, this::onSwap)
                .build();
    }

    private Behavior<Command> onSwap(BoardActor.Swap swap) {
        this.puzzle.updateTiles(swap.tile1, swap.tile2);
        getContext().getLog().info("Swap delle caselle");
        return Behaviors.same();
    }
}
