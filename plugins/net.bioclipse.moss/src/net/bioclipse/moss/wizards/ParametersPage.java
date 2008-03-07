/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.orgÑepl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/

package net.bioclipse.moss.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import moss.Atoms;
import moss.Bonds;
import moss.Extension;
import moss.SMILES;
import net.bioclipse.moss.InputMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMessages;


public class ParametersPage extends WizardPage {
	private Label labelexNode, labelexSeed, labelMaxEmb, labelMinEmb;
	private Text exSeed, exNode;
	private IStructuredSelection selection;

	
	/*------------------------------------------------------------------*/
	/* constants: sizes and flags */
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
	/** flag for converting Kekulé; representations */
	public static final int AROMATIZE = 0x020000;
	/** flag for conversion to another description format */
	public static final int TRANSFORM = 0x040000;
	/** flag for conversion to logic representation */
	public static final int LOGIC = 0x080000;
	/** flag for no search statistics output */
	public static final int NOSTATS = 0x100000;
	/**
	 * default search mode flags: edge extensions, canonical form and full
	 * perfect extension pruning
	 */
	public static final int DEFAULT = EDGEEXT | CLOSED | PR_CANONIC
			| PR_PERFECT;
	
	/* Since MoSS only support theses elements in different methods,
	 * an array of elements is created so that bioclipse also can restrict 
	 * atoms in different methods so errors can be set
	 * This has to be done since MoSS only accept these atoms to be excluded*/
	// private static final String[] ELEMENTS = { "H", "b", "B", "Br", "c", "C",
	// "Cl", "n", "N", "o", "O", "F", "P", "p", "s", "S", "I"};
	protected static final String[] ELEMENTS = { "*", "H", "[He]", "[Li]", "[Be]",
			"B", "C", "N", "O", "[F]", "Ne", "Na", "Mg", "Al", "Si", "P", "S",
			"Cl", "Ar", "[K]", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co",
			"Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr",
			"Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In",
			"Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
			"Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu",
			"Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb",
			"Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np",
			"Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf",
			"Db", "Sg", "Bh", "Hs", "Mt","[", "]" };
	
	private boolean isLowerCase(String s){
		String []lower = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s", "t","u","v","w","x","y","z"};
		for(int i=0;i<=25;i++){
		if (s.equals(lower[i])){
			return true;
		}}
		return false;
	}
	
	/* Throws exception if atoms are not supported by ELEMENTS, restricted 
	 * since MoSS restrict atom support in exclusions of nodes and seeds */
	private static boolean isElement(String s) throws Exception{
		for (String elem : ELEMENTS)
			if (elem.equals(s))
				return true;

		throw new Exception();
		}
	
	private HashMap<Text, String> errors = new HashMap<Text , String>();	
	
	/**
	 * Create the wizard
	 */
	public ParametersPage() {
		super("Moss Parameters");
		setTitle("Moss Parameters");
		setDescription("Please enter parameters for Moss");
	}


	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		container.setLayout(gl);
		
		// Minimum support in focus part
		new Label(container, SWT.NONE).setText("Minimum Support in focus:");

		Text txtFocusSupport = new Text(container, SWT.RIGHT | SWT.BORDER);
		txtFocusSupport.setText("10.0");

		GridData minMaxsuppData = new GridData();
		minMaxsuppData.widthHint = 50;
		minMaxsuppData.heightHint = 13;
		txtFocusSupport.setLayoutData(minMaxsuppData);

		txtFocusSupport.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				((MossWizard) getWizard()).getContainer().updateButtons();
				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					String value = txt.getText();

