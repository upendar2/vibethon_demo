package com.emailsender;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.List;
import java.util.Collections;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;

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
            message.setFrom(new InternetAddress(FROM_NAME));
            
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject != null ? subject : "(No Subject)");
            // 5. Handle Multipart Content (Body + Attachments)
            Multipart multipart = new MimeMultipart();

            // Add Text Body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textBody, "utf-8");
            multipart.addBodyPart(textPart);

            // Add HTML Body if exists
            if (htmlBody != null && !htmlBody.isEmpty()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);
            }

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

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}


