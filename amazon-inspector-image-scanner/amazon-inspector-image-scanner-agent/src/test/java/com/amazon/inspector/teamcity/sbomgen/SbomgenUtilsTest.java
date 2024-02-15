package com.amazon.inspector.teamcity.sbomgen;

import com.amazon.inspector.teamcity.exception.MalformedScanOutputException;
import org.junit.Test;

import static com.amazon.inspector.teamcity.sbomgen.SbomgenUtils.processSbomgenOutput;
import static com.amazon.inspector.teamcity.sbomgen.SbomgenUtils.stripProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SbomgenUtilsTest {
    @Test
    public void testProcessSbomgenOutput() throws MalformedScanOutputException {
        String sbom = "time=dwadaw file=wdadawdwada\n{\ntest\n}\nbdwadawdaw";
        assertEquals(processSbomgenOutput(sbom), "{\ntest\n}");
    }

    @Test
    public void testStripProperties() {
        String bom = "{\"components\": [{\"properties\": []}]}";
        assertFalse(stripProperties(bom).contains("\"properties\""));
    }
}

