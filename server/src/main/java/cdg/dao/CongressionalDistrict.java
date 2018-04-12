package cdg.dao;

import java.util.HashMap;
import java.util.Map;

import cdg.dto.CongressionalDistrictDTO;
import cdg.dto.DistrictDTO;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;

@Entity
@Table (name = "Districts")
public class CongressionalDistrict extends Region {
	@ManyToOne
	private State state;
	@OneToMany(mappedBy="conDistrict", cascade=CascadeType.ALL, orphanRemoval=true)
	@MapKey(name = "id")
	private Map<Integer,Precinct> precincts;
	private Map<Integer,Precinct> borderPrecincts;
	private double goodnessValue;
	
	public CongressionalDistrict()
	{
		super();
		precincts = new HashMap<>();
		borderPrecincts = new HashMap<>();
	}
	public CongressionalDistrict(String name, String geoJsonGeometry, ElectionResult presidentialVoteTotals, State state, double goodnessValue) {
		super(name, geoJsonGeometry, presidentialVoteTotals);
		this.state = state;
		this.precincts = new HashMap<>();
		this.borderPrecincts = new HashMap<>();
		this.goodnessValue = goodnessValue;
	}
	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
	}
	/**
	 * @return the precincts
	 */
	public Map<Integer, Precinct> getPrecincts() {
		return precincts;
	}
	/**
	 * @param precincts the precincts to set
	 */
	public void setPrecincts(Map<Integer, Precinct> precincts) {
		this.precincts = precincts;
	}
	/**
	 * @return the borderPrecincts
	 */
	public Map<Integer, Precinct> getBorderPrecincts() {
		return borderPrecincts;
	}
	/**
	 * @param borderPrecincts the borderPrecincts to set
	 */
	public void setBorderPrecincts(Map<Integer, Precinct> borderPrecincts) {
		this.borderPrecincts = borderPrecincts;
	}
	/**
	 * @return the goodnessValue
	 */
	public double getGoodnessValue() {
		return goodnessValue;
	}
	/**
	 * @param goodnessValue the goodnessValue to set
	 */
	public void setGoodnessValue(double goodnessValue) {
		this.goodnessValue = goodnessValue;
	}	

	@Override
	public DistrictDTO getDataDTO() {
		CongressionalDistrictDTO data = new CongressionalDistrictDTO();
		data.setID(this.getPublicID());
		data.setName(this.getName());
		data.setNumPrecincts(precincts.size());
		data.setGoodness(this.goodnessValue);
		return data;
	}

}
