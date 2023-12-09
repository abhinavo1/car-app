package com.intuit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.models.Engine;
import com.intuit.models.Feature;
import com.intuit.response.ComparisonResponse;
import com.intuit.response.FeatureResponse;
import com.intuit.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.intuit.service.ComparatorUtils.getAllValuesForType;
import static com.intuit.service.ComparatorUtils.isCommonType;

@Service
public class FeatureComparatorImpl implements FeatureComparator {

    @Override
    public ComparisonResponse compareFeatures(Feature features, List<Feature> features1) throws JsonProcessingException {
        ComparisonResponse comparisonResponse = new ComparisonResponse();
        comparisonResponse.setGroupName(Constants.ENGINE);
        List<Engine> engines = features1.stream()
                .map(Feature::getEngine)
                .collect(Collectors.toList());

        compareEngines(features.getEngine(), engines,comparisonResponse);
        return comparisonResponse;
    }

    private void compareEngines(Engine firstEngine, List<Engine> engines, ComparisonResponse comparisonResponse) {
        List<FeatureResponse> featureResponses = new ArrayList<>();

        compareAndAddFeature(Constants.TYPE, firstEngine.getType(), engines, featureResponses);
        compareAndAddFeature(Constants.HORSE_POWER, String.valueOf(firstEngine.getHorsepower()), engines, featureResponses);

        comparisonResponse.setFeature(featureResponses);
    }

    private void compareAndAddFeature(String name, String value, List<Engine> engines, List<FeatureResponse> featureResponses) {
        List<String> values = engines.stream()
                .map(engine -> name.equals(Constants.TYPE) ? engine.getType() : String.valueOf(engine.getHorsepower()))
                .collect(Collectors.toList());

        boolean isCommonValue = isCommonType(value, values);

        featureResponses.add(FeatureResponse.builder()
                .name(name)
                .values(getAllValuesForType(value, values))
                .isCommonValue(isCommonValue)
                .build());
    }

}
