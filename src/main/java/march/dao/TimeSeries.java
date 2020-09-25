package march.dao;

public class TimeSeries {

    private Series[] series;

    public TimeSeries(Series ...series) {
        this.series = series;
    }

    public Series[] getSeries() {
        return series;
    }
}
