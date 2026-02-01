package com.app.municipio.application.cloudinary;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadFileInfo {
    private String publicId;
    private String secureUrl;
    private String resourceType;

}
