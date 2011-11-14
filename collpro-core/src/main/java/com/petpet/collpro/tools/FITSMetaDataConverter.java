package com.petpet.collpro.tools;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.api.ITool;
import com.petpet.collpro.api.Message;
import com.petpet.collpro.api.Call;
import com.petpet.collpro.api.utils.ConfigurationException;
import com.petpet.collpro.common.Config;
import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.datamodel.ValueStatus;
import com.petpet.collpro.utils.FITSHelper;
import com.petpet.collpro.utils.Helper;
import com.petpet.collpro.utils.XMLUtils;

public class FITSMetaDataConverter implements ITool {

	private static final Logger LOG = LoggerFactory.getLogger(FITSMetaDataConverter.class);

	private Map<String, Object> configuration;

	private Date measuredAt;

	private DigitalCollection collection;

	private Set<Call> observers;

	private File[] files;

	private SAXReader reader;

	public FITSMetaDataConverter() {
		this.observers = new HashSet<Call>();
		this.reader = new SAXReader();
	}

	/**
	 * Extracts the values from the elements setup by the configuration. All
	 * created elements are passed via the notifyObservers method back to the
	 * observer for further processing.
	 * 
	 */
	@Override
	public void execute() {
		for (File f : files) {
			this.extractMetaData(f);
		}
	}

	@Override
	public void configure(Map<String, Object> configuration) throws ConfigurationException {
		this.configuration = configuration;

		Object c = this.configuration.get(Config.COLLECTION_CONF);

		if (c == null) {
			throw new ConfigurationException(
			        "No collection provided, please set a collection for field 'config.collection'");
		} else {
			this.collection = (DigitalCollection) c;
		}

		Object f = this.configuration.get(Config.FITS_FILES_CONF);
		if (f == null) {
			throw new ConfigurationException(
			        "No file array provided, please set a file array for field 'config.fits_files'");
		} else {
			this.files = (File[]) f;
		}

		Object d = this.configuration.get(Config.DATE_CONF);
		if (d == null) {
			this.measuredAt = new Date();

		} else {
			Date date = (Date) d;
			this.measuredAt = date;
		}
	}

	@Override
	public void addObserver(Call listener) {
		if (!this.observers.contains(listener)) {
			this.observers.add(listener);
		}
	}

	@Override
	public void removeObserver(Call listener) {
		this.observers.remove(listener);

	}

	@Override
	public void notifyObservers(Object data) {
		final Message<Element> n = new Message<Element>((Element) data);

		for (Call call : this.observers) {
			call.back(n);
		}
	}

	private Element extractValues(Document xml) {
		org.dom4j.Element root = xml.getRootElement();
		org.dom4j.Element fileinfo = root.element(FITSConstants.FILEINFO);
		org.dom4j.Element identification = root.element(FITSConstants.IDENTIFICATION);
		org.dom4j.Element filestatus = root.element(FITSConstants.FILESTATUS);
		org.dom4j.Element metadata = (org.dom4j.Element) root.element(FITSConstants.METADATA);
		if (metadata != null && metadata.elements().size() > 0) {
			// fetch first tag (one of document, audio, video, image, etc...)
			metadata = (org.dom4j.Element) metadata.elements().get(0);
		}

		String md5 = fileinfo.element(FITSConstants.MD5CHECKSUM).getText();
		String filename = fileinfo.element(FITSConstants.FILENAME).getText();
		String filepath = fileinfo.element(FITSConstants.FILEPATH).getText();

		boolean processed = Helper.isElementAlreadyProcessed(this.collection, md5);
		if (processed) {
			LOG.info("Element '{}' is already processed", filename);
			return null;
		}

		Element e = new Element(filename, filepath);
		e.setCollection(this.collection);
		this.collection.getElements().add(e);

		this.getIdentification(identification, e);
		this.getFlatProperties(fileinfo, e);
		this.getFlatProperties(filestatus, e);
		this.getFlatProperties(metadata, e);

		return e;
	}

	private void extractMetaData(File f) {
		if (f.isFile()) {
			try {
				Document document = reader.read(f);
				Element element = this.extractValues(document);
				if (element != null) {
					element.setCollection(this.collection);
					notifyObservers(element);
				}

			} catch (DocumentException e) {
				LOG.error("An error occurred: {}", e.getMessage());
			}
		}
	}

