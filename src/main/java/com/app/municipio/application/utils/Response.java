package com.app.municipio.application.utils;

import com.app.municipio.application.exceptions.ApResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Response {

    /* ===================== */
    /* RESPUESTAS OK (200)   */
    /* ===================== */

    // Solo mensaje
    public static ResponseEntity<ApResponse> success(String message) {
        return ResponseEntity.ok(
                new ApResponse(true, message, null, null)
        );
    }

    // Mensaje + data (sin total)
    public static ResponseEntity<ApResponse> success(String message, Object data) {
        return ResponseEntity.ok(
                new ApResponse(true, message, null, data)
        );
    }

    //  Mensaje + total + data (NUEVO, sin romper nada)
    public static ResponseEntity<ApResponse> successWithTotal(
            String message,
            Object data,
            Integer total
    ) {
        return ResponseEntity.ok(
                new ApResponse(true, message, total, data)
        );
    }

    /* ===================== */
    /* RESPUESTAS CREATED   */
    /* ===================== */

    public static ResponseEntity<ApResponse> created(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApResponse(true, message, null, data));
    }

    /* ===================== */
    /* RESPUESTAS SIN DATA  */
    /* ===================== */

    public static ResponseEntity<ApResponse> noContent(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApResponse(true, message, null, null));
    }

    /* ===================== */
    /* RESPUESTAS ERROR     */
    /* ===================== */

    public static ResponseEntity<ApResponse> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApResponse(false, message, null, null));
    }

    public static ResponseEntity<ApResponse> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApResponse(false, message, null, null));
    }

    public static ResponseEntity<ApResponse> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApResponse(false, message, null, null));
    }
}
