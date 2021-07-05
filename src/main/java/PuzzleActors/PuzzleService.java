package PuzzleActors;

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

public class PuzzleService extends AbstractBehavior<PuzzleService.Command> {
    interface Command {
    }

    public static class GetInitParams implements Command {}

    public class Start implements Command { }

    private interface InternalCommand extends Command { }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<LWWRegister<InitService.InitParams>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<LWWRegister<InitService.InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalGetResponse implements InternalCommand {
        final Replicator.GetResponse<LWWRegister<InitService.InitParams>> rsp;
        final ActorRef<PuzzleService.Command> replyTo;

        InternalGetResponse(Replicator.GetResponse<LWWRegister<InitService.InitParams>> rsp, ActorRef<PuzzleService.Command> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
        }
    }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<LWWRegister<InitService.InitParams>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<LWWRegister<InitService.InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    public static Behavior<Command> create(String initParamsKey) {
        System.out.println("\n Create PuzzleService with first:\n");
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, LWWRegister<InitService.InitParams>> replicatorAdapter) ->
                                        new PuzzleService(ctx, replicatorAdapter, initParamsKey)));
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<Command, LWWRegister<InitService.InitParams>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWRegister<InitService.InitParams>> key;

    private ActorRef<BoardActor> boardActor;
    private InitService.InitParams cachedValue;

    private PuzzleService(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, LWWRegister<InitService.InitParams>> replicatorAdapter,
            String initParamsKey) {
        super(context);

        this.replicatorAdapter = replicatorAdapter;
        this.key = LWWRegisterKey.create(initParamsKey);

        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InternalUpdateResponse.class, msg -> Behaviors.same())
                .onMessage(Start.class, this::onStart)
                .onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }

    private Behavior<PuzzleService.Command> onGetInitParams(PuzzleService.GetInitParams command) {
        this.replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, new Replicator.ReadMajority(Duration.ofSeconds(3)), askReplyTo),
                rsp -> new PuzzleService.InternalGetResponse(rsp, this.getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<Command> onStart(Start a) {
        if (this.boardActor == null) {
            getContext().spawn(BoardActor.create(cachedValue.n, cachedValue.m, cachedValue.imagePath), "boardActor");
        }
        return this;
    }

    private Behavior<PuzzleService.Command> onInternalGetResponse(PuzzleService.InternalGetResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            System.out.println("\n onInternalGetResponse Success\n");
            //int value = ((Replicator.GetSuccess<?>) msg.rsp).get(key).getValue().intValue();
            //msg.replyTo.tell(value);
        } else {
            // not dealing with failures
            System.out.println("\n onInternalGetResponse Failed\n");
            //this.puzzle.createAndLoadTiles();
        }
        return Behaviors.same();
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        if (msg.rsp instanceof Replicator.Changed) {
            LWWRegister<InitService.InitParams> initParams = ((Replicator.Changed<LWWRegister<InitService.InitParams>>) msg.rsp).get(key);
            cachedValue = initParams.getValue();
            System.out.println("\n " + this.getContext().getSelf() + " Parametri iniziali: " + cachedValue.n + " " + cachedValue.m + " " + cachedValue.imagePath);
            replicatorAdapter.unsubscribe(key);
            this.getContext().getSelf().tell(new Start());
            return this;
        } else {
            // no deletes
            return Behaviors.unhandled();
        }
    }
}
