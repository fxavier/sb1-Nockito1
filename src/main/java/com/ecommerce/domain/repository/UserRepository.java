package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.User;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
    public Uni<User> findByEmail(String email) {
        return find("email", email).firstResult();
    }
    
    public Uni<User> findByPhoneNumber(String phoneNumber) {
        return find("phoneNumber", phoneNumber).firstResult();
    }
    
    public Uni<User> findByGoogleId(String googleId) {
        return find("googleId", googleId).firstResult();
    }
    
    public Uni<List<User>> findByEmailVerified(boolean verified) {
        return list("emailVerified", verified);
    }
    
    public Uni<Boolean> existsByEmail(String email) {
        return count("email", email)
            .map(count -> count > 0);
    }
    
    public Uni<Boolean> existsByPhoneNumber(String phoneNumber) {
        return count("phoneNumber", phoneNumber)
            .map(count -> count > 0);
    }
}