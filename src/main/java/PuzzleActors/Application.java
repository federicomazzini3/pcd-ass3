package PuzzleActors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.GCounterKey;
import akka.cluster.ddata.GSetKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Application {

    /* private static class RootBehavior {
        static Behavior<Void> create(int n, int m, String imagePath) {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());

                if (cluster.selfMember().hasRole("player")) {
                    context.spawn(PuzzleActor.create(n, m, imagePath), "player");
                }

                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args) {

        if (args.length == 0)
            startup("player", 25251, 5, 3, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg");
        else {
            if (args.length == 2)
                startup(args[0], Integer.parseInt(args[1]), 5, 3, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg");
            else if (args.length == 5)
                startup(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
            else
                throw new IllegalArgumentException("Usage: role port");
        }
    }

    private static void startup(String role, int port, int n, int m, String imagePath) {

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("transformation"));

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(n, m, imagePath), "ClusterSystem", config);
    } */

    private static class RootBehavior {
        static Behavior<Void> create(boolean first) {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());

                if (cluster.selfMember().hasRole("player")) {
                    context.spawn(PuzzleService.create(first), "player");
                }

                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            startup("player", 25251, true);
            startup("player", 25252, false);
            startup("player", 0, false);
            startup("player", 0, false);
        }
        else {
            if (args.length == 2)
                startup(args[0], Integer.parseInt(args[1]),false);
            else if (args.length == 5)
                startup(args[0], Integer.parseInt(args[1]),false);
            else
                throw new IllegalArgumentException("Usage: role port");
        }
    }

    private static void startup(String role, int port, boolean first) {

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("transformation"));

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(first), "ClusterSystem", config);
    }
}
