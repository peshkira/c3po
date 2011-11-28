package com.petpet.c3po.beans;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.db.PreparedQueries;

@ManagedBean
@RequestScoped
public class ProfileBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProfileBean.class);
	
	@PersistenceContext
	private EntityManager em;
	
	private PreparedQueries queries;
	
	private List<String> collections;
	
	private DigitalCollection coll;
	
	
	@PostConstruct
	public void init() {
		this.queries = new PreparedQueries(this.em);
		this.setCollections(this.queries.getAllCollectionNames());
	}


	public List<String> getCollections() {
		return collections;
	}

	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	public void setCollection(DigitalCollection coll) {
		this.coll = coll;
	}
	
	public void projectSelected(ValueChangeEvent evt) {
		LOG.info("event received " + evt.getNewValue());
	}

}
