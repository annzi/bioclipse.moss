/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.moss.business.backbone;

/**
 * Base class that runs MoSS
 * 
 * @author Annsofie Andersson
 * 
 */

import java.io.*;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.util.LogUtils;

import moss.Fragment;
import moss.Graph;
import moss.Miner;
import moss.NamedGraph;
import moss.Notation;
import moss.SMILES;

public class MossRunner {
    
    private static final Logger logger = Logger.getLogger(MossRunner.class);

    /**
     * This method runs MoSS on a MossModel
     * 
     * @param mossModel
     * @param outputFileName,
     *            outputFileNameId
     * @return
     * @throws
     */
    public static MossModel initFromSelection(String inputfile, MossModel mossmodel) {
    	IFile filename = ResourcePathTransformer.getInstance().transform(inputfile);
    	InputMolecule imol= null;
		IPath path = filename.getLocation();
		// Read file line by line as. Typical line: a,0,CCCO
		String line;
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new FileReader(path.toOSString()));
			// Read line by line
			while ((line = bufferedReader.readLine()) != null) {
				// Split each line by comma to look like
				String[] parts = line.split(",");
				// We require the following form: ID, VALUE, DESCRIPTION(SMILES).
				String id = parts[0];	 
				float value = Float.parseFloat(parts[1]);
				String description = parts[2];
				// Checks the molecules if they are correctly written in SMILES.
				Notation ntn = new SMILES();
				ntn.parse(new StringReader(description));
				// Adds molecules to mossmodel.
				imol = new InputMolecule(id, value,
						description);
				mossmodel.addMolecule(imol);
			}
		} catch (Exception e) {
			System.out.println(e); 
		}
		return mossmodel;	
	}
    
    public static MossModel runMoss(MossModel mossModel, String in, String outputFileName,
            String outputFileNameId) {
        // Integer that decides group since MoSS made their group accessibility
        // private
    	
    	//Adds molecules to the model
    	mossModel = initFromSelection(in, mossModel);
    	
        int g;
        // This is the class (in original MoSS)that does the mining
        Miner miner = new Miner();

        /*
         * Creates setters that set the different parameters to miner
         * 
         * Set threshold and whether or not to split into focus/complement. Set
         * up minimum and maximum support Set the search mode Set support type(
         * TODO: make types changeable has default settings) Set limits, sizes,
         * minimal and maximal ring sizes Set bond and atom types Set maximum
         * number of embedding
         */

        // Settings for split
        boolean invert = mossModel.getSplit();
        if (invert == false)
            g = 0;
        else
            g = 1;

        miner.setGrouping(mossModel.getThreshold(), invert);
        miner.setLimits(mossModel.getMinimalSupport(), mossModel.getMaximalSupport());
        miner.setMode(mossModel.getMode());
        miner.setSizes(mossModel.getMinEmbed(), mossModel.getMaxEmbed());
        miner.setRingSizes(mossModel.getMinRing(), mossModel.getMaxRing());
        miner.setMasks(mossModel.getMatom(), mossModel.getMbond(), mossModel
                .getMrgat(), mossModel.getMrgbd());
        miner.setMaxEmbs(mossModel.getMaxEmbMemory());

        // TODO: Let type be a changeable parameter
        int type = Fragment.GRAPHS | Fragment.GREEDY;
        miner.setType(type);

        try {
            miner.setSeed(mossModel.getSeed(), "smiles");
        } catch (IOException e) {
            LogUtils.debugTrace(logger, e);
        }
        
        try {
            miner.setExcluded(mossModel.getExNode(), mossModel.getExSeed(),
                    "smiles");
        } catch (IOException e) {
            LogUtils.debugTrace(logger, e);
        }
        
        

        // Loop over all inputMolecules in mossModel
//        for (int i = 0; i < mossModel.getInputMolecules().size(); i++) {
            for(InputMolecule mol : mossModel.getInputMolecules()){
//            InputMolecule mol = mossModel.getInputMolecules().get(i);
//            if (mol.isChecked()) {

                SMILES smiles = new SMILES();
                StringReader reader = new StringReader(mol.getDescription());

                Graph graph = null;
                try {
                    graph = smiles.parse(reader);
                } catch (IOException e) {
                    LogUtils.debugTrace(logger, e);
                    System.exit(1);
                }

                // Since Moss group is private we go around it with initiating g
                int grp = (mol.getValue() > mossModel.getThreshold()) ? 1 - g
                        : g;
                NamedGraph ngraph = new NamedGraph(graph, mol.getId(), mol
                        .getValue(), grp);
                miner.addGraph(ngraph);

        }
        // Set logging
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bo);
        miner.setLog(ps);
        // Tries to set output files by using a method from Moss
        try {
            miner.setOutput(outputFileName, "smiles", outputFileNameId);
        } catch (IOException e) {
            LogUtils.debugTrace(logger, e);
            return null;
        }

        // Run the mining and get statistics
        miner.run();
        miner.stats();

       net.bioclipse.scripting.ui.Activator.getDefault().getJavaJsConsoleManager().say(bo.toString());

        try {
            bo.close();
        } catch (IOException e) {
            LogUtils.debugTrace(logger, e);
        }
        ps.close();
        return null;

    }
}
