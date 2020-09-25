package march.dao;

public class ImportResponse {
    private final String status;
    private final int id;

    public ImportResponse(String status, int id) {
        this.status = status;
        this.id = id;
    }

    public int getId() { return id; }

    public String getStatus() {
        return status;
    }
}
