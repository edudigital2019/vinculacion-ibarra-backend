package com.app.municipio.modules.users.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum IdTypes {
    CEDULA("cedula de identidad"),
    PASAPORTE("Pasaporte");

    private final String description;

    IdTypes(String description) {
        this.description = description;
    }
    @JsonCreator
    public static IdTypes fromDescription(String value) {
        for (IdTypes type : values()) {
            if (type.description.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de identificación inválido: " + value);
    }

}
