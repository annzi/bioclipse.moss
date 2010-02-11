/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.moss.business;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import moss.Notation;
import moss.SMILES;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.moss.business.InputMolecule;
import net.bioclipse.moss.business.MossModel;
import net.bioclipse.moss.business.MossRunner;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;


public class MossManager implements IBioclipseManager {

	private static final Logger logger = Logger.getLogger(MossManager.class);

	/**
	 * Gives a short one word name of the manager used as variable name when
	 * scripting.
	 */
	public String getManagerName() {
		return "moss";
	}

	public int testa(int a){
		return a+10;
	}

	public void modd(String filename){
		File file = new File(filename);

	}

	public ArrayList<String> createArrayList(){
		return new ArrayList<String>();
	}


	public IFile saveMoss(IFile filename, List<ArrayList> a, IProgressMonitor monitor)
	throws BioclipseException, IOException {
		String s;

		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);


		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			for(int i = 0; i<a.size(); i++){
				s = i +",0," +(String) a.get(i).get(0) + "\n";
				byte but[]= s.getBytes();
				output.write(but); 
			}

			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing moss file.", e);
		} 

		monitor.worked(100);
		monitor.done();
		return filename;
	};

	public IFile saveMossOutputHelper(String file) throws BioclipseException, IOException{
		IFile ifile =ResourcePathTransformer.getInstance().transform(file);
		return saveMossOutput(ifile, null);
	}
	public IFile saveMossOutput(IFile filename, IProgressMonitor monitor)
	throws BioclipseException, IOException {
		
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);


		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing moss outfile.", e);
		} 

		monitor.worked(100);
		monitor.done();
		return filename;
	};
	
	public String init(String filename, String outfile,String outidfile) throws BioclipseException, IOException{
		IFile file = ResourcePathTransformer.getInstance().transform(filename);
		
		MossModel mossmodel = initFromSelection(file);
		IFile out= saveMossOutputHelper(outfile);
		IFile outid =saveMossOutputHelper(outidfile);
		MossRunner.runMoss(initFromSelection(file),out.getLocation()+"", outid.getLocation()+"" );
		return "Two output files has been created " ;
	}

	public MossModel initFromSelection(IFile filename) {

		MossModel mossmodel = new MossModel();
		InputMolecule imol= null;

		IPath path = filename.getLocation();

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
				imol = new InputMolecule(id, value,
						description);

				mossmodel.addMolecule(imol);
			}
		} catch (Exception e) {
			System.out.println(e); 
		}
		
		return mossmodel;

		

	}








}

