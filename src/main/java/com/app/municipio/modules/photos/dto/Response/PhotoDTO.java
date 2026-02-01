package com.app.municipio.modules.photos.dto.Response;

import com.app.municipio.modules.photos.enums.PhotoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhotoDTO {

    private Long id;

    private String url;

    private String fileType;

    private String publicId;

    private PhotoType photoType;
}
