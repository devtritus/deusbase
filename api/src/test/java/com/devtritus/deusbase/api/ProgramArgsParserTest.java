package com.devtritus.deusbase.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramArgsParserTest {
    @Test
    void parse_test() {
        String[] args = new String[] {"-p", "1024", "-S", "host", "-d", "-k", "-ee=rr"};
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        assertThat(programArgs.get("p")).isEqualTo("1024");
        assertThat(programArgs.get("s")).isEqualTo("host");
        assertThat(programArgs.get("ee")).isEqualTo("rr");
        assertThat(programArgs.contains("d")).isTrue();
        assertThat(programArgs.contains("k")).isTrue();
        assertThat(programArgs.contains("not_exist")).isFalse();
    }
}
