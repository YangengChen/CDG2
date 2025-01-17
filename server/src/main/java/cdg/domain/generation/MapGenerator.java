package cdg.domain.generation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cdg.dao.CongressionalDistrict;
import cdg.dao.Precinct;
import cdg.dao.SavedMap;
import cdg.dao.SavedMapping;
import cdg.dao.State;
import cdg.domain.map.MapType;
import cdg.repository.StateRepository;
import cdg.responses.GenerationResponse;
import cdg.services.ImportService;

public class MapGenerator {
	private GoodnessEvaluator goodnessEvaluator;
	private ConstraintEvaluator constraintEvaluator;
	private String stateId;
	private GenerateMapAlgorithm currAlgorithmRun;

	public boolean startGeneration(State state, Map<String,String> manualMappings) {
		if (goodnessEvaluator == null || constraintEvaluator == null || state == null) {
			throw new IllegalStateException();
		}

		try {
			currAlgorithmRun = new GenerateMapAlgorithm(state, goodnessEvaluator, constraintEvaluator, manualMappings);
		} catch (IllegalArgumentException iae) {
			System.err.println(iae);
			throw iae;
		} catch (Exception e) {
			System.err.println(e);
			throw new IllegalStateException();
		}
		
		return currAlgorithmRun.start();
	}
	
	public void stopGeneration() {
		if (currAlgorithmRun != null) {
			currAlgorithmRun.stop();
		}
	}
	
	public boolean pauseGeneration() {
		if (currAlgorithmRun == null) {
			return false;
		}
		return currAlgorithmRun.pause();
	}
	
	public GenerationStatus getStatus() {
		if (currAlgorithmRun == null) {
			return GenerationStatus.NOTSTARTED;
		}
		if (currAlgorithmRun.isComplete()) {
			if (currAlgorithmRun.isCancelled()) {
				return GenerationStatus.CANCELED;
			}
			else {
				boolean generationSuccess = currAlgorithmRun.getGenerationResult();
				if (generationSuccess) {
					return GenerationStatus.COMPLETE;
				} else {
					return GenerationStatus.ERROR;
				}
			}
		} else {
			return GenerationStatus.INPROGRESS;
		}
	}
	
	public GenerationResponse getGenerationState() {
		GenerationStatus currStatus = getStatus();
		GenerationResponse response = new GenerationResponse(currStatus);
		if (currStatus.equals(GenerationStatus.INPROGRESS) || currStatus.equals(GenerationStatus.COMPLETE)) {
			GenerationState currGenState = currAlgorithmRun.getState();
			response.setStartTotalGoodness(currGenState.getStartTotalGoodness());
			response.setCurrTotalGoodness(currGenState.getLastTotalGoodness());
			response.setCurrIteration(currGenState.getCurrGenIteration());
			List<PrecinctDistrictMap> precinctToDistrict = new ArrayList<>();
			PrecinctDistrictMap mapping;
			for (Map.Entry<String, String> map : currGenState.getPrecinctToDistrict().entrySet()) {
				mapping = new PrecinctDistrictMap(map.getKey(),map.getValue());
				precinctToDistrict.add(mapping);
			}
			response.setPrecinctToDistrict(precinctToDistrict);
			response.setStartDistrictsGoodness(currGenState.getStartDistrictsGoodness());
			response.setDistrictsGoodness(currGenState.getDistrictsGoodness());
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(currGenState.getGenStartTime());
			response.setStartTime(calendar.getTime().toString());
			if (currStatus.equals(GenerationStatus.COMPLETE)) {
				calendar.setTimeInMillis(currGenState.getGenStopTime());
				response.setStopTime(calendar.getTime().toString());
			}
			calendar.setTimeInMillis(System.currentTimeMillis());
			response.setTimestamp(calendar.getTime().toString());
		}
		return response;
	}
	
	public State getGeneratedState() {
		if (currAlgorithmRun == null) {
			return null;
		}
		if (!getStatus().equals(GenerationStatus.COMPLETE)) {
			return null;
		}
		State genState = currAlgorithmRun.getGeneratedState();
		return genState;
	}
	
