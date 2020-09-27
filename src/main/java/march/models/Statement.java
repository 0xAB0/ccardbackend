package march.models;

import java.time.Instant;
import java.time.LocalDate;

public class Statement {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Transaction [] transactions;

    public Statement() {}


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

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
    }

}
