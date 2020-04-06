package de.digitalcollections.flusswerk.engine.flow.builder;

import de.digitalcollections.flusswerk.engine.model.Message;
import java.util.function.Function;

/**
 * Set a new transformer while building the new flow (typesafe).
 *
 * @param <M> The message class
 * @param <R> Generic type for the reader output/transformer input
 * @param <W> Generic type for the transformer output/writer input
 */
public class SetTransformerStep<M extends Message, R, W> {

  private Model<M, R, W> model;

  public SetTransformerStep(Model<M, R, W> model) {
    this.model = model;
  }

  /**
   * Sets a transformer that receives data of type <code>R</code> and returns new data of type
   * <code>R</code>, then moves you to the next builder step.
   *
   * @param t the transformer to set
   * @return the next step (setting a writer)
   */
  public SetWriterStep<M, R, W> transformer(Function<R, W> t) {
    model.setTransformer(t);
    return new SetWriterStep<>(model);
  }

  /**
   * Declares that the flow does not have a transformer and the output of the reader should go
   * directly into the writer, then moves you to the next builder step (seting a writer).
   *
   * @return the next step (setting a writer)
   */
  public SetWriterStep<M, R, W> noTransformer() {
    return new SetWriterStep<>(model);
  }
}
