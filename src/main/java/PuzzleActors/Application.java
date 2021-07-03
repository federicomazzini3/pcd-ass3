package PuzzleActors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.*;

public class Application {

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
            if (args.length == 3){
                boolean first = false;
                if(args[2].equals("1"))
                    first = true;
                startup(args[0], Integer.parseInt(args[1]),first);
            }
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
