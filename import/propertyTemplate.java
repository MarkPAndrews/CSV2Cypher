    private String ${property};

    public String get${PCProperty}() { return ${property};}

    public void set${PCProperty}(String value) {
            if(value==null) value=REMOVE_STRING_VALUE;
            this.${property}=value;
    }
