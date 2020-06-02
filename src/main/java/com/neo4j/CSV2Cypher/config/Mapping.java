package com.neo4j.CSV2Cypher.config;

import java.util.ArrayList;
import java.util.List;

public class Mapping {
    private String inputFile;
    private String label;
    private String additionalLabels;
    private String idColumn;
    private String javaParent;
    private boolean canIgnoreInvalidError;
    private boolean skipJavaFileGeneration;
    private boolean skipResyncFileGeneration;
    private List<String> javaProperties;
    private List<String> toLower;
    private List<String> indexes;
    private List<String> ignoreFields= new ArrayList<>();
    private Relationship[] relationships;

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAdditionalLabels() {
        return additionalLabels == null ? "" : additionalLabels;
    }

    public void setAdditionalLabels(String additionalLabels) {
        this.additionalLabels = additionalLabels;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public String getJavaParent() {
        if(javaParent==null) javaParent=".";
        return javaParent;
    }

    public void setJavaParent(String javaParent) {
        this.javaParent = javaParent;
    }

    public List<String> getToLower() {
        return toLower;
    }

    public void setToLower(List<String> toLower) {
        this.toLower = toLower;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }

    public Relationship[] getRelationships() {
        return relationships;
    }

    public void setRelationships(Relationship[] relationships) {
        this.relationships = relationships;
    }

    public List<String> getIgnoreFields() {
        return ignoreFields;
    }

    public void setIgnoreFields(List<String> ignoreFields) {
        this.ignoreFields = ignoreFields;
    }

    public List<String> getJavaProperties() {
        return javaProperties;
    }

    public void setJavaProperties(List<String> javaProperties) {
        this.javaProperties = javaProperties;
    }

    public boolean isCanIgnoreInvalidError() {
        return canIgnoreInvalidError;
    }

    public void setCanIgnoreInvalidError(boolean canIgnoreInvalidError) {
        this.canIgnoreInvalidError = canIgnoreInvalidError;
    }

    public boolean isSkipJavaFileGeneration() {
        return skipJavaFileGeneration;
    }

    public void setSkipJavaFileGeneration(boolean skipJavaFileGeneration) {
        this.skipJavaFileGeneration = skipJavaFileGeneration;
    }

    public boolean isSkipResyncFileGeneration() {
        return skipResyncFileGeneration;
    }

    public void setSkipResyncFileGeneration(boolean skipResyncFileGeneration) {
        this.skipResyncFileGeneration = skipResyncFileGeneration;
    }
}
