package com.example.AccountSystem.controller;

import com.example.AccountSystem.dto.AccountInfo;
import com.example.AccountSystem.dto.CreateAccount;
import com.example.AccountSystem.dto.DeleteAccount;
import com.example.AccountSystem.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {
        return CreateAccount.Response.from(accountService.createAccount(
                request.getUserId(), request.getInitialBalance()));
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ) {
        return DeleteAccount.Response.from(accountService.deleteAccount(
                request.getUserId(), request.getAccountNumber()));
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(
            @RequestParam("user_id") Long userId
    ) {
        return accountService.getAccountByUserId(userId)
                .stream().map(AccountInfo::from)
                .collect(Collectors.toList());
    }

}
