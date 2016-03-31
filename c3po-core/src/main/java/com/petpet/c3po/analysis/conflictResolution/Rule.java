package com.petpet.c3po.analysis.conflictResolution;


import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;

/**
 * Created by artur on 31/03/16.
 */
public class Rule {
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    Filter filter;

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
        if (this.element.getId()!=null)
        {
            this.element.setId(null);
        }
    }

    Element element;


}
