package com.neo4j.CSV2Cypher.config;

public class Relationship {
    private String parent;
    private String label;
    private String idColumn;
    private String matchProperty;
    private boolean indexNeeded = false;
    private String direction = "Out";
    private boolean createParent = false;
    private String intermediateChild;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public String getMatchProperty() {
        return matchProperty;
    }

    public void setMatchProperty(String matchProperty) {
        this.matchProperty = matchProperty;
    }

    public boolean isIndexNeeded() {
        return indexNeeded;
    }

    public void setIndexNeeded(boolean indexNeeded) {
        this.indexNeeded = indexNeeded;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isCreateParent() {
        return createParent;
    }

    public void setCreateParent(boolean createParent) {
        this.createParent = createParent;
    }

    public String getIntermediateChild() {
        return intermediateChild;
    }

    public void setIntermediateChild(String intermediateChild) {
        this.intermediateChild = intermediateChild;
    }
}
