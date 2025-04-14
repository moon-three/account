package com.example.AccountSystem.exception;

import com.example.AccountSystem.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException {
    private ErrorCode errorCode;

    public AccountException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

}
