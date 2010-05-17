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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;

import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.model.IStringMatrix;

@PublishedClass(
    value="Manager to provide MoSS substructure mining functionality.",
    doi="10.1145/1133905.1133908"
)
public interface IMossManager extends IBioclipseManager {
	
	@Recorded
	@PublishedMethod(
			params = "String fileName, List family",
			methodSummary = "Saves moss file from SPARQL query" )
			public void saveMoss(String fileName, List<ArrayList<String>> fam1)
	throws BioclipseException, IOException ;
	@Recorded
	@PublishedMethod(
			params = "String fileName, List fam1,  List fam2",
			methodSummary = "Saves moss file from SPARQL query, list two families" )
			public void saveMoss(String fileName, List<ArrayList<String>> fam1, 
					List<ArrayList<String>> fam2)
	throws BioclipseException, IOException ;

	@Recorded
	@PublishedMethod(
			params = "String fileName, List fam1",
			methodSummary = "Saves info from SPARQL query, in file" )
			public void saveInformation(String filename, List<ArrayList<String>> fam)
	throws BioclipseException, IOException ;

	@Recorded
	@PublishedMethod(
			params = "String inputfile, String outfile, String outidfile",
			methodSummary = "Run MoSS" )
			public void run(String inputfile, String outfile, String outidfile) 
	throws BioclipseException, IOException, ScriptException;
	
	@Recorded
	@PublishedMethod(
			params = "String fam, int limit",
			methodSummary = "Create query" )
			public String query(String fam, String act, int limit) 
	throws BioclipseException, IOException, ScriptException;

	@Recorded
	@PublishedMethod(
			params = "String property ,Object value" +
			"Existing properties:"+
			" minimalSupport,maximalSupport,threshold, split, closed" + 
			" exNode, exSeed, seed,minEmbed, maxEmbed,Limits" + 
			" mbond, mrgbd, matom, mrgat,maxRing, minRing" +
			" mode, maxEmbMemory, path, pathId, namefile, namefileId",
			methodSummary =  "Sets paramters. Examples moss.createParamteters(\"aromatic\", \"always\") and" + "\n" +
			"moss.createParamteters(\"minEmbed\", 6) \n" +
			"Following are arguments to createParameters: \n \n"+
			" (\"aromatic\", \"never\"/\"upgrade\"/\"downgrade\")  | \"String\"\n"+
			" (\"canonicequiv\", false/true)   | boolean\n"+
			" (\"canonic\", true/false)   | boolean\n" +
			" (\"carbonChainLength\", true/false)  | boolean\n"+
			" (\"closed\", true/false)   | boolean \n"+
			" (\"exNode\", \"Atom\")    | \"String\"\n"+
			" (\"exSeed\", \"Atom\")    | \"String\"\n"+
			" (\"extPrune\", \"none\"/\"full\"/\"partial\"/)    | \"String\"\n"+
			" (\"ignoreAtomTypes\", \"never\"/\"always\"/\"in rings\")    | \"String\"\n"+
	        " (\"ignoreBond\", \"never\"/\"always\"/\"in rings\")    | \"String\"\n"+
			" (\"kekule\", true/false)   | boolean \n" +
			" (\"matchChargeOfAtoms\", \"never\"/\"always\"/\"in rings\")    | \"String\"\n"+
			" (\"matchAromaticityAtoms\", \"match\"/\"no match\")    | \"String\"\n"+
			" (\"maxEmbMemory\", value)   | integer \n"+
			" (\"maxEmbed\",value)   | integer \n"+
			" (\"maxRing\", value)   | integer \n"+
			" (\"maximalSupport\", value)   | double \n"+
			" (\"minEmbed\", value)   | integer \n"+
			" (\"maxRing\", value)   | integer \n" +
			" (\"minimalSupport\", value)   | double"+
			" (\"ringExtension\", \"none\"/\"full\"/\"merge\"/\"filter\")   | \"String\"\n"+
			" (\"seed\", \"Atom\")   | \"String\"\n"+
			" (\"split\", true/false)  | boolean \n"+
			" (\"threshold\", value)   | double \n"+
			" (\"unembedSibling\", false/true)   | boolean \n")
			public String createParameters(String property, Object value);

	@Recorded
	@PublishedMethod(
			methodSummary = "Shows value of settings" )
			public String parameterValues() throws Exception;

}