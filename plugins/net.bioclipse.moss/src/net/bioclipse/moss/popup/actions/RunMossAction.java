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
package net.bioclipse.moss.popup.actions;

import net.bioclipse.moss.wizards.MossWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author ola
 *
 */
public class RunMossAction implements IObjectActionDelegate {

	IWorkbenchPart part;
	ISelection selection;
	
	/**
	 * Constructor for Action1.
	 */
	public RunMossAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part=targetPart;
	}

	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		//Check what the selection is and extract input molecules
		
		
	    // Instantiate, initialize, and open the wizard
	    MossWizard wizard = new MossWizard(selection);
	    WizardDialog dialog = new WizardDialog(part.getSite().getShell(), wizard);
	    dialog.create();
	    dialog.open();		
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection=selection;
	}

}
