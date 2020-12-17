package march.transforms;


import static march.transforms.TimeSeriesTransform.*;

public class TimeSeriesTranformBuilder {
    private Breakdown breakdown = Breakdown.ALL;
    private Resolution resolution = Resolution.DAY;
    private Select select = Select.SUM;
    private Fill fill = Fill.NONE;
    private Payments payment = Payments.VISIBLE;
    private Interest interest = Interest.HIDDEN;
    private Balance balance = Balance.HIDDEN;

    private boolean error = false;


    public TimeSeriesTranformBuilder breakdown(String breakdown) {
        if(breakdown!=null || !breakdown.equals("null")) {
            if(breakdown.equals("category")) {
                this.breakdown = Breakdown.CATEGORY;
            } else if(breakdown.equals("all")) {
                this.breakdown = Breakdown.ALL;
            } else {
                error = true;
            }
        }
        return this;
    }

    public TimeSeriesTranformBuilder resolution(String resolution) {
        if(resolution!=null && !resolution.equals("null")) {
            if(resolution.equals("year")) {
                this.resolution = Resolution.YEAR;
            } else if(resolution.equals("month")) {
                this.resolution = Resolution.MONTH;
            } else if(resolution.equals("day")) {
                this.resolution = Resolution.DAY;
            } else {
                error = true;
            }
        }
        return this;
    }

    public TimeSeriesTranformBuilder select(String select) {
        if(select!=null && !select.equals("null")) {
            if(select.equals("sum")) {
                this.select = Select.SUM;
            } else if(select.equals("max")) {
                this.select = Select.MAX;
            } else if(select.equals("runningSum")) {
                this.select = Select.RSUM;
            } else {
                error = true;
            }
        }
        return this;
    }


    public TimeSeriesTranformBuilder fill(String fill) {
        if(fill!=null && !fill.equals("null")) {
            if(fill.equals("none")) {
                this.fill = Fill.NONE;
            } else if(fill.equals("zero")) {
                this.fill = Fill.ZERO;
            } else if(fill.equals("previous")) {
                this.fill = Fill.PREVIOUS;
            } else {
                error = true;
            }
        }
        return this;
    }

    public TimeSeriesTranformBuilder payments(String payments) {
        if(payments!=null && !payments.equals("null")) {
            if(payments.equals("visible")) {
                this.payment = Payments.VISIBLE;
            } else if(payments.equals("hidden")) {
                this.payment = Payments.HIDDEN;
            } else {
                error = true;
            }
        }
        return this;
    }

    public TimeSeriesTranformBuilder interest(String interest) {
        if(interest!=null && !interest.equals("null")) {
            if(interest.equals("visible")) {
                this.interest = Interest.VISIBLE;
            } else if(interest.equals("hidden")) {
                this.interest = Interest.HIDDEN;
            } else {
                error = true;
            }
        }
        return this;
    }

    public void balance(String balance) {
        if(balance!=null && !balance.equals("null")) {
            if(balance.equals("visible")) {
                this.balance = Balance.VISIBLE;
            } else if(balance.equals("hidden")) {
                this.balance = Balance.HIDDEN;
            } else {
                error = true;
            }
        }
    }

    public boolean isValid() {
        return !error;
    }

    public TimeSeriesTransform build() {
        return new TimeSeriesTransform(breakdown, resolution, select, fill, payment, interest, balance);
    }



}
