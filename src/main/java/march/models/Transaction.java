package march.models;

import java.time.LocalDate;
import java.util.Comparator;

public class Transaction {
    private LocalDate date;
    private String description;
    private double amount;
    private String category;

    public Transaction () {}

    public Transaction(LocalDate date, String description, double amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static Comparator<Transaction> getDecendingSort() {
        return (t1,t2) -> Double.compare(t2.amount, t1.amount);
    }
}
