package antifraud.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionInfo {
    AMOUNT("amount"),
    CARD_NUMBER("card-number"),
    IP("ip"),
    IP_CORRELATION("ip-correlation"),
    REGION_CORRELATION("region-correlation"),
    NONE("none");

    private String text;

    TransactionInfo(String text) {
        this.text = text;
    }
}
