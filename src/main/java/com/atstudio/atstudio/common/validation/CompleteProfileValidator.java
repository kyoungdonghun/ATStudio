package com.atstudio.atstudio.common.validation;

import com.atstudio.atstudio.dto.user.CompleteProfileRequest;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CompleteProfileValidator implements ConstraintValidator<ValidCompleteProfile, CompleteProfileRequest> {

    @Override
    public boolean isValid(CompleteProfileRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getUserType() == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.getUserType() == UserType.INDIVIDUAL && value.getJob() == null) {
            context.buildConstraintViolationWithTemplate("job is required for individual members")
                    .addPropertyNode("job")
                    .addConstraintViolation();
            valid = false;
        }

        if (value.getUserType() == UserType.BUSINESS
                && (value.getCompanyName() == null || value.getCompanyName().isBlank())) {
            context.buildConstraintViolationWithTemplate("companyName is required for business members")
                    .addPropertyNode("companyName")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
