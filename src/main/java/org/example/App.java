package org.example;


import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class App 
{
    public static void main( String[] args )
    {
        HttpClient client = HttpClient.create();

        String res = String.valueOf(client.get()
                .uri("https://catfact.ninja/fact")
                .responseContent()
                .asString()
                .collectList()
                .block(Duration.ofSeconds(5)));

        System.out.println(res);
    }

}
