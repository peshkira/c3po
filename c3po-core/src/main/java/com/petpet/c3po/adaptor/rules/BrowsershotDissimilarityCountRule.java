package com.petpet.c3po.adaptor.rules;

import java.util.List;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;

public class BrowsershotDissimilarityCountRule implements PostProcessingRule{

	@Override
	public int getPriority() {
		return 990;
	}

    @Override
    public void onCommandFinished() {

    }

    @Override
	public Element process(Element e) {
		List<MetadataRecord> BrowsershotRecords = e.getMetadata();
		int total=0;
		int negative=0;
		for ( MetadataRecord mr : BrowsershotRecords ) {
			if (mr.getProperty().getType()=="FLOAT"){
				total++;
				Float value = Float.parseFloat(mr.getValue());
				if (value<0)
					negative++;
			}
		}
		Property p = new Property("dissimilarities", PropertyType.INTEGER);
		MetadataRecord mr = new MetadataRecord(p,String.valueOf(negative));
		e.getMetadata().add(mr);
		return e;
	}

}
