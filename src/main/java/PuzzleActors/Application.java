package PuzzleActors;

import akka.actor.AddressFromURIString;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Int;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Application {

    private static class RootBehavior {
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                String initParamsKey = "initParams";

                if (cluster.selfMember().hasRole("player")) {
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                }

                if (cluster.selfMember().hasRole("firstPlayer")) {
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                    context.spawn(InitService.create(initParamsKey, 2, 4, "https://i.ytimg.com/vi/JNslcFZw7Zo/maxresdefault.jpg"), "initService");
                }

                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args) throws UnknownHostException {

        if (args.length == 0) {
            startup("firstPlayer", "localhost:25251", "localhost", 25251, 25251);
            startup("player", "localhost:25251", "localhost", 25252, 25252);
            startup("player", "localhost:25251", "localhost", 0, 0);
            startup("player", "localhost:25251", "localhost", 0, 0);
        } else {
            if (args.length == 5) {
                startup(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            } else
                throw new IllegalArgumentException("Usage: role port");
        }
    }

    private static void startup(String role, String friendNode, String publicIPAddress, int publicPort, int localPort) throws UnknownHostException {
        // Override the configuration
        Map<String, Object> overrides = new HashMap<>();

        String localIpAddress = "localhost";
        if(!publicIPAddress.equals("localhost"))
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
                .withFallback(ConfigFactory.load("transformation"));

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }
}
