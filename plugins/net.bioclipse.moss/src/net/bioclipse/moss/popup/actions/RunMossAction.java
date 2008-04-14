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
import net.bioclipse.moss.MossRunner;
import net.bioclipse.moss.wizards.MossWizard;

import org.apache.log4j.Logger;
import net.bioclipse.core.util.LogUtils;

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
 * @author Annsofie Andersson, Ola Spjuth
 * 
 */
public class RunMossAction implements IObjectActionDelegate {

    private static final Logger logger = Logger.getLogger(RunMossAction.class);
    
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
		part = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {

		// Check what the selection is and extract input molecules

		// Instantiate, initialize, and open the wizard

		MossWizard wizard = new MossWizard(selection);

		WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
				wizard);

		dialog.create();

		int ret = dialog.open();

		if (ret != 0) {
			return;
		}

		// We pressed finish...
		final MossModel mossModel = wizard.getMossModel();
//
//		// Check if parameters are correct by printing them
//		// TODO: Remove syso only checks if e given parameter is correctly
//		// checked
//		System.out.println("Max support: " + mossModel.getMaximalsupport());
//		System.out.println("Min support: " + mossModel.getMinimalSupport());
//		System.out.println("Threshold: " + mossModel.getThreshold());
//		System.out.println("Invert split: " + mossModel.getSplit());
//		System.out.println("closed: " + mossModel.getClosed());
//		System.out.println("ExNode: " + mossModel.getExNode());
//		System.out.println("ExSeed: " + mossModel.getExSeed());
//		System.out.println("ExSeed: " + mossModel.getSeed());
//		System.out.println("MaxEmb: " + mossModel.getMaxEmbed());
//		System.out.println("MinEmb: " + mossModel.getMinEmbed());
//		System.out.println("MBond: " + mossModel.getMbond());
//		System.out.println("Mrgbd: " + mossModel.getMrgbd());
//		System.out.println("Matom: " + mossModel.getMatom());
//		System.out.println("Mrgat: " + mossModel.getMrgat());
//		System.out.println("Max Ring: " + mossModel.getMaxRing());
//		System.out.println("Min Ring: " + mossModel.getMinRing());
//		System.out.println("Mode: " + mossModel.getMode());
//		System.out.println("MaxEmbMemory: " + mossModel.getMaxEmbMemory());
//		System.out.println("test" + mossModel.getTest());
//		// TODO: Remove
//		for (InputMolecule mol : mossModel.getInputMolecules()) {
//			System.out.println("Molecule: " + mol.getId() + " include: "
//					+ mol.isChecked());
//		}

		String path2 = "";
		parent = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();

			if (obj instanceof IResource) {
				IResource res = (IResource) obj;
				parent = res.getParent();
				path2 = parent.getLocation().toOSString();
			}
		}

		// Gives a default outputFile directory but if a path is given
		// output will be printed there instead.
		String fileName, fileNameId;
		String path1 = mossModel.getPath();
		String pathId = mossModel.getPathId();
		String namefile = mossModel.getNamefile();
		String namefileid = mossModel.getNamefileId();
		
		// Finding path for outputFileName
		if (path1 != null) {
			fileName = path1;
		} else if (namefile != null) {
			fileName = path2 + File.separator + namefile;
		} else {
			fileName = path2 + File.separator + "MossOutput.txt";
		}
		// Finding path for outputFileNameId
		if (path1 != null) {
			fileNameId = pathId;
		} else if (namefile != null) {
			fileNameId = path2 + File.separator + namefileid;
		} else {
			fileNameId = path2 + File.separator + "MossOutputId.txt";
		}

		
		//		if (pathId != null) {
//			fileNameId = pathId;
//
//		} else {
//			fileNameId = path2 + File.separator + "MossOutputId.txt";
//		}

		// Set the final outputFileName(Id) paths
		final String outputFileName = fileName;
		final String outputFileNameId = fileNameId;

		// We now have all info we need. Start Moss in a new Job

		Job job = new Job("Moss") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Calling Moss", 4);

				monitor.worked(2);

				MossRunner.runMoss(mossModel, outputFileName,
						outputFileNameId);

				monitor.done();

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							parent.refreshLocal(IResource.DEPTH_ONE, null);
						} catch (CoreException e) {
						    LogUtils.debugTrace(logger, e);
						}
						// viewer.refresh(refreshFrom.getParent (), false);
					}
				});

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
		this.selection = selection;
	}

}
