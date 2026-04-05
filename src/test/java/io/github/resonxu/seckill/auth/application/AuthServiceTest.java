package io.github.resonxu.seckill.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resonxu.seckill.auth.interfaces.dto.AuthLoginDTO;
import io.github.resonxu.seckill.auth.interfaces.dto.AuthRegisterDTO;
import io.github.resonxu.seckill.auth.interfaces.vo.AuthLoginVO;
import io.github.resonxu.seckill.auth.interfaces.vo.AuthRegisterVO;
import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.security.JwtTokenService;
import io.github.resonxu.seckill.user.application.UserService;
import io.github.resonxu.seckill.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserWhenUsernameNotExists() {
        AuthRegisterDTO request = new AuthRegisterDTO();
        request.setUsername("alice");
        request.setPassword("123456");

        when(userService.findActiveByUsername("alice")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userService.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthRegisterVO response = authService.register(request);

        assertEquals(1L, response.getUserId());
        assertEquals("alice", response.getUsername());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertEquals("alice", userCaptor.getValue().getUsername());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
        assertEquals(1, userCaptor.getValue().getStatus());
    }

    @Test
    void shouldRejectRegisterWhenUsernameAlreadyExists() {
        AuthRegisterDTO request = new AuthRegisterDTO();
        request.setUsername("alice");
        request.setPassword("123456");

        when(userService.findActiveByUsername("alice")).thenReturn(new User());

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("1001", exception.getCode());
    }

    @Test
    void shouldConvertDuplicateKeyDuringRegister() {
        AuthRegisterDTO request = new AuthRegisterDTO();
        request.setUsername("alice");
        request.setPassword("123456");

        when(userService.findActiveByUsername("alice")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userService.create(any(User.class))).thenThrow(new DuplicateKeyException("duplicate"));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("1001", exception.getCode());
    }

    @Test
    void shouldLoginSuccessfully() {
        AuthLoginDTO request = new AuthLoginDTO();
        request.setUsername("alice");
        request.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setStatus(1);

        when(userService.findActiveByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtTokenService.generateToken(1L, "alice")).thenReturn("jwt-token");

        AuthLoginVO response = authService.login(request);

        assertEquals(1L, response.getUserId());
        assertEquals("alice", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void shouldRejectLoginWhenPasswordMismatch() {
        AuthLoginDTO request = new AuthLoginDTO();
        request.setUsername("alice");
        request.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setStatus(1);

        when(userService.findActiveByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals("1003", exception.getCode());
        assertTrue(exception.getMessage().contains("invalid"));
    }
}
