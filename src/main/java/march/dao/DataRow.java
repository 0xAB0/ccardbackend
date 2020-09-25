package march.dao;

import java.time.Instant;
import java.time.LocalDate;

public class DataRow {
    private final LocalDate date;
    private final String description;
    private final double amount;
    private final String category;
    private final int rowId;



    public DataRow(int rowId, LocalDate date, String description, double amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.rowId = rowId;
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

    public LocalDate getDate() {
        return date;
    }

    public int getRowId() {
        return rowId;
    }
}
