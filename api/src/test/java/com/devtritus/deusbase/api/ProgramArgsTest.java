package com.devtritus.deusbase.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramArgsTest {
    @Test
    void contains_test() {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("test", "value");
        keyValue.put("test1", null);
        ProgramArgs programArgs = new ProgramArgs(keyValue);

        assertThat(programArgs.contains("test")).isTrue();
        assertThat(programArgs.contains("test1")).isTrue();
        assertThat(programArgs.contains("not_exist")).isFalse();
    }

    @Test
    void get_or_default_test() {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("test", "value");
        ProgramArgs programArgs = new ProgramArgs(keyValue);

        assertThat(programArgs.getOrDefault("test", "default_value")).isEqualTo("value");
        assertThat(programArgs.getOrDefault("not_exist", "default_value")).isEqualTo("default_value");
    }

    @Test
    void get_value_test() {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("test", "value");
        ProgramArgs programArgs = new ProgramArgs(keyValue);

        String actual = programArgs.get("test");
        assertThat(actual).isEqualTo("value");
    }

    @Test
    void get_integer_value_test() {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("test", "345");
        ProgramArgs programArgs = new ProgramArgs(keyValue);

        int actual = programArgs.getInteger("test");
        assertThat(actual).isEqualTo(345);
    }

    @Test
    void get_list_of_values_test() {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("test1", "one,two,three");
        keyValue.put("test2", "one:two:three");
        keyValue.put("test3", "one;two;three");
        ProgramArgs programArgs = new ProgramArgs(keyValue);

        assertThat(programArgs.getList("test1")).isEqualTo(Arrays.asList("one", "two", "three"));
        assertThat(programArgs.getList("test2")).isEqualTo(Arrays.asList("one", "two", "three"));
        assertThat(programArgs.getList("test3")).isEqualTo(Arrays.asList("one", "two", "three"));
    }
}
