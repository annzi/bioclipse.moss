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

import java.io.File;

import net.bioclipse.moss.InputMolecule;
import net.bioclipse.moss.MossModel;
import net.bioclipse.moss.MossTestRunner;
import net.bioclipse.moss.wizards.MossWizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author ola
 *
 */
public class RunMossAction implements IObjectActionDelegate {

	private IWorkbenchPart part;
	private ISelection selection;
	private IResource parent;
	
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
	    int ret=dialog.open();		
	    if (ret!=0){
	    	//Cancel, kan vara 1 också, osäker
	    	return;
	    }
	    
	    //We pressed finish...
	    
	    final MossModel mossModel=wizard.getMossModel();
	    
		// Check if parameters are correct by printing them
		System.out.println("MossModel:");
		System.out.println("Max support: " + mossModel.getMaximalsupport());
		System.out.println("Min support: " + mossModel.getMinimalSupport());
		System.out.println("Threshold: " + mossModel.getThreshold());
		System.out.println("Invert split: " + mossModel.getSplit());
		System.out.println("closed: " + mossModel.getClosed());
		System.out.println("ExNode: " + mossModel.getExNode());
		System.out.println("ExSeed: " + mossModel.getExSeed());
		System.out.println("ExSeed: " + mossModel.getSeed());
		System.out.println("MaxEmb: " + mossModel.getMaxEmbed());
		System.out.println("MinEmb: " + mossModel.getMinEmbed());
		System.out.println("MBond: " + mossModel.getMbond());
		System.out.println("Mrgbd: " + mossModel.getMrgbd());
		System.out.println("Matom: " + mossModel.getMatom());
		System.out.println("Mrgat: " + mossModel.getMrgat());
		System.out.println("Max Ring: " + mossModel.getMaxRing());
		System.out.println("Min Ring: " + mossModel.getMinRing());
		System.out.println("Mode: " + mossModel.getMode());
		System.out.println("MaxEmbMemory: " + mossModel.getMaxEmbMemory());
		
		
		for (InputMolecule mol : mossModel.getInputMolecules()) {
			System.out.println("Molecule: " + mol.getId() + " include: "
					+ mol.isChecked());
		}

		String path = "";
		parent = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IResource res = (IResource) obj;
				parent = res.getParent();
				path = parent.getLocation().toOSString();
			}
		}

		final String outputFileName = path + File.separator + "MossOutput.txt";
		System.out.println("Output file name: " + outputFileName);
		

		final String outputFileNameId = path + File.separator + "MossOutputId.txt";
		System.out.println("Id output file name: " + outputFileNameId);

		
		//We now have all info we need. Start Moss in a new Job
		//TODO
		
		Job job=new Job("Moss"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Calling Moss", 4);
				
				monitor.worked(2);
				
				MossTestRunner.runMoss(mossModel, outputFileName, outputFileNameId);

				monitor.done();

				Display.getDefault().asyncExec( new Runnable() {
				    public void run() {
						try {
							parent.refreshLocal(IResource.DEPTH_ONE, null);
						} catch (CoreException e) {
							e.printStackTrace();
						}
//				        viewer.refresh(refreshFrom.getParent (), false);
				    }
				} );
				

				return Status.OK_STATUS;
			}
		};
		
		job.setUser(true);
		job.schedule();

		

	    
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection=selection;
	}

}
