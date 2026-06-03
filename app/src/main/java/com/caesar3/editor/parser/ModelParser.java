package com.caesar3.editor.parser;

import com.caesar3.editor.data.BuildingEntry;
import com.caesar3.editor.data.HouseEntry;
import com.caesar3.editor.data.ModelData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses c3_model.txt into a ModelData instance.
 *
 * Building lines start with a digit (the building ID).
 * House lines start with the literal word "House" followed by a digit.
 * Both must contain "{" to be parsed; all other lines are preserved verbatim.
 */
public class ModelParser {

    public static ModelData parse(InputStream inputStream) throws IOException {
        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        }

        List<BuildingEntry> buildings = new ArrayList<>();
        List<HouseEntry>    houses    = new ArrayList<>();

        for (int i = 0; i < allLines.size(); i++) {
            String line    = allLines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (isBuildingLine(trimmed)) {
                BuildingEntry e = parseBuilding(trimmed, i);
                if (e != null) buildings.add(e);
            } else if (isHouseLine(trimmed)) {
                HouseEntry e = parseHouse(trimmed, i);
                if (e != null) houses.add(e);
            }
        }

        return new ModelData(allLines, buildings, houses);
    }

    private static boolean isBuildingLine(String trimmed) {
        // Starts with one or more digits then a comma, and contains "{"
        return trimmed.length() > 2
                && Character.isDigit(trimmed.charAt(0))
                && trimmed.contains("{");
    }

    private static boolean isHouseLine(String trimmed) {
        // Starts with "House " followed by a digit (but NOT "House N,{" which are
        // building IDs 10-29 — those always start with their numeric ID, not "House").
        return trimmed.startsWith("House ")
                && trimmed.length() > 6
                && Character.isDigit(trimmed.charAt(6))
                && trimmed.contains("{");
    }

    // -------------------------------------------------------------------------
    // Building parser
    // -------------------------------------------------------------------------

    private static BuildingEntry parseBuilding(String line, int lineIndex) {
        int braceOpen = line.indexOf('{');
        if (braceOpen < 0) return null;

        int braceClose = line.indexOf('}', braceOpen);
        if (braceClose < 0) return null;

        // prefix = everything up to and including "{,"
        String prefix;
        if (braceOpen + 1 < line.length() && line.charAt(braceOpen + 1) == ',') {
            prefix = line.substring(0, braceOpen + 2);
        } else {
            prefix = line.substring(0, braceOpen + 1);
        }

        // Inner content: between the comma after "{" and the comma before "}"
        int contentStart = braceOpen + 1;
        if (contentStart < line.length() && line.charAt(contentStart) == ',') contentStart++;

        int contentEnd = braceClose;
        if (contentEnd > 0 && line.charAt(contentEnd - 1) == ',') contentEnd--;

        if (contentStart >= contentEnd) return null;

        String inner = line.substring(contentStart, contentEnd);
        String[] parts = inner.split(",", -1);

        int[] values = new int[8];
        for (int i = 0; i < 8; i++) {
            if (i < parts.length) {
                try { values[i] = Integer.parseInt(parts[i].trim()); }
                catch (NumberFormatException ignored) { values[i] = 0; }
            }
        }

        // suffix = everything after "}"
        String suffix = line.substring(braceClose + 1);

        return new BuildingEntry(lineIndex, prefix, suffix, values);
    }

    // -------------------------------------------------------------------------
    // House parser
    // -------------------------------------------------------------------------

    private static HouseEntry parseHouse(String line, int lineIndex) {
        int braceOpen = line.indexOf('{');
        if (braceOpen < 0) return null;

        // name is everything before the "," immediately before "{"
        int commaPos = braceOpen - 1;
        if (commaPos >= 0 && line.charAt(commaPos) == ',') {
            // normal: "House 1 - Tents,{"
        } else {
            commaPos = braceOpen; // fallback: no comma
        }

        String name          = line.substring(0, commaPos).trim();
        String rawAfterBrace = line.substring(braceOpen + 1); // everything after "{"

        return new HouseEntry(lineIndex, name, rawAfterBrace);
    }
}
