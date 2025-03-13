package com;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a collection of drug interaction records, organized by unique keys (drug1-drug2-condition) and their corresponding Twosides data
 */
public class TwosidesCol {
    private Map<String, Map<String, Twosides>> col;

    /**
     * Default constructor initializing an empty collection
     */
    public TwosidesCol() { this.col = new HashMap<>(); }

    /**
     * Constructs a TwosidesCol with an interaction and its corresponding map of Twosides records
     *
     * @param twosidesInteraction the key representing the interaction
     * @param twosidesInteractionMap the map of Twosides records for the interaction
     */
    public TwosidesCol(String twosidesInteraction, Map<String, Twosides> twosidesInteractionMap) {
        this.col = new HashMap<>();
        this.col.put(twosidesInteraction, twosidesInteractionMap);
    }
    /**
     * Returns the collection of interactions and their corresponding Twosides records
     *
     * @return a map representing the collection
     */
    public Map<String, Map<String, Twosides>> getCol() { return col; }
    /**
     * Sets the collection of interactions and their corresponding Twosides records
     *
     * @param col the map representing the collection
     */
    public void setCol(Map<String, Map<String, Twosides>> col) { this.col = col; }

    /**
     * Adds a single Twosides record to the collection under the specified interaction key
     *
     * @param twosideInteraction the key representing the interaction
     * @param twosides the Twosides record to be added
     */
    public void addTwosides(String twosideInteraction, Twosides twosides) {
        col.computeIfAbsent(twosideInteraction, k -> new HashMap<>())
        .put(getMapStrId(twosides), twosides);
    }

    /**
     * Adds a map of Twosides records to the collection under the specified interaction key
     *
     * @param twosideInteraction the key representing the interaction
     * @param twosidesInteractionMap the map of Twosides records to be added
     */
    public void addTwosides(String twosideInteraction, Map<String, Twosides> twosidesInteractionMap) {
        col.put(twosideInteraction, twosidesInteractionMap);
    }

    /**
     * Generates a string key for a Twosides record based on its drug and condition information
     *
     * @param twosides the Twosides record to generate a key for
     * @return the generated string key
     */
    public static String getMapStrId(Twosides twosides) {
        return twosides.getDrug1ConceptName().replace(" ", "_")+"%" +
               twosides.getDrug2ConceptName().replace(" ", "_")+"%"+
               twosides.getConditionName().replace(" ", "_");
    }

    /**
     * Generates a string key for a Twosides interaction based on the drug names
     *
     * @param drug1ConceptName the name of the first drug
     * @param drug2ConceptName the name of the second drug
     * @return the generated string key
     */
    public static String getMapStrId(String drug1ConceptName, String drug2ConceptName) {
        return drug1ConceptName.replace(" ", "_")+"%"+drug2ConceptName.replace(" ", "_");
    }

    /**
     * Extracts a list of original string components from a mapped interaction key
     *
     * @param twosideinteraction the mapped interaction key
     * @return a list of unmapped string components
     */
    public static List<String> getUnmapedStrId(String twosideinteraction) {
        List<String> temp = Arrays.asList(twosideinteraction.split("%"));
        for (int i = 0; i < temp.size(); i++) {
            temp.set(i, temp.get(i).replace("_", " "));
        }
        return temp;
    }

    /**
     * Returns a string representation of the TwosidesCol object
     *
     * @return a string describing the collection
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Map.Entry<String, Map<String, Twosides>> entry : col.entrySet()) {
            sb.append(index++)
              .append(". Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue())
              .append("\n");
        }
        return sb.toString();
    }

    /**
     * Compares this object with another for equality based on their collections
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o== null || getClass() !=o.getClass()) return false;
        TwosidesCol that = (TwosidesCol) o;
        return Objects.equals(col, that.col);
    }

    /**
     * Generates a hash code for the TwosidesCol object based on its collection
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(col);
    }
}
