package org.cbioportal.web.parameter;

import java.util.List;

/**
 * Wrapper class for specifying filter criteria for GeneticData items
 * in the GeneticDataController services.
 *  
 * @author pieter
 *
 */
public class GeneticDataFilterCriteria {
	//The list of identifiers for the genetic entities of interest. 
	//If entity type is GENE: list of Entrez Gene IDs. If entity type is GENESET: list of gene set identifiers
    private List<String> geneticEntityIds;

    //Identifier of pre-defined sample list with samples to query. E.g. brca_tcga_all 
    private String sampleListId;

    //Full list of samples or patients to query, E.g. list with TCGA-AR-A1AR-01, TCGA-BH-A1EO-01...
    private List<String> sampleIds;

	public List<String> getGeneticEntityIds() {
		return geneticEntityIds;
	}

	public void setGeneticEntityIds(List<String> geneticEntityIds) {
		this.geneticEntityIds = geneticEntityIds;
	}

	public String getSampleListId() {
		return sampleListId;
	}

	public void setSampleListId(String sampleListId) {
		this.sampleListId = sampleListId;
	}

	public List<String> getSampleIds() {
		return sampleIds;
	}

	public void setSampleIds(List<String> sampleIds) {
		this.sampleIds = sampleIds;
	} 

}