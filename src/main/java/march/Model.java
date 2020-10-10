package march;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import march.dao.ImportResponse;
import march.models.Statement;
import march.models.Transaction;
import march.util.DateUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.PropertyValues;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Scope("singleton")
public class Model {
    private final List<Statement> statements;
    private final List<String> categories;

    private Transaction [] pendingTransaction;
    private String pendingStatementName;

    public Model() {
        statements = new ArrayList<>();
        categories = new ArrayList<>();
        pendingTransaction = new Transaction[]{};

        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var tempStatements =
                mapper.readerForListOf(Statement.class)
                .readValue(Model.class.getResourceAsStream("/data.json"));

        statements.addAll((List<Statement>)tempStatements);

        categories.addAll(statements.stream()
                  .flatMap( s -> Stream.of(s.getTransactions()) )
                  .map( t -> t.getCategory() )
                  .collect(
                          Collectors.toSet()
                  ));
    }

    private void save() throws IOException {
        /*ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        FileWriter fw = new FileWriter("c:/users/evan marchant/src/test/ccardbackend/data.json");
        mapper.writeValue(fw, statements);
        fw.close();*/
    }

    public String [] getStatementNames() {
        return statements.stream().map(s -> s.getName()).toArray(String[]::new);
    }

    public String[] getCategories() {
        return categories.stream().toArray(String[]::new);
    }

    public Statement getStatement(String statement) {
        for(Statement s : statements) {
            if(s.getName().equals(statement)) {
                return s;
            }
        }
        return null;
    }

    public Transaction[] getTxnsInRange(String startStr, String endStr) {
        final LocalDate start = LocalDate.parse(startStr);
        final LocalDate end = LocalDate.parse(endStr);

        ArrayList<Transaction> txns = new ArrayList<>();
        for(Statement s : statements) {

            if( inRange(s, start, end) ) {
                Stream.of(s.getTransactions())
                        .filter(
                                t -> inRange(t.getDate(), start, end)
                        )
                        .forEach(txns::add);
            }
        }
        return txns.toArray(new Transaction[]{});
    }

    private boolean inRange(Statement s, LocalDate start, LocalDate end) {
        // the statement might straddle the dates.
        return inRange(s.getStartDate(), start,end) || inRange(s.getEndDate(), start,end);
    }

    private boolean inRange(LocalDate t, LocalDate start, LocalDate end) {
        return (t.isEqual(start) || t.isEqual(end)) ||
                        (t.isAfter(start) && t.isBefore(end));

    }

    /*
    Date,Date entered,Reference,Description,Amount,
13/07/2020,14/07/2020,40000189,CLARKS.CO.UK            ,39.95,
*/

    public ImportResponse importCsv(String name, String csv) {
        // Strip off json
        System.out.println("Before" + csv);
        csv = csv.substring(2, csv.length() - 6);

        System.out.println("Import recieved:" + csv);

        try (CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT)) {
            pendingTransaction = parser.getRecords()
                    .stream()
                    .skip(1)
                    .map( rec -> {
                        var date = LocalDate.parse(rec.get(0), DateUtils.getDateFormatter());
                        var desc = rec.get(3);
                        var amount = Double.parseDouble(rec.get(4));

                        return new Transaction(
                             date, desc, amount, "Unknown"
                        );
                    }).toArray(Transaction[]::new);

            pendingStatementName = name;

            return new ImportResponse("ok", 1);
        } catch (IOException e) {
            //clear pending
            pendingTransaction = new Transaction[]{};

            e.printStackTrace();
            return new ImportResponse("fail:" + e.getMessage(), -1);
        }
    }

    public Transaction[] getPendingTransactions(int id) {
        return pendingTransaction;
    }

    public void commitPending(int parseInt) {
        LocalDate min = LocalDate.MAX;
        LocalDate max = LocalDate.MIN;

        for(Transaction t : pendingTransaction) {
            if(t.getDate().isBefore(min)) {
                min = t.getDate();
            }
            if(t.getDate().isAfter(max)) {
                max = t.getDate();
            }
        }

        Statement s = new Statement(pendingStatementName, min, max, pendingTransaction);
        statements.add(s);

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pendingTransaction = new Transaction[]{};
        pendingStatementName = "";
    }
}
