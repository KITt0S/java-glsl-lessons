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

public class Field implements GLEventListener {

    private final BlenderObj model = BlenderObjReader.read( "Model/Obj_files/ogre.obj", "", true );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 1 ] );
    private Matrix4 mvMatrix = new Matrix4();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 1f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        IntBuffer vbo = IntBuffer.wrap( new int[ 4 ] );
        gl.glGenBuffers( 4, vbo );
        FloatBuffer v = model.getAllVertexesBuffer();
        FloatBuffer n = model.getAllNormalsBuffer();
        FloatBuffer tex = model.getAllTexCoordsBuffer();
        FloatBuffer tang = model.getAllTangentsBuffer();
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, tex.capacity() * 4, tex, GL3.GL_STATIC_DRAW );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 3 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, tang.capacity() * 4, tang, GL3.GL_STATIC_DRAW );
        gl.glGenVertexArrays( 1, vaoHandle );
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
        int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
        gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( vLoc );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) );
        int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
        gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( nLoc );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
        int texLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTexCoord" );
        gl.glVertexAttribPointer( texLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( texLoc );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 3 ) );
        int tangLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTangent" );
        gl.glVertexAttribPointer( tangLoc, 4, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( tangLoc );

        //освещение
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 0f, 0f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //материал
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka"), 0.32f, 0.32f, 0.32f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks"), 0.5f, 0.5f, 0.5f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );

        //текстуры
        File[] texFiles = new File[]{ new File( "Model/Textures/ogre_diffuse.png" ),
                new File( "Model/Textures/ogre_normalmap.png" ) };
        IntBuffer tbo = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenTextures( 2, tbo );
        for (int i = 0; i < texFiles.length; i++) {

            gl.glActiveTexture( GL3.GL_TEXTURE0 + i );
            gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( i ) );
            try {

                TextureData texData = TextureIO.newTextureData( gl.getGLProfile(), texFiles[ i ], false,
                        null );
                gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, texData.getWidth(), texData.getHeight() );
                gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, texData.getWidth(),
                        texData.getHeight(), GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texData.getBuffer() );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
                gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
                int tLoc = ShaderUtils.getUniLoc( gl, program, "Tex[" + i + "]" );
                gl.glUniform1i( tLoc, i );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //матрицы
        mvMatrix.translate( 0f, 0f, -2f );
        //mvMatrix.rotate( ( float ) Math.PI / 8f, 0f, 1f, 0f );
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
                gl.glBindVertexArray( vaoHandle.get( 0 ) );
        mvMatrix.rotate( 0.007f, 0f, 1f, 0f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
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
        Matrix4 pMatrix = new Matrix4();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "PMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );
    }
}
