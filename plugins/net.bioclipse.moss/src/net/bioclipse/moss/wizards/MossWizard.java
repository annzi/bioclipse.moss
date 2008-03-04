/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/

package net.bioclipse.moss.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import moss.Atoms;
import moss.Bonds;
import moss.Extension;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.moss.InputMolecule;
import net.bioclipse.moss.MossFileTestRunner;
import net.bioclipse.moss.MossModel;
import net.bioclipse.moss.MossTestRunner;
import net.bioclipse.moss.popup.actions.RunMossAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author ola, Annzi
 * 
 */
public class MossWizard extends Wizard {

	ISelection selection;
	InputPage page1;
	ParametersPage page2;
	ParametersPage2 page3;
	ParametersPage3 page4;

	MossModel mossModel;
	/*------------------------------------------------------------------*/
	/*  constants: sizes and flags                                      */
	/*------------------------------------------------------------------*/
	/** flag for extensions by single edges */
	public static final int EDGEEXT = Extension.EDGE;
	/** flag for extensions by rings */
	public static final int RINGEXT = Extension.RING;
	/** flag for extensions by chains */
	public static final int CHAINEXT = Extension.CHAIN;
	/** flag for extensions by equivalent variants of rings */
	public static final int EQVARS = Extension.EQVARS;
	/** flag for rightmost path extensions */
	public static final int RIGHTEXT = 0x000010;
	/** flag for restriction to closed fragments */
	public static final int CLOSED = 0x000020;
	/** flag for filtering open rings */
	public static final int CLOSERINGS = 0x000040;
	/** flag for merging ring extensions with the same first edge */
	public static final int MERGERINGS = 0x000080;
	/** flag for full (unmerged) ring extensions */
	private static final int FULLRINGS = 0x000100;
	/** flag for pruning fragments with unclosable rings */
	public static final int PR_UNCLOSE = 0x000200;
	/** flag for partial perfect extension pruning */
	public static final int PR_PARTIAL = 0x000400;
	/** flag for full perfect extension pruning */
	public static final int PR_PERFECT = 0x000800;
	/** flag for equivalent sibling extension pruning */
	public static final int PR_EQUIV = 0x001000;
	/** flag for canonical form pruning */
	public static final int PR_CANONIC = 0x002000;
	/** flag for unembedding siblings of the current search tree nodes */
	public static final int UNEMBED = 0x004000;
	/** flag for normalized substructure output */
	public static final int NORMFORM = 0x008000;
	/** flag for verbose reporting */
	public static final int VERBOSE = 0x010000;
	/** flag for converting Kekul&eacute; representations */
	public static final int AROMATIZE = 0x020000;
	/** flag for conversion to another description format */
	public static final int TRANSFORM = 0x040000;
	/** flag for conversion to logic representation */
	public static final int LOGIC = 0x080000;
	/** flag for no search statistics output */
	public static final int NOSTATS = 0x100000;
	/** default search mode flags: edge extensions,
	 *  canonical form and full perfect extension pruning */
	public static final int DEFAULT = EDGEEXT | CLOSED | PR_CANONIC
			| PR_PERFECT;
	// These tables are for storing data temporary and is written to mossModel
	// on performFinish()
	Hashtable<String, Integer> bondsTable = new Hashtable<String, Integer>();
	Hashtable<String, Integer> atomsTable = new Hashtable<String, Integer>();
	Hashtable<String, Integer> modeTable = new Hashtable<String, Integer>();
	Hashtable<String, Integer> typeTable = new Hashtable<String, Integer>();

	// Constructor that stores the selection in navigator
	public MossWizard(ISelection selection) {
		this.selection = selection;
		mossModel = new MossModel();
		initFromSelection(mossModel);
	}

	@Override
	public void addPages() {
		// Add a page for molecules as input
		page1 = new InputPage();
		addPage(page1);

		// Adds a page for Moss parameters
		page2 = new ParametersPage();
		addPage(page2);

		// Adds a new page for Moss parameters
		page3 = new ParametersPage2();
		addPage(page3);

		// Adds a new page for Moss parameters
		page4 = new ParametersPage3();
		addPage((IWizardPage) page4);
	}
	// To be able to show error if MoSS does not support the input file
	public void showMessage(String title, String message) {
		MessageDialog.openInformation(
				getShell(),
				title,
				message);
	}
	// If there are something wrong in the boxes one should not be able to finsh
	public boolean canFinish() {
		// TODO Auto-generated method stub
		if (page2.isPageComplete() == false) return false;
		return super.canFinish();
	}
	
