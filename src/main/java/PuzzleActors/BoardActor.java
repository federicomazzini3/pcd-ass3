package PuzzleActors;

import PuzzleActors.Puzzle.PuzzleBoardView;
import PuzzleActors.Puzzle.Tile;
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
import java.time.Duration;
import java.util.ArrayList;

public class BoardActor extends AbstractBehavior<BoardActor.Command> {

    public interface Command {
    }

    public static class GetTiles implements Command{}

    public static class TileRaw implements Command, CborSerializable {
        public int originalPosition;
        public int currentPosition;

        public TileRaw(int originalPosition, int currentPosition) {
            this.originalPosition = originalPosition;
            this.currentPosition = currentPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TileRaw tile = (TileRaw) o;
            return originalPosition == tile.originalPosition;
        }
    }

    public static class Swap implements Command, CborSerializable {
        private Tile tile1;
        private Tile tile2;

        public Swap(Tile tile1, Tile tile2) {
            this.tile1 = tile1;
            this.tile2 = tile2;
        }
    }

    public static class Tiles implements BoardActor.Command, CborSerializable {
        public ArrayList<TileRaw> tiles;

        public Tiles(ArrayList<TileRaw> tiles) {
            this.tiles = tiles;
        }

        public Tiles() {
            this.tiles = new ArrayList<>();
        }
    }

    private interface InternalCommand extends Command { }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<LWWRegister<Tiles>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<LWWRegister<Tiles>> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalGetResponse implements InternalCommand {
        final Replicator.GetResponse<LWWRegister<Tiles>> rsp;
        final ActorRef<BoardActor.Command> replyTo;

        InternalGetResponse(Replicator.GetResponse<LWWRegister<Tiles>> rsp, ActorRef<BoardActor.Command> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
        }
    }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<LWWRegister<Tiles>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<LWWRegister<Tiles>> rsp) {
            this.rsp = rsp;
        }
    }


    private final ReplicatorMessageAdapter<BoardActor.Command, LWWRegister<Tiles>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWRegister<Tiles>> key;
    private Tiles cachedValue;
    private final PuzzleBoardView puzzle;

    /**
     * Factory method e costruttore
     */
    public static Behavior<Command> create(int n, int m, String imagePath) {
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<BoardActor.Command, LWWRegister<Tiles>> replicatorAdapter) ->
                                        new BoardActor(ctx, replicatorAdapter, n, m, imagePath)));
    }

    private BoardActor(ActorContext<Command> context, ReplicatorMessageAdapter<BoardActor.Command, LWWRegister<Tiles>> replicatorAdapter, int n, int m, String imagePath) {
        super(context);
        System.out.println("\n BoardActor create \n");
        this.replicatorAdapter = replicatorAdapter;
        this.key = LWWRegisterKey.create("tiles");
        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);
        String nodeAddress = node.uniqueAddress().address().getHost().get() + ":" + node.uniqueAddress().address().getPort().get();
        this.puzzle = new PuzzleBoardView(n, m, imagePath, nodeAddress, this.getContext().getSelf());
        this.getContext().getSelf().tell(new GetTiles());
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
                .onMessage(GetTiles.class, this::onGetTiles)
                .onMessage(Tiles.class, this::onLoadTile)
                .onMessage(BoardActor.Swap.class, this::onSwap)
                .build();
    }

    private Behavior<Command> onGetTiles(GetTiles command) {
        System.out.println("\nGet Tiles");
        this.replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, new Replicator.ReadAll(Duration.ofSeconds(3)), askReplyTo),
                rsp -> new InternalGetResponse(rsp, this.getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<Command> onLoadTile(Tiles tiles) {
        System.out.println("\n " + this.getContext().getSelf().toString() + "Load tiles \n");
        cachedValue = tiles;
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWRegister.create(node, new Tiles(new ArrayList<>())),
                                new Replicator.WriteMajority(Duration.ofSeconds(5)),
                                askReplyTo,
                                curr -> LWWRegister.create(node, tiles)),
                BoardActor.InternalUpdateResponse::new);
        return this;
    }

    private Behavior<Command> onInternalGetResponse(InternalGetResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            System.out.println("\nGetResponse Success\n");
            LWWRegister<Tiles> tiles = ((Replicator.GetSuccess<LWWRegister<Tiles>>) msg.rsp).get(key);
            cachedValue = tiles.getValue();
            System.out.println("\nCreo le prime tiles\n" + this.getContext().getSelf().toString() + "Numero tiles: \n " + cachedValue.tiles.size());
            this.puzzle.refreshTiles(cachedValue);
            return this;
        } else {
            System.out.println("\nGetResponse Failed: need new tiles");
            this.puzzle.createAndLoadTiles();
            return Behaviors.same();
        }
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        if (msg.rsp instanceof Replicator.Changed) {
            LWWRegister<Tiles> tiles = ((Replicator.Changed<LWWRegister<Tiles>>) msg.rsp).get(key);
            if(node.uniqueAddress().uid()  != ((Replicator.Changed<LWWRegister<Tiles>>) msg.rsp).dataValue().updatedBy().uid()){
                System.out.println("\nNew board moves by: " + ((Replicator.Changed<LWWRegister<Tiles>>) msg.rsp).dataValue().updatedBy());
                cachedValue = tiles.getValue();
                System.out.println("\n" + this.getContext().getSelf().toString() + "Numero tiles: \n " + cachedValue.tiles.size());
                this.puzzle.refreshTiles(cachedValue);
            }
            return this;
        } else {
            // no deletes
            return Behaviors.unhandled();
        }
    }

    private Behavior<Command> onSwap(BoardActor.Swap swap) {
        //internal swap
        getContext().getLog().info("Swap delle caselle");
        this.puzzle.updateTiles(swap.tile1, swap.tile2);
        System.out.println("\n " + this.getContext().getSelf().toString() + "Load tiles \n");
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWRegister.create(node, cachedValue),
                                new Replicator.WriteMajority(Duration.ofSeconds(5)),
                                askReplyTo,
                                curr -> {
                                    System.out.println("\n cached value: " + cachedValue);
                                    TileRaw tileRaw1 = new TileRaw(swap.tile1.getOriginalPosition(), swap.tile1.getCurrentPosition());
                                    TileRaw tileRaw2 = new TileRaw(swap.tile2.getOriginalPosition(), swap.tile2.getCurrentPosition());
                                    cachedValue.tiles.remove(tileRaw1);
                                    cachedValue.tiles.remove(tileRaw2);
                                    cachedValue.tiles.add(tileRaw1);
                                    cachedValue.tiles.add(tileRaw2);
                                    return LWWRegister.create(node, cachedValue);
                                }),
                BoardActor.InternalUpdateResponse::new);
        return Behaviors.same();
    }
}
