package march.dao;

public class Series {
    public String label;
    public TSDataPoint [] dataPoints;

    public Series(String label, TSDataPoint ... point) {
        this.label = label;
        this.dataPoints = point;
    }


}