	public SavedMap createSavedMap(StateRepository repo) {
		if (currAlgorithmRun == null) {
			return null;
		}
		if (!getStatus().equals(GenerationStatus.COMPLETE)) {
			return null;
		}
		State genState = currAlgorithmRun.getGeneratedState();
		repo.detach(genState);
		SavedMap savedMap = new SavedMap(currAlgorithmRun.getGenerationID().toString());
		savedMap.setState(genState);
		savedMap.setContiguityWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.CONTIGUITY));
		savedMap.setEqualPopWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.EQUALPOPULATION));
		savedMap.setPartisanFairWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.PARTISANFAIRNESS));
		savedMap.setHullRatioWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.HULLRATIO));
		savedMap.setReockWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.REOCK));
		savedMap.setSchwarzbergWeight(goodnessEvaluator.getGoodnessMeasure(GoodnessMeasure.SCHWARTZBERG));
		savedMap.setDistricts(new HashSet<>());
		SavedMapping currDistrictMapping;
		Set<Precinct> currPrecinctSet;
		for (CongressionalDistrict district : genState.getConDistricts().values()) {
			currDistrictMapping = new SavedMapping();
			currDistrictMapping.setGoodness(district.getGoodnessValue());
			currDistrictMapping.setDistrict(district);
			currPrecinctSet = new HashSet<>(district.getPrecincts().values());
			currDistrictMapping.setPrecincts(currPrecinctSet);
			savedMap.getDistricts().add(currDistrictMapping);
		}
		return savedMap;
	}
	
	public static State loadSavedMapState(StateRepository repo, SavedMap savedMap) {
		if (savedMap == null) {
			throw new IllegalArgumentException();
		}
		
		State state = savedMap.getState();
		if (state == null) {
			throw new IllegalStateException();
		}
		repo.detach(state);
		
		Set<SavedMapping> districtToPrecinctMaps = savedMap.getDistricts();
		if (districtToPrecinctMaps == null) {
			throw new IllegalStateException();
		}
		CongressionalDistrict currDistrict;
		Set<Precinct> currPrecincts;
		HashMap<Integer,Precinct> currPrecinctsMap;
		HashMap<Integer,Precinct> allPrecincts = new HashMap<Integer,Precinct>();
		HashMap<Integer,CongressionalDistrict> allDistricts = new HashMap<Integer,CongressionalDistrict>();
		for (SavedMapping mapping : districtToPrecinctMaps) {
			currDistrict = mapping.getDistrict();
			currPrecincts = mapping.getPrecincts();
			if (currDistrict == null || currPrecincts == null) {
				throw new IllegalStateException();
			}
			currPrecinctsMap = new HashMap<Integer,Precinct>();
			for (Precinct p : currPrecincts) {
				currPrecinctsMap.put(p.getId(), p);
				p.setConDistrict(currDistrict);
			}
			allPrecincts.putAll(currPrecinctsMap);
			currDistrict.setPrecincts(currPrecinctsMap);
			currDistrict.setGoodnessValue(mapping.getGoodness());
			allDistricts.put(currDistrict.getId(),currDistrict);
		}
		state.setConDistricts(allDistricts);
		state.setPrecincts(allPrecincts);
		
		ImportService.setPopulation(state);
		ImportService.updateElectionData(state);
		return state;
	}
	
	public void setState(String stateId) {
		this.stateId = stateId;
	}
	
	public void setGoodnessEvaluator(GoodnessEvaluator goodnessEvaluator) {
		this.goodnessEvaluator = goodnessEvaluator;
	}

	public void setConstraintEvaluator(ConstraintEvaluator constraintEvaluator) {
		this.constraintEvaluator = constraintEvaluator;
	}
	
	public void resetGenerator(String stateId, GoodnessEvaluator goodnessEval, ConstraintEvaluator constraintEval) {
		reset();
		setState(stateId);
		setGoodnessEvaluator(goodnessEval);
		setConstraintEvaluator(constraintEval);
	}
	
	public String getCurrGenerationID() {
		if (currAlgorithmRun == null) {
			return null;
		}
		String generationID = currAlgorithmRun.getGenerationID().toString();
		return generationID;
	}
	
	private void reset() {
		goodnessEvaluator = null;
		constraintEvaluator = null;
		stateId = null;
		stopGeneration();
		currAlgorithmRun = null;
	}
}
