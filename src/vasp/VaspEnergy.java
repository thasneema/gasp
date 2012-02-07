/* Genetic algorithm for crystal structure prediction.  */

package vasp;

import ga.*;
import chemistry.*;
import java.io.*;
import org.xml.sax.SAXException;

import crystallography.Cell;

import java.util.*;

import utility.Utility;

import javax.xml.parsers.ParserConfigurationException;

// VaspEnergy implements Energy.  It computes the total energy of a 
// StructureOrg using Vasp and the given directory of pseudopotentials.
// It contains all of the methods and utilities in ga that 
// are specific to VASP.

public class VaspEnergy implements Energy {
	
	private String kpointsFile;
	private Map<Element,String> potcarFileMap;
	private String incarFile;
	private boolean cautious;
		
	public VaspEnergy(List<String> args) {
		if (args == null || args.size() < 5 || args.size() % 2 != 1)
			GAParameters.usage("Not enough or malformed parameters given to VaspEnergy", true);
		
		cautious = Boolean.parseBoolean(args.get(0));
		kpointsFile = args.get(1);
		incarFile = args.get(2);
		potcarFileMap = new HashMap<Element,String>();
		for (int i = 3; i < args.size(); i = i+2) {
			Element e = Element.getElemFromSymbol(args.get(i));
			String potcarFile = args.get(i+1);
			potcarFileMap.put(e, potcarFile);
		}

	}
	
	// runs VASP on the input file given and returns the results in a String
	private String runVasp(String inputDir) {
		int verbosity = GAParameters.getParams().getVerbosity();
		StringBuilder vaspOutput = new StringBuilder();

		String s = null;
		try {
			// run the vasp command. in order to avoid hard-coding things which are
			// probably system-dependent, we call a wrapper script which is probably
			// just "cd $1; vasp" for simple set ups
			Process p = Runtime.getRuntime().exec("callvasp " + inputDir);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			try {
				// read the output
				while ((s = stdInput.readLine()) != null) {
					vaspOutput.append(s + GAUtils.newline());
					if (verbosity >= 5)
						System.out.println(s);
				}
	
				// print out any errors
				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}
			} finally {
				stdInput.close();
				stdError.close();
			}

		} catch (IOException e) {
			System.out.println("IOException in VaspEnergy.runVasp: " + e.getMessage());
			System.exit(-1);
		}

		return vaspOutput.toString();
	}
	
	private double vaspRun(StructureOrg o) {
		GAParameters params = GAParameters.getParams();
		int verbosity = params.getVerbosity();
		
		// some output
		if (verbosity >= 3)
			System.out.println("Starting VASP computation on organism "
					+ o.getID() + "... ");
		
		// make temp directory
		File outDir = new File(params.getTempDirName() + "/" + params.getRunTitle() + "." + o.getID());
		outDir.mkdir();
		
		// make the vasp files
		VaspIn vaspin = new VaspIn(o.getCell(), kpointsFile, incarFile, potcarFileMap);
		vaspin.makeINCAR(outDir.getAbsolutePath() + "/");
		vaspin.makePOSCAR(outDir.getAbsolutePath() + "/");
		vaspin.makeKPOINTS(outDir.getAbsolutePath() + "/");
		vaspin.makePOTCAR(outDir.getAbsolutePath() + "/");
	
		// run vasp
		String vaspOutput = runVasp(outDir.getAbsolutePath());
		
		if (verbosity >= 5)
			System.out.println(vaspOutput);

		// store the relaxed structure back into o
		Cell newCell = VaspOut.getPOSCAR(outDir.getAbsolutePath() + "/CONTCAR");
		if (newCell != null)
			o.setCell(newCell);
		
		// return the energy
		double finalEnergy = VaspOut.getFinalEnergy(outDir.getAbsolutePath() + "/OUTCAR", cautious);
		if (verbosity >= 3)
			System.out.println("Energy of org " + o.getID() + ": " + finalEnergy + " ");
		
		return finalEnergy; 
	}

	public double getEnergy(StructureOrg o) {
		return vaspRun(o);
	}
	

	// just for testing:
	public static void main(String[] args) {
	/*	try {
			VaspXMLReader vaspReader = new VaspXMLReader("vasprun.xml", System.err);
			Cell bob = vaspReader.getFinalStructure();
			vaspReader.getFinalEnergy();
			System.out.println(bob);
		} catch (SAXException x) {
			System.out.println(x.getMessage());
		} catch (ParserConfigurationException x) {
			System.out.println(x.getMessage());
		} catch (IOException x) {
			System.out.println(x.getMessage());
		} */
		
	}
}
