package march.util;

import java.time.format.DateTimeFormatter;

public abstract class DateUtils {

    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
}
