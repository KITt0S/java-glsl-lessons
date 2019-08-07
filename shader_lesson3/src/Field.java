import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
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

    private int programId;
    private float rot;

    private GLU glu;
    IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );

    private File vs = new File( "Shaders/vertex_shader.glsl" );
    private File fs = new File( "Shaders/fragment_shader.glsl" );

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glClearDepth( 1f );
        gl.glEnable( GL2.GL_SHADE_MODEL );
        gl.glShadeModel( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        glu = GLU.createGLU( gl );
        int vertexShaderId = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, vs );
        int fragmentShaderId = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, fs );
        programId = ShaderUtils.createProgram( gl, vertexShaderId, fragmentShaderId );
        gl.glUseProgram( programId );
        setData( gl );
    }

    private void setData( GL2 gl ) {

        FloatBuffer positionData = Buffers.newDirectFloatBuffer( new float[]{-0.8f, -0.8f, 0.0f,
                0.8f, -0.8f, 0.0f,
                0.0f, 0.8f, 0.0f});
        FloatBuffer colorData = Buffers.newDirectFloatBuffer( new float[] { 1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f } );
        IntBuffer vboHandles = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenBuffers( 2, vboHandles );
        int positionBufferHandle = vboHandles.get( 0 );
        int colorBufferHandle = vboHandles.get( 1 );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, positionBufferHandle );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 9 * 4, positionData, GL2.GL_STATIC_DRAW );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorBufferHandle );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 9 * 4, colorData, GL2.GL_STATIC_DRAW );
        gl.glGenVertexArrays(1, vaoHandle );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glEnableVertexAttribArray( 0 );
        gl.glEnableVertexAttribArray( 1 );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, positionBufferHandle );
        gl.glVertexAttribPointer( 0, 3, GL2.GL_FLOAT, false, 0, 0L );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorBufferHandle );
        gl.glVertexAttribPointer( 1, 3, GL2.GL_FLOAT, false, 0, 0L );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glLoadIdentity();
        gl.glTranslatef( 0f, 0f, -5f );
        Mat4 rotationMatrix = Matrices.rotate( rot, new Vec3( 0, 0, 1 ) );
        int location = gl.glGetUniformLocation( programId, "RotationMatrix" );
        if( location >= 0 ) {

            gl.glUniformMatrix4fv( location, 1, false, rotationMatrix.getBuffer() );
        }
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glDrawArrays( GL2.GL_TRIANGLES, 0, 3 );
        rot += 0.005f;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        glu.gluPerspective( 45f, h, 0.1f, 100f );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity();
    }
}
