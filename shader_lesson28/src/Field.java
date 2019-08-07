import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private BlenderObj torusModel = BlenderObjReader.read( "Model/torus.obj",
            "Model/torus.mtl", false );
    private int program;
    private IntBuffer vaoHandle;
    private IntBuffer fboHandle;
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();
    private Matrix4 projMatrix = new Matrix4();
    private Matrix4 mvMatrix = new Matrix4();
    private Matrix4 mvpMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 0f );
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
        setFBO( gl );
    }

    private void setVaryingData( GL3 gl ) {

        if( gl != null ) {

            vaoHandle = IntBuffer.wrap( new int[ 2 ] );
            gl.glGenVertexArrays( 2, vaoHandle );
            IntBuffer vbo = IntBuffer.wrap( new int[ 4 ] );
            gl.glGenBuffers( 4, vbo );
            gl.glBindVertexArray( vaoHandle.get( 0 ) );
            FloatBuffer v = torusModel.getAllVertexesBuffer();
            FloatBuffer n = torusModel.getAllNormalsBuffer();
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) ) ;
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
            int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
            gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( nLoc );

            gl.glBindVertexArray( vaoHandle.get( 1 ) );
            FloatBuffer v1 = FloatBuffer.wrap( new float[]{

                    -1f, -1f,
                    1f, -1f,
                    1f, 1f,
                    -1f, -1f,
                    1f, 1f,
                    -1f, 1f
            } );
            FloatBuffer t = FloatBuffer.wrap( new float[]{

                    0f, 0f,
                    1f, 0f,
                    1f, 1f,
                    0f, 0f,
                    1f, 1f,
                    0f, 1f
            } );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v1.capacity() * 4, v1, GL3.GL_STATIC_DRAW );
            gl.glVertexAttribPointer( vLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 3 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, t.capacity() * 4, t, GL3.GL_STATIC_DRAW );
            int tLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTexCoord" );
            gl.glVertexAttribPointer( tLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( tLoc );
        }
    }

    private void setMaterialAndLight( GL3 gl ) {

        if( gl != null ) {

            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.8f, 0.8f, 0.8f );
        }
    }

    private void setFBO( GL3 gl ) {

        if( gl != null ) {

            fboHandle = IntBuffer.wrap( new int[ 1 ] );
            gl.glGenFramebuffers(1, fboHandle );
            gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
            IntBuffer depthBuf = IntBuffer.wrap( new int[ 1 ] );
            gl.glGenRenderbuffers( 1, depthBuf );
            gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, depthBuf.get( 0 ) );
            gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, 1024, 768 );
            IntBuffer gBufTex = IntBuffer.wrap( new int[ 3 ] );
            gl.glGenTextures( 3, gBufTex );
            int posTex = gBufTex.get( 0 );
            int normTex = gBufTex.get( 1 );
            int colorTex = gBufTex.get( 2 );
            createGBufTex( gl, GL3.GL_TEXTURE0, GL3.GL_RGB32F, posTex );
            createGBufTex( gl, GL3.GL_TEXTURE1, GL3.GL_RGB32F, normTex );
            createGBufTex( gl, GL3.GL_TEXTURE2, GL3.GL_RGB8, colorTex );
            gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER,
                    depthBuf.get( 0 ) );
            gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, posTex,
                    0 );
            gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT1, GL3.GL_TEXTURE_2D, normTex,
                    0 );
            gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT2, GL3.GL_TEXTURE_2D, colorTex,
                    0 );
            IntBuffer drawBuffers = IntBuffer.wrap( new int[]{

                    GL3.GL_NONE, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1, GL3.GL_COLOR_ATTACHMENT2
            } );
            gl.glDrawBuffers( 4, drawBuffers );
        }
    }

    private void createGBufTex( GL3 gl, int texUnit, int format, int texId ) {

        if( gl != null ) {

            gl.glActiveTexture( texUnit );
            gl.glBindTexture( GL3.GL_TEXTURE_2D, texId );
            gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, format, 1024, 768 );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {


    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        int stepLoc = ShaderUtils.getUniLoc( gl, program, "Step" );
        gl.glUniform1i( stepLoc, 0 );
        setMatrices( gl );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        int pSize = 0;
        for (int i = 0; i < torusModel.getElementsSize(); i++) {

            pSize += torusModel.getElement( i ).getFacesSize() * 3;
        }
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        gl.glUniform1i( stepLoc, 1 );
        mvpMatrix.loadIdentity();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glUniform1i( ShaderUtils.getUniLoc( gl, program, "PositionTex" ), 0 );
        gl.glUniform1i( ShaderUtils.getUniLoc( gl, program, "NormalTex" ), 1 );
        gl.glUniform1i( ShaderUtils.getUniLoc( gl, program, "ColorTex" ), 2 );
        gl.glBindVertexArray( vaoHandle.get( 1 ) );
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, 6 );
    }

    private void setMatrices( GL3 gl ) {

        viewMatrix.loadIdentity();
        viewMatrix.translate( 0f, 0f, 7f );
        viewMatrix.invert();
        mvMatrix.loadIdentity();
        mvMatrix.multMatrix( viewMatrix );
        mvMatrix.multMatrix( modelMatrix );
        mvpMatrix.loadIdentity();
        mvpMatrix.multMatrix( projMatrix );
        mvpMatrix.multMatrix( mvMatrix );
        if( gl != null ) {

           gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                   FloatBuffer.wrap( mvMatrix.getMatrix() ) );
           gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                   FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
       }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        projMatrix.loadIdentity();
        projMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
