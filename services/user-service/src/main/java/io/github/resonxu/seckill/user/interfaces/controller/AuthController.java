package io.github.resonxu.seckill.user.interfaces.controller;

import io.github.resonxu.seckill.common.annotation.OperationLog;
import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.user.application.AuthenticationAppService;
import io.github.resonxu.seckill.user.application.UserQueryAppService;
import io.github.resonxu.seckill.user.interfaces.dto.AuthLoginDTO;
import io.github.resonxu.seckill.user.interfaces.dto.AuthRegisterDTO;
import io.github.resonxu.seckill.user.interfaces.vo.AuthLoginVO;
import io.github.resonxu.seckill.user.interfaces.vo.AuthMeVO;
import io.github.resonxu.seckill.user.interfaces.vo.AuthRegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口控制器。
 */
@Tag(name = "认证接口", description = "用户注册与登录")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthenticationAppService authenticationAppService;
    private final UserQueryAppService userQueryAppService;

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "创建新的用户账号")
    @PostMapping("/register")
    @OperationLog(description = "用户注册")
    public Result<AuthRegisterVO> register(@Valid @RequestBody AuthRegisterDTO request) {
        return Result.success(authenticationAppService.register(request));
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "校验用户名密码并签发 JWT")
    @PostMapping("/login")
    @OperationLog(description = "用户登录")
    public Result<AuthLoginVO> login(@Valid @RequestBody AuthLoginDTO request) {
        return Result.success(authenticationAppService.login(request));
    }

    /**
     * 查询当前登录用户信息。
     *
     * @param authorizationHeader Authorization 请求头
     * @return 当前用户信息
     */
    @Operation(summary = "查询当前用户", description = "根据 JWT 令牌解析当前登录用户")
    @GetMapping("/me")
    public Result<AuthMeVO> me(@RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader) {
        return Result.success(userQueryAppService.getCurrentUser(authorizationHeader));
    }

    /**
     * 退出登录。
     *
     * @param authorizationHeader Authorization 请求头
     * @return 处理结果
     */
    @Operation(summary = "退出登录", description = "当前版本为无状态 JWT，校验令牌后由客户端自行丢弃")
    @PostMapping("/logout")
    @OperationLog(description = "用户退出登录")
    public Result<Void> logout(@RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader) {
        userQueryAppService.logout(authorizationHeader);
        return Result.success();
    }
}
