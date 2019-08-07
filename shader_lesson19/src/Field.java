import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import utils.ShaderUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener, MouseMotionListener, MouseListener {

    final private BlenderObj skyBox = BlenderObjReader.read( "Models/Obj_files/Skybox_cube/Skybox_cube.obj",
            "Models/Obj_files/Skybox_cube/Skybox_cube.mtl", false );
    final private BlenderObj teapot = BlenderObjReader.read( "Models/Obj_files/Teapot/teapot.obj",
            "Models/Obj_files/Teapot/teapot.mtl", false );
    private BlenderObj[] objs = new BlenderObj[]{ skyBox, teapot };
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 2 ] );
    private Matrix4 viewMatrix = new Matrix4();
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();
    private float angX, angY;

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

        gl.glGenVertexArrays( 2, vaoHandle );
        for (int i = 0; i < objs.length; i++) {

            IntBuffer vbo = IntBuffer.wrap( new int[ 2 ] );
            gl.glGenBuffers( 2, vbo );
            FloatBuffer v = objs[ i ].getAllVertexesBuffer();
            FloatBuffer n = objs[ i ].getAllNormalsBuffer();
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) );
            gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
            gl.glBindVertexArray( vaoHandle.get( i ) );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 0 ) );
            int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
            gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( vLoc );
            gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 1 ) );
            int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
            gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
            gl.glEnableVertexAttribArray( nLoc );
        }

        //цвет материала чайника
        gl.glUniform4f( ShaderUtils.getUniLoc( gl, program, "MaterialColor" ), 0.9f, 0.6f, 0.4f, 1f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "ReflectFactor" ), 0.9f );

        //загрузка кубической текстуры
        IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenTextures(1, tbo );
        gl.glBindTexture( GL3.GL_TEXTURE_CUBE_MAP, tbo.get( 0 ) );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        gl.glTexStorage2D( GL3.GL_TEXTURE_CUBE_MAP, 1, GL3.GL_RGBA8, 256, 256 );
        int[] targets = new int[]{

                GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
                GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
                GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
                GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
                GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
                GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
        };
        String[] fNames = new String[]{ "posx", "negx", "posy", "negy", "posz", "negz" };
        for (int i = 0; i < 6; i++) {

            try {

                TextureData txData = TextureIO.newTextureData( gl.getGLProfile(),
                        new File( "Models/Texture/" + fNames[ i ] + ".png" ), false, null );
                gl.glTexSubImage2D( targets[ i ], 0, 0, 0, txData.getWidth(), txData.getHeight(),
                        GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, txData.getBuffer() );
                gl.glTexParameteri( GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
                gl.glTexParameteri( GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
                gl.glTexParameteri( GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE );
                gl.glTexParameteri( GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE );
                gl.glTexParameteri( GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_EDGE );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int txLoc = ShaderUtils.getUniLoc( gl, program, "CubeMapTex" );
        gl.glUniform1i( txLoc, 0 );


        //матрицы
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ModelMatrix" ), 1, false,
                FloatBuffer.wrap( modelMatrix.getMatrix() ) );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        viewMatrix.loadIdentity();
        Matrix4 cameraTransform = new Matrix4();
        //cameraTransform.translate( 0f, 2f, 0f );
        cameraTransform.rotate(angX, 0f, 1f, 0f );
        cameraTransform.rotate(angY, 1f, 0f, 0f );
        cameraTransform.translate( 0f, 0f, 7f );
        float[] camPos = new float[]{ 0, 0, 0, 1 };
        cameraTransform.multVec( camPos, camPos );
        gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "WorldCameraPosition" ), 1,
                FloatBuffer.wrap( camPos ) );
        cameraTransform.invert();
        viewMatrix.multMatrix( cameraTransform );
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.multMatrix( pMatrix );
        mvpMatrix.multMatrix( viewMatrix );
        mvpMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVPMatrix" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );
        int drawSkyBox;
        for (int i = 0; i < 2; i++) {

            if( i == 0 ) {

                drawSkyBox = 1;
            } else {

                drawSkyBox = 0;
            }
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "DrawSkyBox" ), drawSkyBox );
            gl.glBindVertexArray( vaoHandle.get( i ) );
            int pSize = 0;
            for (int j = 0; j < objs[ i ].getElements().size(); j++) {

                pSize += objs[ i ].getElements().get( j ).getFaces().size() * 3;
            }
            gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );
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
        pMatrix.loadIdentity();
        pMatrix.makePerspective( 45f, h, 0.1f, 1000f );
    }

    private Point mousePt = new Point();

    @Override
    public void mouseDragged(MouseEvent e) {

        angX += ( float )( e.getX() - mousePt.getX() ) / 300f;
        angY += ( float )( e.getY() - mousePt.getY() ) / 100f;
        mousePt = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        mousePt = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
