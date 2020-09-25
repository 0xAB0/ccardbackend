package march.dao;

public class NameValue {
    public String name;
    public double value;

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public NameValue(String name, double value) {
        this.name = name;
        this.value = value;
    }
}
