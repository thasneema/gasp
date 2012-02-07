package utility;

// from http://www.devx.com/tips/Tip/13004

//TODO: i don't like having to associate key-value pairs with =

import ga.GAParameters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class ArgumentParser {
	
    private List<Pair<String,List<String>>> options = new ArrayList<Pair<String,List<String>>>();
    
    public ArgumentParser(String[] args) {
    	parseArgs(args);
    }
    
    public ArgumentParser(String[] args, String inputFileName) {
    	parseArgs(args);
    	parseInputFile(inputFileName);
    }
    
    private void parseArgs(String args[]) {
    	List<String> arguments = null;
    	String key = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
            	if (key != null)
            		options.add(new Pair<String, List<String>>(key.toLowerCase(), arguments));
                key = args[i].substring(2);
                arguments = new LinkedList<String>();
            } else {
            	if (arguments == null) {
            		System.out.println("ERROR: malformed command-line input");
            		System.exit(-1);
            	}
                arguments.add(args[i]);
            }
        }
        if (key != null)
        	options.add(new Pair<String, List<String>>(key.toLowerCase(), arguments));
    }

    public boolean hasOption(String opt) {
    	for (Pair<String,List<String>> o : options)
    		if (o.getFirst().equalsIgnoreCase(opt))
    			return true;
    	return false;
    }
    
    public boolean hasArguments(String opt) {
    	for (Pair<String,List<String>> o : options)
    		if (o.getFirst().equalsIgnoreCase(opt) && o.getSecond().size() > 0)
    			return true;
    	return false;
    }

    
    public List<String> getArguments(String opt) {
    	for (Pair<String,List<String>> o : options)
    		if (o.getFirst().equalsIgnoreCase(opt) && o.getSecond().size() > 0)
    			return o.getSecond();
        return null;
    }
    
    public String getArgument(String opt) {
    	if (hasArguments(opt))
    		return getArguments(opt).get(0);
    	else
    		return null;
    }
    
   /*
    public List<Pair<String, String[]>> getCLParserData() {
    	List<Pair<String, String[]>> result = new ArrayList<Pair<String,String[]>>();
    	
    	for (String s : options.keySet()) {
			int numArgs = options.get(s).size();
    		String args[] = new String[numArgs];
    		for (int i = 0; i < numArgs; i++)
    			args[i] = options.get(s).get(i);
    		result.add(new Pair<String,String[]>("--" + s, args));
    	}
    	
    	return result;
    }
    
    */
    
	// returns a mapping of key-value pairs parsed from the input file
	private void  parseInputFile(String inputFileName) {
		int verbosity = GAParameters.getParams().getVerbosity();
		String line = null;
		
		// make sure we have a file to parse
		if (inputFileName == null || inputFileName.equals("")) {
			System.out.println("Give an input file.");
			System.exit(-1);
		}
		
		// open the input file
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFileName));
			while ((line = in.readLine()) != null) {
    			StringTokenizer t = new StringTokenizer(line);
    			// skip empty lines:
    			if (!t.hasMoreTokens())
    				continue;
    			String key = t.nextToken();
    			// allow for comment lines starting with #
    			if (key.charAt(0) == '#')
    				continue;
    			// read in all the values associated with the key
    			ArrayList<String> valuesList = new ArrayList<String>();
    			while (t.hasMoreTokens())
    				valuesList.add(t.nextToken());
    			
    			options.add(new Pair<String,List<String>>(key,valuesList));
    			//result.put(key, values);
    			if (verbosity >= 5) {
    				System.out.print("Parsed from input file: " + key + " ");
    				for (int i = 0; i < valuesList.size(); i++)
    					System.out.print(valuesList.get(i) + " ");
    				System.out.println("");
    			}
			}
		} catch(IOException x) {
			System.out.println("Problem parsing input file " + inputFileName + ": " + x.getMessage());
			System.exit(-1);
		}
	}
	
	/*
	// returns the first value associated with the given flag.
	// if no such value is available, we print the usage statement and exit.
	private String[] getValues(String flag) {		
		String[] result = null;
		for (Pair<String,String[]> p : argmap) {
			if (p.getFirst().compareToIgnoreCase(flag) == 0) {
				result = p.getSecond();
				break;
			}
		}
		
		if(!isSet(flag) || result == null || result.length == 0)
			usage("Improper usage of " + flag, true);
		
	    return result;
	}
	// overloaded getValues:
	// make sure we have at least n values following the given flag
	private String[] getValues (String flag, int n) {
		String[] answer = getValues(flag);
		if (answer.length < n)
			usage("Improper usage of " + flag + ". Requires " + n + " arguments.", true);
		return answer;
	}
	
	// boolean function to determine whether or not a particular flag
	// was passed on the command line
	private Boolean isSet(String flag)
	{
		if (argmap == null)
			return false;
		
		for (Pair<String,String[]> p : argmap) {
			if (p.getFirst().compareToIgnoreCase(flag) == 0)
				return true;
		}
		return false;
	}*/
	
	public List<Pair<String,List<String>>> getOptions() {
		return new ArrayList<Pair<String,List<String>>>(options);
	}

}
