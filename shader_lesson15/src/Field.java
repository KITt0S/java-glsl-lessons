import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
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
import java.util.List;

public class Field implements GLEventListener {

    private int program;
    private BlenderObj model = BlenderObjReader.read( "Model/Obj_files/cube.obj", "Model/Obj_files/cube.mtl", false );
    private IntBuffer vaoHandle;
    private Matrix4 mvMatrix = new Matrix4();
    private float rotY = 0.02f;

    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 0.5f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glDepthFunc( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        int vs = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setupShaderProgram( gl );
    }

    private void setupShaderProgram( GL2 gl ) {

        int objSize = model.getElements().size();
        IntBuffer vbo = IntBuffer.wrap( new int[ objSize * 3 ] );
        gl.glGenBuffers( objSize * 3, vbo );
        IntBuffer ubo = IntBuffer.wrap( new int[ objSize * 4 ] );
        gl.glGenBuffers( objSize * 4, ubo );
        vaoHandle = IntBuffer.wrap( new int[ objSize ] );
        gl.glGenVertexArrays( objSize, vaoHandle );
        for (int i = 0; i < objSize; i++) {

            FloatBuffer vertexes = model.getVertexesBuffer( i );
            FloatBuffer normals = model.getNormalsBuffer( i );
            FloatBuffer texCoords = model.getTexCoordsBuffer( i );
            gl.glBindVertexArray( vaoHandle.get( i ) );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 3 * i ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, vertexes.capacity() * 4, vertexes, GL2.GL_STATIC_DRAW );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            gl.glVertexAttribPointer( vLoc, 3, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 3 * i + 1 ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, normals.capacity() * 4, normals, GL2.GL_STATIC_DRAW );
            int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
            gl.glVertexAttribPointer( nLoc,  3, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( nLoc );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 3 * i + 2 ) );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, texCoords.capacity() * 4, texCoords, GL2.GL_STATIC_DRAW );
            int tLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTexCoord" );
            gl.glVertexAttribPointer( tLoc, 2, GL2.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( tLoc );
        }

        //инициализация освещения
        gl.glUniform4f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 0f, 0f, 1f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //инициализация матриц
        mvMatrix.translate( 0f, 0f, -7f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix" ), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        Matrix4 pMatrix = new Matrix4();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "PMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );

        //загрузка текстуры
        IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
        gl.glActiveTexture( GL2.GL_TEXTURE0 );
        gl.glGenTextures( 1, tbo );
        gl.glBindTexture( GL2.GL_TEXTURE_2D, tbo.get( 0 ) );
        try {
            TextureData txData = TextureIO.newTextureData( gl.getGLProfile(),
                    new File( "Model/Texture/brick.jpg" ), false, null );
            gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR );
            gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR );
            gl.glTexImage2D( GL2.GL_TEXTURE_2D, 0, 3, txData.getWidth(), txData.getHeight(), 0,
                    GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, txData.getBuffer() );
            int txLoc = ShaderUtils.getUniLoc( gl, program, "Tex1" );
            gl.glUniform1i( txLoc, 0 );
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            Element o = model.getElement( i );
            gl.glBindVertexArray( vaoHandle.get( i ) );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 1, o.getKa() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 1,o.getKd() );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 1, o.getKs() );
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );
            gl.glDrawArrays( GL2.GL_TRIANGLES, 0, o.getFaces().size() * 3 );
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
