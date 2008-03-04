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

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class InputPage extends WizardPage {

	private CheckboxTableViewer tableViewer;
	private Table table;
	private TableColumn column1, column2, column3;
	private String[] colnames = { "id", "value", "description" };
	private Image circle;
	MossWizard wizard;

	/**
	 * Create the wizard
	 */
	public InputPage() {
		super("Moss Input");
		setTitle("Moss input");
		setDescription("Please select and review molecules for Moss");

		wizard = (MossWizard) getWizard();

	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		{
			parent.setSize(493, 227);
		}

		wizard = (MossWizard) getWizard();

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		GridLayout gl = new GridLayout();
		container.setLayout(gl);

		// Created table and a viewer so input file can be displayed
		table = new Table(container, SWT.CHECK);
		tableViewer = new CheckboxTableViewer(table);

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

		GridData da = new GridData();
		da.grabExcessHorizontalSpace = true;
		da.grabExcessVerticalSpace = true;
		da.widthHint = 300;
		da.heightHint = 200;
		table.setLayoutData(da);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Add columns to table id, value, and description(in SMILES)
		column1 = new TableColumn(table, SWT.NONE);
		column1.setText(colnames[0]);
		column1.setWidth(50);
		column2 = new TableColumn(table, SWT.NONE);
		column2.setText(colnames[1]);
		column2.setWidth(100);
		column3 = new TableColumn(table, SWT.NONE);
		column3.setText(colnames[2]);
		column3.setWidth(167);

		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(wizard.getMossModel());
		tableViewer.setAllChecked(true);

		final Button help = new Button(container, SWT.PUSH);
//		circle = new Image(container.getDisplay(),"C:/Documents and Settings/Lenny/Skrivbord/Annzi/qmark.gif ");
//		help.setImage(circle);
		Font font = new Font(container.getDisplay(), "Helvetica", 10, SWT.BOLD); 
		help.setText("?");
		help.setFont(font);
		GridData helpData = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
//		helpData.verticalIndent = 50;
		help.setLayoutData(helpData);
		
		help.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = help.getSelection();
				if(selected == true){
				//TODO link to help site for bioclipse
				}
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

