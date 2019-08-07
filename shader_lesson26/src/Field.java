import com.hackoeur.jglm.*;
import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field extends CustomAdapter implements GLEventListener {

    private BlenderObj scene = BlenderObjReader.read( "Scene/teapot_and_sphere_scene.obj",
            "Scene/teapot_and_sphere_scene.mtl", false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 6 ] );
    private IntBuffer fboHandle = IntBuffer.wrap( new int[ 3 ] );
    private IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();
    private Matrix4 projMatrix = new Matrix4();
    private Dimension screenSize = new Dimension();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        setVaryingData( gl );
        setLightsAndMaterial( gl );
        setUnchangedMatrices( gl );
        prepareFBO( gl );
    }

    private void setVaryingData( GL3 gl ) {

        if( gl != null ) {

            IntBuffer vbo = IntBuffer.wrap( new int[ 11 ] );
            gl.glGenBuffers( 11, vbo );
            gl.glGenVertexArrays( 6, vaoHandle );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            for (int i = 0; i < scene.getElementsSize(); i++) {

                gl.glBindVertexArray( vaoHandle.get( i ) );
                FloatBuffer v = scene.getVertexesBuffer( i );
                FloatBuffer n = scene.getNormalsBuffer( i );
                gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 * i ) );
                gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
                gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
                gl.glEnableVertexAttribArray( vLoc );
                gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 * i + 1 ) );
                gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
                int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
                gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
                gl.glEnableVertexAttribArray( nLoc );
            }
            gl.glBindVertexArray( vaoHandle.get( 5 ) );
            FloatBuffer v1 = FloatBuffer.wrap( new float[]{

                    -1f, -1f,
                     1f,  1f,
                    -1f,  1f,
                    -1f, -1f,
                     1f, -1f,
                     1f,  1f
            } );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 10 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v1.capacity() * 4, v1, GL3.GL_STATIC_DRAW );
            gl.glVertexAttribPointer( vLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
        }
    }

    private void setLightsAndMaterial(GL3 gl ) {

        if( gl != null ) {

            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[0].Intensity" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[0].Position" ), -5f, 7f, 0f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[1].Intensity" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[1].Position" ), 0f, 7f, 0f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[2].Intensity" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light[2].Position" ), 5f, 7f, 0f );

            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );
        }
    }

    private void setUnchangedMatrices( GL3 gl ) {

        if( gl != null ) {

            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ModelMatrix" ), 1, false,
                    FloatBuffer.wrap( modelMatrix.getMatrix() ) );
        }
    }

    private void prepareFBO( GL3 gl ) {

        if( gl != null ) {

            gl.glGenFramebuffers( 3, fboHandle );
            gl.glGenTextures( 1, tbo );
            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
            gl.glActiveTexture( GL3.GL_TEXTURE0 );
            gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
            gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGB32F, 1920, 1080 );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
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
            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
        int stepLoc = ShaderUtils.getUniLoc( gl, program, "Step" );
        gl.glUniform1i( stepLoc, 0 );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        viewMatrix.loadIdentity();
        viewMatrix.rotate( angX, 0f, 1f, 0f );
        viewMatrix.rotate( angY, 1f, 0f, 0f );
        viewMatrix.translate( 0f, 4f, dist );
        float[] cameraPosition = new float[]{ 0f, 0f, 0f, 1f };
        viewMatrix.multVec( cameraPosition, cameraPosition );
        gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "WorldCameraPosition" ), 1,
                FloatBuffer.wrap( cameraPosition ) );
        viewMatrix.invert();
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.multMatrix( projMatrix );
        mvpMatrix.multMatrix( viewMatrix );
        mvpMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );

        for (int i = 0; i < scene.getElementsSize(); i++) {

            gl.glBindVertexArray( vaoHandle.get( i ) );
            float[] ka = new float[ 3 ];
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ),1,
                    scene.getElement( i ).getKa() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 1,
                    scene.getElement( i ).getKd() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 1,
                    scene.getElement( i ).getKs() );
            gl.glDrawArrays( GL3.GL_TRIANGLES, 0, scene.getElement( i ).getFacesSize() * 3 );
        }

        float logAve = getLogAveLum( gl );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "AveLum" ), logAve );

        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        mvpMatrix.loadIdentity();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glUniform1i( stepLoc, 1 );
        int txLoc = ShaderUtils.getUniLoc( gl, program, "Tex0" );
        gl.glUniform1i( txLoc, 0 );
        gl.glBindVertexArray( vaoHandle.get( 5 ) );
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, 6 );
    }

    private float getLogAveLum( GL3 gl ) {

        FloatBuffer txDataBuffer = FloatBuffer.wrap( new float[ 1920 * 1080 * 3 ] );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
        gl.glGetTexImage( GL3.GL_TEXTURE_2D, 0, GL3.GL_RGB, GL3.GL_FLOAT, txDataBuffer );
        float[] txData = txDataBuffer.array();
        float sum = 0.0f;
        for (int i = 0; i < screenSize.getHeight(); i++) {

            for (int j = 0; j < screenSize.getWidth(); j++) {

                int texPos = i * 1920 + j;
                float lum = VectorUtil.dotVec3( new float[]{ txData[ texPos * 3 ], txData[ texPos * 3 + 1 ],
                                txData[ texPos * 3 + 2 ]  }, new float[]{ 0.2126f, 0.7152f, 0.0722f } );
                sum += ( float ) Math.log( lum + 0.00001f );
            }
        }
        int size = screenSize.getWidth() * screenSize.getHeight();
        return ( float ) Math.exp( sum / size );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport( 0, 0, width, height );
        screenSize.set( width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        projMatrix.loadIdentity();
        projMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
