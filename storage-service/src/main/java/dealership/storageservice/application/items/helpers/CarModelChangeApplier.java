package dealership.storageservice.application.items.helpers;

import dealership.storageservice.application.items.operations.carmodel.UpdateCarModel;
import dealership.storageservice.application.mappers.CarModelInputMapper;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class CarModelChangeApplier {
    private final CarModelInputMapper carModelInputMapper;

    public void applyChange(CarModel carModel, UpdateCarModel.Change change) {
        if (change instanceof UpdateCarModel.ChangeBrand changeBrand) {
            carModel.changeBrand(changeBrand.brand());
            return;
        }
        if (change instanceof UpdateCarModel.Rename rename) {
            carModel.rename(rename.name());
            return;
        }
        if (change instanceof UpdateCarModel.ChangeBasePrice changeBasePrice) {
            carModel.changeBasePrice(new Money(changeBasePrice.price()));
            return;
        }
        if (change instanceof UpdateCarModel.ChangeBaseSlots) {
            throw new DomainValidationException(
                    "ChangeBaseSlots must be handled in CarModelService before applying car model changes."
            );
        }
        if (change instanceof UpdateCarModel.ChangeReplaceableSlots changeReplaceableSlots) {
            Set<String> replaceableSlots = changeReplaceableSlots.replaceableSlots();

            if (replaceableSlots == null) {
                throw new DomainValidationException("Replaceable slots must not be null.");
            }

            carModel.changeReplaceableSlots(
                    carModelInputMapper.toReplaceableSlots(replaceableSlots)
            );
            return;
        }

        throw new DomainValidationException(
                "Unsupported car model change type: " + change.getClass().getSimpleName()
        );
    }
}

