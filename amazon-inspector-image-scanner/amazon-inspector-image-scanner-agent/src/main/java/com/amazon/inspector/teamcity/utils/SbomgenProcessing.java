package com.amazon.inspector.teamcity.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SbomgenProcessing {
    public static int findSbomgenStartLineIndex(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).length() > 0 && list.get(i).charAt(0) == '{') {
                return i;
            }
        }

        return -1;
    }

    public static int findSbomgenEndLineIndex(List<String> list) {
        for (int i = list.size() - 1; i > 0 ; i--) {
            if (list.get(i).length() > 0 && list.get(i).charAt(0) == '}') {
                return i;
            }
        }

        return -1;
    }

    public static String processSbomgenFile(File outFile) throws IOException {
        String rawFileContent = new String(new FileInputStream(outFile).readAllBytes(), StandardCharsets.UTF_8);

        String[] splitRawFileContent = rawFileContent.split("\n");
        List<String> lines = new ArrayList<>();

        for (String line : splitRawFileContent) {
            lines.add(line.replaceAll("time=.+file=.+\"", ""));
        }

        lines = lines.subList(findSbomgenStartLineIndex(lines), findSbomgenEndLineIndex(lines)+1);
        return String.join("\n", lines);
    }
}
