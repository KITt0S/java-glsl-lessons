import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import org.apache.commons.math3.analysis.function.Gaussian;
import utils.ShaderUtils;

import java.awt.*;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private BlenderObj myModel = BlenderObjReader.read( "Model/Teapot_and_thorus/teapot_and_thorus.obj",
            "Model/Teapot_and_thorus/teapot_and_thorus.mtl", false );
    private int program;
    private IntBuffer vao = IntBuffer.wrap( new int[ 2 ] );
    private IntBuffer fboHandle = IntBuffer.wrap( new int[ 2 ] );
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();
    private Matrix4 projMatrix = new Matrix4();
    private Matrix4 mvMatrix = new Matrix4();
    private Matrix4 mvpMatrix = new Matrix4();
    private Dimension scrSize = new Dimension();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
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
        setGaussWeights( gl );
        prepareFBO( gl );
    }

    private void setVaryingData( GL3 gl ) {

        if( gl != null ) {

            IntBuffer vbo = IntBuffer.wrap( new int[ 3 ] );
            gl.glGenBuffers( 3, vbo );
            gl.glGenVertexArrays( 2, vao );
            gl.glBindVertexArray( vao.get( 0 ) );
            FloatBuffer v = myModel.getAllVertexesBuffer();
            FloatBuffer n = myModel.getAllNormalsBuffer();
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
            gl.glBindVertexArray( vao.get( 1 ) );
            FloatBuffer v1 = FloatBuffer.wrap( new float[]{

                    -1f, -1f,
                    1f, 1f,
                    -1f, 1f,
                    -1f, -1f,
                    1f, -1f,
                    1f, 1f
            } );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v1.capacity() * 4, v1, GL3.GL_STATIC_DRAW );
            gl.glVertexAttribPointer( vLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
        }
    }

    private void setMaterialAndLight( GL3 gl ) {

        if( gl != null ) {

            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 10f, 10f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.2f, 0.2f, 0.2f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.64f, 0.64f, 0.64f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 0.5f, 0.5f, 0.5f );
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess"), 20f );
        }
    }
    
    private void setGaussWeights( GL3 gl ) {

        if( gl != null ) {

            float[] weights = new float[ 5 ];
            float sum, sigma2 = 4.0f;
            Gaussian gauss = new Gaussian( 0, sigma2 );
            weights[ 0 ] = ( float ) gauss.value( 0.0 );
            sum = weights[ 0 ];
            for (int i = 1; i < 5; i++) {

                weights[ i ] = ( float ) gauss.value( i );
                sum += 2 * weights[ i ];
            }
            for (int i = 0; i < 5; i++) {

                gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, String.format( "Weight[%d]", i ) ),
                        weights[ i ] / sum );
            }
        }
    }

    private void prepareFBO(GL3 gl ) {

        if( gl != null ) {

            gl.glGenFramebuffers( 2, fboHandle );
            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );

            IntBuffer tbo = IntBuffer.wrap( new int[ 2 ] );
            gl.glGenTextures( 2, tbo );
            for (int i = 0; i < 2; i++) {

                gl.glActiveTexture( GL3.GL_TEXTURE0 + i );
                gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( i ) );
                gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, 1920, 1080 );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
            }
            gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, tbo.get( 0 ),
                    0 );

            IntBuffer depthBuf = IntBuffer.wrap( new int[ 1 ] );
            gl.glGenRenderbuffers( 1, depthBuf );
            gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, depthBuf.get( 0 ) );
            gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, 1920, 1080 );
            gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER,
                    depthBuf.get( 0 ) );

            IntBuffer drawBufs = IntBuffer.wrap( new int[]{ GL3.GL_COLOR_ATTACHMENT0 } );
            gl.glDrawBuffers( 1, drawBufs );

            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 1 ) );
            gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, tbo.get( 1 ),
                    0 );
            gl.glDrawBuffers( 1, drawBufs );
            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        int stepLoc = ShaderUtils.getUniLoc( gl, program, "Step" );
        gl.glUniform1i( stepLoc, 0 );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        viewMatrix.loadIdentity();
        viewMatrix.translate( 0f, 2f, 10f );
        viewMatrix.invert();
        mvMatrix.loadIdentity();
        mvMatrix.multMatrix( viewMatrix );
        mvMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        mvpMatrix.loadIdentity();
        mvpMatrix.multMatrix( projMatrix );
        mvpMatrix.multMatrix( mvMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glBindVertexArray( vao.get( 0 ) );
        int pSize = 0;
        for (int i = 0; i < myModel.getElementsSize(); i++) {

            pSize += myModel.getElement( i ).getFacesSize() * 3;
        }
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );

        gl.glUniform1i( stepLoc, 1 );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 1 ) );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        int txLoc = ShaderUtils.getUniLoc( gl, program, "Texture0" );
        gl.glUniform1i( txLoc, 0 );
        mvMatrix.loadIdentity();
        mvpMatrix.loadIdentity();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glBindVertexArray( vao.get( 1 ) );
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, 6 );

        gl.glUniform1i( stepLoc, 2 );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        gl.glUniform1i( txLoc, 1 );
        gl.glBindVertexArray( vao.get( 1 ) );
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, 6 );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport( 0, 0, width, height );
        scrSize.setSize( width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        projMatrix.loadIdentity();
        projMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
