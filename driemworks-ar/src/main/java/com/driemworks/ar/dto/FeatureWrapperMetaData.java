package com.driemworks.ar.dto;

/**
 * Meta Data for the FeatureWrapper
 * @author Tony
 */
public class FeatureWrapperMetaData {

    private String featureDescriptor;

    private String featureExtractor;

    /**
     * The default constructor
     */
    public FeatureWrapperMetaData() {}

    public FeatureWrapperMetaData(String featureDescriptor, String featureExtractor) {
        this.featureDescriptor = featureDescriptor;
        this.featureExtractor = featureExtractor;
    }

    public String getFeatureDescriptor() {
        return featureDescriptor;
    }

    public void setFeatureDescriptor(String featureDescriptor) {
        this.featureDescriptor = featureDescriptor;
    }

    public String getFeatureExtractor() {
        return featureExtractor;
    }

    public void setFeatureExtractor(String featureExtractor) {
        this.featureExtractor = featureExtractor;
    }
}
