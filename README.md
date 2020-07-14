# Flusswerk - Digital Collections Workflow Engine

[![Javadocs](https://javadoc.io/badge/de.digitalcollections.flusswerk/dc-flusswerk-parent.svg)](https://javadoc.io/doc/de.digitalcollections.flusswerk/dc-flusswerk-parent)
[![Build status](https://gitlab.com/dbmdz/flusswerk/badges/master/pipeline.svg)](https://gitlab.com/dbmdz/flusswerk/commits/master)
[![Codecov](https://img.shields.io/codecov/c/github/dbmdz/flusswerk/master.svg)](https://codecov.io/gh/dbmdz/flusswerk)
[![License](https://img.shields.io/github/license/dbmdz/flusswerk.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/flusswerk.svg)](https://github.com/dbmdz/flusswerk/releases)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections.flusswerk/dc-flusswerk-parent.svg)](https://search.maven.org/search?q=a:dc-flusswerk-parent)

Flusswerk makes it easy to create multi threaded workers for read-transform-write chains (aka ETL jobs). Workflows are coordinated via RabbitMQ, so it's easy to create chains of independent workflow jobs (each a new Java application).

**Maven:**

```xml
<dependency>
  <groupId>com.github.dbmdz.flusswerk</groupId>
  <artifactId>framework</artifactId>
  <version>4.0.0-SNAPSHOT</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    compile group: 'com.github.dbmdz.flusswerk', name: 'flusswerk', version: '4.0.0-SNAPSHOT'
}
``` 
 
 ## Getting started

To get started, clone or copy the example at TODO. The example has three mino

 
## Migration to version 4

Starting with Flusswerk 4, there are two major changes:

 - Any Flusswerk application uses now Spring Boot and needs beans for `FlowSpec` (defining the
    processing) and `IncomingMessage`.
 - The package names changed from `de.digitalcollections.flusswerk.engine` to `com.github.dbmdz.framework`.

## The Big Picture

A typical Flusswerk application has three parts:

 - Messages
 - the data processing flow (usually a `Reader`, a `Transformer` and a `Writer`)
 - Some Spring Boot glue code
 - Spring Boot `application.yml` to configure all the Flusswerk things

Usually it is also useful to define your own data model classes, although that is not strictly required.

 - Any Flusswerk application uses now Spring Boot and needs beans for `FlowSpec` (defining the
    processing) and `IncomingMessage`.
 - The package names changed from `de.digitalcollections.flusswerk.engine` to `com.github.dbmdz.framework`.
Optional parts are

 - Custom metrics collection
 - Custom logging formats (aka `ProcessReport`)


### Messages

Message classes are for a sending and receiving data from RabbitMQ. All Message classes extend `Message`, which automatically forwards tracing ids from incoming to outgoing messages (if you set the tracing id by hand, it will not be overwritten).

```java
class IndexMessage implements Message {

  private String documentId;

  public IndexMessage(String documentId) {
    this.documentId = requireNonNull(documentId);
  }

  public String getId() { ... }

  public boolean equals(Object other) { ... }

  public int hashCode() { ... }

}
```

Register the type for the incoming message, so it gets automatically deserialized:

```java
@Bean
public IncomingMessageType incomingMessageType() {
  return new IncomingMessageType(IndexMessage.class);
}
```


### Configuration

All configuraton magic happens in Spring's `application.yml`.

A minimal configuration might look like:

```yml
# Default spring profile is local
spring:
  application:
    name: flusswerk-example

flusswerk:
  routing:
    incoming:
      - search.index
    outgoing:
      default: search.publish
```

This defaults to connecting to RabbitMQ localhost:5672, with user and password `guest`, five threads and retrying a message five times. The only outgoing route defined is default, which is used by Flusswerk to automatically send messages. For most applications these are sensible defaults and works out of the box for local testing.

The connection information can be overwritten for different environments using Spring Boot profiles:

```yml
---
spring:
  profiles: production
flusswerk:
  rabbitmq:
    hosts:
      - rabbitmq.stg
    username: secret
    password: secret
```

The sections of the `Flusswerk` configuration

`processing` - control of processing

| property  | default |                                                  |
|-----------|---------|--------------------------------------------------| 
| `threads` | 5       | Number of threads to use for parallel processing |

`rabbitmq` - Connection to RabbitMQ:

| property    | default     |                             |
|-------------|-------------|-----------------------------| 
| `hosts`     | `localhost` | list of hosts to connect to |
| `username`  | `guest`     | RabbitMQ username           |
| `passwords` | `guest`     | RabbitMQ password           |


`routing` - Messages in and out

| property           | default   |                                                   |
|--------------------|-----------|---------------------------------------------------| 
| `incoming`         | `–`       | list of queues to read from in order              |
| `outgoing`         | `–`       | routes to send messages to (format 'name: topic') |
| `failure policies` | `default` | how to handle messages with processing errors     |

`routing.failure policies` - how to handle messages with processing errors

| property           | default |                                                              |
|--------------------|---------|--------------------------------------------------------------|
| `retries`          | `5`     | how many times to retry                                      |
| `retryRoutingKey`  | `–`     | where to send messages to retry later *(dead lettering)*     |
| `failedRoutingKey` | `–`     | where to send messages to that should not be processed again |
| `backoff`          | `–`     | how long to wait until retrying a message                    |

`monitoring` - Prometheus settings

| property | default     |                               |
|----------|-------------|-------------------------------| 
| `prefix` | `flusswerk` | prefix for prometheus metrics |

### Data Processing

To setup your data processing flow, define a Spring bean of type FlowSpec:

```java
@Bean
public FlowSpec flowSpec(Reader reader, Transformer transformer, Writer writer) {
  return FlowBuilder.flow(IndexMessage.class, Document.class, IndexDocument.class)
      .reader(reader)
      .transformer(transformer)
      .writerSendingMessage(writer)
      .build();
}
```

With the `Reader`, `Transformer` and `Writer` implementing the `Function` interface:

|               |                                     |                                                                         |
|---------------|-------------------------------------|-------------------------------------------------------------------------|
| `Reader`      | `Function<IndexMessage, Document>`  | loads document from storage                                             |
| `Transformer` | `Function<Document, IndexDocument>` | uses `Document` to build up the datastructure needed for indexing       |
| `Writer`      | `Function<IndexDocument, Message>`  | sends indexes the data and returns a message for the next workflow step |




## Best Practices

### Stateless Processing

All classes that do data processing (Reader, Transformer, Writer,...) should be stateless. This has two reasons:

First, it makes your code threadsafe and multiprocessing easy without you having to even think about it. Just keep it stateless and fly!

Second, it makes testing a breeze: You throw in data and check the data that comes out. Things can go wrong? Just check if the right exception is thrown. Wherever you need to interact with extrenal services, mock the behaviour and your good to go (the Flusswerk tests make heay use of Mockito, btw.).


If you absolutely have to introduce state, make sure your code is threadsafe.


### Immutable Data

Wherever sesnsible, make your data classes immutable - set everything via constructor and avoid setters. Implement `equals()` and `hashCode()`. This leads usually to more readable code, and makes writing tests much easier. This applies to Message classes and to the classes you use to pass data around. 

Your particular data processing needs to build your data over time and can't be immutable? Think again if that is the best way, but don't worry too much.



## Manual Interaction with RabbitMQ

For manual interaction with RabbitMQ there is a Spring component with the same class:

| `RabbitMQ`       |                                                                         |
|------------------|-------------------------------------------------------------------------|
| `ack(Message)`   | acknowledges a `Message` received from a `Queue`                        |
| `queue(String)`  | returns the `Queue` instance to interact with a queue of the given name |
| `topic(Message)` | returns the `Topic` instance for the given name to send messages to     |
| `route(Message)` | returns the `Topic` instance for the given route from `application.yml` |


## Cleanup

If you want to perform cleanups after processing of the message, e.g. for triggering a garbage collection, you can use the
```cleanup()``` method of the FlowBuilder:

```java
class Application {
  public static void main(String[] args) {
    MessageBroker messageBroker = new MessageBrokerBuilder()
        .readFrom("your.input.queue")
        .writeTo("your.output.queue")
        .build();
    
    Flow flow = new FlowBuilder<DefaultMessage, String, String>()
        .read(new Reader())
        .transform(new Transformer())
        .writeAndSend(new Writer())
        .cleanup(() -> Runtime.getRuntime().gc())
        .build();
    
    Engine engine = new Engine(messageBroker, flow);
    engine.start();
  }
}
```

## Propagate FlowIds

If incoming and outgoing message classes implement `HasFlowId`, automated propagation of flow ids is available. If the option `flowBuilder.propagateFlowId(true)` is set, Flusswerk copies flow ids from incoming to all outgoing messages.




## Failure Policies

TODO


## Collecting Metrics

TODO

## Customize Logging

TODO