					try {
						double d = Double.parseDouble(value);
						errors.put(txt, null);
						((MossWizard) getWizard()).getMossModel()
								.setMinimalSupport(d * 0.01);
					} catch (NumberFormatException e1) {
					errors.put(txt, "Minimal support in focus must be set as a number");
					check();
						return;
					}
				check();
				}
			}
		});
		// Maximum support in complement part
		new Label(container, SWT.NONE)
				.setText("Maximum support in complement:");
		Text txtCompSupport = new Text(container, SWT.RIGHT | SWT.BORDER);
		txtCompSupport.setText("2.0");
		txtCompSupport.setLayoutData(minMaxsuppData);

		txtCompSupport.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				((MossWizard) getWizard()).getContainer().updateButtons();
				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					String value = txt.getText();
					try {
						double d = Double.parseDouble(value);
						errors.put(txt, null);
						((MossWizard) getWizard()).getContainer()
								.updateButtons();
						((MossWizard) getWizard()).getMossModel()
								.setMaximalsupport(d * 0.01);
					} catch (NumberFormatException e1) {
						errors.put(txt,"Maximal support in complement must be set as a number");
						check();
						return;
					}
					check();
				}
			}
		});
		// Threshold for split
		new Label(container, SWT.NONE).setText("Threshold:");

		Text thres = new Text(container, SWT.RIGHT | SWT.BORDER);
		thres.setText("0.5");

		GridData thresLData = new GridData();
		thresLData.widthHint = 50;
		thresLData.heightHint = 13;
		thres.setLayoutData(thresLData);

		thres.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				((MossWizard) getWizard()).getContainer().updateButtons();
				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					String value = txt.getText();
					try {
						double d = Double.parseDouble(value);
						errors.put(txt, null);

						((MossWizard) getWizard()).getMossModel().setThreshold(d);
					} catch (NumberFormatException e1) {
						errors.put(txt, "Threshold must be set as a number");
						check();
						return;
					}
					check();
				}
			}
		});
		// Invert split
		final Button split = new Button(container, SWT.CHECK);
		split.setText("Invert split");

		GridData splitLData = new GridData();
		splitLData.horizontalSpan = 2;
		split.setLayoutData(splitLData);

		split.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = split.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getMossModel().setSplit(true);
				} else {
					((MossWizard) getWizard()).getMossModel().setSplit(false);
				}
			}
		});
		// Minimal value of embedding
		labelMinEmb = new Label(container, SWT.SEARCH);
		labelMinEmb.setText("Minimum embedding:");

		GridData labelMinEmbLData = new GridData();
		labelMinEmbLData.verticalAlignment = GridData.END;
		labelMinEmbLData.verticalSpan = 2;
		labelMinEmb.setLayoutData(labelMinEmbLData);

		final Spinner minEmb = new Spinner(container, SWT.RIGHT | SWT.BORDER);
		minEmb.setSelection(1);

		GridData minEmbData = new GridData();
		minEmbData.widthHint = 50;
		minEmbData.heightHint = 13;
		minEmbData.verticalSpan = 2;
		minEmb.setLayoutData(minEmbData);

		minEmb.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				int selected = minEmb.getSelection();

				((MossWizard) getWizard()).getMossModel().setMinEmbed(selected);
			}
		});

		// Maximum value of embedding
		labelMaxEmb = new Label(container, SWT.SEARCH);
		labelMaxEmb.setText("Maximal embedding:");

		GridData labelMaxEmbLData = new GridData();
		labelMaxEmbLData.verticalAlignment = GridData.END;
		labelMaxEmbLData.verticalSpan = 5;
		labelMaxEmb.setLayoutData(labelMaxEmbLData);

		final Spinner maxEmb = new Spinner(container, SWT.RIGHT | SWT.BORDER);
		maxEmb.setSelection(0);

		GridData gridData2 = new GridData();
		gridData2.widthHint = 50;
		gridData2.heightHint = 13;
		gridData2.verticalAlignment = GridData.END;
		gridData2.verticalSpan = 5;

		maxEmb.setLayoutData(gridData2);

		maxEmb.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				int selected = maxEmb.getSelection();
				((MossWizard) getWizard()).getMossModel().setMaxEmbed(selected);
			}
		});

		// Exclude atoms 
		labelexNode = new Label(container, SWT.NONE);
		labelexNode.setText("Type of nodes to exclude:");

		GridData labelexNodeData = new GridData();
		labelexNodeData.verticalSpan = 7;
		labelexNodeData.horizontalSpan = 1;
		labelexNodeData.verticalAlignment = GridData.END;
		labelexNode.setLayoutData(labelexNodeData);

		exNode = new Text(container, SWT.BORDER);
		exNode.setText("H");

		GridData exNodeData = new GridData();
		exNodeData.widthHint = 50;
		exNodeData.heightHint = 13;
		exNodeData.verticalSpan = 7;
		exNodeData.verticalAlignment = GridData.END;
		exNode.setLayoutData(exNodeData);

		exNode.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					String value = txt.getText();
					try {
						String test;
						setErrorMessage(null);
						((MossWizard) getWizard()).getContainer()
								.updateButtons();

						for (int i = 0; i < value.length(); i++) {
							String v = value.substring(i, i+1);
							int a = i;
							int d = i;
							int valu = value.length();
							if (v.equals("[")){
								for(int k=0; k<value.length();k++){
									String b = "]";
									String c = value.substring(d+1,a+k+2);
									d++;
									i+=1;
									if( b.equals(c)){
										v = value.substring(a, a+k+2);
										
										k = value.length();
									}
									}
							}
 							if (value.length() > i + 1) {
								test = value.substring(i + 1, i + 2);
								} 
							
							 else {
								test = value.substring(i + 1, i + 1);
							}

							if (isLowerCase(test) == true) {
								v = value.substring(i, i + 2);
								i++;
							}
							isElement(v);
						}
						((MossWizard) getWizard()).getMossModel().setExNode(
								value);
					}
						

					catch (Exception e1) {
						setErrorMessage("Must be an atom that have support, see help");
						((MossWizard) getWizard()).getContainer()
								.updateButtons();
						return;
					}
				}
			}
		});

		// Exclude atoms to be set as seeds
		labelexSeed = new Label(container, SWT.NONE);
		labelexSeed.setText("Seed types to exclude:");

		GridData labelexSeedData = new GridData();
		labelexSeed.setLayoutData(labelexSeedData);

		exSeed = new Text(container, SWT.BORDER);

		GridData exSeedData = new GridData();
		exSeedData.widthHint = 50;
		exSeedData.heightHint = 13;
		exSeed.setLayoutData(exSeedData);

		exSeed.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					String value = txt.getText();
					
					try{
						String test;
						setErrorMessage(null);
						((MossWizard) getWizard()).getContainer().updateButtons();

						for (int i = 0; i < value.length(); i++) {
							String v = value.substring(i, i + 1);

							if (value.length() > i + 1) {
								test = value.substring(i + 1, i + 2);
							} else {
								test = value.substring(i + 1, i + 1);
							}

							if ( isLowerCase(test) == true) {
								v = value.substring(i, i + 2);
								i++;
							}
							isElement(v);
						}
						
						((MossWizard) getWizard()).getMossModel().setExSeed(
								value);
					}
					catch(Exception e1) {
						setErrorMessage("Must be an atom that have support, see help");
						((MossWizard) getWizard()).getContainer().updateButtons();
						return;
						}
				}
			}
		});
		// Closed structures reporting
		final Button closed = new Button(container, SWT.CHECK);
		closed.setText("Only report closed substructures");

		GridData closedLData = new GridData();
		closedLData.verticalSpan = 20;
		closed.setLayoutData(closedLData);
		closed.setSelection(true);

		((MossWizard) getWizard()).getModeTable().put("closed",
				new Integer(DEFAULT));

		closed.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = closed.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("closed",
							new Integer(DEFAULT));
				} else {
					((MossWizard) getWizard()).getModeTable().put("closed",
							new Integer(~CLOSED));
				}
			}
		});

	}

	protected void check() {

		String empty=null;
		String errorLine="";
		//Return this
		String totalErrors="";
		//Check if any textbox is empty. Not allowed in primary data objects
		String emptyBoxes="";
		setErrorMessage(null);
		
		
//		for (Text txt: errors.keySet()){
//			if (txt.getText().equals("")){
				//emptyBoxes=emptyBoxes + textBox2MobyDataObject.get(txt).getName() + ", ";
//			}
//		}
		
		
//		if (emptyBoxes.length()>1){
//			empty="The following inputs cannot be empty: " + emptyBoxes;
//			totalErrors = totalErrors + empty + "\n";
//		}

		
		//Check for errors
		for (Text txt: errors.keySet()){
			if (errors.get(txt)!=null){
				errorLine = errorLine + errors.get(txt);
			}
		}

		if (errorLine.length()>1){
			totalErrors = totalErrors + errorLine + "\n";
		}
		
		if (totalErrors.compareTo("")==0){
			setErrorMessage(null);
			setPageComplete(true);
		}else {
			setErrorMessage(errorLine);
			setPageComplete(false);
		}

		
		((MossWizard) getWizard()).getContainer().updateButtons();
	}
	
	
	/** If error occurs on the page next button will be disable will
	 be restored when errors disappeared*/
//	public boolean canFlipToNextPage() {
//		if (getErrorMessage() != null)
//			return false;
//		return true;
//	}

	/** If error occurs on the page the finish button will be disabled
	 * will be restored when errors disappeared*/
//	public boolean isPageComplete() {
//		if (getErrorMessage() == null)
//			return true;
//		return false;
//	}
	
}
