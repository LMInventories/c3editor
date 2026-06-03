package com.caesar3.editor.data;

import java.util.List;

/**
 * Full parsed model file.
 *
 * allLines holds every line verbatim so that non-data content (comments,
 * headers, blank lines) is preserved exactly on save. Each BuildingEntry
 * and HouseEntry stores a lineIndex pointing back into allLines; serialisation
 * replaces only those indexed lines.
 */
public class ModelData {

    private final List<String> allLines;
    private final List<BuildingEntry> buildings;
    private final List<HouseEntry> houses;

    public ModelData(List<String> allLines,
                     List<BuildingEntry> buildings,
                     List<HouseEntry> houses) {
        this.allLines  = allLines;
        this.buildings = buildings;
        this.houses    = houses;
    }

    public List<String>        getAllLines() { return allLines; }
    public List<BuildingEntry> getBuildings(){ return buildings; }
    public List<HouseEntry>    getHouses()   { return houses; }

    /** Reconstruct the full file content ready to write to disk. */
    public String serialize() {
        String[] lines = allLines.toArray(new String[0]);

        for (BuildingEntry b : buildings) {
            lines[b.lineIndex] = b.toLine();
        }
        for (HouseEntry h : houses) {
            lines[h.lineIndex] = h.toLine();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}
