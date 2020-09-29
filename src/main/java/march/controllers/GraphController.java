package march.controllers;

import march.Model;
import march.dao.*;
import march.models.Transaction;
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
                                                    @RequestParam(name="resolution",required=false) String resolution) {
        final String breakdownOpt =
                breakdown!=null ? breakdown.toLowerCase() : "all";

        if(!(breakdownOpt.equals("all") || breakdownOpt.equals("category"))) {
            return ResponseEntity.badRequest().build();
        }

        if(statement!=null && (start!=null|| end!=null)) {
            return ResponseEntity.badRequest().build();
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return ResponseEntity.badRequest().build();
        }

        Transaction[] txns;
        if(statement != null) {
            // Return data based on the statement
            txns = model.getStatement(statement).getTransactions();
        } else {
            // Return data based on the date range
            txns = model.getTxnsInRange(start,end);
        }

        var charges = Stream.of(txns).filter( t -> t.getAmount() > 0.0);
        var payments = Stream.of(txns).filter( t -> t.getAmount() <0.0);

        //TODO -> Duplicate code here.
        var chrgRes = charges
                .map( t -> new Tuple2(getKey(breakdownOpt, resolution, t), t.getAmount()))
                .collect(
                        Collectors.groupingBy(
                            Tuple2::getFirst, Collectors.summingDouble( t -> (Double)t.getSecond() )
                        )
                );

        var payRes = payments.map( t -> new Tuple2(getKey("all", resolution, t), t.getAmount()))
                            .collect(
                                    Collectors.groupingBy(
                                            Tuple2::getFirst, Collectors.summingDouble( t -> (Double)t.getSecond() )
                                    )
                            );

        // We now have all the transactions
        //  need to split into series
        Function<Object,String> keyExtractor =
                (r) -> {
                    var entry = ((Entry<Tuple2<Instant,String>,Double>)r).getKey();
                    return entry.getSecond();
                };

        var series = chrgRes.entrySet().stream().collect(
                Collectors.groupingBy(
                        keyExtractor,
                        Collectors.toList()
                )
        );

        var paymentSeries = payRes.entrySet().stream().collect(
                Collectors.groupingBy(
                        keyExtractor,
                        Collectors.toList()
                )
        );

        // Now we need to transform into our results
        final List<Series> result = new ArrayList<>(series.size());
        for( var entry : series.entrySet() ) {
            // for each entry we create a number of TSDataPoints
            var dp = entry.getValue().stream()
                          .map( m -> {
                              var time = ((Tuple2<LocalDate,String>)m.getKey()).getFirst();
                              return new TSDataPoint(time, m.getValue());
                          })
                          .sorted()
                          .toArray(TSDataPoint[]::new);
            result.add(new Series(entry.getKey(), dp));
        }

        // Like above but translate payments into postive.
        if(paymentSeries.containsKey("all")) {
            var dp = paymentSeries.get("all").stream().map(
                    m -> {
                        var time = ((Tuple2<LocalDate, String>) m.getKey()).getFirst();
                        return new TSDataPoint(time, -1 * m.getValue());
                    })
                    .sorted()
                    .toArray(TSDataPoint[]::new);

            result.add(new Series("payment", dp));
        }
        // For payments, should be a single entry


        return ResponseEntity.ok(new TimeSeries(result.toArray(new Series[]{})));
    }

    @GetMapping("/Pie")
    public ResponseEntity<Pie> getPie(@RequestParam(name="statement", required = false) String statement,
                      @RequestParam(name="start", required = false) String start,
                      @RequestParam(name="end", required = false) String end) {

        if(statement!=null && (start!=null|| end!=null)) {
            return ResponseEntity.badRequest().build();
        } else if( (start == null && end != null) || (start != null && end == null)) {
            return ResponseEntity.badRequest().build();
        }

        Transaction[] txns;
        if(statement != null) {
            // Return data based on the statement
            txns = model.getStatement(statement).getTransactions();
        } else {
            // Return data based on the date range
            txns = model.getTxnsInRange(start,end);
        }

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

    /*
     * Generate our key
     */
    private static Tuple2<LocalDate, String> getKey(String breakOpt, String res, Transaction t) {
        LocalDate time = t.getDate();
        if(res == null) {
            res = "";
        }

        switch(res) {
            case "year" : {
                // Normalise to the year
                time = time.minusMonths(time.getMonthValue()).minusDays(time.getDayOfMonth());
                break;
            }
            case "month" : {
                time = time.minusDays(time.getDayOfMonth());
                break;
            }
            default:
                break;
        }

        return new Tuple2<>(
                time,
                breakOpt.equalsIgnoreCase("all") ? "all" : t.getCategory()
        );
    }
}
