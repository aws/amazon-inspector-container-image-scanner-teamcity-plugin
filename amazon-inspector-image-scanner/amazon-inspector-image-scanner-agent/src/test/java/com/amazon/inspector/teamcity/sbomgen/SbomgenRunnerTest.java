package com.amazon.inspector.teamcity.sbomgen;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SbomgenRunnerTest {

    @Test
    public void testIsValidPath() {
        SbomgenRunner runner = new SbomgenRunner(null, null, null, null);
        assertTrue(runner.isValidPath("alpine:latest"));
        assertFalse(runner.isValidPath("alpine:latest&&ls"));
    }
}