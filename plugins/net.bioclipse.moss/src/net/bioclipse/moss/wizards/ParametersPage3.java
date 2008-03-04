package net.bioclipse.moss.wizards;

import java.util.ArrayList;

import moss.Atoms;
import moss.Bonds;
import moss.Extension;
import net.bioclipse.moss.MossModel;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public class ParametersPage3 extends WizardPage {

	/***************************************************************************
	 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
	 * This program and the accompanying materials are made available under the
	 * terms of the Eclipse Public License v1.0 which accompanies this
	 * distribution, and is available at www.eclipse.org—epl-v10.html
	 * <http://www.eclipse.org/legal/epl-v10.html>
	 * 
	 * Contributors: Annzi - initial API and implementation
	 * 
	 **************************************************************************/
	private Label labelPruning, labelMemory, labelMiscell;
	private Label LabelPerfExtPruning;
	private Label labelMaxEmbs;
	
	/**
	 * Create the wizard
	 */
	public ParametersPage3() {
		super("Moss Parameters");
		setTitle("Moss Parameters");
		setDescription("Please enter parameters for Moss");
	}

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

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.RIGHT);
		setControl(container);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		container.setLayout(gl);

		
		// Label for pruning first of three parts on this page
		labelPruning = new Label(container,SWT.NONE);
		labelPruning.setText("Prunings");
		Font font1 = new Font(container.getDisplay(), "Helvetica", 8, SWT.BOLD); 
		labelPruning.setFont(font1);
		
		GridData labelPruningData = new GridData();
		labelPruningData.verticalIndent = 10;
		labelPruningData.horizontalSpan = 2;
		labelPruning.setLayoutData(labelPruningData);
		
		// Parameter to select what kind of perfect extension pruning one would lika to use
		LabelPerfExtPruning = new Label(container, SWT.NONE);
		LabelPerfExtPruning.setText("Perfect extension pruning");

		final Combo extPruning = new Combo(container, SWT.BORDER
				| SWT.READ_ONLY);
		String[] extPruningItems = { "full", "partial", "none" };
		extPruning.setItems(extPruningItems);
		extPruning.setText("full");
		
		((MossWizard) getWizard()).getModeTable().put("extPruning1",
				new Integer(PR_PERFECT));
		((MossWizard) getWizard()).getModeTable().put("extPruning2",
				new Integer(~PR_PARTIAL));
		
		extPruning.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String selected = extPruning.getItem(extPruning
						.getSelectionIndex());

				if (selected.equals(extPruning.getItem(0))) {
					((MossWizard) getWizard()).getModeTable().put(
							"extPruning1", new Integer(PR_PERFECT));
					((MossWizard) getWizard()).getModeTable().put(
							"extPruning2", new Integer(~PR_PARTIAL));
				}
				if (selected.equals(extPruning.getItem(1))) {
					((MossWizard) getWizard()).getModeTable().put(
							"extPruning1", new Integer(PR_PARTIAL));
					((MossWizard) getWizard()).getModeTable().put(
							"extPruning2", new Integer(~PR_PERFECT));
				}
				if(selected.equals(extPruning.getItem(2))){
					
				}

			}
		});
		// Parameter for deciding if to use canonical form pruning or not
		final Button canonical = new Button(container, SWT.CHECK);
		canonical.setText("Canonical form pruning");
		canonical.setSelection(true);

		GridData canonicalLData = new GridData();
		canonicalLData.horizontalSpan = 2;
		canonical.setLayoutData(canonicalLData);

		((MossWizard) getWizard()).getModeTable().put("canonPruning",
				new Integer(DEFAULT));

		canonical.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = canonical.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put(
							"canonPruning", new Integer(DEFAULT));
				} else {
					((MossWizard) getWizard()).getModeTable().put(
							"canonPruning", new Integer(~PR_CANONIC));
				}
			}
		});

		// Button for adding equivalent sibling pruning
		final Button sibling = new Button(container, SWT.CHECK);
		sibling.setText("Equivalent sibling pruning");

		GridData siblingLData = new GridData();
		siblingLData.horizontalSpan = 2;
		sibling.setLayoutData(siblingLData);

		// Sets default value for equivalent sibling pruning
		((MossWizard) getWizard()).getModeTable().put("eqPruning",
				new Integer(DEFAULT));

		sibling.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = sibling.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("eqPruning",
							new Integer(PR_EQUIV));
				} else {
					((MossWizard) getWizard()).getModeTable().put("eqPruning",
							new Integer(DEFAULT));
				}

			}
		});
		// Divide in to one of the three parts on this page
		labelMemory = new Label(container,SWT.NONE);
		labelMemory.setText("Memory adjustments");
		Font font2 = new Font(container.getDisplay(), "Helvetica", 8, SWT.BOLD); 
		labelMemory.setFont(font2);
		
		GridData labelMemoryData = new GridData();
		labelMemoryData.verticalIndent = 10;
		labelMemoryData.horizontalSpan = 2;
		labelMemory.setLayoutData(labelMemoryData);
		
		// Restrict number of embeddings to save memory
		labelMaxEmbs = new Label(container, SWT.NONE);
		labelMaxEmbs.setText("Maximal embedding:");
		GridData labelMaxEmbsData = new GridData();
	
		labelMaxEmbs.setLayoutData(labelMaxEmbsData);
		
		final Spinner maxembs = new Spinner(container, SWT.BORDER | SWT.RIGHT);
		maxembs.setSelection(0);

		GridData maxembsData = new GridData();
		maxembsData.widthHint = 50;
		maxembsData.heightHint = 13;
		maxembs.setLayoutData(maxembsData);

		maxembs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				int selected = maxembs.getSelection();
			((MossWizard) getWizard()).getMossModel().setMaxEmbMemory(selected);
			}
		});
		// Unembed sibling nodes to save space in memory
		final Button unembSibling = new Button(container, SWT.CHECK);
		unembSibling.setText("Unembed sibling nodes");
		
		GridData unembSiblingData = new GridData();
		unembSiblingData.verticalIndent = 5;
		unembSiblingData.horizontalSpan = 2;
		unembSibling.setLayoutData(unembSiblingData);

		((MossWizard) getWizard()).getModeTable().put("unembSibling",
				new Integer(DEFAULT));

		unembSibling.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = unembSibling.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put(
							"unembedSibling", new Integer(UNEMBED));
				}
			}
		});
		
		// Divide in to one of the three parts on this page
		labelMiscell = new Label(container, SWT.NONE );
		labelMiscell.setText("Miscellaneous");
		Font font3 = new Font(container.getDisplay(), "Helvetica", 8, SWT.BOLD); 
		labelMiscell.setFont(font3);
		GridData labelMiscellData = new GridData();
		labelMiscellData.verticalIndent = 5;
		labelMiscellData.horizontalSpan = 2;
		labelMiscell.setLayoutData(labelMiscellData);
		// If verbose output is to be displayed
		final Button verbose = new Button(container, SWT.CHECK);
		verbose.setText("Verbose output");

		GridData verboseData = new GridData();
		verboseData.verticalIndent = 5;
		verboseData.horizontalSpan = 2;
		
		verbose.setLayoutData(verboseData);
		// Default value
		((MossWizard) getWizard()).getModeTable().put("verbose",
				new Integer(DEFAULT));

		verbose.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = verbose.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("verbose",
							new Integer(VERBOSE));
				} else {
					((MossWizard) getWizard()).getModeTable().put("verbose",
							new Integer(DEFAULT));
				}
			}
		});

		// If statistics is to be shown or not
		final Button stats = new Button(container, SWT.CHECK);
		stats.setText("Do not show statistics ");

		GridData statsData = new GridData();
		statsData.horizontalSpan = 2;
		stats.setLayoutData(statsData);
		// Default value
		((MossWizard) getWizard()).getModeTable().put("stats",
				new Integer(DEFAULT));

		stats.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = stats.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("stats",
							new Integer(NOSTATS));
				} else {
					((MossWizard) getWizard()).getModeTable().put("stats",
							new Integer(DEFAULT));
				}
			}});
			
	}

}



