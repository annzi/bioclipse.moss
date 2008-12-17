/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     Annzi
 *     
 ******************************************************************************/
package net.bioclipse.moss.wizards;
import net.bioclipse.moss.InputMolecule;
import net.bioclipse.moss.MossModel;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
/**
 * A class for the first page in wizard, sets its contents and collects input
 * file and displays it
 * 
 * @author Annsofie Andersson
 * 
 */
public class InputPage extends WizardPage {
    private IWorkbenchPart part;
    private CheckboxTableViewer tableViewer;
    private Table table;
    private TableColumn column1, column2, column3;
    private String[] colnames = { "id", "value", "description" };
    /**
     * Create the wizard
     */
    public InputPage() {
        super("Moss Input");
        setTitle("Moss input");
        setDescription("Please select and review molecules and directories(optional) for Moss  ");
    }
    // Display help when button pushed
    // TODO: Individualize help- should be able to open help for this page
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp();
    }
    /**
     * Create contents of the wizard
     * 
     * @param parent
     */
    public void createControl(final Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        setControl(container);
        GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        container.setLayout(gl);
        // Created table and a viewer so input file can be displayed
        table = new Table(container, SWT.CHECK);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData tableData = new GridData();
        tableData.horizontalAlignment = GridData.FILL;
        tableData.verticalAlignment = GridData.FILL;
        tableData.grabExcessHorizontalSpace = true;
        tableData.grabExcessVerticalSpace = true;
        tableData.widthHint = 300;
        tableData.heightHint = 200;
        tableData.horizontalSpan = 2;
        table.setLayoutData(tableData);
        tableViewer = new CheckboxTableViewer(table);
        tableViewer.setContentProvider(new ViewContentProvider());
        tableViewer.setLabelProvider(new ViewLabelProvider());
        // Add columns to table id, value, and description(in SMILES)
        column1 = new TableColumn(table, SWT.NONE);
        column1.setText(colnames[0]);
        column1.setWidth(50);
        column2 = new TableColumn(table, SWT.NONE);
        column2.setText(colnames[1]);
        column2.setWidth(100);
        column3 = new TableColumn(table, SWT.NONE);
        column3.setText(colnames[2]);
        column3.setWidth(1000);
        try {
            tableViewer.setInput(((MossWizard) getWizard()).getMossModel());
        } catch (Exception e) {
            return;
        }
        tableViewer.setAllChecked(true);
        // A molecule can get unselected this method checks whether or not the
        // molecule is to be treated or not
        tableViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object obj = event.getElement();
                boolean status = event.getChecked();
                if (obj instanceof InputMolecule) {
                    InputMolecule imol = (InputMolecule) obj;
                    imol.setChecked(status);
                }
            }
        });
        // Output directory settings
        Label label = new Label(container, SWT.NONE);
        label.setText("Workspace directory for output file");
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        label.setLayoutData(labelData);
        final Text txtoutputFile = new Text(container, SWT.BORDER);
        txtoutputFile.setText("MossOutput.txt");
        txtoutputFile.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                ((MossWizard) getWizard()).getContainer().updateButtons();
                if (e.getSource() instanceof Text) {
                    Text txt = (Text) e.getSource();
                    String value = txt.getText();
                    ((MossWizard) getWizard()).getFileTable().put("outputName",
                            value);
                }
            }
        });
        GridData outputFileData = new GridData(GridData.FILL_HORIZONTAL);
        outputFileData.verticalAlignment = 2;
        txtoutputFile.setLayoutData(outputFileData);
        Button browse = new Button(container, SWT.PUSH);
        browse.setText("Browse...");
        GridData browseData = new GridData();
        browse.setLayoutData(browseData);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dial = new FileDialog(getShell());
                dial.setFilterPath(Platform.getLocation().toOSString());
                // dial.setFilterPath(txtoutputFile.getText());
                dial.setText("MoSS file directory");
                String dir = dial.open();
                if (dir != null) {
                    txtoutputFile.setText(dir);
                } else {
                    return;
                }
                ((MossWizard) getWizard()).getFileTable().put("output", dir);
            }
        });
        // Identifier(id) output directory settings
        Label labelId = new Label(container, SWT.NONE);
        labelId.setText("Workspace directory for identifier output file");
        GridData labelIdData = new GridData();
        labelIdData.horizontalSpan = 2;
        labelId.setLayoutData(labelIdData);
        final Text txtIdOutputFile = new Text(container, SWT.BORDER);
        txtIdOutputFile.setText("MossOutputId.txt");
        txtIdOutputFile.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                ((MossWizard) getWizard()).getContainer().updateButtons();
                if (e.getSource() instanceof Text) {
                    Text txt = (Text) e.getSource();
                    String value = txt.getText();
                    ((MossWizard) getWizard()).getFileTable().put(
                            "outputNameId", value);
                }
            }
        });
        GridData idOutputFileData = new GridData(GridData.FILL_HORIZONTAL);
        idOutputFileData.verticalAlignment = 2;
        txtIdOutputFile.setLayoutData(idOutputFileData);
        Button browseId = new Button(container, SWT.PUSH);
        browseId.setText("Browse...");
        GridData browseIdData = new GridData();
        browseId.setLayoutData(browseIdData);
        // final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
        browseId.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // ResourceSelectionDialog dialId = new
                // ResourceSelectionDialog(getShell(), root,null );
                // dialId.open();
                // dialId.
                // dialId.setInitialSelections(selectedResources);
                // dialId.setFilterPath(txtIdOutputFile.getText());
                FileDialog dialId = new FileDialog(getShell());
                dialId.setFilterPath(Platform.getLocation().toOSString());
                dialId.setText("MoSS file directory");
                String dirId = dialId.open();
                if (dirId != null) {
                    txtIdOutputFile.setText(dirId);
                } else {
                    return;
                }
                ((MossWizard) getWizard()).getFileTable()
                        .put("outputId", dirId);
            }
        });
    }
    class ViewContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
        public Object[] getElements(Object parent) {
            if (parent instanceof MossModel) {
                MossModel model = (MossModel) parent;
                return model.getInputMolecules().toArray(
                        new InputMolecule[model.getInputMolecules().size()]);
            }
            return new String[] { "??" };
        }
    }
    // Get index, value and description from input file
    class ViewLabelProvider extends LabelProvider implements
            ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            if (!(obj instanceof InputMolecule))
                return "";
            InputMolecule mol = (InputMolecule) obj;
            if (index == 0)
                return mol.getId();
            if (index == 1)
                return String.valueOf(mol.getValue());
            if (index == 2)
                return mol.getDescription();
            return "??";
        }
        public Image getColumnImage(Object obj, int index) {
            return null;
        }
        @Override
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_ELEMENT);
        }
    }
}