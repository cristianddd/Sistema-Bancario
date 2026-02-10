package com.bank.authservice.security;

import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JwtUtilMockitoTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserEntity user;

    private static final String SECRET_256BIT =
            "ChangeMeToASecureSecretKeyChangeMeToASecureSecretKey";
    private static final long EXPIRATION_MS = 60_000;

    @BeforeEach
    void setUp() throws Exception {
        setField(jwtUtil, "secret", SECRET_256BIT);
        setField(jwtUtil, "expirationMs", EXPIRATION_MS);
        jwtUtil.init();

        when(user.getUsername()).thenReturn("cristian");
        when(user.getRoles()).thenReturn(Set.of(Role.USER, Role.ADMIN));
    }

    @Test
    void generateToken_shouldCreateToken_withSubjectAndRoles() {
        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals("cristian", jwtUtil.extractUsername(token));

        String roles = jwtUtil.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));

        verify(user, atLeastOnce()).getUsername();
        verify(user, atLeastOnce()).getRoles();
    }

    @Test
    void validateToken_shouldReturnTrue_whenValid() {
        String token = jwtUtil.generateToken(user);

        assertTrue(jwtUtil.validateToken(token, user));
    }

    @Test
    void validateToken_shouldReturnFalse_whenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(user);

        UserEntity other = mock(UserEntity.class);
        when(other.getUsername()).thenReturn("other");
        assertFalse(jwtUtil.validateToken(token, other));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenExpired() throws Exception {
        String expiredToken = invokeGenerateTokenWithCustomExpiration(jwtUtil, user, -1);

        assertFalse(jwtUtil.validateToken(expiredToken, user));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenSignedWithDifferentSecret() throws Exception {
        JwtUtil otherJwt = new JwtUtil();
        setField(otherJwt, "secret", "AnotherSecretKeyThatIsLongEnough_32bytes_minimum!!");
        setField(otherJwt, "expirationMs", EXPIRATION_MS);
        otherJwt.init();

        String token = otherJwt.generateToken(user);

        assertFalse(jwtUtil.validateToken(token, user));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static String invokeGenerateTokenWithCustomExpiration(JwtUtil util, UserEntity user, long expMs) throws Exception {
        var m = JwtUtil.class.getDeclaredMethod("generateToken", UserEntity.class, long.class);
        m.setAccessible(true);
        return (String) m.invoke(util, user, expMs);
    }
}
