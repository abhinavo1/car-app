package com.intuit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.exception.ValidationException;
import com.intuit.models.*;
import com.intuit.repository.CarRepository;
import com.intuit.request.CompareRequest;
import com.intuit.response.ComparisonResponse;
import com.intuit.response.ComparisonList;
import com.intuit.validator.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ComparisonLogicImpl implements ComparisonLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonLogicImpl.class);

    private final CarRepository carRepository;
    private final FeatureComparator featureComparator;
    private final SpecificationsComparator specificationsComparator;
    private final RequestValidator requestValidator;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Change the pool size as needed


    @Autowired
    public ComparisonLogicImpl(CarRepository carRepository, FeatureComparator featureComparator,
                               SpecificationsComparator specificationsComparator, RequestValidator requestValidator) {
        this.carRepository = carRepository;
        this.featureComparator = featureComparator;
        this.specificationsComparator = specificationsComparator;
        this.requestValidator = requestValidator;
    }

    @Override
    public ComparisonList compare(CompareRequest compareRequest) {
        requestValidator.validateRequest(compareRequest);
        try {
            Car firstCar = getCarById(compareRequest.getViewingCarId());
            Feature firstCarFeatures = firstCar.getFeatures();
            Specifications firstCarSpecifications = firstCar.getSpecifications();

            List<Car> listOfCars = getListOfCars(compareRequest.getIdList());

            List<Feature> features = listOfCars.stream().map(Car::getFeatures).collect(Collectors.toList());
            List<Specifications> specifications = listOfCars.stream().map(Car::getSpecifications).collect(Collectors.toList());

            List<ComparisonResponse> comparisonResponses = new ArrayList<>();


            CompletableFuture<ComparisonResponse> featureComparisonFuture = CompletableFuture.supplyAsync(() ->
                    featureComparator.compareFeatures(firstCarFeatures, features),executorService
            );
            CompletableFuture<ComparisonResponse> specificationsComparisonFuture = CompletableFuture.supplyAsync(() ->
                    specificationsComparator.compareSpecifications(firstCarSpecifications, specifications),executorService
            );

            comparisonResponses.add(featureComparisonFuture.get());
            comparisonResponses.add(specificationsComparisonFuture.get());
            ComparisonList comparisonList = new ComparisonList();
            comparisonList.setComparisonResponses(comparisonResponses);
            return comparisonList;
        } catch (ValidationException e) {
            LOGGER.error("Error during comparison: {}", e.getMessage());
            throw new ValidationException(e.getMessage());
        } catch (Exception exception) {
            LOGGER.error("Unexpected error occurred: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    private Car getCarById(String id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Car not found with ID: " + id));
    }

    private List<Car> getListOfCars(List<String> carIds){
        List<Car> list = new ArrayList<>();
        for (String id: carIds) {
            list.add(getCarById(id));
        }
        return list;
    }
}
