/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.*;
import org.cbioportal.model.GeneSet;
import org.cbioportal.model.GeneSetInfo;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author ochoaa
 */
public class ImportGeneSetData extends ConsoleRunnable {
    
	public static int skippedGenes;
    DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
    DaoGeneSet daoGeneSet = DaoGeneSet.getInstance();
    DaoGeneSetInfo daoGeneSetInfo = DaoGeneSetInfo.getInstance();

    @Override
    public void run() {
        try {
            String progName = "ImportGeneSetData";
            String description = "Import geneset data files.";
            // usage: --data <data_file.txt> --supp <supp_file.txt>  --version <Version> --update [allow updates to existing geneset data]
            
            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "Geneset data file")
                    .withRequiredArg().ofType(String.class);
            OptionSpec<String> supp = parser.accepts("supp", "Option geneset supplemental data file")
                    .withRequiredArg().ofType(String.class);
            OptionSpec<String> version = parser.accepts("version", "Version of gene sets")
                    .withRequiredArg().ofType(String.class);
            //parser.accepts("update", "Permits updates to geneset data even if geneset is in use");
            parser.accepts("update", "Allow new version of genesets. Removes geneset data for genesets that are updated");
            
            OptionSet options = null;
            try {
                options = parser.parse(args);
            }
            catch (Exception ex) {
                throw new UsageException(
                        progName, description, parser, 
                        ex.getMessage());
            }
            
            // if neither option was set then throw UsageException
            if (!options.has(data) && !options.has(supp)) {
                throw new UsageException(
                        progName, description, parser,
                        "'data' and/or 'supp' argument required");
            }
            if (!options.has(version)) {
                throw new UsageException(
                        progName, description, parser,
                        "'version' argument required");
            }
            
            // import geneset data file and/or supplemental geneset data file
            boolean allowUpdates = options.has("update");
            
            //TODO parse version from command line args
            //TODO WARNINGS for wrong versions: if version 2 and 1 already exists: give error saying version 1 needs to be removed first
            // if version 1 and update option not given: check if geneset already exists in version 1, if exists give error (tell user to use update option)
            
            // Check database version
            GeneSetInfo geneSetInfo = new GeneSetInfo();
            geneSetInfo = daoGeneSetInfo.getGeneSetVersion();
            String databaseVersion = geneSetInfo.getVersion();
            
            // Check new database version
            String dataVersion = options.valueOf(version);
            
            // New geneSetInfo for new data version, to add or update later.
            geneSetInfo = new GeneSetInfo();
            geneSetInfo.setVersion(dataVersion);
    
            // Print gene set versions for testing purposes.
            System.out.println("DB: " + databaseVersion);
            System.out.println("GS: " + dataVersion);

            // Empty database
            if (databaseVersion == null) {
            	System.out.println("Filling empty database.\n");
            	startImport(options, data, supp, allowUpdates, dataVersion);
                daoGeneSetInfo.setGeneSetVersion(geneSetInfo);;
                
            // Not empty database, different version
            } else if (!dataVersion.equals(databaseVersion)) {
            	System.out.println("Input gene set version is different from database version.\n");
            	
            	if (allowUpdates) {
                	System.out.println("Updates are allowed. Updating gene sets.\n");
                	startImport(options, data, supp, allowUpdates, dataVersion);
                    daoGeneSetInfo.updateGeneSetVersion(geneSetInfo);;
                    
            	} else {
            		throw new RuntimeException("Input geneset version '" + dataVersion + "' differs from database geneset version '" + databaseVersion + "'.\n" +
                            "Set option '--update' to REMOVE database GSVA Scores and Pvalues and allow updates to existing genesets.");
            	}
            	
            // Not empty database, same version	
            } else {
            	System.out.println("Same version database. Updating gene sets.\n");
            	startImport(options, data, supp, allowUpdates, dataVersion);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Start import process for gene set file and supplementary file.
     */  
    public void startImport(OptionSet options, OptionSpec<String> data, OptionSpec<String> supp, boolean allowUpdates, String dataVersion){
    	try {
       	 	if (options.hasArgument(data)) {
	             File genesetFile = new File(options.valueOf(data));
	             importData(genesetFile, allowUpdates, dataVersion);
       	 	}            
	         if (options.hasArgument(supp)) {
	             File genesetSuppFile = new File(options.valueOf(supp));
	             importSuppGeneSetData(genesetSuppFile);
	         }
    	}
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    /**
     * Imports data from geneset file.
     * @param genesetFile
     * @param allowUpdates
     * @param version 
     * @throws Exception 
     */
    public int importData(File genesetFile, boolean allowUpdates, String version) throws Exception {

        ProgressMonitor.setCurrentMessage("Reading data from: " + genesetFile.getCanonicalPath());
   
        // read geneset data file - note: this file does not contain headers
        FileReader reader = new FileReader(genesetFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();
                
        while (line != null) {            
            String[] parts = line.split("\t");
                        
            // assumed that geneset id and ref link are the first two columns in file            
            GeneSet geneSet = new GeneSet();
            geneSet.setExternalId(parts[0]);
            geneSet.setRefLink(parts[1]);
            
            // by default name and nameshort are the same as external id, and can be overriden in importSuppGeneSetData:
            geneSet.setName(geneSet.getExternalId());
            geneSet.setNameShort(geneSet.getExternalId());
            
            // Set version
            GeneSetInfo geneSetInfo = new GeneSetInfo();
            geneSetInfo.setVersion(version);
            
            // parse entrez ids for geneset
            List<Integer> genesetGenes = new ArrayList();
            for (int i=2; i<parts.length; i++) {
                genesetGenes.add(Integer.valueOf(parts[i]));
            }
            geneSet.setGenesetGenes(genesetGenes);
            
            // check if geneset already exists by external id
            GeneSet existingGeneSet = daoGeneSet.getGeneSetByExternalId(geneSet.getExternalId());
            // if geneset exists then check usage
            if (existingGeneSet != null) {
                // if geneset in use by other studies and option to allow updates 
                // was not set then alert user and throw RuntimeException
                if (daoGeneSet.checkUsage(existingGeneSet.getGeneticEntityId()) && !allowUpdates) {
                    throw new RuntimeException("Geneset " + geneSet.getExternalId() + " exists and is already in use in DB. " +
                            "Set option 'update' to allow updates to existing genesets.");
                }                
                // assumed that ref link and geneset genes are updated
                existingGeneSet.setRefLink(geneSet.getRefLink());
                existingGeneSet.setGenesetGenes(geneSet.getGenesetGenes());
                //existingGeneSet.setVersion(version);
                
                // update geneset record and geneset genes in db
                daoGeneSet.updateGeneSet(existingGeneSet, true);                
            }
            else {
                // import new geneset record
                daoGeneSet.addGeneSet(geneSet);
                
            }
            line = buf.readLine();
        }
        // close file
        reader.close();
        
        // print warnings message with skipped genes
        if (skippedGenes > 0) {
        System.err.println("\n" + skippedGenes + " times a gene was not found in local gene table. Possible reasons:\n\n"
        		+ "1. The Entrez gene IDs are relatively new. Consider adding them to database.\n"
        		+ "2. The Entrez gene IDs are depricated. Consider updating gene sets and recalculating GSVA scores.\n"
        		+ "3. Invalid Entrez gene IDs. Please check .gmt file to verify genes are in Entrez gene ID format.\n\n");
        }
        return skippedGenes;
    }

    /**
     * Imports supplemental geneset data from supp file.
     * @param suppFile
     * @throws Exception 
     */
    static void importSuppGeneSetData(File suppFile) throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from: " + suppFile.getCanonicalPath());
        DaoGeneSet daoGeneSet = DaoGeneSet.getInstance();
        
        // read supplemental geneset data file - note: this file does not contain headers
        FileReader reader = new FileReader(suppFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();
        
        while (line != null) {
            String[] parts = line.split("\t");
            
            // assumed that fields contain: geneset id, name, short name
            GeneSet geneSet = daoGeneSet.getGeneSetByExternalId(parts[0]);
            
            // if geneset does not already exist then alert user and skip record
            if (geneSet == null) {
                ProgressMonitor.logWarning("Could not find geneset " + parts[0] + " in DB. Record will be skipped.");
            }
            else {
                // update name and short name for geneset
                geneSet.setNameShort(parts[1]);
                geneSet.setName(parts[2]);
                
                // update geneset record in db without updating geneset genes
                daoGeneSet.updateGeneSet(geneSet, false);
            }
            
            line = buf.readLine();
        }
        // close file
        reader.close();
    }

    public ImportGeneSetData(String[] args) {
        super(args);
    }
    
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGeneSetData(args);
        runner.runInConsole();        
    }


    
}