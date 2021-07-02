package PuzzleActors;

import PuzzleActors.Puzzle.Tile;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.*;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

import java.time.Duration;
import java.util.*;

import static akka.cluster.ddata.typed.javadsl.Replicator.*;

public class PuzzleService2 {

    public interface Command {
    }

    public enum Open implements Command {
        INSTANCE
    }

    public enum Close implements Command {
        INSTANCE
    }

    public static class InitParams implements Command, CborSerializable {
        int n;
        int m;
        String imagePath;

        public InitParams(int n, int m, String imagePath) {
            this.n = n;
            this.m = m;
            this.imagePath = imagePath;
        }
    }

    public static class Tiles implements Command {
        public final List<Tile> result;
        public final boolean open;

        public Tiles(List<Tile> result, boolean open) {
            this.result = result;
            this.open = open;
        }
    }

    public static class Swap implements Command {
        private Tile tile1;
        private Tile tile2;

        Swap(Tile tile1, Tile tile2) {
            this.tile1 = tile1;
            this.tile2 = tile2;
        }
    }

    public static class GetTiles implements Command {
        public final ActorRef<Tiles> replyTo;

        public GetTiles(ActorRef<Tiles> replyTo) {
            this.replyTo = replyTo;
        }
    }

    private interface InternalCommand extends Command {
    }

    private static class InternalSubscribeResponse<A extends ReplicatedData> implements InternalCommand {
        public final SubscribeResponse<A> rsp;

