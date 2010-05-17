/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
 * Author: Annsofie Andersson <annzi.andersson@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.moss.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.moss.business.backbone.MossBean;
import net.bioclipse.moss.business.backbone.MossModel;
import net.bioclipse.moss.business.backbone.MossRunner;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.bioclipse.rdf.Activator;
import net.bioclipse.rdf.model.IStringMatrix;
import net.bioclipse.rdf.business.IJavaRDFManager;
import net.bioclipse.rdf.business.IRDFManager;

public class MossManager implements IBioclipseManager {
	
	private IJavaRDFManager rdf = Activator.getDefault().getJavaManager();
	private static final Logger logger = Logger.getLogger(MossManager.class);
	
	/**A bean that sets the parameters
	 * Parameters: propertyName, Object values
	 * */
	MossBean mossbean = new MossBean();
	public String createParameters(String propertyName, Object value) throws Exception{
		//	if(propertyName.equals("matom")||propertyName.equals("mbond")||propertyName.equals("mrgat")||propertyName.equals("mrgbd")){
			//	return "Not allowed to set value to " + propertyName+ ", internal proerty.";
			//}
			//else{
				if(value.getClass().equals(Double.class)){
					value= ((Double) value).intValue();
					int values = (Integer) value;
					MossBean.setParameters(mossbean, propertyName, values);
				}else{
					MossBean.setParameters(mossbean, propertyName, value);	
				}
				return value  +" is set to " +propertyName;
			}
		//}	
	/**
	 * Gives a short one word name of the manager used as variable name when
	 * scripting.
	 */
	
	public String getManagerName() {
		return "moss";}
	
	
	public String parameterValues() throws Exception{
		ArrayList<String> name = mossbean.getPropertyNames(mossbean);
		String info="";
		String names;
		for(int i=0; i<name.size(); i++){
			names = name.get(i);
			if(names.equals("Limits")||names.equals("matom")||names.equals("mbond")||names.equals("mrgat")||names.equals("mrgbd")||names.equals("mode")){		
			}else {
			info= info + names +": " + MossBean.getProperty(mossbean, names)  + " \n";
			}
		}
		return info;
	}
	//Collects compounds from a protein family
	public IStringMatrix query(String fam, String actType, int limit) throws BioclipseException{
		
		String sparql =
		"PREFIX onto: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
		"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
		"SELECT ?smiles where{ " +
		"	?target a onto:Target." +
		"   ?target onto:classL5 ?fam. " + //+ " \"" + fam + "\"." +
		"	?assay onto:hasTarget ?target . ?activity onto:onAssay ?assay ." +
		" ?activity onto:standardValue ?st ." +
		"	?activity onto:type ?actType . " + //" \"" + actType + "\"."+ 
		"	?activity onto:forMolecule ?mol ."+
		"	?mol bo:smiles ?smiles.  " +
		"FILTER regex(?fam, " + "\"" + fam + "\"" + ", \"i\")."+
		"FILTER regex(?actType, " + "\"" + actType + "\"" + ", \"i\")."+
	    "}LIMIT "+ limit; 
		
		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql",sparql);
		return matrix;
	}

	public void run(String inputfile, String outfile,String outidfile) 
	throws BioclipseException, IOException,ScriptException{
		IFile in = ResourcePathTransformer.getInstance().transform(inputfile);
		IFile out= saveMossOutputHelper(outfile);
		IFile outid =saveMossOutputHelper(outidfile);
		MossModel mossmodel = new MossModel();
		//Add values to mossmodel. TODO Change MossModel to bean class?
		setMossModel(mossmodel);
		MossRunner.runMoss(mossmodel,in.getLocation().toOSString(), out.getLocation().toOSString(), outid.getLocation().toOSString());
	}


