package io.github.resonxu.seckill.user.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.user.domain.model.User;
import io.github.resonxu.seckill.user.domain.repository.UserRepository;
import io.github.resonxu.seckill.user.interfaces.vo.AuthMeVO;
import io.github.resonxu.seckill.user.interfaces.vo.SeckillEligibilityVO;
import io.github.resonxu.seckill.user.interfaces.vo.UserProfileVO;
import io.github.resonxu.seckill.user.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class UserQueryAppService {

    private static final Integer USER_STATUS_ENABLED = 1;

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    /**
     * 查询当前登录用户。
     *
     * @param authorizationHeader Authorization 请求头
     * @return 当前用户信息
     */
    public AuthMeVO getCurrentUser(String authorizationHeader) {
        User user = loadUser(jwtTokenService.parseUserIdFromAuthorizationHeader(authorizationHeader));
        return AuthMeVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }

    /**
     * 退出登录。
     *
     * @param authorizationHeader Authorization 请求头
     */
    public void logout(String authorizationHeader) {
        jwtTokenService.parseUserIdFromAuthorizationHeader(authorizationHeader);
    }

    /**
     * 查询用户资料。
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    public UserProfileVO getUserProfile(Long userId) {
        User user = loadUser(userId);
        return UserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }

    /**
     * 查询秒杀资格。
     *
     * @param userId 用户ID
     * @return 秒杀资格
     */
    public SeckillEligibilityVO getSeckillEligibility(Long userId) {
        User user = loadUser(userId);
        boolean eligible = USER_STATUS_ENABLED.equals(user.getStatus());
        return SeckillEligibilityVO.builder()
                .userId(user.getId())
                .eligible(eligible)
                .reason(eligible ? "ELIGIBLE" : "USER_DISABLED")
                .build();
    }

    private User loadUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }
}
