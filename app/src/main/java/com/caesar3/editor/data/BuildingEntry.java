package com.caesar3.editor.data;

/**
 * One building row from the model file.
 *
 * Line format:  ID,Name,{,CST,DES,STP,SZE,RGE,EMP,F1,F2,},suffix
 *
 * prefix     - everything up to and including "{,"  e.g. "30,Ampitheatre,{,"
 * values[0]  - cost
 * values[1]  - initial desirability
 * values[2]  - desirability step (in tiles)
 * values[3]  - desirability step size
 * values[4]  - max desirability range
 * values[5]  - employees
 * values[6]  - future expansion 1 (preserved, not editable)
 * values[7]  - future expansion 2 (preserved, not editable)
 * suffix     - everything after "}"  e.g. "," or ",16 or multiple of 16"
 */
public class BuildingEntry {

    public final int lineIndex;
    public final int id;
    public final String name;
    private final String prefix;
    private final String suffix;
    private final int[] values = new int[8];

    public BuildingEntry(int lineIndex, String prefix, String suffix, int[] src) {
        this.lineIndex = lineIndex;
        this.prefix    = prefix;
        this.suffix    = suffix;
        System.arraycopy(src, 0, values, 0, Math.min(src.length, 8));

        // Extract id and name from "ID,Name,{," — split gives ["ID","Name","{",""]
        String[] parts = prefix.split(",", -1);
        int parsedId = 0;
        try { parsedId = Integer.parseInt(parts[0].trim()); } catch (NumberFormatException ignored) {}
        this.id   = parsedId;
        this.name = parts.length >= 2 ? parts[1].trim() : prefix;
    }

    public int getCost()        { return values[0]; }
    public int getDesirability(){ return values[1]; }
    public int getDesStep()     { return values[2]; }
    public int getDesStepSize() { return values[3]; }
    public int getDesRange()    { return values[4]; }
    public int getEmployees()   { return values[5]; }
    public int getExpansion1()  { return values[6]; }
    public int getExpansion2()  { return values[7]; }

    public void setCost(int v)         { values[0] = v; }
    public void setDesirability(int v) { values[1] = v; }
    public void setDesStep(int v)      { values[2] = v; }
    public void setDesStepSize(int v)  { values[3] = v; }
    public void setDesRange(int v)     { values[4] = v; }
    public void setEmployees(int v)    { values[5] = v; }
    public void setExpansion1(int v)   { values[6] = v; }
    public void setExpansion2(int v)   { values[7] = v; }

    public boolean isNothing() {
        return "Nothing".equalsIgnoreCase(name);
    }

    /** Reconstruct the full file line. */
    public String toLine() {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) {
            sb.append(values[i]);
            if (i < 7) sb.append(',');
        }
        sb.append(",}");
        if (suffix != null && !suffix.isEmpty()) sb.append(suffix);
        return sb.toString();
    }
}
