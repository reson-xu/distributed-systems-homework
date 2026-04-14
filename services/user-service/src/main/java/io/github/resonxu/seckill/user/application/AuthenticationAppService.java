package io.github.resonxu.seckill.user.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.user.domain.model.User;
import io.github.resonxu.seckill.user.domain.repository.UserRepository;
import io.github.resonxu.seckill.user.interfaces.dto.AuthLoginDTO;
import io.github.resonxu.seckill.user.interfaces.dto.AuthRegisterDTO;
import io.github.resonxu.seckill.user.interfaces.vo.AuthLoginVO;
import io.github.resonxu.seckill.user.interfaces.vo.AuthRegisterVO;
import io.github.resonxu.seckill.user.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 处理认证相关应用服务。
 */
@Service
@RequiredArgsConstructor
public class AuthenticationAppService {

    private static final Integer USER_STATUS_ENABLED = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    /**
     * 注册用户。
     *
     * @param request 注册请求
     * @return 注册结果
     */
    public AuthRegisterVO register(AuthRegisterDTO request) {
        User existingUser = userRepository.findActiveByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(USER_STATUS_ENABLED);
        user.setIsDeleted(0);

        try {
            userRepository.save(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        return AuthRegisterVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    public AuthLoginVO login(AuthLoginDTO request) {
        User user = userRepository.findActiveByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }
        if (!USER_STATUS_ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }

        String token = jwtTokenService.generateToken(user.getId(), user.getUsername());
        return AuthLoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
    }
}
