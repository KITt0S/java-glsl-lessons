import com.jogamp.opengl.*;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private BlenderObj model = BlenderObjReader.read( "Model/torus.obj",
            "Model/torus.mtl", false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );
    private Matrix4 mvMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        gl.glEnable( GL3.GL_MULTISAMPLE );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER,
                new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER,
                new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        setVaryingData( gl );
        setMaterialAndLight( gl );
        setMVMatrix( gl );
    }

    private void setVaryingData( GL3 gl ) {

        if( gl != null ) {

            IntBuffer vbo = IntBuffer.wrap( new int[ 2 ] );
            gl.glGenBuffers( 2, vbo );
            gl.glGenVertexArrays( 1, vaoHandle );
            gl.glBindVertexArray( vaoHandle.get( 0 ) );
            FloatBuffer v = model.getAllVertexesBuffer();
            FloatBuffer n = model.getAllNormalsBuffer();
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
            int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
            gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( nLoc );
        }
    }

    private void setMaterialAndLight( GL3 gl ) {

        if( gl != null ) {

            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 2f, 2f, 2f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ),1,
                    model.getElement( 0 ).getKa() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ),1,
                    model.getElement( 0 ).getKd() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ),1,
                    model.getElement( 0 ).getKs() );
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );
        }
    }

    private void setMVMatrix( GL3 gl ) {

        if( gl != null ) {

            Matrix4 modelMatrix = new Matrix4();
            Matrix4 viewMatrix = new Matrix4();
            viewMatrix.rotate( ( float ) Math.PI, 0f, 1f, 0f );
            viewMatrix.translate( 0f, 0f, 3f );
            viewMatrix.invert();
            mvMatrix.multMatrix( viewMatrix );
            mvMatrix.multMatrix( modelMatrix );
            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                    FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        Matrix4 mvp = new Matrix4();
        mvp.multMatrix( pMatrix );
        mvp.multMatrix( mvMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvp.getMatrix() ) );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        int pSize = model.getElement( 0 ).getFacesSize() * 3;
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        pMatrix.loadIdentity();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
