package net.bioclipse.moss.business.backbone;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;


import moss.Atoms;
import moss.Bonds;
import moss.Extension;
import moss.Miner;

public class MossBean implements java.io.Serializable {

	private double minimalSupport = 0.1, maximalSupport = 0.02, threshold = 0.5;
	private boolean split= false, closed= true;
	private String exNode= "H", exSeed= "", seed = "";
	private int minEmbed, maxEmbed;
	private double limits;
	private int mbond , mrgbd, matom, mrgat, mode;
	private int maxRing = 0, minRing = 0;
	private int maxEmbMemory = 0;
	
	/** No-arg constructor (takes no arguments). */
	public MossBean() {
	}

	public static void setParameters(Object o, String propertyName, Object value)throws Exception{
		PropertyDescriptor[] pds = Introspector.getBeanInfo( o.getClass() ).getPropertyDescriptors();
		for( int i=0; i< pds.length; i++){
			if( pds[i].getName().equals(propertyName)){
				pds[i].getWriteMethod().invoke( o, value );
				return;
			}
		}
		throw new Exception("Property not found. Display existing parameters by moss.getPropertyName()");
	}

	public ArrayList<String> getPropertyNames(Object o) throws IntrospectionException {
		PropertyDescriptor[] pds = Introspector.getBeanInfo( o.getClass() ).getPropertyDescriptors();
		ArrayList<String> propertyNames = new ArrayList<String>();
		for( int i=0; i< pds.length; i++){
			propertyNames.add(pds[i].getName());	
		}
		return propertyNames;
	}

	public static Object getProperty( Object o, String propertyName ) throws Exception {
		PropertyDescriptor[] pds = Introspector.getBeanInfo( o.getClass() ).getPropertyDescriptors();
		for( int i=0; i< pds.length; i++){
			if( pds[i].getName().equals(propertyName)){
				return pds[i].getReadMethod().invoke( o ) ;
			}
		}
		throw new Exception("Property not found.");
	}
	public  double getMinimalSupport() {
		return minimalSupport;
	}

	public void setMinimalSupport(double minimalSupport) {
		this.minimalSupport = minimalSupport*0.01;
	}

	public double getMaximalSupport() {
		return maximalSupport;
	}

	public void setMaximalSupport(double maximalSupport) {
		this.maximalSupport = maximalSupport*0.01;
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
		return limits;
	}

