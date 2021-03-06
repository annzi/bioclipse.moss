/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/

package net.bioclipse.moss.wizards;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import moss.Atoms;
import moss.Bonds;
import moss.Extension;
import moss.Notation;
import moss.SMILES;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.moss.InputMolecule;
import net.bioclipse.moss.MossModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/** Class for creating wizard and performFinish()
 * 
 * @author Annsofie Andersson
 * 
 */
public class MossWizard extends Wizard implements IAdaptable {

    ISelection selection;
    InputPage page1;
    ParametersPage page2;
    ParametersPage2 page3;
    ParametersPage3 page4;

    MossModel mossModel;

    // Taken directly from the original MoSS nothing is modified not even
    // comments
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
    /**
     * default search mode flags: edge extensions, canonical form and full
     * perfect extension pruning
     */
    public static final int DEFAULT = EDGEEXT | CLOSED | PR_CANONIC
            | PR_PERFECT;

    // These tables are for storing temporary data that gets collected and set
    // to mossModel. The data is flags
    Hashtable<String, Integer> bondsTable = new Hashtable<String, Integer>();
    Hashtable<String, Integer> atomsTable = new Hashtable<String, Integer>();
    Hashtable<String, Integer> modeTable = new Hashtable<String, Integer>();
    Hashtable<String, Integer> typeTable = new Hashtable<String, Integer>();
    Hashtable<String, String> fileTable = new Hashtable<String, String>();

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

    // Help button will be shown on all pages
    public boolean isHelpAvailable() {
        return true;
    }

    // To be able to show error if MoSS does not support the input file
    public void showMessage(String title, String message) {
        MessageDialog.openWarning(getShell(), title, message);
    }

