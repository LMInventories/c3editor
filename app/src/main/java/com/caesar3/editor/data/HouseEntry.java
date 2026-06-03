package com.caesar3.editor.data;

/**
 * One house row from the model file.
 *
 * Line format (no closing brace):
 *   House N - Name,{,-99,-10,0,0,...,2.5,,,,,,
 *
 * name          - text before "{",  e.g. "House 1 - Tents"
 * rawAfterBrace - everything after "{",  e.g. ",-99,-10,0,..."
 *
 * rawAfterBrace split by "," with limit -1 (preserving trailing empties):
 *   index 0  - empty  (from the leading comma in ",-99,...")
 *   index 1  - devolve desirability threshold
 *   index 2  - evolve  desirability threshold
 *   index 3+ - undocumented requirements, preserved verbatim
 */
public class HouseEntry {

    public final int lineIndex;
    public final String name;

    private final String[] parts;   // rawAfterBrace.split(",", -1)
    private String devolveRaw;
    private String evolveRaw;

    public HouseEntry(int lineIndex, String name, String rawAfterBrace) {
        this.lineIndex = lineIndex;
        this.name      = name;
        this.parts     = rawAfterBrace.split(",", -1);
        this.devolveRaw = parts.length > 1 ? parts[1].trim() : "0";
        this.evolveRaw  = parts.length > 2 ? parts[2].trim() : "0";
    }

    public int getDevolveLevel() {
        try { return Integer.parseInt(devolveRaw); }
        catch (NumberFormatException e) { return 0; }
    }

    public int getEvolveLevel() {
        try { return Integer.parseInt(evolveRaw); }
        catch (NumberFormatException e) { return 0; }
    }

    public void setDevolveLevel(int v) { devolveRaw = String.valueOf(v); }
    public void setEvolveLevel(int v)  { evolveRaw  = String.valueOf(v); }

    /** Reconstruct the full file line. */
    public String toLine() {
        String[] out = parts.clone();
        if (out.length > 1) out[1] = devolveRaw;
        if (out.length > 2) out[2] = evolveRaw;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(out[i]);
        }
        return name + ",{" + sb.toString();
    }
}
