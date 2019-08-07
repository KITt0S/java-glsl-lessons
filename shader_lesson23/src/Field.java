import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import utils.ShaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field extends CustomAdapter implements GLEventListener {

    private BlenderObj model = BlenderObjReader.read( "Model/Cube/cube.obj",
            "Model/Cube/cube.mtl", false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );
    private IntBuffer samplers = IntBuffer.wrap( new int[ 2 ] );
    private int smplr;
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 cameraMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 1f );
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

        IntBuffer vbo = IntBuffer.wrap( new int[ 3 ] );
        gl.glGenBuffers( 3, vbo );
        gl.glGenVertexArrays( 1, vaoHandle );
        FloatBuffer v = model.getAllVertexesBuffer();
        FloatBuffer n = model.getAllNormalsBuffer();
        FloatBuffer t = model.getAllTexCoordsBuffer();
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
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
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, t.capacity() * 4, t, GL3.GL_STATIC_DRAW );
        int tLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTexCoord" );
        gl.glVertexAttribPointer( tLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( tLoc );

        //освещение
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 0f, 0f, 10f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //материал куба
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.2f, 0.2f, 0.2f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.6f, 0.6f, 0.6f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 1f, 1f, 1f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );

        //текстура куба
        IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenTextures( 1, tbo );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        try {
            TextureData txData = TextureIO.newTextureData( gl.getGLProfile(),
                    new File( "Model/Texture/brick.png" ), false, null );
            gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, txData.getWidth(), txData.getHeight() );
            gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, txData.getWidth(), txData.getHeight(),
                    GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, txData.getBuffer() );
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int txLoc = ShaderUtils.getUniLoc( gl, program, "Tex" );
//        gl.glUniform1i( txLoc, 0 );

        //создание объектов-семплеров

        gl.glGenSamplers( 2, samplers );
        int linearSampler = samplers.get( 0 );
        int nearestSampler = samplers.get( 1 );
        gl.glSamplerParameteri( linearSampler, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
        gl.glSamplerParameteri( linearSampler, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
        gl.glSamplerParameteri( nearestSampler, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        gl.glSamplerParameteri( nearestSampler, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );

        modelMatrix.scale( 2f, 2f, 2f );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        cameraMatrix.loadIdentity();
        cameraMatrix.rotate( angX, 0f, 1f, 0f );
        cameraMatrix.rotate( angY, 1f, 0f, 0f );
        cameraMatrix.translate( 0f, 0f, 7f );
        cameraMatrix.invert();
        Matrix4 mvMatrix = new Matrix4();
        mvMatrix.multMatrix( cameraMatrix );
        mvMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.multMatrix( pMatrix );
        mvpMatrix.multMatrix( mvMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glBindSampler( 0, smplr );
        System.out.println( smplr );
        int pSize = 0;
        for (int i = 0; i < model.getElements().size(); i++) {

            pSize += model.getElements().get( i ).getFaces().size() * 3;
        }
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

    @Override
    public void keyPressed( KeyEvent e ) {

        switch ( e.getKeyCode() ) {

            case KeyEvent.VK_S: {

                smplr = smplr == 0 ? 1 : 0;
            }
        }
    }
}
