package de.digitalcollections.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.workflow.engine.model.Message;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public class MessageBrokerBuilder {

  private final MessageBrokerConfig config;

  public MessageBrokerBuilder() {
    config = new MessageBrokerConfig();
    config.setHostName("localhost");
    config.setPassword("guest");
    config.setPort(5672);
    config.setUsername("guest");
    config.setVirtualHost("/");
    config.setObjectMapper(new ObjectMapper());
    config.setMaxRetries(5);
    config.setDeadLetterWait(30 * 1000);
  }

  /**
   * Sets the RabbitMQ host name. Default is <em>localhost</em>.
   *
   * @param hostName The hostname to connect to.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder hostName(String hostName) {
    config.setHostName(requireNonNull(hostName));
    return this;
  }

  /**
   * Sets the RabbitMQ password for authentication.
   *
   * @param password The password for authentication.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder password(String password) {
    config.setPassword(requireNonNull(password));
    return this;
  }

  /**
   * Sets the RabbitMQ port.
   *
   * @param port The RabbitMQ port.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder port(int port) {
    if (port <= 0) {
      throw new IllegalArgumentException("Port value must be > 0");
    }
    config.setPort(port);
    return this;
  }

  /**
   * Sets the RabbitMQ username for authentication.
   *
   * @param username The username for authentication.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder username(String username) {
    config.setUsername(requireNonNull(username));
    return this;
  }

  /**
   * Sets the internal RabbitMQ virtualHost (default is "\").
   *
   * @param virtualHost The virtual host.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder virtualHost(String virtualHost) {
    config.setVirtualHost(requireNonNull(virtualHost));
    return this;
  }


  /**
   * Sets the Jackson {@link ObjectMapper} to use if you do not want to use the default one.
   *
   * @param objectMapper The {@link ObjectMapper}
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder objectMapper(ObjectMapper objectMapper) {
    config.setObjectMapper(requireNonNull(objectMapper));
    return this;
  }

  /**
   * Sets the time to wait for dead-lettered messages before these are returned to the queue.
   *
   * @param milliseconds The waiting time.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder deadLetterWait(int milliseconds) {
    config.setDeadLetterWait(milliseconds);
    return this;
  }

  /**
   * Sets the maximum number of attempts before a message is sent to the failed queue instead of the dead letter queue.
   *
   * @param number The maximum number of attempts to process a message.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder maxRetries(int number) {
    if (number < 0) {
      throw new IllegalArgumentException("Max number of retries must be at least 0.");
    }
    config.setMaxRetries(number);
    return this;
  }

  /**
   * Sets a Jackson mixin for a custom message implementation.
   *
   * @param messageClass The custom message implementation you want to use.
   * @param messageMixin The mixin to serialize/deserialize this message.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder messageMapping(Class<? extends Message> messageClass, Class<?> messageMixin) {
    config.setMessageClass(messageClass);
    config.setMessageMixin(messageMixin);
    return this;
  }

  /**
   * Sets the AMQP exchange and dead letter exchange (optional, defaults to the RabbitMQ defaults). If an exchange does not exist it will be created.
   *
   * @param exchange The regular exchange.
   * @param deadLetterExchange The dead letter exchange.
   * @return This {@link MessageBrokerBuilder} instance to chain configuration calls.
   */
  public MessageBrokerBuilder exchanges(String exchange, String deadLetterExchange) {
    config.setExchange(requireNonNull(exchange));
    config.setDeadLetterExchange(requireNonNull(deadLetterExchange));
    return this;
  }

  /**
   * Finally builds the {@link MessageBroker} as configured, up and running and connected it to RabbitMQ.
   *
   * @return A new MessageBroker
   * @throws IOException If connection to RabbitMQ fails.
   * @throws TimeoutException If connection to RabbitMQ fails.
   */
  public MessageBroker build() throws IOException, TimeoutException {
    MessageBrokerConnection connection = new MessageBrokerConnection(config);
    return new MessageBroker(config, connection);
  }
}
