package net.bioclipse.moss;

import java.io.*;
import java.io.IOException;
import java.io.PrintStream;
import moss.Atoms;
import moss.Bonds;
import moss.Fragment;
import moss.Miner;



public class MossFileTestRunner {
	public static void main(String[] args) {

		//Model to store data
		MossModel mossModel=new MossModel();
		// Run Moss
		runMoss(mossModel);
		}

	/**
	 * This method runs MOSS on a MossModel
	 * @param mossModel
	 * @return MossResult
	 * @throws 
	 */
	public static MossModel runMoss(MossModel mossModel) {
		int g;														//integer that decides group	
		Miner miner=new Miner();									//This is the class that does the mining
		
		try {
			miner.setInput("C:\\Documents and Settings\\Lenny\\Skrivbord\\Annzi\\exrun\\ex1.txt", "smiles");
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
				
		//Settings for different modes,atoms and bonds
//			int mode = (Miner.DEFAULT | Miner.AROMATIZE);
//		    int atom  = Atoms.ELEMMASK;
//		    int mrgat = Atoms.ELEMMASK; 
//	anv ej längre	    int bond  = Bonds.BONDMASK;
//	anv ej längre	    int mrgbd = Bonds.BONDMASK;
		    int type = Fragment.GRAPHS | Fragment.GREEDY;
		
		//Settings for parameters 
		// changed so that the wizard can set test, works, haven't checked if it works correctly
		boolean invert = mossModel.getSplit();										//Set threshold and whether or not to split into focus/complement
		// Since group is private in Moss g helps us go around this problem
		if (invert== false)g=0; 
		else g=1;
		miner.setGrouping(mossModel.getThreshold(),invert);	 
//		miner.setLimits(mossModel.getMinimalSupport(),mossModel.getMaximalsupport()); // 0.01* if not converted correct set up minimum and maximum support 
//		miner.setMode(mossModel.getMode()); 		     					   		// set the search mode 
//	    miner.setType(type);							      		// set support type and limits 
//	    miner.setSizes(mossModel.getMinEmbed(),mossModel.getMaxEmbed()); 								   		// set sizes, masks etc.
//	    miner.setRingSizes(mossModel.getMinRing(),mossModel.getMaxRing());									// set minimal and maximal ring sizes 
//	    miner.setMasks(mossModel.getMatom(), mossModel.getMbond(), mossModel.getMrgat(), mossModel.getMrgbd());					// settings for bonds and atoms
//	    miner.setMaxEmbs(mossModel.getMaxEmbed());   									    // set maximum number of embedding, 0 --> no restriction 
//	    try {														// set seed
//			miner.setSeed("", "smiles" );
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		} 						   
//		try {														// set seed and excluded types 
//			miner.setExcluded(mossModel.getExNode(),mossModel.getExSeed(), "smiles");
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bo);
		miner.setLog(ps);
		try {
			miner.setOutput("testOutput.out", "smiles");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		//Run the mining and get statistics
		//miner.run();             
//		miner.stats();    		  

	System.out.println("log:\n" + bo.toString());
	try {
		bo.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	ps.close();
	return null;
			
	}
}

