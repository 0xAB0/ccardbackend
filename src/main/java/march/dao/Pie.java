package march.dao;

public class Pie {
    private PieSlice [] slice;

    public Pie(PieSlice ... slice) {
        this.slice = slice;
    }

    public PieSlice[] getSlices() {
        return slice;
    }
}
