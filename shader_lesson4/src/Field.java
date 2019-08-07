import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.io.File;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );
    private Matrix4 mvMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glClearDepth( 1f );
        gl.glEnable( GL2.GL_SHADE_MODEL );
        gl.glShadeModel( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        int vs = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setupData( gl );
    }

    private void setupData( GL2 gl ) {

        FloatBuffer vPositions = FloatBuffer.wrap( new float[] {
                -1, -1,
                 1,  1,
                -1,  1, //треугольник 1
                -1, -1,
                 1, -1,
                 1,  1 //треугольник 2
        } );
        FloatBuffer vtCords = FloatBuffer.wrap( new float[] {
                 0,  0,
                 1,  1,
                 0,  1, //текстурные координаты треугольника 1
                 0,  0,
                 1,  0,
                 1,  1 // текстурные координаты треугольника 2
        } );
        IntBuffer vboBuffers = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenBuffers( 2, vboBuffers );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 0 ) );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 4 * 12, vPositions, GL2.GL_STATIC_DRAW );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 1 ) );
        gl.glBufferData( GL2.GL_ARRAY_BUFFER, 4 * 12, vtCords, GL2.GL_STATIC_DRAW );
        gl.glGenVertexArrays( 1, vaoHandle );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 0 ) );
        int vPosLoc = gl.glGetAttribLocation( program, "VertexPosition" );
        gl.glVertexAttribPointer( vPosLoc, 2, GL2.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( vPosLoc );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboBuffers.get( 1 ) );
        int vtCordsLoc = gl.glGetAttribLocation( program, "VertexTexCord" );
        gl.glVertexAttribPointer( vtCordsLoc, 2, GL2.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( vtCordsLoc );

        // настройка Uniform-блока
        int blockIndex = gl.glGetUniformBlockIndex( program, "BlobSettings" );
        float[] innerColor = new float[]{ 1f, 1f, 0.75f, 1f };
        float[] outerColor = new float[]{ 0.5f, 0.5f, 0.5f, 1f };
        float innerRadius = 0.25f, outerRadius = 0.45f;
        FloatBuffer blockBuffer = Buffers.newDirectFloatBuffer( new float[ 10 ] );
        blockBuffer.put( innerColor );
        blockBuffer.put( outerColor );
        blockBuffer.put( innerRadius );
        blockBuffer.put( outerRadius );
        IntBuffer ubo = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenBuffers( 1, ubo );
        gl.glBindBuffer( GL2.GL_UNIFORM_BUFFER, ubo.get( 0 ) );
        gl.glBufferData( GL2.GL_UNIFORM_BUFFER, 4 * 10, blockBuffer.rewind(), GL2.GL_DYNAMIC_DRAW );
        gl.glBindBufferBase( GL2.GL_UNIFORM_BUFFER, blockIndex, ubo.get( 0 ) );

        // настройка матрицы модели-вида-проекции
        mvMatrix.translate( 0f, 0f, -4f );
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.multMatrix( mvMatrix );
        int mvpMatPos = gl.glGetUniformLocation( program, "MVPMatrix" );
        gl.glUniformMatrix4fv( mvpMatPos, 1, false, FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
    }


    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glDrawArrays( GL2.GL_TRIANGLES, 0, 6 );
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
        int mvpMatPos = gl.glGetUniformLocation( program, "MVPMatrix" );
        gl.glUniformMatrix4fv( mvpMatPos, 1, false, FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
    }
}
