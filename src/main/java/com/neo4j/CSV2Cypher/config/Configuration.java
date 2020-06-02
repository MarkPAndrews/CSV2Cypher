package com.neo4j.CSV2Cypher.config;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private Cypher cypher;
    private Java java;
    private List<String> toLower;
    private List<String> dateFields;
    private List<String> ignoreFields = new ArrayList<>();
    private Mapping[] mappings;
    private String importDir;

    public Cypher getCypher() {
        return cypher;
    }

    public void setCypher(Cypher cypher) {
        this.cypher = cypher;
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java = java;
    }

    public List<String> getToLower() {
        return toLower;
    }

    public void setToLower(List<String> toLower) {
        this.toLower = toLower;
    }

    public List<String> getDateFields() {
        return dateFields;
    }

    public void setDateFields(List<String> dateFields) {
        this.dateFields = dateFields;
    }

    public Mapping[] getMappings() {
        return mappings;
    }

    public void setMappings(Mapping[] mappings) {
        this.mappings = mappings;
    }

    public List<String> getIgnoreFields() {
        return ignoreFields;
    }

    public void setIgnoreFields(List<String> ignoreFields) {
        this.ignoreFields = ignoreFields;
    }

    public String getImportDir() {
        return importDir;
    }

    public void setImportDir(String importDir) {
        this.importDir = importDir;
    }

}
