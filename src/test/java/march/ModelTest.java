package march;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

public class ModelTest {

    private Model model;


    @Before
    public void setup() {
        model = new Model();
    }

    @Ignore
    @Test
    public void importTest() throws IOException {
        var stream = ModelTest.class.getResourceAsStream("/example_statement.csv");
        var csv = new String(stream.readAllBytes());

        var result = model.importCsv("Import1", csv);
        Assert.assertEquals("ok", result.getStatus());

        var txns = model.getPendingTransactions(1);

        Assert.assertEquals(78, txns.length);

        Assert.assertEquals(39.95, txns[0].getAmount(), 0.000001);
    }

    @Ignore
    @Test
    public void getTransactionsPerStatement() throws IOException {
        importAndCommitTestData();

        Assert.assertArrayEquals(new String[]{"Import1"}, model.getStatementNames());


        var txns = model.getStatement("Import1").getTransactions();
        Assert.assertEquals(78, txns.length);


    }


    private void importAndCommitTestData() throws IOException {
        var stream = ModelTest.class.getResourceAsStream("/example_statement.csv");
        var csv = new String(stream.readAllBytes());

        var result = model.importCsv("Import1", csv);
        model.commitPending(result.getId());
    }

}
