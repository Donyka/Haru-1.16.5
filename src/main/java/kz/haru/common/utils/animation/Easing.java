package kz.haru.common.utils.animation;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

import static java.lang.Math.*;

@Getter
public enum Easing {
    LINEAR(x -> x),
    SIGMOID(x -> 1 / (1 + exp(-x))),

    QUAD_IN(x -> x * x),
    QUAD_OUT(x -> x * (2 - x)),
    QUAD_BOTH(x -> x < 0.5 ? 2 * x * x : -1 + (4 - 2 * x) * x),

    CUBIC_IN(x -> x * x * x),
    CUBIC_OUT(x -> (--x) * x * x + 1),
    CUBIC_BOTH(x -> x < 0.5 ? 4 * x * x * x : (x - 1) * (2 * x - 2) * (2 * x - 2) + 1),

    QUART_IN(x -> x * x * x * x),
    QUART_OUT(x -> 1 - (--x) * x * x * x),
    QUART_BOTH(x -> x < 0.5 ? 8 * x * x * x * x : 1 - 8 * (--x) * x * x * x),

    QUINT_IN(x -> x * x * x * x * x),
    QUINT_OUT(x -> 1 + (--x) * x * x * x * x),
    QUINT_BOTH(x -> x < 0.5 ? 16 * x * x * x * x * x : 1 + 16 * (--x) * x * x * x * x),

    SINE_IN(x -> 1 - cos(x * PI / 2)),
    SINE_OUT(x -> sin(x * PI / 2)),
    SINE_BOTH(x -> (1 - cos(PI * x)) / 2),

    EXPO_IN(x -> x == 0 ? 0 : pow(2, 10 * x - 10)),
    EXPO_OUT(x -> x == 1 ? 1 : 1 - pow(2, -10 * x)),
    EXPO_BOTH(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? pow(2, 20 * x - 10) / 2 : (2 - pow(2, -20 * x + 10)) / 2),

    CIRC_IN(x -> 1 - sqrt(1 - x * x)),
    CIRC_OUT(x -> sqrt(1 - (--x) * x)),
    CIRC_BOTH(x -> x < 0.5 ? (1 - sqrt(1 - 4 * x * x)) / 2 : (sqrt(1 - 4 * (x - 1) * x) + 1) / 2),

    BACK_IN(x -> (1.70158 + 1) * x * x * x - 1.70158 * x * x),
    BACK_OUT(x -> 1 + (1.70158 + 1) * pow(x - 1, 3) + (1.70158) * pow(x - 1, 2)),
    BACK_BOTH(x -> {
        double c1 = 1.70158 * 1.525;
        double c3 = c1 + 1;
        return x < 0.5 ? (pow(2 * x, 2) * (c3 * 2 * x - c1)) / 2 : (pow(2 * x - 2, 2) * (c3 * (2 * x - 2) + c1) + 2) / 2;
    }),

    ELASTIC_IN(x -> x == 0 ? 0 : x == 1 ? 1 : -pow(2, 10 * x - 10) * sin((x * 10 - 10.75) * ((2 * PI) / 3))),
    ELASTIC_OUT(x -> x == 0 ? 0 : x == 1 ? 1 : pow(2, -10 * x) * sin((x * 10 - 0.75) * ((2 * PI) / 3)) + 1),
    ELASTIC_BOTH(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? -(pow(2, 20 * x - 10) * sin((20 * x - 11.125) * (PI / 4.5))) : (pow(2, -20 * x + 10) * sin((20 * x - 11.125) * (PI / 4.5)) + 2) / 2),

    SHRINK(x -> {
        float easeAmount = 1.3f;
        float shrink = easeAmount + 1;
        return max(0, 1 + shrink * pow(x - 1, 3) + easeAmount * pow(x - 1, 2));
    });

    private final Function<Double, Double> function;

    Easing(final Function<Double, Double> function) {
        this.function = function;
    }

    public double apply(double x) {
        return getFunction().apply(x);
    }

    public float apply(float x) {
        return getFunction().apply((double) x).floatValue();
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase().replace("_", " "));
    }
}