        private InternalSubscribeResponse(SubscribeResponse<A> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalUpdateResponse<A extends ReplicatedData> implements InternalCommand {
        public final UpdateResponse<A> rsp;

        private InternalUpdateResponse(UpdateResponse<A> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalGetResponse implements InternalCommand {
        public final ActorRef<Tiles> replyTo;
        public final GetResponse<GSet<Tile>> rsp;

        private InternalGetResponse(ActorRef<Tiles> replyTo, GetResponse<GSet<Tile>> rsp) {
            this.replyTo = replyTo;
            this.rsp = rsp;
        }
    }

    private final ReplicatorMessageAdapter<Command, Flag> replicatorFlag;
    private final ReplicatorMessageAdapter<Command, GSet<InitParams>> replicatorParams;
    private final ReplicatorMessageAdapter<Command, GSet<Tile>> replicatorTiles;
    private final SelfUniqueAddress node;

    private final Key<Flag> openedKey = FlagKey.create("contestOpened");
    private final Key<Flag> closedKey = FlagKey.create("contestClosed");
    private final Key<GSet<InitParams>> paramsKey = GSetKey.create("initParams");
    private final Key<GSet<Tile>> countersKey = GSetKey.create("contestTiles");
    private final WriteConsistency writeAll = new WriteAll(Duration.ofSeconds(5));
    private final ReadConsistency readAll = new ReadAll(Duration.ofSeconds(3));
    private final boolean first;

    public static Behavior<Command> create(Boolean first) {
        return Behaviors.setup(context ->
                DistributedData.withReplicatorMessageAdapter(
                        (ReplicatorMessageAdapter<Command, Flag> replicatorFlag) ->
                                DistributedData.withReplicatorMessageAdapter(
                                        (ReplicatorMessageAdapter<Command, GSet<InitParams>> replicatorParams) ->
                                                DistributedData.withReplicatorMessageAdapter(
                                                        (ReplicatorMessageAdapter<Command, GSet<Tile>> replicatorCounters) ->
                                                                new PuzzleService2(context, replicatorFlag, replicatorParams, replicatorCounters, first).createBehavior()
                                                )
                                )
                )
        );
    }

    private PuzzleService2(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, Flag> replicatorFlag,
            ReplicatorMessageAdapter<Command, GSet<InitParams>> replicatorParams,
            ReplicatorMessageAdapter<Command, GSet<Tile>> replicatorTiles,
            Boolean first
    ) {
        this.replicatorFlag = replicatorFlag;
        this.replicatorParams = replicatorParams;
        this.replicatorTiles = replicatorTiles;
        this.first = first;
        node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        replicatorFlag.subscribe(openedKey, InternalSubscribeResponse::new);
        replicatorParams.subscribe(paramsKey, InternalSubscribeResponse::new);
        if (first) {
            context.getSelf().tell(Open.INSTANCE);
        }
    }

    public Behavior<Command> createBehavior() {
        return Behaviors
                .receive(Command.class)
                .onMessageEquals(Open.INSTANCE, this::receiveOpen)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .onMessage(GetTiles.class, this::receiveGetVotesEmpty)
                .build();
    }

    private Behavior<Command> receiveOpen() {
        replicatorFlag.askUpdate(
                askReplyTo -> new Update<>(openedKey, Flag.create(), writeAll, askReplyTo, Flag::switchOn),
                InternalUpdateResponse::new);
        System.out.println("INFO: receiveOpen");

        if (this.first)
            replicatorParams.askUpdate(
                    askReplyTo -> new Update<>(paramsKey, GSet.create(), writeAll, askReplyTo,
                            curr -> {
                                System.out.println("\nCreo init params\n");
                                InitParams initParams = new InitParams(3, 5, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg");
                                return curr.add(initParams);
                            }),
                    InternalUpdateResponse::new);
        return becomeOpen();
    }

    private Behavior<Command> becomeOpen() {
        replicatorFlag.unsubscribe(openedKey);
        replicatorFlag.subscribe(closedKey, InternalSubscribeResponse::new);
        System.out.println("INFO: becomeOpen");
        return matchGetVotesImpl(true, matchOpen());
    }

    private Behavior<Command> receiveGetVotesEmpty(GetTiles getTiles) {
        getTiles.replyTo.tell(new Tiles(new ArrayList<>(), false));
        return Behaviors.same();
    }

    private BehaviorBuilder<Command> matchOpen() {
        System.out.println("INFO: matchOpen");
        return Behaviors
                .receive(Command.class)
                .onMessage(Swap.class, this::receiveSwap)
                .onMessage(InternalUpdateResponse.class, notUsed -> Behaviors.same()) // ok
                .onMessageEquals(Close.INSTANCE, this::receiveClose)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse);
    }

    private Behavior<Command> receiveSwap(Swap swap) {
        /*replicatorTiles.askUpdate(
                askReplyTo -> new Update<>(countersKey, GSet.create(), writeLocal(), askReplyTo,
                        curr -> {
                            curr.remove(swap.tile1);
                            curr.remove(swap.tile2);
                            curr.add(swap.tile1);
                            curr.remove(swap.tile2);
                            return curr;
                        }),
                InternalUpdateResponse::new);*/
        return Behaviors.same();
    }

    private Behavior<Command> receiveClose() {
        replicatorFlag.askUpdate(
                askReplyTo -> new Update<>(closedKey, Flag.create(), writeAll, askReplyTo, Flag::switchOn),
                InternalUpdateResponse::new);

        return matchGetVotes(false);
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse rsp) {
        System.out.println("\n onInternalSubscribeResponse \n");
        if (rsp.rsp instanceof Changed && rsp.rsp.key().equals(openedKey)) {
            if (((Changed<Flag>) rsp.rsp).dataValue().enabled()) {
                return becomeOpen();
            }
        } else if (rsp.rsp instanceof Changed && rsp.rsp.key().equals(closedKey)) {
            if (((Changed<Flag>) rsp.rsp).dataValue().enabled()) {
                return matchGetVotes(false);
            }
        } else if (rsp.rsp instanceof Changed && rsp.rsp.key().equals(paramsKey)) {
            if (((Changed<GSet<InitParams>>) rsp.rsp).dataValue() != null) {
                Set<InitParams> set = ((Changed<GSet<InitParams>>) rsp.rsp).dataValue().getElements();

                /*ORSet<InitParams> params = ((Changed<ORSet<InitParams>>) rsp.rsp).get(paramsKey);
                Set<InitParams> initParams = params.getElements();
                ArrayList<InitParams> list = new ArrayList<>(initParams);
                System.out.println("\n imagePath \n" + list.get(0).imagePath);*/
                System.out.println("\n size: " + ((Changed<GSet<InitParams>>) rsp.rsp).dataValue().getElements().size());
                return matchGetVotes(false);
            }
        }
        return Behaviors.same();
    }

    private Behavior<Command> matchGetVotes(boolean open) {
        return matchGetVotesImpl(open, Behaviors.receive(Command.class));
    }

    private Behavior<Command> matchGetVotesImpl(boolean open, BehaviorBuilder<Command> receive) {
        return receive
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .onMessage(GetTiles.class, this::receiveGetVotes)
                .onMessage(InternalGetResponse.class, rsp -> onInternalGetResponse(open, rsp))
                .onMessage(InternalUpdateResponse.class, notUsed -> Behaviors.same())
                .build();
    }

    private Behavior<Command> receiveGetVotes(GetTiles getTiles) {
        replicatorTiles.askGet(
                askReplyTo -> new Get<>(countersKey, readAll, askReplyTo),
                rsp -> new InternalGetResponse(getTiles.replyTo, rsp)
        );
        return Behaviors.same();
    }

    private Behavior<Command> onInternalGetResponse(boolean open, InternalGetResponse rsp) {
        if (rsp.rsp instanceof GetSuccess && rsp.rsp.key().equals(countersKey)) {
            GetSuccess<GSet<Tile>> rsp1 = (GetSuccess<GSet<Tile>>) rsp.rsp;
            Set<Tile> result = rsp1.dataValue().getElements();
            rsp.replyTo.tell(new Tiles(new ArrayList<>(result), open));
        } else if (rsp.rsp instanceof NotFound && rsp.rsp.key().equals(countersKey)) {
            rsp.replyTo.tell(new Tiles(new ArrayList<>(), open));
        } else if (rsp.rsp instanceof GetFailure && rsp.rsp.key().equals(countersKey)) {
            // skip
        }
        return Behaviors.same();
    }
}
