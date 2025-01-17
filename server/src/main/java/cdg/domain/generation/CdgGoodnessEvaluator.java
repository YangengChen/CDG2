package cdg.domain.generation;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

import cdg.dao.CongressionalDistrict;
import cdg.dao.ElectionResult;
import cdg.dao.State;
import cdg.domain.region.Party;
import cdg.properties.CdgConstants;

public class CdgGoodnessEvaluator extends GoodnessEvaluator {

	public CdgGoodnessEvaluator(double maxGoodness) {
		super(maxGoodness);
	}
	
	public static GoodnessEvaluator toGoodnessEvaluator(double maxGoodness, GenerationConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException();
		}
		GoodnessEvaluator evaluator = new CdgGoodnessEvaluator(maxGoodness);
		if ((configuration.getSchwartzbergWeight() + configuration.getHullRatioWeight() + configuration.getReockWeight() +
				configuration.getContiguityWeight() + configuration.getEqualPopWeight() +
				configuration.getPartisanFairWeight()) != 1) {
			throw new IllegalArgumentException();
		}
		evaluator.setGoodnessMeasure(GoodnessMeasure.SCHWARTZBERG, configuration.getSchwartzbergWeight());
		evaluator.setGoodnessMeasure(GoodnessMeasure.HULLRATIO, configuration.getHullRatioWeight());
		evaluator.setGoodnessMeasure(GoodnessMeasure.REOCK, configuration.getReockWeight());
		evaluator.setGoodnessMeasure(GoodnessMeasure.CONTIGUITY, configuration.getContiguityWeight());
		evaluator.setGoodnessMeasure(GoodnessMeasure.EQUALPOPULATION, configuration.getEqualPopWeight());
		evaluator.setGoodnessMeasure(GoodnessMeasure.PARTISANFAIRNESS, configuration.getPartisanFairWeight());
		return evaluator;
	}
	
	public double calculateGoodness(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		double schwarzbergValue = 0;
		double hullRatioValue = 0;
		double reockValue = 0;
		double contiguityValue = 0;
		double populationEqualityValue = 0;
		double partisanFairnessValue = 0;
		if (this.getGoodnessMeasure(GoodnessMeasure.SCHWARTZBERG) > 0) {
			schwarzbergValue = evaluateCompactness(district, GoodnessMeasure.SCHWARTZBERG);
		}
		if (this.getGoodnessMeasure(GoodnessMeasure.HULLRATIO) > 0) {
			hullRatioValue = evaluateCompactness(district, GoodnessMeasure.HULLRATIO);
		}
		if (this.getGoodnessMeasure(GoodnessMeasure.REOCK) > 0) {
			reockValue = evaluateCompactness(district, GoodnessMeasure.REOCK);
		}
		if (this.getGoodnessMeasure(GoodnessMeasure.CONTIGUITY) > 0) {
			contiguityValue = evaluateContiguity(district);
		}
		if (this.getGoodnessMeasure(GoodnessMeasure.EQUALPOPULATION) > 0) {
			populationEqualityValue = evaluatePopulationEquality(district);
		}
		if (this.getGoodnessMeasure(GoodnessMeasure.PARTISANFAIRNESS) > 0) {
			partisanFairnessValue = evaluatePartisanFairness(district);
		}
		double goodness = runObjectiveFunction(schwarzbergValue, hullRatioValue, reockValue, contiguityValue, populationEqualityValue, partisanFairnessValue);
		return goodness;
	}
	
	/**
	 * Compactness using Schwarzberg, Hull Ratio, and Reock compactness measures
	 * @param district Congressional district
	 * @return compactness value normalized between 0 and MAXGOODNESS
	 */
	public double evaluateCompactness(CongressionalDistrict district, GoodnessMeasure compactnessMeasure) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		
		double compactnessScore;
		switch(compactnessMeasure) {
			case SCHWARTZBERG:
				compactnessScore = getSchwartzbergCompactness(district);
				break;
			case HULLRATIO:
				compactnessScore = getHullRatioCompactness(district);
				break;
			case REOCK:
				compactnessScore = getReockCompactness(district);
				break;
			default:
				throw new IllegalArgumentException();
		}
		double compactnessValue = MAXGOODNESS * compactnessScore;
		return compactnessValue;
	}
	
	public static double getSchwartzbergCompactness(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		Geometry districtGeom = district.getGeometry();
		if (!(districtGeom instanceof Polygonal)) {
			throw new IllegalArgumentException();
		}
		double area = districtGeom.getArea();
		double perimeter = districtGeom.getLength();
		if (area == 0 || perimeter == 0) {
			throw new IllegalArgumentException();
		}
		double equalARadius = Math.sqrt(area/Math.PI);
		double circumferance = 2 * Math.PI * equalARadius;
		double schwarzbergScore = 1 / (perimeter/circumferance);
		if (schwarzbergScore > 1) {
			throw new IllegalArgumentException();
		}
		return schwarzbergScore;
	}
	
	public static double getHullRatioCompactness(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		Geometry districtGeom = district.getGeometry();
		if (!(districtGeom instanceof Polygonal)) {
			throw new IllegalArgumentException();
		}
		ConvexHull minConvexHullGenerator = new ConvexHull(districtGeom);
		Geometry minConvexHull = minConvexHullGenerator.getConvexHull();
		if (!(minConvexHull instanceof Polygonal)) {
			throw new IllegalArgumentException();
		}
		double districtArea = districtGeom.getArea();
		double hullArea = minConvexHull.getArea();
		if (districtArea == 0 || hullArea == 0) {
			throw new IllegalArgumentException();
		}
		double hullRatioScore = districtArea/hullArea;
		if (hullRatioScore > 1) {
			throw new IllegalArgumentException();
		}
		return hullRatioScore;
	}
	
	public static double getReockCompactness(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		Geometry districtGeom = district.getGeometry();
		if (!(districtGeom instanceof Polygonal)) {
			throw new IllegalArgumentException();
		}
		MinimumBoundingCircle minBoundingCircleGenerator = new MinimumBoundingCircle(districtGeom);
		Geometry minBoundingCircle = minBoundingCircleGenerator.getCircle();
		double districtArea = districtGeom.getArea();
		double mbcArea = minBoundingCircle.getArea();
		if (districtArea == 0 || mbcArea == 0) {
			throw new IllegalArgumentException();
		}
		double reockScore = districtArea/mbcArea;
		if (reockScore > 1) {
			throw new IllegalArgumentException();
		}
		return reockScore;
	}
	
	/**
	 * 
	 * @param district Congressional district
	 * @return contiguity value normalized between 0 and MAXGOODNESS
	 */
	public double evaluateContiguity(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		double contiguityValue;
		Geometry districtGeom = district.getGeometry();
		if (districtGeom instanceof Polygonal) {
			contiguityValue = MAXGOODNESS/districtGeom.getNumGeometries();
		} else {
			throw new IllegalArgumentException();
		}
 		return contiguityValue;
	}
	
	public double evaluatePopulationEquality(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		double populationEqualityValue;
		State state = district.getState();
		if (state == null || state.numConDistricts() == 0) {
			throw new IllegalArgumentException();
		}
		if (state.getPopulation() == 0) {
			return 0;
		}
		long popAvg = state.getPopulation() / state.numConDistricts();
		long population = district.getPopulation();
		double percentage = (double)population / (double)popAvg;
		if (Math.abs(1 - percentage) > 1) {
			populationEqualityValue = 0;
		} else {
			populationEqualityValue = MAXGOODNESS - MAXGOODNESS * Math.abs(1 - percentage);
		}
		return populationEqualityValue;
	}
	
	public double evaluatePartisanFairness(CongressionalDistrict district)
	{
		if (district == null) {
			throw new IllegalArgumentException();
		}
		double partisanFairnessValue;
		double efficiencyGapPercentage = CdgGoodnessEvaluator.getEfficiencyGap(district);
		partisanFairnessValue = MAXGOODNESS - (MAXGOODNESS * efficiencyGapPercentage);
		return partisanFairnessValue;
	}
	
	public static double getEfficiencyGap(CongressionalDistrict district) {
		if (district == null) {
			throw new IllegalArgumentException();
		}
		ElectionResult election = district.getPresidentialVoteTotals();
		if (election == null) {
			throw new IllegalArgumentException();
		}
		long demVotes = election.getTotal(Party.DEMOCRATIC);
		long repVotes = election.getTotal(Party.REPUBLICAN);
		if ((demVotes + repVotes) == 0) {
			return 1;
		}
		long wastedDemVotes;
		long wastedRepVotes;
		if (repVotes == demVotes) { //bug??
			return 0;
		} else if (repVotes < demVotes) {
			wastedDemVotes = demVotes - (repVotes+demVotes)/2;
			wastedRepVotes = repVotes;
		} else {
			wastedDemVotes = demVotes;
			wastedRepVotes = repVotes - (repVotes+demVotes)/2;
		}
		if (wastedDemVotes < 0 || wastedRepVotes < 0) {
			throw new IllegalStateException();
		}
		double percentage;
		if (wastedDemVotes <= wastedRepVotes) {
			percentage = (double)(wastedRepVotes - wastedDemVotes)/(double)(demVotes + repVotes);
		} else {
			percentage = (double)(wastedDemVotes - wastedRepVotes)/(double)(demVotes + repVotes);
		}
		if (percentage > 1) {
			throw new IllegalStateException();
		};
		return percentage;
	}
	
	public double evaluateRacialFairness(CongressionalDistrict district)
	{
		return 0;
	}
	
	private double runObjectiveFunction(double schwartzberg, double hullRatio, double reock, double contiguity, double populationEqual, double partisanFair) 
	{
		double totalGoodness = 0;
		double weight = 0;
		int totalMeasures = 0;
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.SCHWARTZBERG);
		if (weight > 0) {
			totalGoodness += (weight * schwartzberg);
			totalMeasures++;
		}
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.HULLRATIO);
		if (weight > 0) {
			totalGoodness += (weight * hullRatio);
			totalMeasures++;
		}
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.REOCK);
		if (weight > 0) {
			totalGoodness += (weight * reock);
			totalMeasures++;
		}
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.CONTIGUITY);
		if (weight > 0) {
			totalGoodness += (weight * contiguity);
			totalMeasures++;
		}
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.EQUALPOPULATION);
		if (weight > 0) {
			totalGoodness += (weight * populationEqual);
			totalMeasures++;
		}
		
		weight = this.getGoodnessMeasure(GoodnessMeasure.PARTISANFAIRNESS);
		if (weight > 0) {
			totalGoodness += (weight * partisanFair);
			totalMeasures++;
		}

		double goodness = totalGoodness * (1/(double)totalMeasures);
		
		return goodness;
	}
}