	public IFile saveInformation(IFile filename, List<ArrayList<String>> fam, IProgressMonitor monitor)
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
			for(int i = 0; i<fam.size(); i++){
				for(int j=0; j< fam.get(i).size(); j++){
					if(j == fam.get(i).size()-1){
						s = fam.get(i).get(j) + "\n ";}
					else {s = fam.get(i).get(j) + ", ";}
					byte but[]= s.getBytes();
					output.write(but); 
				}
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
	}
	/**
	 * Saves a moss file, i.e. in a format that is acceptable for moss.
	 * **/
	public IFile saveMoss(IFile filename, List<ArrayList<String>> fam,
			IProgressMonitor monitor) throws BioclipseException, IOException {
		String s;     
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}

		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			for(int i = 0; i<fam.size(); i++){
				s = i+1 +",0," +(String) fam.get(i).get(0) + "\n";	
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
	/**
	 * Saves a moss file, i.e. in a format that is acceptable for moss.
	 * Takes two lists, one for each family.
	 * **/
	public IFile saveMoss(IFile filename, List<ArrayList<String>> fam1, List <ArrayList<String>> fam2,
			IProgressMonitor monitor) throws BioclipseException, IOException {
		String s;
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			for(int i = 0; i<fam1.size(); i++){
				s = i+1+"p1" +",0," +(String) fam1.get(i).get(0) + "\n";
				byte but[]= s.getBytes();
				output.write(but); 
			}
			for(int i = 0; i<fam2.size(); i++){
				s = i+1+"p2" +",0," +(String) fam2.get(i).get(0) + "\n";
				byte but[]= s.getBytes();
				output.write(but); 
			}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);}
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
			);}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing moss outfile.", e);
		} 
		monitor.worked(100);
		monitor.done();
		return filename;
	};


	private void setMossModel(MossModel mossmodel){
		mossmodel.setMinimalSupport(mossbean.getMinimalSupport());
		mossmodel.setMaximalsupport(mossbean.getMaximalSupport());
		mossmodel.setThreshold(mossbean.getThreshold());
		mossmodel.setExNode(mossbean.getExNode());
		mossmodel.setExSeed(mossbean.getExSeed());
		mossmodel.setSeed(mossbean.getSeed());
		mossmodel.setMinRing(mossbean.getMinRing());
		mossmodel.setMaxRing(mossbean.getMaxRing());
		mossmodel.setMaxEmbMemory(mossbean.getMaxEmbed());
		mossmodel.setMbond(mossbean.getMbond());
		mossmodel.setMrgbd(mossbean.getMrgbd());
		mossmodel.setMatom(mossbean.getMatom());
		mossmodel.setMrgat(mossbean.getMrgat());
		mossmodel.setMode(mossbean.getMode());
	}


	/*String[] desc ={
	"Examplea moss.createParamteters(\"aromatic\", \"always\")," + "\n" +
	"moss.createParamteters(\"minEmbed\", 6)" + "\n",
	" (\"aromatic\", \"never\"/\"upgrade\"/\"downgrade\")  |\"String\"",
	" (\"canonicequiv\", false/true)   |boolean",
	" (\"canonic\", true/false)   |boolean" ,
	" (\"carbonChainLength\", true/false)  |boolean",
	" not for use",
	" (\"closed\", true/false)   |boolean",
	" (\"exNode\", \"Atom\")    |\"String\"",
	" (\"exSeed\", \"Atom\")    |\"String\"",
	" (\"extPrune\", \"none\"/\"full\"/\"partial\"/)    |\"String\"",
	" (\"ignoreAtomTypes\", \"never\"/\"always\"/\"in rings\")    |\"String\"",
    " (\"ignoreBond\", \"never\"/\"always\"/\"in rings\")    |\"String\"", 
	" (\"kekule\", true/false)   |boolean" ,
	" not for use",
	" (\"matchChargeOfAtoms\", \"never\"/\"always\"/\"in rings\")    |\"String\"",
	" (\"matchAromaticityAtoms\", \"match\"/\"no match\")    |\"String\"",
	" not for use",
	" (\"maxEmbMemory\", value)   |integer",
	" (\"maxEmbed\",value)   |integer",
	" (\"maxRing\", value)   |integer",
	" (\"maximalSupport\", value)   |double",
	" not for use",
	" (\"minEmbed\", value)   |integer", 
	" (\"maxRing\", value)   |integer", 
	" (\"minimalSupport\", value)   |double",
	" not for use"," not for use"," not for use",
	" (\"ringExtension\", \"none\"/\"full\"/\"merge\"/\"filter\")   |\"String\"",
	" (\"seed\", \"Atom\")   |\"String\"",
	" (\"split\", true/false)  |boolean",
	" (\"threshold\", value)   |double",
	" (\"unembedSibling\", false/true)   |boolean",""
	
	};

//returns all names of parameters
public String parameterDescription() throws Exception, BioclipseException{
ArrayList<String> name = mossbean.getPropertyNames(mossbean);
String names = desc[0] +"\n";
int i=0;
while(i<name.size()){
		names= names + name.get(i)+ ": " + desc[i+1] +"\n" ;
		i++;
}
return names; 

}
	*/
}


