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
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                }

                if (cluster.selfMember().hasRole("firstPlayer")) {
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                    context.spawn(InitService.create(initParamsKey, rows, cols, imageUrl), "initService");

                }

                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args){
        View view = new View();
        view.setVisible(true);
    }

    public static ActorSystem<Void> startup(String role, int rows, int cols, String imageUrl, String friendNode, String publicIPAddress, int publicPort, int localPort) throws UnknownHostException {
        // Override the configuration
        Map<String, Object> overrides = new HashMap<>();

        String localIpAddress = "localhost";
        if(!(publicIPAddress.equals("localhost") || publicIPAddress.equals("127.0.0.1")))
            localIpAddress = InetAddress.getLocalHost().getHostAddress();

        overrides.put("akka.remote.artery.canonical.hostname", publicIPAddress);
        overrides.put("akka.remote.artery.canonical.port", publicPort);


        overrides.put("akka.remote.artery.bind.hostname", localIpAddress);
        overrides.put("akka.remote.artery.bind.port", localPort);

        overrides.put("akka.cluster.seed-nodes", Collections.singletonList("akka://ClusterSystem@" + friendNode));


        System.out.println("\nakka.remote.artery.canonical.hostname = " + publicIPAddress);
        System.out.println("\nakka.remote.artery.canonical.port = " + publicPort);
        System.out.println("\nakka.remote.artery.bind.hostname = " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("\nakka.remote.artery.bind.port = " + localPort);
        System.out.println("\nakka.cluster.seed-nodes = " + Collections.singletonList("akka://ClusterSystem@" + friendNode));

        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("Puzzle/transformation"));

        return ActorSystem.create(RootBehavior.create(rows, cols, imageUrl), "ClusterSystem", config);
    }
}
