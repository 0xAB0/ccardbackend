package march.dao;

public class DataResponse {
    private DataRow[] rows;

    public DataResponse(DataRow ... rows) {
        this.rows = rows;
    }

    public DataRow[] getRows() {
        return rows;
    }
}