	private void getIdentification(org.dom4j.Element identification, Element e) {
		ValueStatus stat = ValueStatus.OK;

		if (identification.elements().size() > 1) {
			LOG.warn("There are more than one identity tags. There must be a conflict");
			stat = ValueStatus.valueOf(identification.attributeValue("status"));
		}

		Iterator<org.dom4j.Element> iter = (Iterator<org.dom4j.Element>) identification.elementIterator();

		// iterate over identity tags
		while (iter.hasNext()) {
			org.dom4j.Element identity = iter.next();
			String format = identity.attributeValue(FITSConstants.FORMAT_ATTR);
			String mime = identity.attributeValue(FITSConstants.MIMETYPE_ATTR);

			// TODO manage value source conflict/single_result
			Property p1 = FITSHelper.getPropertyByFitsName(FITSConstants.FORMAT_ATTR);
			Property p2 = FITSHelper.getPropertyByFitsName(FITSConstants.MIMETYPE_ATTR);

			ValueSource vs = new ValueSource();
			vs.setName(identity.element(FITSConstants.TOOL).attributeValue(FITSConstants.TOOL_ATTR));
			vs.setVersion(identity.element(FITSConstants.TOOL).attributeValue(FITSConstants.TOOLVERSION_ATTR));

			StringValue v1 = new StringValue(format);
			v1.setMeasuredAt(this.measuredAt.getTime());
			v1.setProperty(p1);
			v1.setElement(e);
			v1.setSource(vs);
			v1.setStatus(stat);

			StringValue v2 = new StringValue(mime);
			v2.setMeasuredAt(this.measuredAt.getTime());
			v2.setProperty(p2);
			v2.setElement(e);
			v2.setSource(vs);
			v2.setStatus(stat);

			e.getValues().add(v1);
			e.getValues().add(v2);

			Property p3 = FITSHelper.getPropertyByFitsName(FITSConstants.FORMAT_VERSION_ATTR);

			Iterator verIter = identity.elementIterator(FITSConstants.VERSION);
			while (verIter.hasNext()) {
				org.dom4j.Element ver = (org.dom4j.Element) verIter.next();

				vs = new ValueSource(ver.attributeValue(FITSConstants.TOOL_ATTR),
				        ver.attributeValue(FITSConstants.TOOLVERSION_ATTR));

				StringValue v = new StringValue(ver.getText());
				v.setMeasuredAt(this.measuredAt.getTime());
				v.setProperty(p3);
				v.setElement(e);
				v.setSource(vs);

				if (stat.equals(ValueStatus.OK))
					v.setStatus(XMLUtils.getStatusOfFITSElement(ver));
				else
					v.setStatus(stat);

				e.getValues().add(v);

			}

			Iterator extIterator = identity.elementIterator(FITSConstants.EXT_ID);
			while (extIterator.hasNext()) {
				org.dom4j.Element extId = (org.dom4j.Element) extIterator.next();
				Property p = FITSHelper.getPropertyByFitsName(extId.attributeValue(FITSConstants.EXT_ID_TYPE_ATTR));

				vs = new ValueSource(extId.attributeValue(FITSConstants.TOOL_ATTR),
				        extId.attributeValue(FITSConstants.TOOLVERSION_ATTR));

				StringValue v = new StringValue(extId.getText());
				v.setMeasuredAt(this.measuredAt.getTime());
				v.setProperty(p);
				v.setElement(e);
				v.setSource(vs);

				if (stat.equals(ValueStatus.OK))
					v.setStatus(XMLUtils.getStatusOfFITSElement(extId));
				else
					v.setStatus(stat);

				e.getValues().add(v);

			}
		}
	}

	// TODO set reliability
	private void getFlatProperties(org.dom4j.Element info, Element e) {

		if (info != null) {
			Iterator<org.dom4j.Element> iter = (Iterator<org.dom4j.Element>) info.elementIterator();
			while (iter.hasNext()) {
				org.dom4j.Element elmnt = iter.next();

				Property p = FITSHelper.getPropertyByFitsName(elmnt.getName());

				ValueSource vs = new ValueSource(elmnt.attributeValue(FITSConstants.TOOL_ATTR),
				        elmnt.attributeValue(FITSConstants.TOOLVERSION_ATTR));

				Value v = Helper.getTypedValue(p.getType(), elmnt.getText());
				v.setMeasuredAt(this.measuredAt.getTime());
				v.setSource(vs);
				v.setProperty(p);
				v.setElement(e);
				v.setStatus(XMLUtils.getStatusOfFITSElement(elmnt));

				e.getValues().add(v);

			}
		}
	}
}
