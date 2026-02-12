package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationUtils {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Valide un objet et retourne la liste des erreurs sous forme de String.
     * Retourne une liste vide si l'objet est valide.
     */
    public static <T> List<String> validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        List<String> errors = new ArrayList<>();

        for (ConstraintViolation<T> violation : violations) {
            // Construit un message du type : "title : Le titre ne peut pas être vide"
            errors.add(violation.getPropertyPath() + " : " + violation.getMessage());
        }
        return errors;
    }
}