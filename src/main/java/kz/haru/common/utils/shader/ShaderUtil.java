package kz.haru.common.utils.shader;

import kz.haru.client.ClientInfo;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.shader.shaders.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.ARBShaderObjects;

import java.io.*;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;

public class ShaderUtil implements IMinecraft {
    private final int programID;
    
    private static ShaderUtil roundShader;
    private static ShaderUtil textShader;
    private static ShaderUtil outlineShader;
    private static ShaderUtil roundTextureShader;
    private static ShaderUtil shadowShader;

    public ShaderUtil(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = glCreateProgram();
        try {
            int fragmentShaderID;
            fragmentShaderID = switch (fragmentShaderLoc) {
                case "round" -> createShader(new ByteArrayInputStream(new RoundShader().getShaderCode().getBytes()), GL_FRAGMENT_SHADER);
                case "text" -> createShader(new ByteArrayInputStream(new TextShader().getShaderCode().getBytes()), GL_FRAGMENT_SHADER);
                case "outline" -> createShader(new ByteArrayInputStream(new OutlineShader().getShaderCode().getBytes()), GL_FRAGMENT_SHADER);
                case "roundTexture" -> createShader(new ByteArrayInputStream(new RoundTextureShader().getShaderCode().getBytes()), GL_FRAGMENT_SHADER);
                default -> createShader(mc.getResourceManager().getResource(new ResourceLocation(fragmentShaderLoc)).getInputStream(), GL_FRAGMENT_SHADER);
            };
            glAttachShader(program, fragmentShaderID);

            int vertexShaderID = createShader(mc.getResourceManager().getResource(new ResourceLocation(vertexShaderLoc)).getInputStream(), GL_VERTEX_SHADER);
            glAttachShader(program, vertexShaderID);


        } catch (IOException e) {
            e.printStackTrace();
        }

        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;
    }

    public ShaderUtil(String fragmentShadersrc, boolean notUsed) {
        int program = glCreateProgram();
        int fragmentShaderID = createShader(new ByteArrayInputStream(fragmentShadersrc.getBytes()), GL_FRAGMENT_SHADER);
        int vertexShaderID = 0;
        try {
            vertexShaderID = createShader(mc.getResourceManager().getResource(new ResourceLocation(ClientInfo.clientName.toLowerCase() + "/shaders/vertex.vsh")).getInputStream(), GL_VERTEX_SHADER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        glAttachShader(program, fragmentShaderID);
        glAttachShader(program, vertexShaderID);


        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;

    }

    public ShaderUtil(String fragmentShaderLoc) {
        this(fragmentShaderLoc, ClientInfo.clientName.toLowerCase() + "/shaders/vertex.vsh");
    }

    // Статические методы для получения шейдеров
    public static ShaderUtil getRoundShader() {
        if (roundShader == null) {
            roundShader = new ShaderUtil("round");
        }
        return roundShader;
    }
    
    public static ShaderUtil getTextShader() {
        if (textShader == null) {
            textShader = new ShaderUtil("text");
        }
        return textShader;
    }
    
    public static ShaderUtil getOutlineShader() {
        if (outlineShader == null) {
            outlineShader = new ShaderUtil("outline");
        }
        return outlineShader;
    }
    
    public static ShaderUtil getRoundTextureShader() {
        if (roundTextureShader == null) {
            roundTextureShader = new ShaderUtil("roundTexture");
        }
        return roundTextureShader;
    }

    public void init() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public void setUniform(final String name, final float... args) {
        final int loc = ARBShaderObjects.glGetUniformLocationARB(this.programID, name);
        switch (args.length) {
            case 1: {
                ARBShaderObjects.glUniform1fARB(loc, args[0]);
                break;
            }
            case 2: {
                ARBShaderObjects.glUniform2fARB(loc, args[0], args[1]);
                break;
            }
            case 3: {
                ARBShaderObjects.glUniform3fARB(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                ARBShaderObjects.glUniform4fARB(loc, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }

    public void setUniformf(String var1, double... var2) {
        int var3 = ARBShaderObjects.glGetUniformLocationARB(this.programID, var1);
        switch (var2.length) {
            case 1 -> ARBShaderObjects.glUniform1fARB(var3, (float) var2[0]);
            case 2 -> ARBShaderObjects.glUniform2fARB(var3, (float) var2[0], (float) var2[1]);
            case 3 -> ARBShaderObjects.glUniform3fARB(var3, (float) var2[0], (float) var2[1], (float) var2[2]);
            case 4 -> ARBShaderObjects.glUniform4fARB(var3, (float) var2[0], (float) var2[1], (float) var2[2],
                    (float) var2[3]);
        }
    }

    public void setUniform(final String name, final int... args) {
        final int loc = ARBShaderObjects.glGetUniformLocationARB(this.programID, name);
        switch (args.length) {
            case 1: {
                ARBShaderObjects.glUniform1iARB(loc, args[0]);
                break;
            }
            case 2: {
                ARBShaderObjects.glUniform2iARB(loc, args[0], args[1]);
            }
            case 3: {
                ARBShaderObjects.glUniform3iARB(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                ARBShaderObjects.glUniform4iARB(loc, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }

    public void setUniformf(final String var1, final float... args) {
        final int var2 = ARBShaderObjects.glGetUniformLocationARB(this.programID, var1);
        switch (args.length) {
            case 1: {
                ARBShaderObjects.glUniform1fARB(var2, args[0]);
                break;
            }
            case 2: {
                ARBShaderObjects.glUniform2fARB(var2, args[0], args[1]);
                break;
            }
            case 3: {
                ARBShaderObjects.glUniform3fARB(var2, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                ARBShaderObjects.glUniform4fARB(var2, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }

    public static void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }

    public static void drawQuads() {
        float width = (float) window.getScaledWidth();
        float height = (float) window.getScaledHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public static void drawQuads(float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, readInputStream(inputStream));
        glCompileShader(shader);


        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }

        return shader;
    }

    public String readInputStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .map(line -> line + '\n')
                .collect(Collectors.joining());
    }
}
