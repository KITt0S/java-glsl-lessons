import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import geometry.Element;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Field implements GLEventListener {

    private int program;
    private BlenderObj model = BlenderObjReader.read( "Model/Teapot_and_thorus.obj",
            "Model/Teapot_and_thorus.mtl", false );
    private IntBuffer vaoHandle;
    private Matrix4 mvMatrix = new Matrix4();
    private float rotY = 0.02f;


    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 1f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glShadeModel( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        int vs = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL2 gl ) {

        int objSize = model.getElements().size();
        IntBuffer vbo = IntBuffer.wrap( new int[ objSize * 2 ] );
        gl.glGenBuffers( objSize * 2, vbo );
        vaoHandle = IntBuffer.wrap( new int[ objSize ] );
        gl.glGenVertexArrays( objSize, vaoHandle );
        for (int i = 0; i < objSize; i++) {

            gl.glBindVertexArray( vaoHandle.get( i ) );
            FloatBuffer vertexes = model.getVertexesBuffer( i );
            FloatBuffer normals = model.getNormalsBuffer( i );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 2 * i ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, vertexes.capacity() * 4, vertexes, GL2.GL_STATIC_DRAW );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            gl.glVertexAttribPointer( vLoc, 3, GL2.GL_FLOAT, false,0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 2 * i + 1 ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, normals.capacity() * 4, normals, GL2.GL_STATIC_DRAW );
            int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
            gl.glVertexAttribPointer( nLoc, 3, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( nLoc );
        }

        //инициализация источника света
        gl.glUniform4f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 20f, 17f, 10f, 1f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //инициализация свойств материала
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Ka" ), 0.2f, 0.2f, 0.2f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Kd" ), 1f, 1f, 1f );

        //инициалиизация матриц
        mvMatrix.translate( 0f, -3f, -10f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        Matrix4 pMatrix = new Matrix4();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "PMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        mvMatrix.rotate( rotY, 0f, 1f, 0f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        for (int i = 0; i < model.getElements().size(); i++) {

            gl.glBindVertexArray( vaoHandle.get( i ) );
            gl.glDrawArrays( GL2.GL_TRIANGLES, 0, model.getElement( i ).getFaces().size() * 3 );
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        Matrix4 pMatrix = new Matrix4();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "PMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );
    }
}
