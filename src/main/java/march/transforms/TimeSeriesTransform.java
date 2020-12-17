package march.transforms;

import march.dao.Series;
import march.dao.TSDataPoint;
import march.dao.TimeSeries;
import march.models.Transaction;
import march.util.Tuple2;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeSeriesTransform {


    public enum Breakdown { ALL, CATEGORY };
    public enum Select { SUM, MAX, RSUM };
    public enum Fill { NONE, PREVIOUS, ZERO };
    public enum Payments { VISIBLE, HIDDEN }
    public enum Resolution { DAY, MONTH, YEAR };
    public enum Interest { VISIBLE, HIDDEN };
    public enum Balance { VISIBLE, HIDDEN }

    private final Breakdown breakdown;
    private final Resolution resolution;
    private final Select select;
    private final Fill fill;
    private final Payments payment;
    private final Interest interest;
    private final Balance balance;

    private final Function<Transaction, Tuple2<LocalDate, String>> keyFunc;
    private final Function<Transaction, Tuple2<LocalDate, String>> paymentKeyFunc;

    public TimeSeriesTransform(Breakdown breakdown, Resolution resolution, Select select, Fill fill, Payments payment, Interest interest, Balance balance) {
        this.breakdown = breakdown;
        this.resolution = resolution;
        this.select = select;
        this.fill = fill;
        this.payment = payment;
        this.interest = interest;
        this.balance = balance;

        keyFunc = TimeSeriesKeyFactory.getKey(resolution, breakdown);
        paymentKeyFunc = TimeSeriesKeyFactory.getKey(resolution, Breakdown.ALL);
    }

    public TimeSeries transform(Transaction[] txns, double openBalance) {
        // Note; this is probably not anywhere near efficient.
        // Lets put the transactions in date order
        Arrays.sort(txns, Transaction.getDateSort());

        // Now, we need might need to discover our categories
        var categories = getCategories(txns);

        // So we'll store a linked list of data points for each series
        //  then step through transaction defining them.

        var data = new HashMap<String, LinkedList<TSDataPoint>>();

        if(fill != Fill.NONE) {
            // Seed our first data point
            categories.forEach( c -> {
                var list = new LinkedList<TSDataPoint>();

                if(c.equals("payment") || c.equals("interest")) {
                    var key = paymentKeyFunc.apply(txns[0]);
                    list.add(new TSDataPoint(key.getFirst(),0.0));
                } else {
                    var key = keyFunc.apply(txns[0]);
                    list.add(new TSDataPoint(key.getFirst(),0.0));
                }
                data.put(c, list);
            });
        }

        // TODO -> Fill;

        for(int c=0;c<txns.length;++c) {
            // Lets classify this txns
            var txn = txns[c];
            var isPayment = txn.getAmount()<0.0;
            var isInterest = (interest==Interest.VISIBLE) && txn.getDescription().contains("INTEREST");

            var key = isPayment ? paymentKeyFunc.apply(txn) : keyFunc.apply(txn);
            var category =
                    isPayment ? "payment" : (isInterest? "interest" :key.getSecond());

            // This item is included.
            if(categories.contains(category)) {
                var list = data.computeIfAbsent(category, (k) -> new LinkedList<>());

                if(list.isEmpty()) {
                    list.add(new TSDataPoint(key.getFirst(), txn.getAmount()));
                } else {
                    //First we need to workout if we're a new data point or not
                    // get the last one
                    var lastPoint = list.getLast();

                    if(lastPoint.getTime().isEqual(key.getFirst())) {
                        // Same bucket
                        if(select == Select.RSUM || select == Select.SUM) {
                            // We sum
                            lastPoint.sumValue(txn.getAmount());
                        } else {
                            lastPoint.swapIfGreater(txn.getAmount());
                        }
                    } else {
                        // Need a new bucket
                        var newPoint = new TSDataPoint(key.getFirst(),
                                                        select==Select.RSUM? lastPoint.getValue() + txn.getAmount() :
                                                        txn.getAmount());

                        list.add(newPoint);
                    }
                }
            }
        }

        Series [] series = new Series[categories.size() + ((balance==Balance.VISIBLE)?1:0)];
        int sidx = 0;
        // Separate out payment
        if(categories.contains("payment")) {
            series[sidx++] =
                new Series("payments", data.get("payment").stream().map( t -> t.invert()).toArray(TSDataPoint[]::new));
        }

        for(String cat : categories) {
            if(cat.equals("payment")) continue;

            series[sidx++] = new Series(cat, data.get(cat).stream().toArray(TSDataPoint[]::new));
        }

        if(balance == Balance.VISIBLE) {
            series[sidx++] = new Series("balance", getBalanceSeries(txns, resolution, openBalance));
        }
        return new TimeSeries(series);
    }

    private static TSDataPoint[] getBalanceSeries(Transaction[] txns, Resolution res, double openBalance) {
        var keyFunc = TimeSeriesKeyFactory.getKey(res, Breakdown.ALL);

        var ts = Stream.of(txns)
                .map(t -> new Tuple2<>(keyFunc.apply(t), t.getAmount()))
                .collect(
                        Collectors.groupingBy(
                                Tuple2::getFirst,
                                Collectors.summingDouble(Tuple2::getSecond)
                        )
                );

        Tuple2<Tuple2<LocalDate,String>,Double>[] entries =
                ts.entrySet().stream().map(e -> new Tuple2<>(e.getKey(),e.getValue())).toArray(Tuple2[]::new);

        Arrays.sort(entries, new Comparator<>() {
            @Override
            public int compare(Tuple2<Tuple2<LocalDate, String>, Double> x, Tuple2<Tuple2<LocalDate, String>, Double> y) {
                if(x.getFirst().getFirst().isBefore(y.getFirst().getFirst())) return -1;
                if(x.getFirst().getFirst().isEqual(y.getFirst().getFirst())) return 0;
                return 1;
            }
        });

        TSDataPoint[] pt = new TSDataPoint[ts.size()];
        var idx =0;
        var sum = openBalance;
        for(var entry : entries) {
            // Basically we now do the running sum.
            sum += entry.getSecond();

            pt[idx++] = new TSDataPoint(entry.getFirst().getFirst(), sum);
        }

        return pt;
    }

    private Set<String> getCategories(Transaction[] txns) {
        var result = new HashSet<String>();

        if(breakdown == Breakdown.ALL) {
            result.add("all");
        } else {
            // We need to find all the unique categories
            result.addAll(
                    Stream.of(txns).filter(t -> t.getAmount()>=0.0).map(t -> t.getCategory()).distinct().collect(Collectors.toList()));
        }

        if(payment == Payments.VISIBLE) {
            result.add("payment");
        }
        if(interest == Interest.VISIBLE && payment == Payments.VISIBLE) {
            result.add("interest");
        }

        return result;
    }
}
