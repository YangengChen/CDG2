package cdg.dao;

public class Precinct extends Region {
	private CongressionalDistrict conDistrict;
	private State state;
	
	public Precinct()
	{
		super();
	}
	public Precinct(String name, String geoJsonGeometry, ElectionResult presidentialVoteTotals, CongressionalDistrict conDistrict, State state) {
		super(name, geoJsonGeometry, presidentialVoteTotals);
		this.conDistrict = conDistrict;
		this.state = state;
	}
	
	/**
	 * @return the conDistrict
	 */
	public CongressionalDistrict getConDistrict() {
		return conDistrict;
	}
	/**
	 * @param conDistrict the conDistrict to set
	 */
	public void setConDistrict(CongressionalDistrict conDistrict) {
		this.conDistrict = conDistrict;
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
}