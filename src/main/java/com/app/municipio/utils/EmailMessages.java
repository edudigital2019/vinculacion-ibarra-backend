package com.app.municipio.utils;

public class EmailMessages {

    // Mensajes para registro de usuarios
    public static String getRegistrationReceivedMessage(String userName) {
        return "Hola " + userName + ",\n\n" +
               "Gracias por registrarte en la plataforma del Municipio.\n" +
               "Tu solicitud de registro está siendo revisada por nuestro equipo administrativo.\n\n" +
               "Recibirás una notificación por correo electrónico una vez que tu cuenta haya sido aprobada.\n\n" +
               "Si tienes alguna pregunta, no dudes en contactarnos.\n\n" +
               "Atentamente,\nEl equipo del Municipio";
    }

    public static String getRegistrationReceivedSubject() {
        return "Registro en Plataforma Municipal - En revisión";
    }
    public static String getStatusSubject(String status) {
        return "Tu cuenta ha sido " + status.toLowerCase() + " - Plataforma Municipal";
    }

    public static String getRecoveryPasswordSubject() {
        return "Código de Seguridad - Plataforma Municipal";
    }

    public static String getPasswordChangeSubject() {
        return "Actualización de datos - Plataforma Municipal";
    }

    // Mensajes para aprobación de cuenta
    public static String getApprovalMessage(String userName) {
        return "Hola " + userName + ",\n\n" +
                "Nos complace informarte que tu documentación ha sido revisada y aprobada por nuestro equipo.\n\n" +
                "Ya puedes acceder a todas las funcionalidades disponibles en la plataforma, incluyendo el registro de tu empresa.\n\n" +
                "Si tienes alguna dificultad o necesitas asistencia, no dudes en contactarnos.\n\n" +
                "Atentamente,\n" +
                "El equipo del Municipio";

    }


    // Mensajes para rechazo de cuenta
    public static String getRejectionMessage(String userName, String rejectionReason) {
        return """
                Hola %s,
                
                Lamentamos informarte que tu solicitud de registro ha sido rechazada por nuestro equipo administrativo.
                Razón del rechazo: %s.
                
                Si tienes alguna pregunta o necesitas más información, no dudes en contactarnos.
                
                Atentamente,
                
                El equipo del Municipio
                
                """.formatted(userName, rejectionReason);
    }

    public static String getRecoverOtpMessage(String username, String otp) {
        return """
               Hola %s,
               
               Te compartimos tu codigo de seguridad para tu proceso de recuperacion de claves.
               
               Tu codigo OTP es: %s.
               
               Atentamente,
               
               El equipo del Municipio
               """.formatted(username, otp);
    }
    public static String getChangePasswordMessage(String username ) {
        return """
               Hola %s,
               
              Te comunicamos que tu clave ha sido modificada con éxito.
               
               Atentamente,
               
               El equipo del Municipio
               """.formatted(username);
    }

}