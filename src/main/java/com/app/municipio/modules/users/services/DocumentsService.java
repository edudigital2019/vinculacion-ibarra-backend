package com.app.municipio.modules.users.services;

import com.app.municipio.application.cloudinary.CloudinaryService;
import com.app.municipio.application.cloudinary.UploadFileInfo;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.FileInfo;
import com.app.municipio.modules.users.dto.Responses.PaymentReceiptDto;
import com.app.municipio.modules.users.repositories.CertificatesRepository;
import com.app.municipio.modules.users.repositories.PaymentReceiptRepository;
import com.app.municipio.modules.users.repositories.SignedDocumentsRepository;
import com.app.municipio.modules.users.repositories.UserDocumentsRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Service
@Log4j2
public class DocumentsService {
    private final CertificatesRepository certificatesRepository;
    private final UserDocumentsRepository userDocumentsRepository;
    private final CloudinaryService cloudinaryService;
    private final SignedDocumentsRepository signedDocumentsRepository;
    private final PaymentReceiptRepository paymentReciptRepository;


    public List<UploadFileInfo> uploadUserDocuments(
            MultipartFile identityDocument,
            MultipartFile certificate,
            MultipartFile signedDocument,
            MultipartFile paymentReceipt
    ) {
        var filesToUpload = List.of(
                Map.entry(identityDocument, "identificaciones"),
                Map.entry(certificate, "certificados"),
                Map.entry(signedDocument, "documentos-firmados"),
                Map.entry(paymentReceipt, "comprobantes-pago")
        );

        List<UploadFileInfo> results = new ArrayList<>();

        for (var fileEntry : filesToUpload) {
            try {
                var uploadData = cloudinaryService.uploadFile(fileEntry.getKey(), fileEntry.getValue());
                results.add(UploadFileInfo.builder()
                        .secureUrl(uploadData.get(0))
                        .publicId(uploadData.get(1))
                        .resourceType(uploadData.get(2))
                        .build());
            } catch (IOException e) {
                rollback(results);
                throw new ClientException("Error al subir los archivos de identificación", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return results;
    }

    public FileInfo getIdentityDocumentFile(Long userId) {
        try {
            var document = userDocumentsRepository.findByAppUserId(userId)
                    .orElseThrow(() -> new ClientException("Documento de identidad no encontrado", HttpStatus.NOT_FOUND));

            byte[] fileContent = cloudinaryService.downloadFile(document.getPersonalIdUrl());

            String contentType = determineContentType(document.getFileType());
            String filename = generateFilename(userId, document.getFileType(), "documento_identidad_");

            return FileInfo.of(filename, contentType, fileContent);

        } catch (IOException e) {
            throw new ClientException("Error al obtener el archivo de identificación", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public FileInfo getCertificateFile(Long userId) {
        try {
            var certificate = certificatesRepository.findByAppUserId(userId)
                    .orElseThrow(() -> new ClientException("Certificado no encontrado", HttpStatus.NOT_FOUND));

            byte[] fileContent = cloudinaryService.downloadFile(certificate.getCertificatesUrl());

            String contentType = determineContentType(certificate.getFileType());
            String filename = generateFilename(userId, certificate.getFileType(), "certificado_");

            return FileInfo.of(filename, contentType, fileContent);

        } catch (IOException e) {
            throw new ClientException("Error al obtener el certificado", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String determineContentType(String resourceType) {
        return switch (resourceType.toLowerCase()) {
            case "image" -> "image/jpeg";
            case "raw" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private String generateFilename(Long userId, String resourceType, String prefix) {
        String extension = getFileExtension(resourceType);
        return prefix + userId + extension;
    }

    private String getFileExtension(String resourceType) {
        return switch (resourceType.toLowerCase()) {
            case "image" -> ".jpg";
            case "raw" -> ".pdf";
            default -> "";
        };
    }
    public PaymentReceiptDto getPaymentReceipt(Long userId) {
        return paymentReciptRepository.findByAppUserId(userId)
                .map(receipt -> new PaymentReceiptDto(receipt.getPaymentUrl()))
                .orElseThrow(() -> new ClientException("Comprobante de pago no encontrado", HttpStatus.NOT_FOUND));
    }

    private UploadFileInfo getCertificate(Long userId) {
        return certificatesRepository.findByAppUserId(userId)
                .map(cert -> UploadFileInfo.builder()
                        .secureUrl(cert.getCertificatesUrl())
                        .publicId(cert.getPublicId())
                        .resourceType(cert.getFileType())
                        .build())
                .orElseThrow(() -> new ClientException("Certificado no encontrado", HttpStatus.NOT_FOUND));
    }
    private UploadFileInfo getIdentity(Long userId) {
        return userDocumentsRepository.findByAppUserId(userId)
                .map(doc -> UploadFileInfo.builder()
                        .secureUrl(doc.getPersonalIdUrl())
                        .publicId(doc.getPublicId())
                        .resourceType(doc.getFileType())
                        .build())
                .orElseThrow(() -> new ClientException("Documento de identidad no encontrado", HttpStatus.NOT_FOUND));
    }

    public void rollbackFiles(Long userId) {
        List<UploadFileInfo> savedFiles = List.of(getIdentity(userId), getCertificate(userId));
        rollback(savedFiles);
    }

    public void rollback(@NotNull List<UploadFileInfo> savedFiles) {
        for (UploadFileInfo file : savedFiles) {
            try {
                cloudinaryService.deleteFile(file.getPublicId(), file.getResourceType());
            } catch (IOException e) {
                log.error("Error al eliminar el archivos {}: {}", file, e.getMessage());
            }
        }
    }

    public void rollback(@NotNull UploadFileInfo file) {
        try {
            cloudinaryService.deleteFile(file.getPublicId(), file.getResourceType());
        } catch (IOException e) {
            log.error("Error al eliminar el archivo {}: {}", file, e.getMessage());
        }
    }


    /**
     * Obtiene el documento firmado de un usuario
     */
    public FileInfo getSignedDocumentFile(Long userId) {
        try {
            var signedDoc = signedDocumentsRepository.findByAppUserId(userId)
                    .orElseThrow(() -> new ClientException("Documento firmado no encontrado", HttpStatus.NOT_FOUND));

            byte[] content = cloudinaryService.downloadFile(signedDoc.getSignedUrl());

            String contentType = determineContentType(signedDoc.getFileType());
            String filename = generateFilename(userId, signedDoc.getFileType(), "documento_firmado_");

            return FileInfo.of(filename, contentType, content);

        } catch (IOException e) {
            throw new ClientException("Error al obtener el documento firmado", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
