package com.example.AccountSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class CreateAccount {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(0)
        private Long initialBalance;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime registeredAt;

        public static Response from(AccountDTO accountDTO) {
            return Response.builder()
                    .userId(accountDTO.getUserId())
                    .accountNumber(accountDTO.getAccountNumber())
                    .registeredAt(accountDTO.getRegisteredAt())
                    .build();
        }
    }

}
