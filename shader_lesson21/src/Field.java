import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import utils.ShaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field extends CustomAdapter implements GLEventListener {

    private BlenderObj teapot = BlenderObjReader.read( "Data/Model/Teapot/teapot_n_glass.obj",
            "Data/Model/Teapot/teapot_n_glass.mtl", false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();
    private Matrix4 cameraMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        //быстро-меняющиеся переменные
        FloatBuffer v = teapot.getAllVertexesBuffer();
        FloatBuffer n = teapot.getAllNormalsBuffer();
        IntBuffer vbo = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenBuffers( 2, vbo );
        gl.glGenVertexArrays( 1, vaoHandle );
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

        //свет
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 10f, 10f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //материал
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.1f, 0.1f, 0.1f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );

        //текстура цветка
        IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenTextures( 1, tbo );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        try {
            TextureData txData = TextureIO.newTextureData( gl.getGLProfile(), new File( "Data/Texture/flower.png" ),
                    false, null );
            gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, txData.getWidth(), txData.getHeight() );
            gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, txData.getWidth(), txData.getHeight(),
                    GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, txData.getBuffer() );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER );
        } catch (IOException e) {
            e.printStackTrace();
        }
        int txLoc = ShaderUtils.getUniLoc( gl, program, "ProjectorTex" );
        gl.glUniform1i( txLoc, 0 );

        //матрицы
        //загрузка матрицы модели
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ModelMatrix" ), 1, false,
                FloatBuffer.wrap( modelMatrix.getMatrix() ) );
        //создание и загрузка матрицы проектора
        Matrix4 projectorMatrix = new Matrix4();
        float[] projectorView = new float[ 16 ];
        FloatUtil.makeLookAt( projectorView, 0, new float[]{ 7f, 7f, 7f }, 0, new float[]{ -2f, -15f, 0f },
                0, new float[]{ 0f, 1f, 0f }, 0, new float[ 16 ] );
        Matrix4 projectorProj = new Matrix4();
        projectorProj.makePerspective( 30f, 1f, 0.2f, 1000f );
        Matrix4 projectorScaleTrans = new Matrix4();
        projectorScaleTrans.translate( 0.5f, 0.5f, 0.5f );
        projectorScaleTrans.scale( 0.5f, 0.5f, 0.5f );
        projectorMatrix.multMatrix( projectorScaleTrans );
        projectorMatrix.multMatrix( projectorProj );
        projectorMatrix.multMatrix( projectorView );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ProjectorMatrix" ), 1, false,
                FloatBuffer.wrap( projectorMatrix.getMatrix() ) );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        //матрица камеры/вида
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
        mvpMatrix.multMatrix( cameraMatrix );
        mvpMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        int pSize = 0;
        for (int i = 0; i < teapot.getElements().size(); i++) {

            pSize += teapot.getElements().get( i ).getFaces().size() * 3;
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
}
