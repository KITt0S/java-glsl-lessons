import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import geometry.Element;
import utils.ShaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private int program;
    private BlenderObj model = BlenderObjReader.read( "Model/Obj_files/cube.obj",
            "Model/Obj_files/cube.mtl", false );
    private IntBuffer vaoHandle;
    private Matrix4 mvMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 0.5f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        IntBuffer vbo = IntBuffer.wrap( new int[ 3 ] );
        gl.glGenBuffers( 3, vbo );
        vaoHandle = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenVertexArrays( 1, vaoHandle );

        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
        FloatBuffer v = model.getAllVertexesBuffer();
        FloatBuffer n = model.getAllNormalsBuffer();
        FloatBuffer t = model.getAllTexCoordsBuffer();
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
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 0f, 0f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //текстуры
        File[] txfiles = new File[]{ new File( "Model/Texture/brick.png" ),
                new File( "Model/Texture/moss.png" ) };
        IntBuffer tbo = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenTextures( 2, tbo );
        for (int i = 0; i < txfiles.length; i++) {

            gl.glActiveTexture( GL3.GL_TEXTURE0 + i );
            gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( i ) );
            try {
                TextureData txData = TextureIO.newTextureData( gl.getGLProfile(), txfiles[ i ], false,
                        null );
                gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, txData.getWidth(), txData.getHeight() );
                gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, txData.getWidth(),
                        txData.getHeight(), GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, txData.getBuffer() );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
                int txLoc = ShaderUtils.getUniLoc( gl, program, "Tex[" + i + "]" );
                gl.glUniform1i( txLoc, i );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //матрицы
        mvMatrix.translate( 0f, 0f, -7f );
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

        GL3 gl = drawable.getGL().getGL3();
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        mvMatrix.rotate( 0.007f, 0f, 1f, 0f );
        gl.glUniformMatrix4fv(ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        for (int i = 0; i < model.getElements().size(); i++) {

            Element o = model.getElements().get( i );
            //материал
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 1,
                    o.getKa() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 1,
                    o.getKd() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 1,
                    o.getKs() );
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 10f );
            gl.glBindVertexArray( vaoHandle.get( i ) );
            gl.glDrawArrays( GL3.GL_TRIANGLES, 0, o.getFaces().size() * 3 );
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
        Matrix4 pMatrix = new Matrix4();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "PMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );
    }
}
