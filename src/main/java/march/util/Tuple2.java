package march.util;

import java.util.Objects;

public class Tuple2<X,Y> {
    private final X x_;
    private final Y y_;

    public Tuple2(X x_, Y y_) {
        this.x_ = x_;
        this.y_ = y_;
    }

    public X getFirst() {
        return x_;
    }

    public Y getSecond() {
        return y_;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x_,y_);
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other.hashCode() == this.hashCode() && other instanceof Tuple2) {
            return ((Tuple2) other).getFirst().equals(getFirst()) &&
                    ((Tuple2) other).getSecond().equals(getSecond());
        }
        return false;
    }
}
