package com.example.AccountSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class DeleteAccount {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;

        public static Response from(AccountDTO accountDTO) {
            return Response.builder()
                    .userId(accountDTO.getUserId())
                    .accountNumber(accountDTO.getAccountNumber())
                    .unRegisteredAt(accountDTO.getUnRegisteredAt())
                    .build();
        }
    }

}
