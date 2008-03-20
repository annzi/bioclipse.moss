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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.PlatformUI;

public class ParametersPage2 extends WizardPage {

	/***************************************************************************
	 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
	 * This program and the accompanying materials are made available under the
	 * terms of the Eclipse Public License v1.0 which accompanies this
	 * distribution, and is available at www.eclipse.orgÑepl-v10.html
	 * <http://www.eclipse.org/legal/epl-v10.html>
	 * 
	 * Contributors: Annzi - initial API and implementation
	 * 
	 **************************************************************************/
	private Label labelMatch, labelRC;
	private Label labelmaxRSize, labelminRSize;
	private Label labelRing;
	private Label labelIgnBond, labelIgnAtom;
	private Label labelExtension;

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
	/** flag for full perfect extension pruning */
	public static final int PR_PERFECT = 0x000800;
	/** flag for equivalent sibling extension pruning */
	public static final int PR_EQUIV = 0x001000;
	/** flag for canonical form pruning */
	public static final int PR_CANONIC = 0x002000;
	/** flag for converting Kekul&eacute; representations */
	public static final int AROMATIZE = 0x020000;
	/** default search mode flags: edge extensions,
	 *  canonical form and full perfect extension pruning */
	public static final int DEFAULT = EDGEEXT | CLOSED | PR_CANONIC
			| PR_PERFECT;

