package org.glue.glue_be.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserRole attribute) {
        return (attribute != null ? attribute.getCode() : null);
    }

    @Override
    public UserRole convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (UserRole r : UserRole.values()) {
            if (r.getCode() == dbData) {
                return r;
            }
        }

        throw new IllegalArgumentException("Unknown db role code: " + dbData);
    }
}
