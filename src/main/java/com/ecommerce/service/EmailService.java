package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;

@ApplicationScoped
public class EmailService {
    
    @Inject
    ReactiveMailer mailer;
    
    @ConfigProperty(name = "app.base-url")
    String baseUrl;
    
    public Uni<Void> sendPasswordResetEmail(User user) {
        String resetLink = baseUrl + "/reset-password?token=" + user.getResetToken();
        
        String htmlContent = buildPasswordResetEmail(user.getProfile().getFirstName(), resetLink);
        
        return mailer.send(Mail.withHtml(
            user.getEmail(),
            "Reset Your Password",
            htmlContent
        ));
    }
    
    private String buildPasswordResetEmail(String firstName, String resetLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333333; margin-bottom: 10px;">Reset Your Password</h1>
                        <p style="color: #666666; font-size: 16px;">Hello %s,</p>
                        <p style="color: #666666; font-size: 16px;">We received a request to reset your password.</p>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; 
                                  border-radius: 4px; font-size: 16px; display: inline-block;">
                            Reset Password
                        </a>
                    </div>
                    
                    <div style="text-align: center; margin-top: 20px;">
                        <p style="color: #666666; font-size: 14px;">
                            If you didn't request this, you can safely ignore this email.
                        </p>
                        <p style="color: #666666; font-size: 14px;">
                            This link will expire in 1 hour.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(firstName, resetLink);
    }
}