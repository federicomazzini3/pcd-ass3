package PuzzleActors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWRegister;
import akka.cluster.ddata.LWWRegisterKey;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

/**
 * Si occupa dei parametri iniziali del puzzle: rows, columns, image
 */
public class InitActor extends AbstractBehavior<InitActor.Command> {

    interface Command { }

    public static class InitParams implements Command, CborSerializable{
        int n;
        int m;
        String imagePath;

        public InitParams(int n, int m, String imagePath) {
            this.n = n;
            this.m = m;
            this.imagePath = imagePath;
        }
    }

    private interface InternalCommand extends Command { }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<LWWRegister<InitParams>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<LWWRegister<InitParams>> rsp) {
            this.rsp = rsp;
        }
    }

    public static Behavior<Command> create(String registerKey, int n, int m, String imagePath) {
        System.out.println("\n Create InitActor: \n");
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter) ->
                                        new InitActor(ctx, replicatorAdapter, registerKey, n, m, imagePath)));
    }


    private final ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWRegister<InitParams>> key;

    private InitActor(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, LWWRegister<InitParams>> replicatorAdapter,
            String registerKey,
            int n,
            int m,
            String imagePath) {
        super(context);

        this.replicatorAdapter = replicatorAdapter;
        this.key = LWWRegisterKey.create(registerKey);

        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();
        InitParams initParams = new InitParams(n, m, imagePath);
        this.getContext().getSelf().tell(initParams);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InitParams.class, this::onInitialize)
                .onMessage(InternalUpdateResponse.class, msg -> {
                    System.out.println(msg.rsp);
                    return Behaviors.same();
                })
                .build();
    }

    //Inizializzazione dei parametri iniziali del puzzle all' interno della partita
    private Behavior<Command> onInitialize(InitParams initParams) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWRegister.create(node, initParams),
                                Replicator.writeLocal(),
                                askReplyTo,
                                curr -> LWWRegister.create(node, initParams)),
                InternalUpdateResponse::new);
        return Behaviors.same();
    }
}
