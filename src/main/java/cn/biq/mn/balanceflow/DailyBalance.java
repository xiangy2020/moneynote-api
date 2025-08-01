package cn.biq.mn.balanceflow;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyBalance {

    private LocalDate date;
    private BigDecimal amount;

    public DailyBalance(String date, BigDecimal amount) {
        this.date = LocalDate.parse(date);
        this.amount = amount;
    }
}
