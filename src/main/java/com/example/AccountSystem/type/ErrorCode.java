package com.example.AccountSystem.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    MAX_ACCOUNT_PER_USER_10("계좌는 사용자당 최대 10개까지 만들 수 있습니다."),
    ACCOUNT_NOT_FOUND("해당 계좌를 찾을 수 없습니다."),
    USER_ACCOUNT_UN_MATCH("사용자와 계좌소유주가 일치하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("이미 해지된 계좌입니다."),
    BALANCE_NOT_EMPTY("잔액이 남아있습니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔액보다 큽니다."),
    TRANSACTION_NOT_FOUND("거래내역을 찾을 수 없습니다."),
    TRANSACTION_ACCOUNT_UN_MATCH("이 거래는 해당 계좌의 거래가 아닙니다."),
    CANCEL_MUST_FULLY("거래 금액과 취소 금액이 일치하지 않습니다.(부분 취소 불가능)"),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용 중입니다.")

    ;

    private final String description;
}
