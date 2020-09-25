package march.controllers;

import march.Model;
import march.dao.NameValue;
import march.models.Transaction;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/summary")
public class SummaryController {

    @Autowired
    public Model model;

    @GetMapping("/individual")
    public ResponseEntity<NameValue[]> getIndividual(@RequestParam(name="statement",required = false) String statement,
                                                    @RequestParam(name="start", required = false) String start,
                                                    @RequestParam(name="end", required = false) String end) {

        final Transaction[] txns = getTransactions(statement, start, end);
        if(txns == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(Stream.of(txns).sorted(
            Transaction.getDecendingSort()
        ).limit(5).map( t -> new NameValue(t.getDescription(), t.getAmount())).toArray(NameValue[]::new));
    }

    @GetMapping("/frequent")
    public ResponseEntity<NameValue[]> getFrequent(@RequestParam(name="statement",required = false) String statement,
                                            @RequestParam(name="start", required = false) String start,
                                            @RequestParam(name="end", required = false) String end) {

        final Transaction[] txns = getTransactions(statement, start, end);
        if(txns == null) {
            return ResponseEntity.badRequest().build();
        }

        var grouped = Stream.of(txns)
              .collect(
                      Collectors.groupingBy(
                              Transaction::getDescription,
                              Collectors.summingDouble(Transaction::getAmount)
                      )
              );

        return ResponseEntity.ok(grouped.entrySet()
               .stream()
               .sorted( (l,r) -> Double.compare(r.getValue(),l.getValue()) )
               .limit(5)
               .map( e -> {
                   return new NameValue(e.getKey(), e.getValue());
               }) .toArray(NameValue[]::new));
    }

    @GetMapping("/category")
    public ResponseEntity<NameValue[]> getCategory(@RequestParam(name="statement",required = false) String statement,
                                   @RequestParam(name="start", required = false) String start,
                                   @RequestParam(name="end", required = false) String end) {
        final Transaction[] txns = getTransactions(statement, start, end);
        if(txns == null) {
            return ResponseEntity.badRequest().build();
        }

        var grouped = Stream.of(txns)
                .collect(
                        Collectors.groupingBy(
                                Transaction::getCategory,
                                Collectors.summingDouble(Transaction::getAmount)
                        )
                );

        return ResponseEntity.ok(grouped.entrySet()
                .stream()
                .sorted( (l,r) -> Double.compare(r.getValue(),l.getValue()) )
                .limit(5)
                .map( e -> {
                    return new NameValue(e.getKey(), e.getValue());
                }) .toArray(NameValue[]::new));
    }

    private Transaction[] getTransactions(String statement, String start, String end) {
        if(statement!=null && (start!=null|| end!=null)) {
            return null;
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return null;
        }

        if(statement!=null) {
            return model.getStatement(statement).getTransactions();
        } else {
            return model.getTxnsInRange(start,end);
        }
    }
}
