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

public class PuzzleService extends AbstractBehavior<PuzzleService.Command> {
    interface Command {
    }

    public class Initialize implements Command {
        InitParams initParams;

        public Initialize(InitParams initParams) {
            this.initParams = initParams;
        }
    }

    public class Start implements Command {
    }

    public static class InitParams implements Command, CborSerializable, BoardActor.Command {
        int n;
        int m;
        String imagePath;

        public InitParams(int n, int m, String imagePath) {
            this.n = n;
            this.m = m;
            this.imagePath = imagePath;
        }
    }

    public static class GetValue implements Command {
        public final ActorRef<Integer> replyTo;

        public GetValue(ActorRef<Integer> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class GetCachedValue implements Command {
        public final ActorRef<Integer> replyTo;

        public GetCachedValue(ActorRef<Integer> replyTo) {
            this.replyTo = replyTo;
        }
    }

    enum Unsubscribe implements Command {
        INSTANCE
    }

    private interface InternalCommand extends Command {
    }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<LWWRegister<InitParams>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<LWWRegister<InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    private static class InternalGetResponse implements InternalCommand {
        final Replicator.GetResponse<LWWRegister<InitParams>> rsp;
        final ActorRef<Integer> replyTo;

        InternalGetResponse(Replicator.GetResponse<LWWRegister<InitParams>> rsp, ActorRef<Integer> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
        }
    }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<LWWRegister<InitParams>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<LWWRegister<InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    public static Behavior<Command> create(boolean first) {
        System.out.println("\n Create PuzzleService with first: " + first + "\n");
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter) ->
                                        new PuzzleService(ctx, replicatorAdapter, first)));
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWRegister<InitParams>> key;

    private ActorRef<BoardActor> boardActor;
    private InitParams cachedValue;
    private boolean first;

    private PuzzleService(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter,
            boolean first) {
        super(context);

        this.replicatorAdapter = replicatorAdapter;
        this.key = LWWRegisterKey.create("initParams");
        this.first = first;

        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);

        if (first) {
            InitParams initParams = new InitParams(3, 5, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg");
            this.getContext().getSelf().tell(new Initialize(initParams));
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Initialize.class, this::onInitialize)
                .onMessage(InternalUpdateResponse.class, msg -> Behaviors.same())
                .onMessage(Start.class, this::onStart)
                .onMessage(GetValue.class, this::onGetValue)
                .onMessage(GetCachedValue.class, this::onGetCachedValue)
                .onMessage(Unsubscribe.class, this::onUnsubscribe)
                .onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }

    private Behavior<Command> onInitialize(Initialize cmd) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWRegister.create(node, new InitParams(0, 0, "")),
                                Replicator.writeLocal(),
                                askReplyTo,
                                curr -> {
                                    /* System.out.println("Update initParams");
                                    GSet set = GSet.create().add(cmd.initParams);
                                    return set; */

                                    return LWWRegister.create(node, cmd.initParams);
                                }),
                InternalUpdateResponse::new);
        return this;
    }

    private Behavior<Command> onStart(Start a) {
        if (this.boardActor == null) {
            if (first)
                getContext().spawn(BoardActor.create(cachedValue.n, cachedValue.m, cachedValue.imagePath), "boardActor");
            else
                getContext().spawn(BoardActor.create(cachedValue.n, cachedValue.m), "boardActor");
        }
        return this;
    }

    private Behavior<Command> onGetValue(GetValue cmd) {
        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, Replicator.readLocal(), askReplyTo),
                rsp -> new InternalGetResponse(rsp, cmd.replyTo));
        return this;
    }

    private Behavior<Command> onGetCachedValue(GetCachedValue cmd) {
        //cmd.replyTo.tell(cachedValue);
        return this;
    }

    private Behavior<Command> onUnsubscribe(Unsubscribe cmd) {
        replicatorAdapter.unsubscribe(key);
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
        if (msg.rsp instanceof Replicator.Changed) {
            LWWRegister<InitParams> initParams = ((Replicator.Changed<LWWRegister<InitParams>>) msg.rsp).get(key);
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
