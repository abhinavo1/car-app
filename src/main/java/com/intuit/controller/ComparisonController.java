package com.intuit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.request.CompareRequest;
import com.intuit.response.CarResponse;
import com.intuit.response.ComparisonList;
import com.intuit.service.ComparisonLogic;
import com.intuit.service.CarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class ComparisonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonController.class);

    private final CarService carService;
    private final ComparisonLogic comparisonLogic;

    public ComparisonController(CarService carService, ComparisonLogic comparisonLogic) {
        this.carService = carService;
        this.comparisonLogic = comparisonLogic;
    }

    @ResponseBody
    @RequestMapping(value = "/type-and-price", method = RequestMethod.GET)
    public ResponseEntity<List<CarResponse>> getCarsByTypeAndPrice(
            @RequestParam String type,
            @RequestParam double price
    ) {
        try {
            List<CarResponse> cars = carService.getCarsByTypeAndPrice(type, price);
            LOGGER.info("Retrieved cars by type '{}' and price '{}'", type, price);
            return new ResponseEntity<>(cars, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error retrieving cars by type '{}' and price '{}'", type, price, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public ResponseEntity<ComparisonList> selectCarsForComparison(
            @RequestBody CompareRequest idList
    ) throws JsonProcessingException {
        ComparisonList comparisonResponses = comparisonLogic.compare(idList);
        LOGGER.info("Performed comparison for car IDs: {}", idList);
        return new ResponseEntity<>(comparisonResponses, HttpStatus.OK);
    }
}
