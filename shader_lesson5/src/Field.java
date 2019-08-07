import com.hackoeur.jglm.Mat3;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private int program = -1;
    private Matrix4 mvMatrix = new Matrix4();
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glDepthFunc( GL2.GL_LEQUAL );
        int vertexShader = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER,
                new File( "Shaders/vertex_shader.glsl" ) );
        int fragmentShader = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER,
                new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vertexShader, fragmentShader );
        gl.glUseProgram( program );
        setupData( gl );
    }

    private void setupData( GL2 gl ) {

        if( program != -1 ) {

            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "BrickColor" ), 1.0f, 0.3f, 0.2f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "MortarColor" ), 0.85f, 0.86f, 0.84f );
            gl.glUniform2f( ShaderUtils.getUniLoc( gl, program, "BrickSize" ), 0.3f, 0.15f );
            gl.glUniform2f( ShaderUtils.getUniLoc( gl, program, "BrickPct" ), 0.9f, 0.85f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "LightPosition" ), 0f, 0f, 5f );
            IntBuffer vboBuffers = IntBuffer.wrap( new int[ 2 ] );
            FloatBuffer vertexes = Buffers.newDirectFloatBuffer( new float[]{ -1.0f, -1.0f,
                    1.0f,  1.0f,
                    -1.0f,  1.0f, // треугольник 1
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    1.0f,  1.0f // треугольник 2
            } );
            FloatBuffer norms = Buffers.newDirectFloatBuffer( new float[]{ 0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f
            } );
            gl.glGenBuffers( 2, vboBuffers );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 0 ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, 12 * 4, vertexes, GL2.GL_STATIC_DRAW );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 1 ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, 18 * 4, norms, GL2.GL_STATIC_DRAW );
            gl.glGenVertexArrays( 1, vaoHandle );
            gl.glBindVertexArray( vaoHandle.get( 0 ) );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 0 ) );
            int mcVertLoc = ShaderUtils.getVaryingLoc( gl, program, "MCvertex");
            gl.glVertexAttribPointer( mcVertLoc, 2, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( mcVertLoc );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 1 ) );
            int mcNormLoc = ShaderUtils.getVaryingLoc( gl, program, "MCnormal" );
            gl.glVertexAttribPointer( mcNormLoc, 3, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( mcNormLoc );

            mvMatrix.translate( 0f, 0f, -2f );
            Matrix4 mvpMatrix = new Matrix4();
            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                    FloatBuffer.wrap( mvMatrix.getMatrix() ) );
            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVPMatrix" ), 1, false,
                    FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glDrawArrays( GL2.GL_TRIANGLES, 0,  6 );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.makePerspective( 45f, h, 0.1f, 100f );
        mvpMatrix.multMatrix( mvMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVPMatrix" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
    }
}