    public boolean performFinish() {

        /*
         * Wrap up everything and finalize choices in MossModel Get values from
         * has tables and perform setMbond, setMrgbd, setMatom, setMrgat and
         * setMode
         */

        /*
         * Get the values for bond and mrgbd from bondsTable and collect the
         * parameters to one variable each and then set its value to mossModel
         */

        Integer b1 = (Integer) bondsTable.get("mbond1");
        Integer b2 = (Integer) bondsTable.get("mbond2");
        Integer mb1 = (Integer) bondsTable.get("mrgbd1");
        Integer mb2 = (Integer) bondsTable.get("mrgbd2");

        // Initialize B and Mb with default values
        int B = Bonds.BONDMASK;
        int Mb = Bonds.BONDMASK;

        // Select whether to upgrade or down grade, if b1=Bonds.BONMASK nothing
        // needs to be done
        if (b1 == Bonds.UPGRADE) {
            B &= b1;
            Mb &= mb1;
        }
        if (b1 == Bonds.DOWNGRADE) {
            B &= b1;
            Mb &= mb1;
        }
        // Select whether to ignore bonds or not
        if (b2 == Bonds.SAMETYPE && mb2 == Bonds.SAMETYPE) {
            B &= b2;
            Mb &= mb2;
        } else {
            Mb &= mb2;
        }
        // If you like to check the value of flags for atomsTable and bondsTable
        // unmark
        // System.out.println("the table for bonds " + bondsTable);
        // System.out.println("the table for atoms " + atomsTable);

        /*
         * Get the values for bond and mrgat from atomsTable and collect the
         * parameters to one variable each and then set its value to mossModel
         */

        // Get values from hash table
        Integer a1 = (Integer) atomsTable.get("matom1");
        Integer a2 = (Integer) atomsTable.get("matom2");
        Integer a3 = (Integer) atomsTable.get("matom3");
        Integer ma = (Integer) atomsTable.get("mrgat1");

        // Initialize with default settings
        int A = Atoms.ELEMMASK;
        int Ma = Atoms.ELEMMASK;

        // Case: always ignore atom types
        if (a1 == ~Atoms.ELEMMASK && ma == ~Atoms.ELEMMASK) {
            A &= a1;
            Ma &= ma;
        }
        // Case: if to always ignore atom types in rings
        else {
            Ma &= ma;
        }

        // Case: if to match charge of atoms, by default one ignore matching
        if (a2 != Atoms.ELEMMASK) {
            A |= a2;
        }
        // Case: if to match aromaticity of atoms, by default one do not
        if (a3 != Atoms.ELEMMASK) {
            A |= a3;
        }

        // �f you like to check the initiail value of modesTable unmark
        // System.out.println("the table for mode " + modeTable);

        /*
         * Get the values for mode from modeTable and collect the parameters to
         * one variable and then set its value to mossModel
         */

        // Get all the current values in mode hash table, done in alphabetic
        // order
        Integer canonic = (Integer) modeTable.get("canonPruning");
        Integer chain = (Integer) modeTable.get("chain");
        Integer closed = (Integer) modeTable.get("closed");
        Integer equiv = (Integer) modeTable.get("eqPruning");
        Integer extension1 = (Integer) modeTable.get("ext1");
        Integer extension2 = (Integer) modeTable.get("ext2");
        Integer extension3 = (Integer) modeTable.get("ext3");
        Integer extension4 = (Integer) modeTable.get("ext4");
        Integer extprune1 = (Integer) modeTable.get("extPruning1");
        Integer extprune2 = (Integer) modeTable.get("extPruning2");
        Integer kekule = (Integer) modeTable.get("kekule");
        Integer stats = (Integer) modeTable.get("stats");
        Integer unembedSibling = (Integer) modeTable.get("unembSibling");

        // To be able to collect current values, if a default value is set that
        // value won't be necessary to count
        int totMode = DEFAULT;

        if (canonic != DEFAULT) {
            totMode &= canonic;
        }
        if (closed != DEFAULT) {
            totMode &= closed;
        }
        if (chain == CHAINEXT) {
            totMode |= chain;
        }
        if (kekule != DEFAULT) {
            totMode |= kekule;
        }
        if (stats != DEFAULT) {
            totMode |= stats;
        }
        // Removed since this options seems unnecessary to have, can easily be
        // added again its wizard
        // code is on parametersPage3
        // if (verbose != DEFAULT) {
        // totMode |= verbose;
        // }

        // Pruning parameters
        if (equiv == PR_EQUIV) {
            totMode |= equiv;
        }
        if (extprune1 == PR_PERFECT) {
            totMode |= extprune1;
            totMode &= extprune2;
        }
        if (extprune1 == PR_PARTIAL) {
            totMode |= extprune1;
            totMode &= extprune2;
        }
        // TODO: write which parameters this is
        if (extension1 == RINGEXT) {
            totMode |= extension1;
        }
        if (extension1 == MERGERINGS) {
            totMode |= extension2 | extension1 | extension3 | extension4;
        }
        if (extension1 == CLOSERINGS) {
            totMode |= extension1 | extension2;
        }
        if (unembedSibling == UNEMBED) {
            totMode |= unembedSibling;
        }
        // Get file paths or name for file
        String file = fileTable.get("output");
        String nameFile = fileTable.get("outputName");
        String fileId = fileTable.get("outputId");
        String nameFileId = fileTable.get("outputNameId");

        /* Adding values of variables to mossModel */
        mossModel.setMbond(B);
        mossModel.setMrgbd(Mb);
        mossModel.setMatom(A);
        mossModel.setMrgat(Ma);
        mossModel.setMode(totMode);
        mossModel.setPath(file);
        mossModel.setNamefile(nameFile);
        mossModel.setPathId(fileId);
        mossModel.setNamefileId(nameFileId);

        // Action continues

        return true;
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

                // If you like to check path of input file
                // System.out.println("Reading file: " + path.toOSString());

                // Read file line by line as
                // Typical line: a,0,CCCO
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

                        // Checks the molecules if they are written in SMILES
                        // and if they are correctly written
                        Notation ntn = new SMILES();
                        ntn.parse(new StringReader(description));

                        // Add molecules to mossModel
                        InputMolecule imol = new InputMolecule(id, value,
                                description);

                        mossModel.addMolecule(imol);
                    }
                } catch (Exception e) {
                    showMessage(
                            "MoSS Error",
                            " MoSS does not support this input file and will not display a correct file. \n The table "
                                    + " will display correct lines till the wrong input occurred. \n \n "
                                    + e);
                }

            }
            if (obj instanceof IMolecule) {
                // TODO: implement this in the future maybe
                return;
            }

        }

    }

    /** Create generators for getters and setters */

    public MossModel getMossModel() {
        return mossModel;
    }

    public void setMossModel(MossModel mossModel) {
        this.mossModel = mossModel;
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

    public HashMap<String, String> getTyTable() {
        // TODO Auto-generated method stub
        return null;
    }

    public Hashtable<String, String> getFileTable() {
        return fileTable;
    }

    public void setFileTable(Hashtable<String, String> fileTable) {
        this.fileTable = fileTable;
    }

    public Object getAdapter(Class adapter) {
        if (adapter.equals(IContextProvider.class)) {
            return new MossContextProvider();
        }
        return null;
    }

}
