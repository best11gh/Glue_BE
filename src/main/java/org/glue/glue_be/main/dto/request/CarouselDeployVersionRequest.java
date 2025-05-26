package org.glue.glue_be.main.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.glue.glue_be.main.entity.CarouselDeployVersion;

public record CarouselDeployVersionRequest(

        @NotBlank(message = "버전은 필수값입니다.")
        @Size(max = 50, message = "버전은 50글자보다 이하여야 합니다.")
        String version,

        @Size(max = 200, message = "설명은 200글자 이하여야 합니다.")
        String description
) {

    public CarouselDeployVersion toEntity() {
        return CarouselDeployVersion.builder()
                .version(this.version)
                .description(this.description)
                .isActive(true)
                .build();
    }

    @Override
    public String toString() {
        return String.format("CarouselDeployVersionRequest{version='%s', description='%s'}",
                version, description);
    }
}