	public boolean performFinish() {
		// TODO Auto-generated method stub

		/* Wrap up everything and finalize choices in MossModel
		 Get values from has tables and perform setMbond, setMrgbd, setMatom, setMrgat and setMode*/

		System.out.println("the table for bonds " + bondsTable);

		// Get the values for bond and mrgbd 
		Integer b1 = (Integer) bondsTable.get("mbond1");
		Integer b2 = (Integer) bondsTable.get("mbond2");
		Integer mb1 = (Integer) bondsTable.get("mrgbd1");
		Integer mb2 = (Integer) bondsTable.get("mrgbd2");

		// Initialize B and Mb with default values
		int B = Bonds.BONDMASK;
		int Mb = Bonds.BONDMASK;
					
		// Select whether to upgrade or down grade, if b1=Bonds.BONMASK nothing needs to be done
		if (b1 == Bonds.UPGRADE){
			B &= b1;
			Mb &= mb1;
		}
		if(b1 == Bonds.DOWNGRADE){
			B &= b1;
			Mb &= mb1;
		}
		// Select whether to ignore bonds or not
		if(b2 == Bonds.SAMETYPE && mb2 == Bonds.SAMETYPE ){
			B &= b2;
			Mb &= mb2;
		}
		else{Mb &=mb2;}
		
		System.out.println(bondsTable.values());
		
		
		System.out.println("the table for atoms " + atomsTable);
	
		//Get values from hash table
		Integer a1 = (Integer) atomsTable.get("matom1");
		Integer a2 = (Integer) atomsTable.get("matom2");
		Integer a3 = (Integer) atomsTable.get("matom3");
		Integer ma = (Integer) atomsTable.get("mrgat1");

		// Initialize with default settings
		int A = Atoms.ELEMMASK;
		int Ma = Atoms.ELEMMASK;
	
		// Case: if to always ignore atom types
		if (a1 == ~Atoms.ELEMMASK && ma == ~Atoms.ELEMMASK){
			A &= a1;
			Ma &= ma;
			}
		// Case: if to always ignore atom types in rings
		else{Ma &= ma;}	
		
		// Case: if to match charge of atoms, by default one ignore matching
		if(a2 != Atoms.ELEMMASK){
			A |= a2;
		}
		// Case: if to match aromaticity of atoms, by default one do not
		if(a3 != Atoms.ELEMMASK){
		A |= a3;
		}

		// Get all the current values in mode hash table, done in alphabetic order
		System.out.println("the table for mode " + modeTable);
		Integer canonic = (Integer) modeTable.get("canonPruning");
		Integer chain = (Integer) modeTable.get("chain");
		Integer closed = (Integer) modeTable.get("closed");
		Integer equiv = (Integer) modeTable.get("eqPruning");
		Integer extension1 = (Integer) modeTable.get("ext1");
		Integer extension2 = (Integer) modeTable.get("ext2");
		Integer extension3 = (Integer) modeTable.get("ext3");
		Integer extension4 = (Integer) modeTable.get("ext4");
		Integer extprune1 =(Integer) modeTable.get("extPruning1");
		Integer extprune2 =(Integer) modeTable.get("extPruning2");
		Integer kekule = (Integer) modeTable.get("kekule");
		Integer stats = (Integer) modeTable.get("stats");
		Integer verbose = (Integer) modeTable.get("verbose");
		Integer unembedSibling = (Integer) modeTable.get("unembSibling");

		// To be able to collect current values, if a default value is set that value won't be 	necessary to count 
		int totMode = DEFAULT;

		if(canonic != DEFAULT ){
			totMode &=canonic;
		}
		if (closed != DEFAULT) {
			totMode &= closed;
		}
		if (chain == CHAINEXT){
			totMode |= chain;
		}
		if (kekule != DEFAULT) {
			totMode |= kekule;
		}
		if (stats != DEFAULT) {
			totMode |= stats;
		}
		if (verbose != DEFAULT) {
			totMode |= verbose;
		}

		// Pruning parameters working??
		if(equiv == PR_EQUIV){
			totMode |=equiv;
		}

		if(extprune1 == PR_PERFECT){
			totMode |= extprune1;
			totMode &= extprune2;
		}
		if(extprune1 == PR_PARTIAL){
			totMode |= extprune1;
			totMode &= extprune2;
		}

		// could be ?? or write actual values TODO: check what's best values or parameters
		if(extension1 == RINGEXT){
			totMode |= extension1;
		}
		if (extension1 == MERGERINGS) {
			totMode |= extension2| extension1 | extension3 | extension4;
		}
		if (extension1 == CLOSERINGS) {
			totMode  |= extension1 | extension2; 
		}
		if (unembedSibling == UNEMBED){
			totMode |= unembedSibling;
		}
		
		// Set the hash table parameters in to mossModel	
		// Set Mbond and Mrgbd in mossModel
		mossModel.setMbond(B);
		mossModel.setMrgbd(Mb);
		// Set the total value of matom and mrgat
		mossModel.setMatom(A);
		mossModel.setMrgat(Ma);
		// Set the total mode is set
		mossModel.setMode(totMode);
		
		// Check if parameters are correct by printing them
		System.out.println("MossModel:");
		System.out.println("Max support: " + getMossModel().getMaximalsupport());
		System.out.println("Min support: " + getMossModel().getMinimalSupport());
		System.out.println("Threshold: " + getMossModel().getThreshold());
		System.out.println("Invert split: " + getMossModel().getSplit());
		System.out.println("closed: " + getMossModel().getClosed());
		System.out.println("ExNode: " + getMossModel().getExNode());
		System.out.println("ExSeed: " + getMossModel().getExSeed());
		System.out.println("MaxEmb: " + getMossModel().getMaxEmbed());
		System.out.println("MinEmb: " + getMossModel().getMinEmbed());
		System.out.println("MBond: " + getMossModel().getMbond());
		System.out.println("Mrgbd: " + getMossModel().getMrgbd());
		System.out.println("Matom: " + getMossModel().getMatom());
		System.out.println("Mrgat: " + getMossModel().getMrgat());
		System.out.println("Max Ring: " + getMossModel().getMaxRing());
		System.out.println("Min Ring: " + getMossModel().getMinRing());
		System.out.println("Mode: " + getMossModel().getMode());
		System.out.println("MaxEmbMemory: " + getMossModel().getMaxEmbMemory());
		
		
		for (InputMolecule mol : getMossModel().getInputMolecules()) {
			System.out.println("Molecule: " + mol.getId() + " include: "
					+ mol.isChecked());
		}

		String path = "";
		IResource parent = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IResource res = (IResource) obj;
				parent = res.getParent();
				path = parent.getLocation().toOSString();
			}
		}

		String outputFileName = path + File.separator + "MossOutput.txt";
		System.out.println("Output file name: " + outputFileName);

		MossTestRunner.runMoss(mossModel, outputFileName);

		try {
			parent.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return true;
	}

	public MossModel getMossModel() {
		return mossModel;
	}

	public void setMossModel(MossModel mossModel) {
		this.mossModel = mossModel;

	}

	/**
	 * Read selection, create InputMolecules and add to MossModel
	 * 
	 * @throws BioclipseException
	 */
	private void initFromSelection(MossModel mossModel) {

		if (!(selection instanceof IStructuredSelection)) {
			System.out.println("Strange selection. Should not happen.");
			return;
		}

		IStructuredSelection structSel = (IStructuredSelection) selection;

		for (Iterator it = structSel.iterator(); it.hasNext();) {
			Object obj = it.next();

			// Case: selection is a File
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;

				IPath path = file.getLocation();

				System.out.println("Reading file: " + path.toOSString());

				// Read file line by line as
				// Typical line: a,0,CCCO
				// TODO!!
				String line;
				try {
					BufferedReader bufferedReader = new BufferedReader(
							new FileReader(path.toOSString()));
					// Read line by line
					while ((line = bufferedReader.readLine()) != null) {

						// Split each line by comma to look like
						String[] parts = line.split(",");

						// We require the following form: ID, VALUE, DESCRIPTION
						// (SMILES)
						String id = parts[0];
						float value = Float.parseFloat(parts[1]);
						String description = parts[2];

						// Add molecules to mossModel
						InputMolecule imol = new InputMolecule(id, value,
								description);
						mossModel.addMolecule(imol);
					}
				} catch (Exception e) {
					System.out.println("Not a correct input");
				showMessage("Error"," MoSS does not support this input file");
				}

			}

			System.out.println("We have read: "
					+ mossModel.getInputMolecules().size()
					+ " entries from file");

			if (obj instanceof IMolecule) {
				// TODO: implement this in the future maybe
				return;
			}

		}

	}

	public Hashtable<String, Integer> getBondsTable() {
		return bondsTable;
	}

	public void setBondsTable(Hashtable<String, Integer> bondsTable) {
		this.bondsTable = bondsTable;
	}

	public Hashtable<String, Integer> getAtomsTable() {
		return atomsTable;
	}

	public void setAtomsTable(Hashtable<String, Integer> atomsTable) {
		this.atomsTable = atomsTable;
	}

	public Hashtable<String, Integer> getModeTable() {
		return modeTable;
	}

	public void setModeTable(Hashtable<String, Integer> modeTable) {
		this.modeTable = modeTable;
	}
}
