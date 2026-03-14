package com.emailsender;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import org.eclipse.angus.mail.iap.Response;

import java.util.List;
import java.util.Collections;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

// SendGrid Imports
import com.sendgrid.*;
import com.sendgrid.helpers.mail.objects.*;
import com.sendgrid.helpers.mail.objects.Content;

public class EmailSender {

    // Environment Variables
    private static final String FROM_NAME = System.getenv("FROM_NAME");
    private static final String FROM_EMAIL = System.getenv("FROM_EMAIL");
    
    // SMTP Config (For Local)
    private static final String SMTP_HOST = System.getenv("SMTP_HOST");
    private static final String SMTP_PORT = System.getenv("SMTP_PORT");
    private static final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD");

    // SendGrid Config (For Render)
    private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private static final boolean IS_RENDER = System.getenv("RENDER") != null;

    public static class EmailAttachment {
        private final byte[] contentBytes;
        private final String filename;
        private final String mimeType;

        public EmailAttachment(byte[] contentBytes, String filename, String mimeType) {
            this.contentBytes = contentBytes;
            this.filename = filename;
            this.mimeType = mimeType;
        }
        public byte[] getContentBytes() { return contentBytes; }
        public String getFilename() { return filename; }
        public String getMimeType() { return mimeType; }
    }

    public static boolean sendEmail(String to, String subject, String body) {
        return sendEmailInternal(to, subject, body, null, Collections.emptyList());
    }

    private static boolean sendEmailInternal(String to, String subject, String textBody, String htmlBody, List<EmailAttachment> attachments) {
        if (to == null || to.trim().isEmpty()) return false;

        String finalHtml = getThemedHtml(htmlBody != null ? htmlBody : textBody);

        if (IS_RENDER && SENDGRID_API_KEY != null) {
            System.out.println("[EMAIL] Detected Render Environment. Using SendGrid API...");
            return sendViaSendGrid(to, subject, textBody, finalHtml, attachments);
        } else {
            System.out.println("[EMAIL] Detected Local Environment. Using SMTP...");
            return sendViaSMTP(to, subject, textBody, finalHtml, attachments);
        }
    }

    // --- RENDER METHOD: SENDGRID API ---
    private static boolean sendViaSendGrid(String to, String subject, String textBody, String htmlBody, List<EmailAttachment> attachments) {
        com.sendgrid.helpers.mail.Mail mail = new com.sendgrid.helpers.mail.Mail(
                new com.sendgrid.helpers.mail.objects.Email(FROM_EMAIL, FROM_NAME),
                subject,
                new com.sendgrid.helpers.mail.objects.Email(to),
                new Content("text/plain", textBody)
        );
        mail.addContent(new Content("text/html", htmlBody));

        // Handle Attachments for SendGrid
        for (EmailAttachment att : attachments) {
            Attachments sgA = new Attachments();
            sgA.setContent(java.util.Base64.getEncoder().encodeToString(att.getContentBytes()));
            sgA.setType(att.getMimeType());
            sgA.setFilename(att.getFilename());
            sgA.setDisposition("attachment");
            mail.addAttachments(sgA);
        }

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            com.sendgrid.Response response = sg.api(request);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // --- LOCAL METHOD: SMTP ---
    private static boolean sendViaSMTP(String to, String subject, String textBody, String htmlBody, List<EmailAttachment> attachments) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT != null ? SMTP_PORT : "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            for (EmailAttachment att : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.setDataHandler(new DataHandler(new ByteArrayDataSource(att.getContentBytes(), att.getMimeType())));
                attachPart.setFileName(att.getFilename());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getThemedHtml(String bodyContent) {
        return "<html>" +
               "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>" +
               "  <div style='max-width: 600px; margin: auto; background-color: #ffffff; border: 1px solid #ddd;'>" +
               "    <div style='padding: 20px; color: #333; line-height: 1.6;'>" + bodyContent + "</div>" +
               "    <div style='background-color: #5a0a1a; padding: 30px; text-align: center; color: #ffffff; border-top: 4px solid #d4af37;'>" +
               "      <h2 style='color: #d4af37; margin: 0; font-size: 24px; text-transform: uppercase;'>Udbhav 2K26</h2>" +
               "      <p style='margin: 10px 0; font-size: 14px; opacity: 0.9;'>Vibethon Programming Concept | Andhra University</p>" +
               "      <div style='margin: 20px 0;'>" +
               "        <a href='#' style='margin: 0 10px;'><img src='https://cdn-icons-png.flaticon.com/32/733/733547.png' width='24' alt='FB' style='filter: invert(1);'></a>" +
               "        <a href='#' style='margin: 0 10px;'><img src='https://cdn-icons-png.flaticon.com/32/733/733579.png' width='24' alt='TW' style='filter: invert(1);'></a>" +
               "        <a href='#' style='margin: 0 10px;'><img src='https://cdn-icons-png.flaticon.com/32/2111/2111463.png' width='24' alt='IG' style='filter: invert(1);'></a>" +
               "        <a href='#' style='margin: 0 10px;'><img src='https://cdn-icons-png.flaticon.com/32/3536/3536505.png' width='24' alt='IN' style='filter: invert(1);'></a>" +
               "      </div>" +
               "      <p style='font-size: 11px; color: #d4af37;'>&copy; 2026 Udbhav. All rights reserved.</p>" +
               "    </div>" +
               "  </div>" +
               "</body>" +
               "</html>";
    }
}