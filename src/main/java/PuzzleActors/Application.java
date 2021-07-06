package PuzzleActors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Application {

    private static class RootBehavior {
        static Behavior<Void> create(int rows, int cols, String imageUrl) {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                final String initParamsKey = "initParams";

                if (cluster.selfMember().hasRole("player")) {
                    context.spawn(PuzzleActor.create(initParamsKey), "player");
                }

                if (cluster.selfMember().hasRole("firstPlayer")) {
                    context.spawn(PuzzleActor.create(initParamsKey), "player");
                    context.spawn(InitActor.create(initParamsKey, rows, cols, imageUrl), "initService");
                }
                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args){
        InitialView initialView = new InitialView();
        initialView.display(true);
    }

    public static ActorSystem<Void> startup(String role, int rows, int cols, String imageUrl, String friendNode, String publicIPAddress, int publicPort, int localPort) throws UnknownHostException {
        // Override the configuration
        Map<String, Object> overrides = new HashMap<>();

        String localIpAddress = "localhost";
        if(!(publicIPAddress.equals("localhost") || publicIPAddress.equals("127.0.0.1")))
            localIpAddress = InetAddress.getLocalHost().getHostAddress();

        //Indirizzo e porta pubblica, visibile all'interno del cluster
        overrides.put("akka.remote.artery.canonical.hostname", publicIPAddress);
        overrides.put("akka.remote.artery.canonical.port", publicPort);

        //Indirizzo e porta del singolo nodo sulla macchina ospitante
        overrides.put("akka.remote.artery.bind.hostname", localIpAddress);
        overrides.put("akka.remote.artery.bind.port", localPort);

        //Indirizzo del nodo a cui collegarsi per richiedere il join alla partita
        //Può essere se stesso, se inizia una nuova partita, oppure un player connesso a una partita
        overrides.put("akka.cluster.seed-nodes", Collections.singletonList("akka://ClusterSystem@" + friendNode));

        //ruolo all' interno del cluster
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        System.out.println("\nakka.remote.artery.canonical.hostname = " + publicIPAddress);
        System.out.println("\nakka.remote.artery.canonical.port = " + publicPort);
        System.out.println("\nakka.remote.artery.bind.hostname = " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("\nakka.remote.artery.bind.port = " + localPort);
        System.out.println("\nakka.cluster.seed-nodes = " + Collections.singletonList("akka://ClusterSystem@" + friendNode));


        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("Puzzle/transformation"));

        return ActorSystem.create(RootBehavior.create(rows, cols, imageUrl), "ClusterSystem", config);
    }
}
