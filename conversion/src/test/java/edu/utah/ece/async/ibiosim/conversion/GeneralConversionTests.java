/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.conversion;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * General test cases for all conversion supported by iBioSim
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GeneralConversionTests extends ConversionAbstractTests {
	
	@Test
	public void test_validation(){
		/* test validation on a valid sbol file. */
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String selectedMod = "http://sbols.org/CRISPR_Example/CRPb_characterization_Circuit/1.0.0";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-s", selectedMod};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_e(){
		/* compare SBOL files with same content but with different file name*/
		String fileName = "CRISPR_example"; 
		String fileName2 = "meherGolden_RepressionModel";
		
		String inputfile = sbolDir + fileName + ".xml";
		String inputfile2 = sbolDir + fileName2 + ".xml";
		
		String uriPrefix = "http://www.async.ece.utah.edu/";
		
		String[] converter_cmdArgs = {"-no", inputfile, "-p", uriPrefix, "-e", inputfile2, "-mf", "mainfileRes" , "-cf", "secondFileRes"};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}

	
	@Test
	public void test_cmd_l_sbml() throws IOException{
		/* convert sbol2sbml*/
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		String outFile =  sbol2sbml_outputDir + fileName + "_out2";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {"-l", outputLang, "-esf", "-p", uriPrefix, inputfile, "-oDir", sbol2sbml_outputDir, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbml2() throws IOException{
		/* convert sbol2sbml*/
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		String outFile =  sbol2sbml_outputDir + fileName + "_out2";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {"-l", outputLang, "-rsbol", sbolDir, "-p", uriPrefix, inputfile, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_genbank(){
		/* convert sbol2genbank*/
		String fileName = "acoA_full"; 
		String inputfile = sbolDir + fileName + ".xml";
		String outFile = genBank_outputDir + fileName + "_out";
		
		//Options
		String outputLang = "GenBank";
		String uriPrefix = "http://www.async.ece.utah.edu/";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol()
	{
		/* convert sbml2sbol*/
		String fileName = "repressibleTU_Connected"; 
		String inputfile = sbmlDir + fileName + ".xml";
		
		//Options
		String outputLang = "SBOL2";
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String outputFile = sbml2sbol_outputDir + fileName + "_out";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-p", uriPrefix, inputfile, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol2()
	{
		/* convert sbml2sbol with external SBML files using -rsbml*/
		String fileName = "GeneticToggle"; 
		String inputfile = sbmlDir + fileName + ".xml";
		
		//Options
		String outputLang = "SBOL2";
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String outputFile = sbml2sbol_outputDir + fileName + "_out";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-rsbml", sbmlDir, "-p", uriPrefix, inputfile, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol3()
	{
		/* convert sbml2sbol with external SBML files using -rsbml*/
		String fileName = "Main"; 
		String inputfile = sbmlDir + fileName + ".xml";
		
		//Options
		String outputLang = "SBOL2";
		String uriPrefix = "http://www.async.ece.utah.edu/";
		String outputFile = sbml2sbol_outputDir + fileName + "_out";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-rsbml", sbmlDir, "-p", uriPrefix, inputfile, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
}
