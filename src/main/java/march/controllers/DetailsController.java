package march.controllers;

import march.Model;
import march.dao.DataResponse;
import march.dao.DataRow;
import march.models.Statement;
import march.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/details")
public class DetailsController {
    @Autowired
    private Model model;

    @GetMapping("/data")
    public ResponseEntity<DataResponse> getCategory(@RequestParam(name="statement",required = false) String statement,
                                                   @RequestParam(name="start", required = false) String start,
                                                   @RequestParam(name="end", required = false) String end,
                                                   @RequestParam(name="last", required = false) String last) {

        if(statement!=null && (start!=null|| end!=null) && last!=null) {
            return ResponseEntity.badRequest().build();
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return ResponseEntity.badRequest().build();
        }

        if(statement != null) {
            // Statement version
            Statement st = model.getStatement(statement);
            if(st == null) {
                return ResponseEntity.notFound().build();
            } else {
                return processTransactions(st.getTransactions());
            }
        } else if(last!=null) {
            return processTransactions(
                    model.getLastNStatements(Integer.parseInt(last)).stream()
                    .flatMap(s -> Arrays.stream(s.getTransactions())).toArray(Transaction[]::new)
            );
        }  else {
            // Range version
            return processTransactions(model.getTxnsInRange(start,end));
        }
    }

    private static ResponseEntity<DataResponse> processTransactions(Transaction[] transactions2) {
        final Transaction[] transactions = transactions2;
        final List<DataRow> rows = new ArrayList<>(transactions.length);
        for (int c = 0; c < transactions.length; ++c) {
            rows.add(
                    new DataRow(c, transactions[c].getDate(),
                            transactions[c].getDescription(),
                            transactions[c].getAmount(),
                            transactions[c].getCategory())
            );
        }

        return ResponseEntity.ok(new DataResponse(rows.toArray(new DataRow[]{})));
    }
}
