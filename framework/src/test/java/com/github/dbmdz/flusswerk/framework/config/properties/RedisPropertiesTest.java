package com.github.dbmdz.flusswerk.framework.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("The RedisProperties")
class RedisPropertiesTest {

  @DisplayName("should normalize addresses")
  @ParameterizedTest
  @CsvSource({
    "localhost,redis://localhost:6379",
    "redis://localhost,redis://localhost:6379",
    "localhost:1234,redis://localhost:1234",
    "rediss://localhost,rediss://localhost:6379",
    "rediss://localhost:1234,rediss://localhost:1234"
  })
  void shouldNormalizeAddresses(String address, String expected) {
    var properties = new RedisProperties(address, null, null, null);
    assertThat(properties.getAddress()).isEqualTo(expected);
  }

  @DisplayName("should allow not setting address")
  @ParameterizedTest(name = "name=\"{0}\"")
  @NullSource
  @ValueSource(strings = {"", " \t"})
  void shouldAllowNotSettingAddress(String address) {
    var properties = new RedisProperties(address, null, null, null);
    assertThat(properties.redisIsAvailable()).isFalse();
  }

  @DisplayName("should allow not setting password")
  @Test
  void shouldAllowNotSettingPassword() {
    var properties = new RedisProperties("localhost", null, null, null);
    assertThat(properties.getPassword()).isNull();
  }
}
