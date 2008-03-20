package net.bioclipse.moss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import moss.Bonds;

/**
 * This is a model class that contains all information about a MOSS run
 * @author Annzi
 *
 */
public class MossModel {

	private double minimalSupport;
	private double maximalsupport;
	private double threshold;
	private boolean split, closed;
	private String exNode, exSeed, seed;
	private int minEmbed, maxEmbed;
	private double Limits;
	private int mbond, mrgbd;
	private int matom, mrgat;
	private int maxRing, minRing;
	private int mode;
	private int maxEmbMemory;
	private String test, testId;

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public MossModel() {
		super();
		minimalSupport = 0.1;
		maximalsupport = 0.02;
		threshold = 0.5;
		split = false;
		closed = true;
		exNode = "H";
		exSeed = "";
		maxEmbed = 0;
		minEmbed = 1;
		maxRing = 0;
		minRing = 0;
		maxEmbMemory = 0;
	}

	ArrayList<InputMolecule> inputMolecules;

	public ArrayList<InputMolecule> getInputMolecules() {
		return inputMolecules;
	}

	public void addMolecule(InputMolecule mol) {
		if (inputMolecules == null)
			inputMolecules = new ArrayList<InputMolecule>();
		inputMolecules.add(mol);
	}

	public void setInputMolecules(ArrayList<InputMolecule> inputMolecules) {
		this.inputMolecules = inputMolecules;
	}

	public double getMinimalSupport() {
		return minimalSupport;
	}

	public void setMinimalSupport(double minimalSupport) {
		this.minimalSupport = minimalSupport;
	}

	public double getMaximalsupport() {
		return maximalsupport;
	}

	public void setMaximalsupport(double maximalsupport) {
		this.maximalsupport = maximalsupport;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public boolean getSplit() {
		return split;
	}

	public void setSplit(boolean split) {
		this.split = split;
	}

	public String getExNode() {
		return exNode;
	}

	public void setExNode(String exnode) {
		exNode = exnode;

	}

	public String getExSeed() {
		return exSeed;
	}

	public void setExSeed(String exseed) {
		exSeed = exseed;
	}

	public int getMaxEmbed() {
		return maxEmbed;
	}

	public void setMaxEmbed(int maximalEmbed) {
		maxEmbed = maximalEmbed;
	}

	public int getMinEmbed() {
		return minEmbed;
	}

	public void setMinEmbed(int minimalEmbed) {
		this.minEmbed = minimalEmbed;
	}

	public double getLimits() {
		return Limits;
	}

	public void setLimits(double minsupp, double maxsupp) {
		minimalSupport = minsupp;
		maximalsupport = maxsupp;
	}

	public int getMbond() {
		return mbond;
	}

	public void setMbond(int mbond) {
		this.mbond = mbond;
	}

	public int getMrgbd() {
		return mrgbd;
	}

	public void setMrgbd(int mrgbd) {
		this.mrgbd = mrgbd;
	}

	public int getMaxRing() {
		return maxRing;
	}

	public void setMaxRing(int maxRing) {
		this.maxRing = maxRing;
	}

	public int getMinRing() {
		return minRing;
	}

	public int getMatom() {
		return matom;
	}

	public void setMatom(int matom) {
		this.matom = matom;
	}

	public void setMinRing(int minRing) {
		this.minRing = minRing;
	}

	public boolean getClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public int getMrgat() {
		return mrgat;
	}

	public void setMrgat(int mrgat) {
		this.mrgat = mrgat;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMaxEmbMemory() {
		return maxEmbMemory;
	}

	public void setMaxEmbMemory(int maxEmbMemory) {
		this.maxEmbMemory = maxEmbMemory;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

}
