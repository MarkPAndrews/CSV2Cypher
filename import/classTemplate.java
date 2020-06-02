package com.neo4j.test.beans${javaParent};

import com.neo4j.test.beans.BaseBean;
import java.beans.Transient;

public class ${label} extends BaseBean {
    ${shouldRaiseErrorOnInvalid}
    @Transient
    public String getAdditionalLabels() {
        return "${additionalLabels}";
    }

    @Transient
    public String getId() {
        return ${idProperty};
    }

    @Transient
    public void setId(String value) {
        this.${idProperty}=value;
        }

    @Transient
    public String getIdProperty() {
        return "${idProperty}";
    }

    @Override
    public void buildRelationships() {
${relationships}

        super.buildRelationships();
    }

${properties}

}
