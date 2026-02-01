package com.app.municipio.application.cloudinary;

import com.app.municipio.application.exceptions.ClientException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("unchecked")
public class CloudinaryService {
    private final Cloudinary cloudinary;

    private static final String IMAGE_RESOURCE_TYPE = "image";
    private static final String RAW_RESOURCE_TYPE = "raw";

    /**
     * Sube un archivo a Cloudinary
     * @param file archivo a subir (imagen o PDF)
     * @param folder carpeta de destino en Cloudinary (opcional)
     * @return URL segura del archivo subido
     * @throws IOException si ocurre un error durante la subida
     */
    public List<String> uploadFile(MultipartFile file, String folder) throws IOException {
        ArrayList<String> data = new ArrayList<>();
        try {
            // Detectar el tipo de archivo basado en content type y extensión
            String detectedResourceType = detectResourceType(file);

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", detectedResourceType,
                    "folder", folder != null ? folder : "uploads",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            // Para archivos PDF (raw), agregar configuración para hacerlos públicos
            if (RAW_RESOURCE_TYPE.equals(detectedResourceType)) {
                uploadParams.put("access_mode", "public");
                uploadParams.put("type", "upload");
            }

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Usar el tipo detectado en lugar del retornado por Cloudinary
            data.add(secureUrl);
            data.add(publicId);
            data.add(detectedResourceType); // Usar nuestro tipo detectado

            return data;

        } catch (IOException e) {
            throw new IOException("Error al subir el archivo a Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Detecta el tipo de recurso basado en el content type y extensión del archivo
     * @param file archivo a analizar
     * @return tipo de recurso para Cloudinary ("image" o "raw")
     */
    private String detectResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // Verificar por content type
        if (contentType != null) {
            if (contentType.equals("application/pdf")) {
                return RAW_RESOURCE_TYPE;
            }
            if (contentType.startsWith("image/")) {
                return IMAGE_RESOURCE_TYPE;
            }
        }

        // Verificar por extensión como fallback
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (".pdf".equals(extension)) {
                return RAW_RESOURCE_TYPE;
            }
            if (extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)")) {
                return IMAGE_RESOURCE_TYPE;
            }
        }

        // Por defecto, asumir que es imagen
        return IMAGE_RESOURCE_TYPE;
    }

    /**
     * Extrae la extensión del nombre de archivo
     * @param filename nombre del archivo
     * @return extensión con punto incluido (ej: ".pdf", ".jpg")
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * Sube un archivo a Cloudinary sin especificar carpeta
     * @param file archivo a subir
     * @return URL segura del archivo subido
     * @throws IOException si ocurre un error durante la subida
     */
    public List<String> uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, null);
    }

    /**
     * Descarga un archivo desde Cloudinary y lo retorna como ByteArray
     * @param secureUrl URL segura del archivo en Cloudinary
     * @return contenido del archivo como array de bytes
     * @throws IOException si ocurre un error durante la descarga
     */
    public byte[] downloadFile(String secureUrl) throws IOException {
        try {
            URI uri = URI.create(secureUrl);
            URL url = uri.toURL();

            try (InputStream inputStream = url.openStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                byte[] fileContent = outputStream.toByteArray();
                log.info("Archivo descargado exitosamente. Tamaño: {} bytes", fileContent.length);

                return fileContent;
            }

        } catch (IOException e) {
            throw new IOException("Error al descargar el archivo desde Cloudinary: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IOException("URL inválida: " + secureUrl, e);
        }
    }

    /**
     * Obtiene la URL segura de un archivo ya subido en Cloudinary
     * @param publicId ID público del archivo en Cloudinary
     * @param resourceType tipo de recurso (image, video, raw)
     * @return URL segura del archivo
     */
    public String getSecureUrl(String publicId, String resourceType) {
        try {
            return cloudinary.url()
                    .resourceType(resourceType != null ? resourceType : IMAGE_RESOURCE_TYPE)
                    .secure(true)
                    .generate(publicId);

        } catch (Exception e) {
            throw new ClientException("Error al generar la URL segura: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene la URL segura de una imagen
     * @param publicId ID público del archivo
     * @return URL segura de la imagen
     */
    public String getSecureUrl(String publicId) {
        return getSecureUrl(publicId, IMAGE_RESOURCE_TYPE);
    }

    /**
     * Obtiene la URL segura de un PDF
     * @param publicId ID público del archivo
     * @return URL segura del PDF
     */
    public String getSecureUrlForPdf(String publicId) {
        return getSecureUrl(publicId, RAW_RESOURCE_TYPE);
    }

    /**
     * Elimina un archivo de Cloudinary
     * @param publicId ID público del archivo a eliminar
     * @param resourceType tipo de recurso
     * @return resultado de la operación
     * @throws IOException si ocurre un error durante la eliminación
     */
    public Map<String, Object> deleteFile(String publicId, String resourceType) throws IOException {
        try {
            Map<String, Object> deleteParams = ObjectUtils.asMap(
                    "resource_type", resourceType != null ? resourceType : IMAGE_RESOURCE_TYPE,
                    "type", "upload",
                    "invalidate", true
            );

            log.info("Cloudinary destroy -> publicId={}, resourceType={}, options={}", publicId, resourceType, deleteParams);
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, deleteParams);
            log.info("Resultado destroy: {}", result);
            return result;
        } catch (IOException e) {
            throw new IOException("Error al eliminar el archivo de Cloudinary: " + e.getMessage(), e);
        }
    }


    /**
     * Elimina una imagen de Cloudinary
     * @param publicId ID público de la imagen
     * @return resultado de la operación
     * @throws IOException si ocurre un error
     */
    public Map<String, Object> deleteFile(String publicId) throws IOException {
        return deleteFile(publicId, IMAGE_RESOURCE_TYPE);
    }

}
