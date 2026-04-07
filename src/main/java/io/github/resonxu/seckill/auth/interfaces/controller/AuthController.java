package io.github.resonxu.seckill.auth.interfaces.controller;

import io.github.resonxu.seckill.auth.application.AuthService;
import io.github.resonxu.seckill.auth.interfaces.dto.AuthLoginDTO;
import io.github.resonxu.seckill.auth.interfaces.dto.AuthRegisterDTO;
import io.github.resonxu.seckill.auth.interfaces.vo.AuthLoginVO;
import io.github.resonxu.seckill.auth.interfaces.vo.AuthRegisterVO;
import io.github.resonxu.seckill.common.annotation.OperationLog;
import io.github.resonxu.seckill.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "创建新的用户账号")
    @PostMapping("/register")
    @OperationLog(description = "用户注册")
    public Result<AuthRegisterVO> register(@Valid @RequestBody AuthRegisterDTO request) {
        return Result.success(authService.register(request));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "校验用户名密码并签发 JWT")
    @PostMapping("/login")
    @OperationLog(description = "用户登录")
    public Result<AuthLoginVO> login(@Valid @RequestBody AuthLoginDTO request) {
        return Result.success(authService.login(request));
    }
}
