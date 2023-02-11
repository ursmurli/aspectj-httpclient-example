package org.example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Aspect
public class ProxyAspect {

    @Around("execution(public static * reactor.netty.http.client.HttpClient.create())")
    public Object createProxy(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the actual instance

        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(5))
                .maxLifeTime(Duration.ofSeconds(10))
                .pendingAcquireTimeout(Duration.ofSeconds(20))
                .evictInBackground(Duration.ofSeconds(60)).build();

        System.out.println("######## Around Http Client created ");
        HttpClient client = (HttpClient)joinPoint.proceed();

        client = HttpClient
                .create(provider)
                .doOnRequest((req, conn) -> {
                    // only for debugging
                    System.out.printf(
                            ">>>: request(method:%s,  follow redirect: %s)\nuri: %s\n%s\n",
                            req.method(),
                            req.isFollowRedirect(),
                            req.resourceUrl(),
                            req
                    );
                }).doOnError(
                        (req, exp) -> System.out.printf(
                                "\nrequest error: %s\nmethod: %s\nfollow redirect: %s\n%s\n%s\n",
                                req.uri(),
                                req.method(),
                                req.isFollowRedirect(),
                                req,
                                exp.getMessage()
                        ), (res, exp) -> System.out.printf(
                                "\nresponse error: %s\nmethod: %s\n%s\n%s\n",
                                res.uri(),
                                res.method(),
                                res,
                                exp.getMessage()
                        )
                );

        return client;

    }
}
