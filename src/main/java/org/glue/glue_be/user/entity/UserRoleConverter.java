package org.glue.glue_be.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.user.response.UserResponseStatus;

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

        throw new BaseException(UserResponseStatus.INVALID_ROLE);
    }
}
