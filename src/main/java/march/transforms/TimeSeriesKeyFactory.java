package march.transforms;

import march.models.Transaction;
import march.util.Tuple2;

import java.time.LocalDate;
import java.util.function.Function;

public abstract class TimeSeriesKeyFactory {
    public static Function<Transaction, Tuple2<LocalDate, String>> getKey(TimeSeriesTransform.Resolution resolution, TimeSeriesTransform.Breakdown breakdown) {
        Function<Transaction,LocalDate> transform = null;
        switch(resolution) {
            case DAY:
                transform = Transaction::getDate;
                break;
            case MONTH:
                transform = (txn) -> {
                    var time = txn.getDate();
                    return time.minusDays(time.getDayOfMonth());
                };
                break;
            case YEAR:
                transform = (txn) -> {
                    var time = txn.getDate();
                    return time.minusMonths(time.getMonthValue()).minusDays(time.getDayOfMonth());
                };
                break;

            default:
                throw new RuntimeException();
        }

        var labelFunc = getLabel(breakdown);
        var finalTransform = transform;
        return (t) -> new Tuple2<>(finalTransform.apply(t),labelFunc.apply(t));
    }

    public static Function<Transaction, String> getLabel(TimeSeriesTransform.Breakdown breakdown) {
        if(breakdown == TimeSeriesTransform.Breakdown.ALL) {
            return (t) -> "all";
        }
        return Transaction::getCategory;
    }
}


    /*
     * Generate our key
     */
  /*  private static Tuple2<LocalDate, String> getKey(String breakOpt, String res, Transaction t) {
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
    } */