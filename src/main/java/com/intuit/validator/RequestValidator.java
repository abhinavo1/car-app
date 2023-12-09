package com.intuit.validator;

import com.intuit.exception.ValidationException;
import com.intuit.models.Car;
import com.intuit.request.CompareRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestValidator {
    public void validateRequest(CompareRequest idList) {
        if(idList == null) {
            throw new ValidationException("id list is Null");
        } else if (idList.getIdList().size() > 2) {
            throw new ValidationException("id List greater/lesser than required");
        }
    }

    public void validateIfCarsExist(List<Car> cars) {
        if(cars == null) {
            throw new ValidationException("No cars found for particular type and price");
        }
    }
}
