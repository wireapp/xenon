# Xenon

Wire JVM base library.

This library defines how JVM based clients should interacts with cryptographic sessions.
While the underlying cryptobox4j just defines the operations possible on a local crypto session,
Xenon adds an abstraction to send-receive messages, taking care of pre-keys renewal, edge-cases and errors.

Xenon is developed mostly for stateful bots but can possibly work for any "user" related client.

Other functionalities:
- Databases tables defined for sessions stored in databases instead of file
- Definition of interfaces (not implementation) for API and Http client.

## How to use it?

- In your `pom.xml` import this library as:

```
<dependencies>
    <dependency>
        <groupId>com.wire</groupId>
        <artifactId>xenon</artifactId>
        <version>x.y.z</version>
    </dependency>
<dependencies>
```

## How to build the project

Requirements:

- [Java >= 17](http://www.oracle.com)
- [Maven](https://maven.apache.org)
- [Cryptobox4j](https://github.com/wireapp/cryptobox4j)
