package com.github.dbmdz.flusswerk.framework.flow.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dbmdz.flusswerk.framework.TestMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SetReaderStep")
class ReaderStepTest {

  @DisplayName("should set a reader")
  @Test
  void shouldSetReader() {
    Model<TestMessage, String, String> model = new Model<>();
    ReaderStep<TestMessage, String, String> step = new ReaderStep<>(model);

    step.reader(TestMessage::getId);

    var expected = "test";
    var actual = model.getReader().apply(new TestMessage(expected));
    assertThat(actual).isEqualTo(expected);
  }
}
