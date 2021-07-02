package PuzzleActors;

import PuzzleActors.Puzzle.PuzzleBoard;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.*;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class BoardActor extends AbstractBehavior<BoardActor.Command> {

    public interface Command {
    }

    public static class Tile implements Comparable<Tile>, CborSerializable, BoardActor.Command {
        private Image image;
        private int originalPosition;
        private int currentPosition;

        public Tile(final Image image, final int originalPosition, final int currentPosition) {
            this.image = image;
            this.originalPosition = originalPosition;
            this.currentPosition = currentPosition;
        }

        public Image getImage() {
            return image;
        }

        public boolean isInRightPlace() {
            return currentPosition == originalPosition;
        }

        public int getCurrentPosition() {
            return currentPosition;
        }

        public int getOriginalPosition() {
            return originalPosition;
        }

        public void setCurrentPosition(final int newPosition) {
            currentPosition = newPosition;
        }

        @Override
        public int compareTo(Tile other) {
            return this.currentPosition < other.currentPosition ? -1
                    : (this.currentPosition == other.currentPosition ? 0 : 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tile tile = (Tile) o;
            return originalPosition == tile.originalPosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalPosition);
        }
    }

    public static class TileRaw implements Command, CborSerializable{
        private byte[] image;
        private int originalPosition;
        private int currentPosition;
        public TileRaw(byte[] image, int originalPosition, int currentPosition){
            this.image = image;
            this.originalPosition = originalPosition;
            this.currentPosition = currentPosition;
        }
    }

    public static class Swap implements Command, CborSerializable{
        private Tile tile1;
        private Tile tile2;

        public Swap(Tile tile1, Tile tile2) {
            this.tile1 = tile1;
            this.tile2 = tile2;
        }
    }

    public static class Tiles implements PuzzleService.Command, BoardActor.Command, CborSerializable{
        ArrayList<TileRaw> tiles;

        public Tiles(ArrayList<TileRaw> tiles) {
            this.tiles = tiles;
        }
    }

    private interface InternalCommand extends Command {
    }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<ORSet<TileRaw>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<ORSet<TileRaw>> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalGetResponse implements InternalCommand {
        final Replicator.GetResponse<ORSet<TileRaw>> rsp;
        final ActorRef<Integer> replyTo;

        InternalGetResponse(Replicator.GetResponse<ORSet<TileRaw>> rsp, ActorRef<Integer> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
        }
    }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<ORSet<TileRaw>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<ORSet<TileRaw>> rsp) {
            this.rsp = rsp;
        }
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<BoardActor.Command, ORSet<TileRaw>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<ORSet<TileRaw>> key;
    private Tile cachedValue;
    private final PuzzleBoard puzzle;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(int n, int m, String imagePath) {
        //return Behaviors.setup(context -> new BoardActor(context, n, m, imagePath));
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<BoardActor.Command, ORSet<TileRaw>> replicatorAdapter) ->
                                        new BoardActor(ctx, replicatorAdapter, n, m, imagePath)));
    }

    public static Behavior<Command> create(int n, int m) {
        //return Behaviors.setup(context -> new BoardActor(context, n, m));
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<BoardActor.Command, ORSet<TileRaw>> replicatorAdapter) ->
                                        new BoardActor(ctx, replicatorAdapter, n, m)));
    }

    private BoardActor(ActorContext<Command> context, ReplicatorMessageAdapter<BoardActor.Command, ORSet<TileRaw>> replicatorAdapter, int n, int m, String imagePath){
        super(context);
        this.replicatorAdapter = replicatorAdapter;
        this.key = ORSetKey.create("tiles");
        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);

        this.puzzle = new PuzzleBoard(n, m, this.getContext().getSelf());
        this.puzzle.createTiles(imagePath);
        puzzle.setVisible(true);
    }

    private BoardActor(ActorContext<Command> context, ReplicatorMessageAdapter<BoardActor.Command, ORSet<TileRaw>> replicatorAdapter, int n, int m) {
        super(context);
        this.replicatorAdapter = replicatorAdapter;
        this.key = ORSetKey.create("tiles");
        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);

        this.puzzle = new PuzzleBoard(n, m, this.getContext().getSelf());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InternalUpdateResponse.class, msg -> {
                    System.out.println("\n Update done \n " + msg.rsp);
                    return Behaviors.same();
                }) //update effettuato dallo stesso attore
                .onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse) //update del replicator a cui ci si è sottoscritti
                .onMessage(Tiles.class, this::onLoadTile)
                .onMessage(BoardActor.Swap.class, this::onSwap)
                .build();
    }

    private Behavior<Command> onLoadTile(Tiles tiles) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                ORSet.create(),
                                Replicator.writeLocal(),
                                askReplyTo,
                                curr -> {
                                    /* System.out.println("Update initParams");
                                    GSet set = GSet.create().add(cmd.initParams);
                                    return set; */
                                    System.out.println("\n Load tiles \n");
                                    //System.out.println(loadTiles.tiles);
                                    //return LWWRegister.apply(node, loadTiles);
                                    //ORSet<Swap> set = ORSet.create().add(node, new Swap(null,null));

                                    return curr
                                            .add(node, tiles.tiles.get(0));
                                }),
                BoardActor.InternalUpdateResponse::new);
        return this;
    }

    private Behavior<Command> onInternalGetResponse(InternalGetResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            //int value = ((Replicator.GetSuccess<?>) msg.rsp).get(key).getValue().intValue();
            //msg.replyTo.tell(value);
            return this;
        } else {
            // not dealing with failures
            return Behaviors.unhandled();
        }
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        System.out.println("\n onInternalSubscribeResponse \n");
        if (msg.rsp instanceof Replicator.Changed) {
            ORSet<TileRaw> initParams = ((Replicator.Changed<ORSet<TileRaw>>) msg.rsp).get(key);
            System.out.println("onInternalSubscribeResponse");
            //cachedValue = initParams.getValue();
            //System.out.println("\n " + this.getContext().getSelf() + " Nuove tiles" + cachedValue.tiles);
            //replicatorAdapter.unsubscribe(key);
            /*if (!this.puzzle.isTilesInitialized()) {
                this.puzzle.initTiles(cachedValue.tiles);
                this.puzzle.setVisible(true);
            }*/
            return this;
        } else {
            // no deletes
            return Behaviors.unhandled();
        }
    }

    private Behavior<Command> onSwap(BoardActor.Swap swap) {
        this.puzzle.updateTiles(swap.tile1, swap.tile2);
        getContext().getLog().info("Swap delle caselle");
        return Behaviors.same();
    }
}
