package org.cbioportal.model;

import java.io.Serializable;

public class GenesetCorrelation implements Serializable {

	private Integer entrezGeneId;
    private String hugoGeneSymbol;
	private Double correlationValue;
    private String geneticProfileId;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

	public String getHugoGeneSymbol() {
		return hugoGeneSymbol;
	}

	public void setHugoGeneSymbol(String hugoGeneSymbol) {
		this.hugoGeneSymbol = hugoGeneSymbol;
	}
	
    public Double getCorrelationValue() {
        return correlationValue;
    }

    public void setCorrelationValue(Double correlationValue) {
        this.correlationValue = correlationValue;
    }

	public String getGeneticProfileId() {
		return geneticProfileId;
	}

	public void setGeneticProfileId(String geneticProfileId) {
		this.geneticProfileId = geneticProfileId;
	}
}