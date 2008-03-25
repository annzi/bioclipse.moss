package net.bioclipse.moss;

/**
 * Base class that runs Moss
 * 
 * @author Annsofie Andersson
 * 
 */

import java.io.*;

import net.bioclipse.core.Activator;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

import moss.Fragment;
import moss.Graph;
import moss.Miner;
import moss.NamedGraph;
import moss.SMILES;

public class MossTestRunner {
	public static void main(String[] args) {

		// Model to store our data
		MossModel mossModel = new MossModel();
		runMoss(mossModel, "", "");
	}

	/**
	 * This method runs MOSS on a MossModel
	 * 
	 * @param mossModel
	 * @param outputFileName,
	 *            outputFileNameId
	 * @return
	 * @throws
	 */

	public static MossModel runMoss(MossModel mossModel, String outputFileName,
			String outputFileNameId) {
		// integer that decides group
		int g;
		// This is the class that does the mining
		Miner miner = new Miner();

		int type = Fragment.GRAPHS | Fragment.GREEDY;

		/*
		 * Settings for parameters TODO: check if it works correctly!!
		 * 
		 * Set threshold and whether or not to split into focus/complement Set
		 * up minimum and maximum support Set the search mode Set support type
		 * and limits Set sizes Set minimal and maximal ring sizes Settings for
		 * bonds and atoms Set maximum number of embedding
		 */
		boolean invert = mossModel.getSplit();
		if (invert == false)
			g = 0;
		else
			g = 1;
		miner.setGrouping(mossModel.getThreshold(), invert);
		miner.setLimits(mossModel.getMinimalSupport(), mossModel
				.getMaximalsupport());
		miner.setMode(mossModel.getMode());
		miner.setType(type);
		miner.setSizes(mossModel.getMinEmbed(), mossModel.getMaxEmbed());
		miner.setRingSizes(mossModel.getMinRing(), mossModel.getMaxRing());
		miner.setMasks(mossModel.getMatom(), mossModel.getMbond(), mossModel
				.getMrgat(), mossModel.getMrgbd());
		miner.setMaxEmbs(mossModel.getMaxEmbMemory());

		// Set seed
		try {
			miner.setSeed(mossModel.getSeed(), "smiles");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set seed and excluded types
		try {
			miner.setExcluded(mossModel.getExNode(), mossModel.getExSeed(),
					"smiles");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Loop over all inputMolecules in mossModel
		for (int i = 0; i < mossModel.getInputMolecules().size(); i++) {

			InputMolecule mol = mossModel.getInputMolecules().get(i);
			if (mol.isChecked()) {

				System.out.println(">> Molecule. id: " + mol.getId() + " ) "
						+ mol.getDescription());
				SMILES smiles = new SMILES();
				StringReader reader = new StringReader(mol.getDescription());

				Graph graph = null;
				try {
					graph = smiles.parse(reader);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				// TODO:not finished, can not have 1-0:0 since they are not
				// constants
				int grp = (mol.getValue() > mossModel.getThreshold()) ? 1 - g
						: g;

				NamedGraph ngraph = new NamedGraph(graph, mol.getId(), mol
						.getValue(), grp);
				miner.addGraph(ngraph);
				System.out.println("group" + grp + " false=0,true=1 " + g);
				System.out.println("   Added " + mol.getId() + " with graph: "
						+ ngraph.toString() + "\n");
			}

		}
		// Set logging
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bo);
		miner.setLog(ps);
		// Tries to set output files by using a method from Moss
		try {

			miner.setOutput(outputFileName, "smiles", outputFileNameId);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Run the mining and get statistics
		miner.run();
		miner.stats();

		net.bioclipse.ui.Activator.getDefault().CONSOLE.echo(bo.toString());

		// System.out.println("log:\n" + bo.toString());
		try {
			bo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ps.close();
		return null;

	}
}
