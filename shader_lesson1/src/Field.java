import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import utils.ShaderUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Field implements GLEventListener {

    private GLU glu;
    private FloatBuffer vertexData;
    private FloatBuffer colorData;
    private int positionAttribute;
    private int colorAttribute;

    private File vs = new File( "Shaders/vertex_shader.glsl" );
    private File fs = new File( "Shaders/fragment_shader.glsl" );

    Field() {

        prepareData();
    }

    private void prepareData() {

        float[] vertices = new float[]{ -0.5f, -0.2f,
                                         0.0f, 0.2f,
                                         0.5f, -0.2f };
        float [] colors = { 0.0f, 1.0f, 1.0f,
                            0.0f, 1.0f, 1.0f,
                            0.0f, 1.0f, 1.0f };
        vertexData = Buffers.newDirectFloatBuffer( vertices );
        colorData = Buffers.newDirectFloatBuffer( colors );
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glClearDepth( 1f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glDepthFunc( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        glu = GLU.createGLU( gl );
        int vertexShaderId = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, vs );
        int fragmentShaderId = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, fs );
        int programId = ShaderUtils.createProgram( gl, vertexShaderId, fragmentShaderId );
        gl.glUseProgram( programId );
        bindData( gl, programId );
    }

    private void bindData( GL2 gl, int pId ) {

        colorAttribute = gl.glGetUniformLocation( pId, "u_Color" );
        gl.glUniform4f( colorAttribute, 1.0f, 0.0f, 1.0f, 1.0f );
        positionAttribute = gl.glGetAttribLocation( pId, "a_Position" );
        gl.glVertexAttribPointer( positionAttribute, 2, GL2.GL_FLOAT, false, 0, vertexData.rewind() );
        gl.glEnableVertexAttribArray( positionAttribute );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glLoadIdentity();
        gl.glTranslatef( 0f, 0f, -5f );
        gl.glDrawArrays( GL2.GL_TRIANGLES, 0, 3 );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        if( height == 0 ) {
            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        gl.glViewport( 0, 0, width, height );
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();
        glu.gluPerspective( 45f, h, 0.1f, 100f );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity();
    }
}
