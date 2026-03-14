package com.emailsender;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.List;
import java.util.Collections;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;

public class EmailSender {

    /**
     * Helper class to represent an email attachment.
     */

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

    // Environment Variables
	private static final String FROM_NAME = System.getenv("FROM_NAME"); 
    private static final String SMTP_HOST = System.getenv("SMTP_HOST"); // e.g., smtp.gmail.com
    private static final String SMTP_PORT = System.getenv("SMTP_PORT"); // e.g., 587
    private static final String FROM_EMAIL = System.getenv("FROM_EMAIL"); 
    private static final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD"); // App Password

    // --- Overloaded Public Methods ---

    public static boolean sendEmail(String to, String subject, String textBody) {
        return sendEmailInternal(to, subject, textBody, null, Collections.emptyList());
    }

    public static boolean sendEmail(String to, String subject, String textBody, String htmlBody) {
        return sendEmailInternal(to, subject, textBody, htmlBody, Collections.emptyList());
    }

    public static boolean sendEmail(String to, String subject, String textBody, List<EmailAttachment> attachments) {
        return sendEmailInternal(to, subject, textBody, null, attachments);
    }

    // --- Core Internal Method ---
    private static boolean sendEmailInternal(String to, String subject, String textBody, String htmlBody, List<EmailAttachment> attachments) {
        
        // 1. Validation
        if (FROM_EMAIL == null || SMTP_PASSWORD == null || SMTP_HOST == null) {
            System.err.println("CRITICAL: SMTP Environment variables are not set!");
            return false;
        }

        // 2. SMTP Properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT != null ? SMTP_PORT : "587");

        // 3. Create Session with Authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, SMTP_PASSWORD);
            }
        });

        try {
            // 4. Create Message
        	Message message = new MimeMessage(session);
            
            // Set FROM with a Display Name
//            String displayName = (FROM_NAME != null) ? FROM_NAME : "Vibethon Admin";

            
            // Set FROM with Udbhav Theme Name
            String displayName = (FROM_NAME != null) ? FROM_NAME : "Udbhav 2K26";
            message.setFrom(new InternetAddress(FROM_EMAIL, displayName, "UTF-8"));
            
            if (to == null || to.trim().isEmpty()) return false;
            
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            // Text Part (Fallback)
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textBody, "utf-8");
            multipart.addBodyPart(textPart);
         // HTML Part with the Udbhav Footer
            MimeBodyPart htmlPart = new MimeBodyPart();
            String fullHtml = getThemedHtml(htmlBody != null ? htmlBody : textBody);
            htmlPart.setContent(fullHtml, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Add HTML Body if exists

            // Add Attachments
            for (EmailAttachment att : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                ByteArrayDataSource dataSource = new ByteArrayDataSource(att.getContentBytes(), att.getMimeType());
                attachPart.setDataHandler(new DataHandler(dataSource));
                attachPart.setFileName(att.getFilename());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);

            // 6. Send
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
               
               "    " +
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