	public void setLimits(double minsupp, double maxsupp) {
		minimalSupport = minsupp;
		maximalSupport = maxsupp;
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
	
	public void setMinRing(int minRing) {
		this.minRing = minRing;
	}

	public boolean getClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
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
	
//MODE
	
	
	public String ringExtension="none", Kekule="", carbonChainLength="", extPrune ="";
	boolean canonic= true, equiv = false, unembedSibling = false;
	//unembed sibling nodes
	public void setUnembedSibling(boolean unembedSibling){
		this.unembedSibling= unembedSibling;
	}
	public boolean getUnembedSibling(){
		return unembedSibling;
	}
	//equiv
	public void setCanonicEquiv(boolean equiv){
		this.equiv = equiv;
	}
	public boolean getCanonicEquiv(){
		return equiv;
	}
	//canonic
	public void setCanonic(boolean canonic){
		this.canonic = canonic;
	}
	public boolean getCanonic(){
		return canonic;
	}
	//extPrune
	public void setExtPrune(String extPrune){
		this.extPrune = extPrune;
	}
	public String getExtPrune(){
		return extPrune;
	}
	//ringeExtension
	public void setRingExtension(String ringExtension){
		this.ringExtension=ringExtension;
	}
	public String getRingExtension(){
		return ringExtension;
	}
	//kekule
	public void setKekule(String kekule){
		this.Kekule = kekule;
	}	
	public String getKekule() {
		return Kekule;
	}
	
	public void setCarbonChainLength(String carbonChainLength) {
		this.carbonChainLength = carbonChainLength;
	}
	public String getCarbonChainLength() {
		return carbonChainLength;
	}
	
	public int getMode(){
		mode =Miner.DEFAULT;// | Miner.RINGEXT;
		//Ring extension;
		if(ringExtension.equals("full")){
			mode |= Miner.RINGEXT;
		}else if(ringExtension.equals("merge")){
			mode |= Miner.MERGERINGS |Miner.RINGEXT |Miner.CLOSERINGS | Miner.PR_UNCLOSE;
		}else if(ringExtension.equals("filter")){
			mode |= Miner.CLOSERINGS | Miner.PR_UNCLOSE;
		}
		//kekule
		if(Kekule.equals("uncheck")){
			mode |= Miner.AROMATIZE;
		}
		//Carbon chain length.
		if(carbonChainLength.equals("check")){
			mode |= Miner.CHAINEXT;
		}
		//extPrunr
		//TODO CHECK settings for none
		if(extPrune.equals("full")){
			mode |=  Miner.PR_PERFECT;
		    mode &= ~Miner.PR_PARTIAL;
		}else if(extPrune.equals("partial")){
			mode |=  Miner.PR_PARTIAL;
			mode &= ~Miner.PR_PERFECT;
		}
		//canonic
		if(canonic == false){
			mode &= ~Miner.PR_CANONIC;
		}
		//equiv
		if(equiv == true){
			mode |= Miner.PR_EQUIV;		
		}
		//unembed sibling
		if(unembedSibling == true){
			mode |= Miner.UNEMBED;
		}
		if(closed == false){
			mode &= ~Miner.CLOSED;
		}
		
		return mode;
	}
	public void setMode(int mode){
		this.mode = mode;
	}
	
//MATOM MGRAT
	
	String ignoreTypeOfAtoms="", matchChargeOfAtoms="", matchAromaticityAtoms="" ;

	public void setIgnoreAtomTypes(String ignoreTypeOfAtoms){//aromatic,upgrade,downgrade.
		this.ignoreTypeOfAtoms = ignoreTypeOfAtoms;
	}
	public String getIgnoreAtomTypes(){
		return ignoreTypeOfAtoms;
	}
	
	public void setMatchChargeOfAtoms(String matchChargeOfAtoms){
		this.matchChargeOfAtoms = matchChargeOfAtoms;
	}
	public String getMatchChargeOfAtoms() {
		return matchChargeOfAtoms;
	}
	
	public void setMatchAromaticityAtoms(String matchAromaticityAtoms){
		this.matchAromaticityAtoms = matchAromaticityAtoms;
	}
	public String getMatchAromaticityAtoms() {
		return matchAromaticityAtoms;
	}
	

	public int getMatom() {
		matom= Atoms.ELEMMASK;//nollstŠlla if =never, don't match atom/aromaticity
		
		// Ignore atoms
		 if(this.ignoreTypeOfAtoms.equals("always")){
			matom &= ~Atoms.ELEMMASK;}
		
		//Match charge of atoms
		if(matchChargeOfAtoms.equals("match")){
			matom |= Atoms.CHARGEMASK;
		}
	
		//Match aromaticity
		if(matchAromaticityAtoms.equals("match")){
			matom &= Atoms.AROMATIC;}

		return matom;
	}
	public void setMatom(int matom){
		this.matom = matom;
	}
	public int getMrgat() {
		mrgat = Atoms.ELEMMASK;//nollstŠlla =never, 
		
		 if(this.ignoreTypeOfAtoms.equals("always")||this.ignoreTypeOfAtoms.equals("in rings")){
			mrgat &= ~Atoms.ELEMMASK;}
		return mrgat;
	}
	public void setMrgat(int mrgat){
		this.mrgat = mrgat;
	}

	
//MBOND MRGBD
	
	String aromatic ="", ignoreBond="";
	public void setAromatic(String aromatic){//aromatic,upgrade,downgrade.
		this.aromatic = aromatic;
	}
	public String getAromatic(){
		return aromatic;
	}
	
	public void setIgnoreBond(String ignoreBond){//never,always, in rings.
		this.ignoreBond = ignoreBond;
	}
	public String getIgnoreBond() {
		return ignoreBond;
	}
	
	public int getMbond() {
		mbond = Bonds.BONDMASK;//nollstŠlla = never,
		
		//Aromatic bonds
	    if(this.aromatic.equals("upgrade")){
			mbond &= Bonds.UPGRADE;
		}else if(this.aromatic.equals("downgrade")){
			mbond &= Bonds.DOWNGRADE;}
	
	    //Ignore bonds
		 if(ignoreBond.equals("always")){
			mbond &= Bonds.SAMETYPE;
		}
		return mbond;
	}
	public void setMbond(int mbond){
		this.mbond = mbond;
	}
	public int getMrgbd() {
		mrgbd = Bonds.BONDMASK;//nollstŠlla = never
		
		//Aromatic bonds
	    if(this.aromatic.equals("upgrade")){
			mrgbd &= Bonds.UPGRADE;}
		else if(this.aromatic.equals("downgrade")){
			mrgbd &= Bonds.DOWNGRADE;}
		//Ignore bonds
		if(ignoreBond.equals("always") || ignoreBond.equals("in rings")){
			mrgbd &= Bonds.SAMETYPE;
		}
		return mrgbd;
	}
	public void setMrgbd(int mrgbd){
		this.mrgbd = mrgbd;
	}
}
