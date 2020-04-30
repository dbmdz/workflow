package com.github.dbmdz.flusswerk.framework.flow.builder;

import static com.github.dbmdz.flusswerk.framework.flow.builder.InvocationProbe.beenInvoked;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.dbmdz.flusswerk.framework.flow.FlowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("The SetConfigurationStep")
public class SetConfigurationStepTest {

  private Model<TestMessage, String, String> model;
  private SetConfigurationStep<TestMessage, String, String> step;

  @BeforeEach
  void setUp() {
    model = new Model<>();
    step = new SetConfigurationStep<>(model);
  }

  @DisplayName("should set the cleanup task")
  @Test
  void shouldSetCleanupTask2() {
    var cleanupTask = new InvocationProbe<>();
    step.cleanup(cleanupTask);
    model.getCleanup().run();
    assertThat(cleanupTask).has(beenInvoked());
  }

  @DisplayName("should set the metrics task")
  @Test
  void shouldSetMetricsTask() {
    var metricsTask = new InvocationProbe<FlowStatus>();
    step.metrics(metricsTask);
    model.getMetrics().accept(null);
    assertThat(metricsTask).has(beenInvoked());
  }

  @DisplayName("should propagate flow ids")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void shouldSetMetricsTask(boolean value) {
    step.propagateFlowIds(value);
    assertThat(model.isPropagateFlowIds()).isEqualTo(value);
  }
}