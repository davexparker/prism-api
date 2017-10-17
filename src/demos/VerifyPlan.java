//==============================================================================
//	
//	Copyright (c) 2017-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package demos;

import java.io.File;
import java.io.FileNotFoundException;

import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.Result;

public class VerifyPlan
{
	private Prism prism;
	
	public static void main(String[] args)
	{
		new VerifyPlan().run();
	}

	public void run()
	{
		try {
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			prism = new Prism(mainLog);
			prism.initialise();

			// Verify
			boolean result = verify();
			System.out.println(result);
			
			// Close down PRISM
			prism.closeDown();

		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		} catch (PrismException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public boolean verify() throws FileNotFoundException, PrismException
	{
		// Parse and load a PRISM model from a file
		ModulesFile modulesFile = prism.parseModelFile(new File("examples/plan.prism"));
		prism.loadPRISMModel(modulesFile);

		// Parse and load a properties model for the model
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File("examples/plan.props"));

		// Model check the first property from the file
		Result result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
		
		// Return the (booleab) result
		return ((Boolean) result.getResult()).booleanValue();
	}
}