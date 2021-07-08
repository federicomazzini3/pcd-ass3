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

/**
 * Tramite gossip gli vengono comunicati i parametri iniziali e crea la board per il giocatore (facendo lo spawn di un attore BoardActor)
 */
public class PuzzleActor extends AbstractBehavior<PuzzleActor.Command> {
    interface Command {}

    public class Start implements Command { }

    private interface InternalCommand extends Command { }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<LWWRegister<InitActor.InitParams>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<LWWRegister<InitActor.InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    public static Behavior<Command> create(String nodeAddress) {
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, LWWRegister<InitActor.InitParams>> replicatorAdapter) ->
                                        new PuzzleActor(ctx, replicatorAdapter, nodeAddress)));
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<Command, LWWRegister<InitActor.InitParams>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWRegister<InitActor.InitParams>> key;

    private ActorRef<BoardActor.Command> boardActor;
    private InitActor.InitParams cachedValue;

    private PuzzleActor(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, LWWRegister<InitActor.InitParams>> replicatorAdapter,
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
                .onMessage(Start.class, this::onStart)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }

    private Behavior<Command> onStart(Start a) {
        if (this.boardActor == null) {
            this.boardActor = getContext().spawn(BoardActor.create(cachedValue.n, cachedValue.m, cachedValue.imagePath), "boardActor");
        }
        return this;
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        if (msg.rsp instanceof Replicator.Changed) {
            LWWRegister<InitActor.InitParams> initParams = ((Replicator.Changed<LWWRegister<InitActor.InitParams>>) msg.rsp).get(key);
            cachedValue = initParams.getValue();
            this.getContext().getLog().info(" Parametri iniziali: " + cachedValue.n + " " + cachedValue.m + " " + cachedValue.imagePath);
            replicatorAdapter.unsubscribe(key);
            this.getContext().getSelf().tell(new Start());
            return this;
        } else {
            return Behaviors.unhandled();
        }
    }
}
