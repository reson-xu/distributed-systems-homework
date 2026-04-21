package io.github.resonxu.seckill.user.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.user.application.UserQueryAppService;
import io.github.resonxu.seckill.user.interfaces.vo.SeckillEligibilityVO;
import io.github.resonxu.seckill.user.interfaces.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户查询接口控制器。
 */
@Tag(name = "用户接口", description = "用户信息与秒杀资格查询")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryAppService userQueryAppService;

    /**
     * 查询用户信息。
     *
     * @param userId 用户ID
     * @return 用户基础信息
     */
    @Operation(summary = "查询用户信息", description = "根据用户ID返回用户基础资料")
    @GetMapping("/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return Result.success(userQueryAppService.getUserProfile(userId));
    }

    /**
     * 查询秒杀资格。
     *
     * @param userId 用户ID
     * @return 秒杀资格
     */
    @Operation(summary = "查询秒杀资格", description = "返回用户当前是否具备基础秒杀资格")
    @GetMapping("/{userId}/eligibility/seckill")
    public Result<SeckillEligibilityVO> getSeckillEligibility(@PathVariable Long userId) {
        return Result.success(userQueryAppService.getSeckillEligibility(userId));
    }
}