	/**
	 * Create the wizard
	 */
	public ParametersPage2() {
		super("Moss Parameters");
		setTitle("Moss Parameters");
		setDescription("Please enter parameters for Moss");
	}
	// Display help when button pushed
	public void performHelp(){
		  PlatformUI.getWorkbench().getHelpSystem().displayHelp();
		  System.out.println("parameterspage2 help");
		}
	
	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.RIGHT);
		setControl(container);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		container.setLayout(gl);
		
		// Label to divide into bond and atom part
		labelMatch = new Label(container,SWT.NONE);
		labelMatch.setText("Matching of bond and atom types");
		GridData labelMatchData = new GridData();
		labelMatchData.verticalIndent = 5;
		labelMatchData.horizontalSpan = 2;
		labelMatch.setLayoutData(labelMatchData);
		Font font1 = new Font(container.getDisplay(), "Helvetica", 8, SWT.BOLD); 
		labelMatch.setFont(font1);
		
		// Will decided in what way to treat bonds in aromatic ring
		labelRing = new Label(container, SWT.NONE);
		labelRing.setText("How to treat aromatic bonds:");

		final Combo ring = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		String[] items = { "aromatize bonds", "upgrade bonds",
				"downgrade bonds" };
		ring.setItems(items);
		ring.setText("aromatize bonds");

		// Set default parameters
		((MossWizard) getWizard()).getBondsTable().put("mbond1",
				new Integer(Bonds.BONDMASK));
		((MossWizard) getWizard()).getBondsTable().put("mrgbd1",
				new Integer(Bonds.BONDMASK));

		ring.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				String selected = ring.getItem(ring.getSelectionIndex());

				// "aromatize bonds" is a default setting but if it is selected
				// after an other parameter is selected it has to be set again
				if (selected.equals(ring.getItem(0))) {
					((MossWizard) getWizard()).getBondsTable().put("mbond1",
							new Integer(Bonds.BONDMASK));
					((MossWizard) getWizard()).getBondsTable().put("mrgbd1",
							new Integer(Bonds.BONDMASK));
				}
				// If upgrade bonds is selected &
				if (selected.equals(ring.getItem(1))) {
					((MossWizard) getWizard()).getBondsTable().put("mbond1",
							new Integer(Bonds.UPGRADE));
					((MossWizard) getWizard()).getBondsTable().put("mrgbd1",
							new Integer(Bonds.UPGRADE));
				}
				// If downgrade bonds is selected &
				if (selected.equals(ring.getItem(2))) {
					((MossWizard) getWizard()).getBondsTable().put("mbond1",
							new Integer(Bonds.DOWNGRADE));
					((MossWizard) getWizard()).getBondsTable().put("mrgbd1",
							new Integer(Bonds.DOWNGRADE));

				}
			}
		});

		// Will set whether or not to ignore bonds in molecules
		labelIgnBond = new Label(container, SWT.NONE);
		labelIgnBond.setText("Ignore type of bonds:");

		final Combo bond = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		String[] bondItems = { "never", "always", "in rings" };
		bond.setItems(bondItems);
		bond.setText("never");

		((MossWizard) getWizard()).getBondsTable().put("mbond2",
				new Integer(Bonds.BONDMASK));
		((MossWizard) getWizard()).getBondsTable().put("mrgbd2",
				new Integer(Bonds.BONDMASK));

		bond.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String selected = bond.getItem(bond.getSelectionIndex());

				// "never" is a default setting but if it is selected after an
				// other parameter is selected it has to be set again TODO:
				// check whether BONDMASK is a correct setting
				if (selected.equals(bond.getItem(0))) {
					((MossWizard) getWizard()).getBondsTable().put("mbond2",
							new Integer(Bonds.BONDMASK));
					((MossWizard) getWizard()).getBondsTable().put("mrgbd2",
							new Integer(Bonds.BONDMASK));
				}
				// If to always ignore bond types 
				if (selected.equals(bond.getItem(1))) {
					((MossWizard) getWizard()).getBondsTable().put("mbond2",
							new Integer(Bonds.SAMETYPE));
					((MossWizard) getWizard()).getBondsTable().put("mrgbd2",
							new Integer(Bonds.SAMETYPE));
				}
				// If to ignore bond types in only rings 
				if (selected.equals(bond.getItem(2))) {
					((MossWizard) getWizard()).getBondsTable().put("mrgbd2",
							new Integer(Bonds.SAMETYPE));
				}
			}
		});

		// Will set whether or not to ignore atoms in molecules
		labelIgnAtom = new Label(container, SWT.NONE);
		labelIgnAtom.setText("Ignore type of atoms:");

		final Combo atom = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		String[] atomItems = { "never", "always", "in rings" };
		atom.setItems(atomItems);
		atom.setText("never");

		((MossWizard) getWizard()).getAtomsTable().put("matom1",
				new Integer(Atoms.ELEMMASK));
		((MossWizard) getWizard()).getAtomsTable().put("mrgat1",
				new Integer(Atoms.ELEMMASK));

		atom.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String selected = atom.getItem(atom.getSelectionIndex());

				// If to never ignore bond is also a default setting but if it
				// is selected the value needs to be stored
				if (selected.equals(atom.getItem(0))) {
					((MossWizard) getWizard()).getAtomsTable().put("matom1",
							new Integer(Atoms.ELEMMASK));
					((MossWizard) getWizard()).getAtomsTable().put("mrgat1",
							new Integer(Atoms.ELEMMASK));

				}
				// If to always ignore atoms
				if (selected.equals(atom.getItem(1))) {
					((MossWizard) getWizard()).getAtomsTable().put("matom1",
							new Integer(~Atoms.ELEMMASK));
					((MossWizard) getWizard()).getAtomsTable().put("mrgat1",
							new Integer(~Atoms.ELEMMASK));
				}
				// If to ignore atom in rings
				if (selected.equals(atom.getItem(2))) {
					((MossWizard) getWizard()).getAtomsTable().put("mrgat1",
							new Integer(~Atoms.ELEMMASK));//|
				}

			}
		});
		// If to match charge in atoms
		final Button chargeAtom = new Button(container, SWT.CHECK);
		chargeAtom.setText("Match charge of atoms");

		GridData chargeAtomData = new GridData();
		chargeAtomData.verticalSpan = 3;
		chargeAtomData.horizontalSpan = 5;
		chargeAtomData.verticalAlignment = GridData.END;
		chargeAtom.setLayoutData(chargeAtomData);

		((MossWizard) getWizard()).getAtomsTable().put("matom2",
				new Integer(Atoms.ELEMMASK));

		chargeAtom.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Boolean selected = chargeAtom.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getAtomsTable().put("matom2",
							new Integer(Atoms.CHARGEMASK));//|
				} else {
					((MossWizard) getWizard()).getAtomsTable().put("matom2",
							new Integer(Atoms.ELEMMASK));
				}
			}
		});
		// If to match aromaticity in bonds
		final Button aromAtom = new Button(container, SWT.CHECK);
		aromAtom.setText("Match aromaticity of atoms");

		GridData aromAtomData = new GridData();
		aromAtomData.verticalSpan = 5;
		//		aromAtomData.horizontalSpan = 5;
		aromAtomData.verticalAlignment = GridData.END;
		aromAtom.setLayoutData(aromAtomData);

		((MossWizard) getWizard()).getAtomsTable().put("matom3",
				new Integer(Atoms.ELEMMASK));

		aromAtom.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Boolean selected = chargeAtom.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getAtomsTable().put("matom3",
							new Integer(Atoms.AROMATIC));//|
				} else {
					((MossWizard) getWizard()).getAtomsTable().put("matom3",
							new Integer(Atoms.ELEMMASK));
				}

			}
		});

		// Distinguish between bonds set minimum and maximum ring size
		// If this checkbox is clicked then three combos will appear to set

		// Label to divide into rings and chain part
		labelRC = new Label(container,SWT.NONE);
		labelRC.setText("Rings and chains");
		GridData labelRCData = new GridData();
		labelRCData.verticalIndent = 5;
		labelRCData.horizontalSpan = 2;
		labelRC.setLayoutData(labelRCData);
		Font font2 = new Font(container.getDisplay(), "Helvetica", 8, SWT.BOLD); 
		labelRC.setFont(font2);
		
		// their values
		final Button click = new Button(container, SWT.CHECK);

		GridData clickData = new GridData();
		clickData.verticalSpan = 7;
		clickData.horizontalSpan = 7;
		clickData.verticalAlignment = GridData.END;
		click.setLayoutData(clickData);
		click.setText("If to dinstingush between bonds in rings:");

		// Set minimum size in rings
		labelminRSize = new Label(container, SWT.NONE);

		GridData labelminRSizeLData = new GridData();
		labelminRSizeLData.verticalSpan = 1;
		labelminRSize.setLayoutData(labelminRSizeLData);
		labelminRSize.setText("Minimum ring size:");
		labelminRSize.setEnabled(false);
		final Spinner minRing = new Spinner(container, SWT.RIGHT | SWT.BORDER);

		GridData minRingData = new GridData();
		minRingData.widthHint = 44;
		minRingData.heightHint = 14;
		minRingData.verticalSpan = 1;
		minRing.setLayoutData(minRingData);
		minRing.setSelection(5);
		minRing.setEnabled(false);
		minRing.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {

					int selected = minRing.getSelection();
					((MossWizard) getWizard()).getMossModel().setMinRing(selected);
					}
				});

		// Set maximum size in rings
		labelmaxRSize = new Label(container, SWT.NONE);
		labelmaxRSize.setText("Maximum ring size:");
		labelmaxRSize.setEnabled(false);
		
		GridData labelmaxRSizeLData = new GridData();
		labelmaxRSizeLData.verticalSpan = 1;
		labelmaxRSize.setLayoutData(labelmaxRSizeLData);

		final Spinner maxRing = new Spinner(container, SWT.RIGHT | SWT.BORDER);
		maxRing.setSelection(6);
		maxRing.setEnabled(false);

		GridData maxRingData = new GridData();
		maxRingData.widthHint = 44;
		maxRingData.heightHint = 14;
		maxRingData.verticalSpan = 1;
		maxRing.setLayoutData(maxRingData);

		maxRing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){

				int selected = maxRing.getSelection();
				((MossWizard) getWizard()).getMossModel().setMaxRing(selected);
					
				}
		});

		// Ring extension parameters
		labelExtension = new Label(container, SWT.NONE);
		labelExtension.setText("Ring extension");
		labelExtension.setEnabled(false);

		final Combo extension = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		String[] extensionItems = { "none", "full", "merge", "filter" };
		extension.setItems(extensionItems);
		extension.setText("none");
		extension.setEnabled(false);

		((MossWizard) getWizard()).getModeTable().put("ext1",
				new Integer(DEFAULT));
		extension.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				String selected = extension.getItem(extension
						.getSelectionIndex());

				if (selected.equals(extension.getItem(0))) {
					((MossWizard) getWizard()).getModeTable().put("ext1",
							new Integer(DEFAULT));
				}
				if (selected.equals(extension.getItem(1))) {
					((MossWizard) getWizard()).getModeTable().put("ext1",
							new Integer(RINGEXT));
				}
				if (selected.equals(extension.getItem(2))) {
					((MossWizard) getWizard()).getModeTable().put("ext1",
							new Integer(MERGERINGS));
					((MossWizard) getWizard()).getModeTable().put("ext2",
							new Integer(RINGEXT));
					((MossWizard) getWizard()).getModeTable().put("ext3",
							new Integer(CLOSERINGS));
					((MossWizard) getWizard()).getModeTable().put("ext4",
							new Integer(PR_UNCLOSE));
				}
				if (selected.equals(extension.getItem(3))) {
					((MossWizard) getWizard()).getModeTable().put("ext1",
							new Integer(CLOSERINGS));
					((MossWizard) getWizard()).getModeTable().put("ext2",
							new Integer(PR_UNCLOSE));
				}
			}
		});
		
		
		click.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = click.getSelection();
				int minRingSelected = minRing.getSelection();
				int maxRingSelected = maxRing.getSelection();
				
				if (selected == true) {
					labelminRSize.setEnabled(true);
					minRing.setEnabled(true);
					labelmaxRSize.setEnabled(true);
					maxRing.setEnabled(true);
					labelExtension.setEnabled(true);
					extension.setEnabled(true);
					((MossWizard) getWizard()).getMossModel().setMinRing(minRingSelected);
					((MossWizard) getWizard()).getMossModel().setMaxRing(maxRingSelected);
				}
				if (selected == false) {
					labelminRSize.setEnabled(false);
					minRing.setEnabled(false);
					labelmaxRSize.setEnabled(false);
					labelExtension.setEnabled(false);
					maxRing.setEnabled(false);
					extension.setEnabled(false);
					
					((MossWizard) getWizard()).getModeTable().put("ext1",
							new Integer(DEFAULT));
					((MossWizard) getWizard()).getMossModel().setMaxRing(0);// TODO fix null value or something like that
					((MossWizard) getWizard()).getMossModel().setMinRing(0);

				}

			}
		});
		// Convert to aromatic rings 
		final Button kekule = new Button(container, SWT.CHECK);
		kekule.setText("Convert from Kekulé representation to aromatic bonds");
		kekule.setSelection(true);

		((MossWizard) getWizard()).getModeTable().put("kekule",
				new Integer(DEFAULT));
		GridData kekuleData = new GridData();
		kekuleData.verticalIndent = 5;
		kekule.setLayoutData(kekuleData);
	
		kekule.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = kekule.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("kekule",
							new Integer(DEFAULT));
				} else {
					((MossWizard) getWizard()).getModeTable().put("kekule",
							new Integer(AROMATIZE));
				}

			}
		});
		// 
		final Button chain= new Button(container, SWT.CHECK);
		chain.setText("Variable length in carbon chain");

		GridData chainData = new GridData();
		chainData.verticalIndent = 10;
		chainData.horizontalSpan = 2;
		chain.setLayoutData(chainData);

		((MossWizard) getWizard()).getModeTable().put("chain",
				new Integer(DEFAULT));

		chain.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = chain.getSelection();

				if (selected == true) {
					((MossWizard) getWizard()).getModeTable().put("chain",
							new Integer(CHAINEXT));
				} 
				if (selected == false) {
					((MossWizard) getWizard()).getModeTable().put("chain",
							new Integer(DEFAULT));
				} 
			}
		});
		
	}
}

