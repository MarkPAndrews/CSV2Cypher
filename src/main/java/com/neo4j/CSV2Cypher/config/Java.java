package com.neo4j.CSV2Cypher.config;

public class Java {
    private String classTemplate;
    private String propertyTemplate;
    private String outputDir;

    public String getClassTemplate() {
        return classTemplate;
    }

    public void setClassTemplate(String classTemplate) {
        this.classTemplate = classTemplate;
    }

    public String getPropertyTemplate() {
        return propertyTemplate;
    }

    public void setPropertyTemplate(String propertyTemplate) {
        this.propertyTemplate = propertyTemplate;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
