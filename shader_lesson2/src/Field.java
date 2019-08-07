import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private int program;
    IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );

    private File vs = new File( "Shaders/vertex_shader.glsl" );
    private File fs = new File( "Shaders/fragment_shader.glsl" );

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glClearDepth( 1f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glDepthFunc( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        int vertexShaderId = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, vs );
        int fragmentShaderId = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, fs );
        program = ShaderUtils.createProgram( gl, vertexShaderId, fragmentShaderId );
        gl.glUseProgram( program );
        setData( gl, program );
    }

    private void setData( GL2 gl, int pId ) {

        FloatBuffer positionData = Buffers.newDirectFloatBuffer( new float[]{-0.8f, -0.8f, 0.0f,
                0.8f, -0.8f, 0.0f,
                0.0f, 0.8f, 0.0f});
        FloatBuffer colorData = Buffers.newDirectFloatBuffer( new float[] { 1.0f, 0.0f, 0.0f,
                                                                0.0f, 1.0f, 0.0f,
                                                                0.0f, 0.0f, 1.0f } );
        gl.glGenVertexArrays(1, vaoHandle );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        IntBuffer vboHandles = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenBuffers( 2, vboHandles );
        int positionBufferHandle = vboHandles.get( 0 );
        int colorBufferHandle = vboHandles.get( 1 );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, positionBufferHandle );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 9 * 4, positionData, GL2.GL_STATIC_DRAW );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorBufferHandle );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 9 * 4, colorData, GL2.GL_STATIC_DRAW );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, positionBufferHandle );
        int v = gl.glGetAttribLocation( program, "VertexPosition" );
        gl.glVertexAttribPointer( v, 3, GL2.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( v );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorBufferHandle );
        int c = gl.glGetAttribLocation( program, "VertexColor" );
        gl.glVertexAttribPointer( c, 3, GL2.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( c );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glTranslatef( 0f, 0f, -5f );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glDrawArrays( GL2.GL_TRIANGLES, 0, 3 );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        gl.glUniform1f( gl.glGetUniformLocation( program, "resolutionScale" ), h );
    }
}
