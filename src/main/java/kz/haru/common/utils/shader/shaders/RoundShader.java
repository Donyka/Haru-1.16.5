package kz.haru.common.utils.shader.shaders;

import kz.haru.common.utils.shader.Shader;

public class RoundShader extends Shader {
    private static final String SHADER_CODE = """
            uniform vec2 location, rectSize;
            uniform vec4 color1, color2, color3, color4;
            uniform vec4 radius;
            uniform float smoothness;
            
            float rdist(vec2 pos, vec2 size, vec4 radius) {
                radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
                radius.x  = (pos.y > 0.0) ? radius.x : radius.y;
            
                vec2 v = abs(pos) - size + radius.x;
                return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
            }
            
            float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
                vec2 center = size * 0.5;
                float dist = rdist(center - (coord * size), center - 1.0, radius);
                return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
            }
            
            vec4 createGradient(vec2 coords) {
                vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
                return color;
            }
            
            void main() {
                vec2 coord = gl_TexCoord[0].st;
                float alpha = ralpha(rectSize, coord, radius, smoothness);
                vec4 color = createGradient(coord);
                gl_FragColor = vec4(color.rgb, color.a * alpha);
            }
            """;

    @Override
    public String getShaderCode() {
        return SHADER_CODE;
    }
} 