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
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                String initParamsKey = "initParams";

                if (cluster.selfMember().hasRole("player")) {
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                }

                if(cluster.selfMember().hasRole("firstPlayer")){
                    context.spawn(PuzzleService.create(initParamsKey), "player");
                    context.spawn(InitService.create(initParamsKey, 3, 5, "src/main/java/PuzzleCentralized/bletchley-park-mansion.jpg"), "initService");
                }

                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            startup("firstPlayer", 25251);
            startup("player", 25252);
            startup("player", 0);
            startup("player", 0);
        }
        else {
            if (args.length == 2){
                startup(args[0], Integer.parseInt(args[1]));
            }
            else
                throw new IllegalArgumentException("Usage: role port");
        }
    }

    private static void startup(String role, int port) {

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();

        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("transformation"));

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }
}
