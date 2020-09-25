package march.models;

import java.time.Instant;
import java.time.LocalDate;

public class Statement {
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Transaction [] transactions;

    public Statement(String name, LocalDate startDate, LocalDate endDate, Transaction[] transactions) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transactions = transactions;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }
}
