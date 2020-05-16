package com.pasetodemo.demo;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoParser;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.Version;
import dev.paseto.jpaseto.lang.Keys;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class DemoApplication {
    private static final SecretKey SHARED_SECRET = Keys.secretKey();
    private static final KeyPair KEY_PAIR = Keys.keyPairFor(Version.V1);

    public static void main(String[] args) {

        String tokenString = createToken();
        log("Paseto token: "+ tokenString);

        Paseto result = parseToken(tokenString);
        log("Token Claims:");
        result.getClaims().forEach((key, value) -> log("    "+ key + ": " + value));

        String audience = result.getClaims().getAudience();
        log("Audience: "+ audience);

        int rolledValue = result.getClaims().get("customclaim", Integer.class);
        log("customclaim rolled: " + rolledValue);

        parseTokenWithRequirements(tokenString);
    }

    public static String createToken() {
        Instant now = Instant.now();

        return Pasetos.V1.LOCAL.builder()
                .setSharedSecret(SHARED_SECRET)
                .setIssuedAt(now)
                .setExpiration(now.plus(1, ChronoUnit.HOURS))
                .setAudience("blog-post")
                .setIssuer("https://inmeta.no/blog/")
                .claim("customclaim", new Random().nextInt(100) + 1)
                .compact();
    }

    public static Paseto parseToken(String token) {
        PasetoParser parser = Pasetos.parserBuilder()
                .setSharedSecret(SHARED_SECRET)
                .setPublicKey(KEY_PAIR.getPublic())
                .build();

        return parser.parse(token);
    }

    public static void parseTokenWithRequirements(String token) {
        PasetoParser parser = Pasetos.parserBuilder()
                .setSharedSecret(SHARED_SECRET)
                .setPublicKey(KEY_PAIR.getPublic())
                .requireAudience("blog-post")
                .requireIssuer("https://inmeta.no/blog/")
                .build();

        parser.parse(token);
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
