package com.petpet.c3po.analysis.conflictResolution;


import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.MetadataRecord;

import java.io.Serializable;
import java.util.List;

/**
 * Created by artur on 31/03/16.
 */
public class Rule implements Serializable {
    public Filter getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description;

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    Filter filter;

    public List<MetadataRecord> getMetadataRecordList() {
        return metadataRecordList;
    }

    public void setMetadataRecordList(List<MetadataRecord> metadataRecordList) {
        this.metadataRecordList = metadataRecordList;
    }

    List<MetadataRecord> metadataRecordList;

    Element element;


}
