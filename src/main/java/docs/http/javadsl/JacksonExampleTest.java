/*
 * Copyright (C) 2009-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.http.javadsl;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

import akka.actor.AbstractActor;
import akka.pattern.CircuitBreaker;
import akka.event.LoggingAdapter;
import akka.event.Logging;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class JacksonExampleTest extends AllDirectives {

    static class MyCircuitBreaker extends AbstractActor{
        final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
        final CircuitBreaker breaker = new CircuitBreaker(
                getContext().getDispatcher(),
                getContext().getSystem().getScheduler(),
                1,
                Duration.ofSeconds(3600),
                Duration.ofMinutes(1))
                .addOnOpenListener(this::notifyMeOnOpen);

        int cache[] = {0,0,0,0,0};
        int index = 0;

        int sumCache(){
            return cache[0]+cache[1]+cache[2]+cache[3]+cache[4];
        }

        public static Props props() {
            return Props.create(MyCircuitBreaker.class);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(
                            String.class,
                            "Poor"::equals,
                            m -> {
                                    cache[index] = 1;
                                    index = (index + 1) % 5;
                                    if (sumCache() >= 3) {
                                        breaker.fail();
                                    }
                                    sender().tell(breaker.callWithCircuitBreakerCS(this::fetchItem), self());
                            })
                    .match(
                            String.class,
                            "Good"::equals,
                            m -> {

                                    index = (index + 1) % 5;
                                    sender().tell(breaker.callWithCircuitBreakerCS(this::fetchItem), self());

                            })
                    .match(
                            String.class,
                            "Very Good"::equals,
                            m -> {

                                    index = (index + 1) % 5;
                                    sender().tell(breaker.callWithCircuitBreakerCS(this::fetchItem), self());

                            }).build();
        }

        public void notifyMeOnOpen() {
//            log.warning("My CircuitBreaker is now open, and will not close for one minute");
            System.out.println("My CircuitBreaker is now open, and will not close for one minute");
        }

        public CompletionStage<Optional<Item>> fetchItem() {
            return CompletableFuture.completedFuture(Optional.of(new Item("VideoStream"/*, 42*/)));
        }

    }




    public static void main(String[] args) throws Exception {

        // boot up server using the route as defined below
        akka.actor.ActorSystem system = akka.actor.ActorSystem.create("routes");

        final ActorRef breaker = system.classicSystem().actorOf(MyCircuitBreaker.props(), "Breaker");

        final Http http = Http.get(system);

        JacksonExampleTest app = new JacksonExampleTest();

        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                        .bind(app.createRoute(breaker));

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    // (fake) async database query api
    private CompletionStage<Optional<Item>> fetchItem(String name) {
        return CompletableFuture.completedFuture(Optional.of(new Item(name/*, 42*/)));
    }

    private Route createRoute(ActorRef breaker) {

        return concat(
                get(() ->
                        pathPrefix("VideoStream", () ->
                                {
                                    final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem("VideoStream");

                                    return onSuccess(futureMaybeItem, maybeItem -> {

                                                return maybeItem.map(item -> {
                                                            breaker.tell(item.quality, ActorRef.noSender());
                                                            return completeOK(item, Jackson.marshaller());
                                                        })
                                                        .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"));
                                            }
                                    );
                                })),
                get(() ->
                        pathPrefix("AudioStream", () ->
                                {
                                    final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem("AudioStream");
                                    return onSuccess(futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> {
                                                        breaker.tell(item.quality, ActorRef.noSender());
                                                        return completeOK(item, Jackson.marshaller());
                                                    })
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                })),
                get(() ->
                        pathPrefix("ChatStream", () ->
                                {
                                    final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem("ChatStream");
                                    return onSuccess(futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> {
                                                        breaker.tell(item.quality, ActorRef.noSender());
                                                        return completeOK(item, Jackson.marshaller());
                                                    })
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                }))
        );
    }
}

