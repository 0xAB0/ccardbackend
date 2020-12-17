package march.controllers;

import march.Model;
import march.dao.*;
import march.models.Transaction;
import march.transforms.TimeSeriesTranformBuilder;
import march.util.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin
@RestController
@RequestMapping("/graph")
public class GraphController {
    @Autowired
    private Model model;


    /*
     * This is some terrible code; to much in this method
     *   needs breaking out into functional units...
     *
     *   at some point.
     */


    // TODO -> this function is to long; and needs
    //          breaking up.
    @GetMapping("/TimeSeries")
    public ResponseEntity<TimeSeries> getTimeSeries(@RequestParam(name="breakdown",required = false) String breakdown,
                                                   @RequestParam(name="start",required = false) String start,
                                                   @RequestParam(name="end",required=false) String end,
                                                   @RequestParam(name="statement",required=false) String statement,
                                                    @RequestParam(name="resolution",required=false) String resolution,
                                                    @RequestParam(name="select",required=false) String select,
                                                    @RequestParam(name="fill",required=false) String fill,
                                                    @RequestParam(name="payments",required=false) String payments,
                                                    @RequestParam(name="interest",required=false) String interest,
                                                    @RequestParam(name="balance",required=false) String balance,
                                                    @RequestParam(name="last", required=false) String last) {

        final TimeSeriesTranformBuilder builder = new TimeSeriesTranformBuilder();

        // Setup the transform builder
        builder.breakdown(breakdown)
                .resolution(resolution)
                .select(select)
                .fill(fill)
                .payments(payments)
                .interest(interest)
                .balance(balance);

        if(!builder.isValid()) {
            return ResponseEntity.badRequest().build();
        }

        // Get our transactions
        final var txns = getTransactions(statement, start, end, last);
        if(txns==null) {
            return ResponseEntity.badRequest().build();
        }


        // For payments, should be a single entry
        return ResponseEntity.ok(builder.build().transform(txns.getFirst(), txns.getSecond()));
    }

    @GetMapping("/Pie")
    public ResponseEntity<Pie> getPie(@RequestParam(name="statement", required = false) String statement,
                      @RequestParam(name="start", required = false) String start,
                      @RequestParam(name="end", required = false) String end,
                                      @RequestParam(name="last", required=false) String last) {

        if(statement!=null && (start!=null|| end!=null)&&last!=null) {
            return ResponseEntity.badRequest().build();
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return ResponseEntity.badRequest().build();
        }

        var txnsAndBalance = getTransactions(statement, start, end, last);
        var txns = txnsAndBalance.getFirst();

        var res = Stream.of(txns)
                .filter(t -> t.getAmount()>0) // Ignore payments.
                .map( t -> new Tuple2(t.getCategory(), t.getAmount()))
                .collect(
                        Collectors.groupingBy(
                                Tuple2::getFirst, Collectors.summingDouble( t -> (Double)t.getSecond() )
                        )
                );

        final double total = res.entrySet().stream().mapToDouble(t -> t.getValue().doubleValue()).sum();

        var sliceStream = res.entrySet().stream()
                .map( entry -> new PieSlice(
                        (String)entry.getKey(),
                        (entry.getValue()/total) * 100,
                        entry.getValue()
                ));


        return ResponseEntity.ok(new Pie(
               sliceStream.toArray(PieSlice[]::new)
        ));
    }

    private Tuple2<Transaction [],Double> getTransactions(String statement, String start, String end, String last) {
        if(statement!=null && (start!=null|| end!=null) && last!=null) {
            return null;
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return null;
        }

        Transaction[] txns = null;
        double openBalance = 0D;
        if(statement != null) {
            if(model.getStatement(statement)!=null) {
                // Return data based on the statement
                var stat = model.getStatement(statement);
                txns = stat.getTransactions();
                openBalance = stat.getOpenBalance();
            }
        } else if(last!=null) {
            // We want the last N statements
            var stats = model.getLastNStatements(Integer.parseInt(last));

            openBalance = stats.size()>0 ? stats.get(0).getOpenBalance() : 0D;
            // Build a list of transactions
            txns = stats.stream().flatMap( s -> Stream.of(s.getTransactions()) ).toArray(Transaction[]::new);
        } else {
            // Return data based on the date range
            txns = model.getTxnsInRange(start,end);
            //
            openBalance = model.getBalanceAt(start);
        }
        return new Tuple2<>(txns, openBalance);
    }


}
