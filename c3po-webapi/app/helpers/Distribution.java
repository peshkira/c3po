package helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Distribution {
	private String property;
	private String type;
	private Map<String, Long> propertyDistribution;
	private Map<String, Double> statistics;
	public Distribution(){
		propertyDistribution=new HashMap<String, Long>();
		statistics=new HashMap<String, Double>();
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public Map<String, Long> getPropertyDistribution() {
		return propertyDistribution;
	}
	public void setPropertyDistribution(Map<String, Long> propertyDistribution) {
		this.propertyDistribution = propertyDistribution;
	}
	public List<String> getPropertyValues(){
		List<String> result = new ArrayList<String>();
		result.addAll(propertyDistribution.keySet());
		return result;
	}
	public List<String> getPropertyValueCounts(){
		List<String> result=new ArrayList<String>();
		for (Long l:propertyDistribution.values()){
			result.add(String.valueOf(l));
		}
		return result;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, Double> getStatistics() {
		return statistics;
	}
	public void setStatistics(Map<String, Double> statistics) {
		this.statistics = statistics;
	}
	
}
