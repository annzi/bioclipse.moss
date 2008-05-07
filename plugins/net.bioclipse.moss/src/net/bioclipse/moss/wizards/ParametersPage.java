/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/

package net.bioclipse.moss.wizards;

import java.io.StringReader;
import java.util.HashMap;

import moss.SMILES;
import moss.Notation;
import moss.Extension;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.PlatformUI;

public class ParametersPage extends WizardPage {
    private Label labelexNode, labelexSeed, labelMaxEmb, labelMinEmb;
    private Text exSeed, exNode;

    // Constants and flags. Imported directly from MOSS
    // flag for extensions by single edges
    public static final int EDGEEXT = Extension.EDGE;
    // flag for restriction to closed fragments
    public static final int CLOSED = 0x000020;
    // flag for full perfect extension pruning
    public static final int PR_PERFECT = 0x000800;
    // flag for canonical form pruning
    public static final int PR_CANONIC = 0x002000;
    // default search mode flags: edge extensions, canonical form and full
    // perfect extension pruning
    public static final int DEFAULT = EDGEEXT | CLOSED | PR_CANONIC
            | PR_PERFECT;

    private HashMap<Text, String> errors = new HashMap<Text, String>();

    /**
     * Create the wizard
     * 
     */
    public ParametersPage() {
        super("Moss Parameters");
        setTitle("Moss Parameters");
        setDescription("Please enter parameters for Moss");
    }

    /**
     * Create different methods needed for this page
     */
    // Display help when button pushed
    // TODO: Individualize help- should be able to open help for this page
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp();
    }

    // Update buttons if errors occurs/get removed
    protected void check() {
        String errorLine = "";
        // Return this
        String totalErrors = "";

        setErrorMessage(null);

        // Check for errors
        for (Text txt : errors.keySet()) {
            if (errors.get(txt) != null) {
                errorLine = errorLine + errors.get(txt);
            }
        }
        if (errorLine.length() > 1) {
            totalErrors = totalErrors + errorLine + "\n";
        }
        if (totalErrors.compareTo("") == 0) {
            setErrorMessage(null);
            setPageComplete(true);
        } else {
            setErrorMessage(errorLine);
            setPageComplete(false);
        }
        ((MossWizard) getWizard()).getContainer().updateButtons();
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

        // Set the parameter "Minimum support in focus"
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
                        errors
                                .put(txt,
                                        "Minimal support in focus must be set as a number");
                        check();
                        return;
                    }
                    check();
                }
            }
        });
        // Set the parameter for "Maximum support in complement"
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
                        errors
                                .put(txt,
                                        "Maximal support in complement must be set as a number");
                        check();
                        return;
                    }
                    check();
                }
            }
        });
        // Set "Threshold" for split
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

                        ((MossWizard) getWizard()).getMossModel().setThreshold(
                                d);
                    } catch (NumberFormatException e1) {
                        errors.put(txt, "Threshold must be set as a number");
                        check();
                        return;
                    }
                    check();
                }
            }
        });
        // Set "Invert split"
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
        // Set the value of "Minimal value of embedding"
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

        // Set the value of "Maximum value of embedding"
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

        // Exclude atoms and molecules from the mining
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
                ((MossWizard) getWizard()).getContainer().updateButtons();
                if (e.getSource() instanceof Text) {
                    Text txt = (Text) e.getSource();
                    String value = txt.getText();

                    try {
                        errors.put(txt, null);
                        ((MossWizard) getWizard()).getContainer()
                                .updateButtons();
                        Notation ntn = new SMILES();
                        ntn.parse(new StringReader(value));

                        ((MossWizard) getWizard()).getMossModel().setExNode(
                                value);
                    } catch (Exception e1) {
                        errors.put(txt,
                                "Must be an atom that have support, see help");
                        check();
                        return;
                    }
                    check();
                }
            }
        });

        // Exclude atoms and molecules to be set as seeds
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
                ((MossWizard) getWizard()).getContainer().updateButtons();
                if (e.getSource() instanceof Text) {
                    Text txt = (Text) e.getSource();
                    String value = txt.getText();

                    try {
                        errors.put(txt, null);
                        ((MossWizard) getWizard()).getContainer()
                                .updateButtons();

                        Notation ntn = new SMILES();
                        ntn.parse(new StringReader(value));

                        ((MossWizard) getWizard()).getMossModel().setExSeed(
                                value);
                    } catch (Exception e1) {
                        errors.put(txt,
                                "Must be an atom that have support, see help");
                        check();
                        return;
                    }
                    check();
                }
            }
        });
        // Set certain atom or molecule as seed
        Label labelSeed = new Label(container, SWT.NONE);
        labelSeed.setText("Set seed to begin from:");

        GridData labelSeedData = new GridData();
        labelSeed.setLayoutData(labelSeedData);

        Text seed = new Text(container, SWT.BORDER);

        GridData seedData = new GridData();
        seedData.widthHint = 50;
        seedData.heightHint = 13;
        seed.setLayoutData(seedData);

        seed.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                ((MossWizard) getWizard()).getContainer().updateButtons();
                if (e.getSource() instanceof Text) {
                    Text txt = (Text) e.getSource();
                    String value = txt.getText();

                    try {
                        errors.put(txt, null);
                        ((MossWizard) getWizard()).getContainer()
                                .updateButtons();

                        Notation ntn = new SMILES();
                        ntn.parse(new StringReader(value));

                        ((MossWizard) getWizard()).getMossModel()
                                .setSeed(value);
                    } catch (Exception e1) {
                        errors.put(txt,
                                "Must be an atom that have support, see help");
                        check();
                        return;
                    }
                    check();
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
}
