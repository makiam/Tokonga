package ss;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.*;
import java.nio.FloatBuffer;

public class MinimalOpenGLWithShaders {

    private static final String VERTEX_SHADER_CODE =
            "#version 430 core\n" +
                    "layout(location=0) in vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private static final String FRAGMENT_SHADER_CODE =
            "#version 430 core\n" +
                    "out vec4 fragColor;" +
                    "void main() {" +
                    "  fragColor = vec4(1.0, 0.5, 0.2, 1.0);" + // 橙色
                    "}";

    public static void main(String... args) {
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);

        canvas.addGLEventListener(new GLEventListener() {
            private int shaderProgram;
            private int vao;
            private int vbo;

            @Override
            public void init(GLAutoDrawable drawable) {
                GL4 gl = drawable.getGL().getGL4();

                // 编译着色器
                int vertexShader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
                gl.glShaderSource(vertexShader, 1, new String[]{VERTEX_SHADER_CODE}, null);
                gl.glCompileShader(vertexShader);

                int fragmentShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
                gl.glShaderSource(fragmentShader, 1, new String[]{FRAGMENT_SHADER_CODE}, null);
                gl.glCompileShader(fragmentShader);

                // 创建着色器程序
                shaderProgram = gl.glCreateProgram();
                gl.glAttachShader(shaderProgram, vertexShader);
                gl.glAttachShader(shaderProgram, fragmentShader);
                gl.glLinkProgram(shaderProgram);

                // 删除着色器对象
                gl.glDeleteShader(vertexShader);
                gl.glDeleteShader(fragmentShader);

                // 三角形顶点数据 (NDC坐标)
                float[] vertices = {
                        -0.5f, -0.5f, 0.0f, // 左下
                        0.5f, -0.5f, 0.0f,  // 右下
                        0.0f, 0.5f, 0.0f    // 顶部
                };

                // 创建VAO和VBO
                int[] buffers = new int[2];
                gl.glGenVertexArrays(1, buffers, 0);
                gl.glGenBuffers(1, buffers, 1);
                vao = buffers[0];
                vbo = buffers[1];

                gl.glBindVertexArray(vao);
                gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo);
                FloatBuffer vertexBuffer = FloatBuffer.wrap(vertices);
                gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, vertexBuffer, GL3.GL_STATIC_DRAW);

                // 设置顶点属性指针
                gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 0, 0);
                gl.glEnableVertexAttribArray(0);

                gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
                gl.glBindVertexArray(0);
            }

            @Override
            public void display(GLAutoDrawable drawable) {
                GL4 gl = drawable.getGL().getGL4();
                gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
                gl.glUseProgram(shaderProgram);
                gl.glBindVertexArray(vao);
                gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                // 不需要实现
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {
                GL4 gl = drawable.getGL().getGL4();
                gl.glDeleteBuffers(1, new int[]{vbo}, 0);
                gl.glDeleteVertexArrays(1, new int[]{vao}, 0);
                gl.glDeleteProgram(shaderProgram);
            }
        });

        JFrame frame = new JFrame("Minimal OpenGL with Shaders");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.setVisible(true);
    }
}
