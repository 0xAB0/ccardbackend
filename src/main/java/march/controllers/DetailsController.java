package march.controllers;

import march.Model;
import march.dao.DataResponse;
import march.dao.DataRow;
import march.dao.NameValue;
import march.models.Statement;
import march.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/details")
public class DetailsController {
    @Autowired
    private Model model;

    @GetMapping("/data")
    public ResponseEntity<DataResponse> getCategory(@RequestParam(name="statement",required = false) String statement,
                                                   @RequestParam(name="start", required = false) String start,
                                                   @RequestParam(name="end", required = false) String end) {

        if(statement!=null && (start!=null|| end!=null)) {
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
                final Transaction[] transactions = st.getTransactions();
                final List<DataRow> rows = new ArrayList<>(transactions.length);
                for(int c=0;c<transactions.length;++c) {
                    rows.add(
                            new DataRow(c, transactions[c].getDate(),
                                    transactions[c].getDescription(),
                                    transactions[c].getAmount(),
                                    transactions[c].getCategory())
                    );
                }

                return ResponseEntity.ok(new DataResponse(rows.toArray(new DataRow[]{})));
            }
        } else {
            // Range version
            Transaction [] txns = model.getTxnsInRange(start,end);
            final List<DataRow> rows = new ArrayList<>(txns.length);
            for(int c=0;c<txns.length;++c) {
                rows.add(
                        new DataRow(c, txns[c].getDate(),
                                txns[c].getDescription(),
                                txns[c].getAmount(),
                                txns[c].getCategory())
                );
            }
            return ResponseEntity.ok(new DataResponse(rows.toArray(new DataRow[]{})));
        }
    }
}
