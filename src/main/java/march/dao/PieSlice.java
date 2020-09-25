package march.dao;

public class PieSlice {
    private String label;
    private double pcent;
    private double actualValue;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getPcent() {
        return pcent;
    }

    public void setPcent(double pcent) {
        this.pcent = pcent;
    }

    public double getActualValue() {
        return actualValue;
    }

    public void setActualValue(double actualValue) {
        this.actualValue = actualValue;
    }

    public PieSlice(String label, double pcent, double actualValue) {
        this.label = label;
        this.pcent = pcent;
        this.actualValue = actualValue;
    }
}
