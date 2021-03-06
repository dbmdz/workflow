package com.github.dbmdz.flusswerk.framework.fixtures;

import com.github.dbmdz.flusswerk.framework.flow.Flow;
import com.github.dbmdz.flusswerk.framework.flow.builder.FlowBuilder;
import com.github.dbmdz.flusswerk.framework.locking.NoOpLockManager;
import com.github.dbmdz.flusswerk.framework.model.Message;
import com.github.dbmdz.flusswerk.framework.reporting.Tracing;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class Flows {

  public static Flow passthroughFlow() {
    return messageProcessor(m -> m);
  }

  public static Flow consumingFlow() {
    var spec = FlowBuilder.messageProcessor(Message.class).consume(m -> {}).build();
    return new Flow(spec, new NoOpLockManager(), new Tracing());
  }

  private static Flow flowWithTransformer(Function<String, String> transformer) {
    var spec =
        FlowBuilder.flow(Message.class, String.class, String.class)
            .reader(Message::getTracingId)
            .transformer(transformer)
            .writerSendingMessage(Message::new)
            .build();
    return new Flow(spec, new NoOpLockManager(), new Tracing());
  }

  public static Flow flowThrowing(Class<? extends RuntimeException> cls) {
    var message = String.format("Generated %s for unit test", cls.getSimpleName());
    final RuntimeException exception;
    try {
      exception = cls.getConstructor(String.class).newInstance(message);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new Error("Could not instantiate exception", e); // If test is broken give up
    }
    Function<String, String> transformerWithException =
        s -> {
          throw exception;
        };
    return flowWithTransformer(transformerWithException);
  }

  public static Flow flowBlockingAllThreads() {
    return flowWithTransformer(new ThreadBlockingTransformer<>());
  }

  public static Flow messageProcessor(Function<Message, Message> function) {
    var spec = FlowBuilder.messageProcessor(Message.class).process(function).build();
    return new Flow(spec, new NoOpLockManager(), new Tracing());
  }
}
