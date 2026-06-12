package org.instantmessenger.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SesClient sesClient;
    private final String fromEmail;
    private final String baseUrl;

    public EmailService(SesClient sesClient,
                        @Value("${aws.ses.from-email}") String fromEmail,
                        @Value("${app.base-url}") String baseUrl) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
        this.baseUrl = baseUrl;
    }

    public void sendMessageNotification(String toEmail, String senderUsername, String channelName, String preview) {
        String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <p>You have a new message in <strong>%s</strong> from <strong>%s</strong>:</p>
                  <blockquote style="border-left: 4px solid #4f46e5; margin: 16px 0; padding: 8px 16px;
                                     background: #f5f3ff; border-radius: 4px; color: #374151;">
                    %s
                  </blockquote>
                  <p><a href="%s" style="color: #4f46e5;">Open InstantMessenger</a></p>
                </body>
                </html>
                """.formatted(channelName, senderUsername, preview, baseUrl);

        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data("New message from " + senderUsername + " in " + channelName)
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .build());
            log.info("Message notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send message notification to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verifyUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <h2>Welcome to InstantMessenger, %s!</h2>
                  <p>Please verify your email address by clicking the button below:</p>
                  <p style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #4f46e5; color: white; padding: 12px 24px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                      Verify Email Address
                    </a>
                  </p>
                  <p style="color: #6b7280; font-size: 14px;">
                    Or copy this link into your browser:<br>
                    <a href="%s">%s</a>
                  </p>
                  <p style="color: #6b7280; font-size: 12px;">
                    If you did not create an account, you can safely ignore this email.
                  </p>
                </body>
                </html>
                """.formatted(username, verifyUrl, verifyUrl, verifyUrl);

        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("Verify your InstantMessenger email").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .build());
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
