package kz.haru.common.utils.shader.shaders;

import kz.haru.common.utils.shader.Shader;

public class OutlineShader extends Shader {
    private static final String SHADER_CODE = """
            #version 120
            uniform vec2 size;
            uniform vec4 round;
            uniform vec2 smoothness;
            uniform float value;
            uniform vec4 color;
            uniform vec4 outlineColor;
            uniform vec4 outlineColor1;
            uniform vec4 outlineColor2;
            uniform vec4 outlineColor3;
            uniform float outline;
            #define NOISE .5/255.0
            
            float test(vec2 vec_1, vec2 vec_2, vec4 vec_4) {
                vec_4.xy = (vec_1.x > 0.0) ? vec_4.xy : vec_4.zw;
                vec_4.x = (vec_1.y > 0.0) ? vec_4.x : vec_4.y;
                vec2 coords = abs(vec_1) - vec_2 + vec_4.x;
                return min(max(coords.x, coords.y), 0.0) + length(max(coords, vec2(0.0f))) - vec_4.x;
            }
            
            vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4) {
                vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                return color;
            }
            
            void main() {
                vec2 st = gl_TexCoord[0].st * size;
                vec2 halfSize = 0.5 * size;
                float sa = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value, round));
                float outline = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value - outline, round));
                float outlin1 = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value - 1, round));
            
                vec4 finalColor = mix(vec4(color.rgb, 0.0), vec4(color.rgb, color.a), sa);
            
                if (sa > outline) {
                    vec4 color = createGradient(gl_TexCoord[0].st, outlineColor, outlineColor1,outlineColor2,outlineColor3);
                    finalColor = vec4(color.r,color.g,color.b,outlin1);
                }
            
                gl_FragColor = finalColor;
            }
            """;

    @Override
    public String getShaderCode() {
        return SHADER_CODE;
    }
} 