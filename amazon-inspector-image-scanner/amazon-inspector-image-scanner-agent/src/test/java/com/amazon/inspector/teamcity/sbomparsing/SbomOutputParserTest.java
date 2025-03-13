package com.amazon.inspector.teamcity.sbomparsing;

import com.amazon.inspector.teamcity.models.sbom.Components.Rating;
import com.amazon.inspector.teamcity.models.sbom.Components.Source;
import com.amazon.inspector.teamcity.models.sbom.Components.Vulnerability;
import com.amazon.inspector.teamcity.models.sbom.Sbom;
import com.amazon.inspector.teamcity.models.sbom.SbomData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SbomOutputParserTest {
    @Test
    public void testParseSbom_Successful() throws IOException {
        String str = new String(Files.readAllBytes(Paths.get("src/test/resources/data/SbomOutputExample.json")), StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        SbomData sbomData = SbomData.builder().sbom(gson.fromJson(str, Sbom.class)).build();

        SbomOutputParser parser = new SbomOutputParser(sbomData);
        parser.parseVulnCounts();

        SeverityCounts severityCounts = new SeverityCounts(47, 214, 110, 9, 0);
        assertEquals(SbomOutputParser.aggregateCounts.getCounts(), severityCounts.getCounts());
    }

}
