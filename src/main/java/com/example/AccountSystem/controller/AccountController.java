package com.example.AccountSystem.controller;

import com.example.AccountSystem.dto.CreateAccount;
import com.example.AccountSystem.service.AccountService;
import com.example.AccountSystem.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AccountController {
    private final RedisTestService redisTestService;
    private final AccountService accountService;

    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }

    @GetMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {
        return CreateAccount.Response.from(accountService.createAccount(
                request.getUserId(), request.getInitialBalance()));
    }

}
