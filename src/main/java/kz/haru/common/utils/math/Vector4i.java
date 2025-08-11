package kz.haru.common.utils.math;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Vector4i {
    private int x, y, z, w;

    public Vector4i(int i) {
        this.x = i;
        this.y = i;
        this.z = i;
        this.w = i;
    }

    public Vector4i(int i1, int i2) {
        this.x = i1;
        this.y = i1;
        this.z = i2;
        this.w = i2;
    }
}
