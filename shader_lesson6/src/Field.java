import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import geometry.Element;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Field implements GLEventListener {

    private BlenderObj model = BlenderObjReader.read( "Blender_models/Teapot/teapot.obj",
            "Blender_models/Teapot/teapot.mtl", false );
    private Matrix4 mvMatrix = new Matrix4();
    private int program = -1;
    private IntBuffer vaoHandle;
    private float rotX = 0.013f, rotY= 0.015f, rotZ = 0.02f;


    @Override
    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glShadeModel( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        int vs = ShaderUtils.createShader( gl, GL2.GL_VERTEX_SHADER, new File( "Shaders/vertex_shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL2.GL_FRAGMENT_SHADER, new File( "Shaders/fragment_shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL2 gl ) {

        if( program != -1 ) {

            // загрузка varying-данных
            List<Element> elements = model.getElements();
            int elSize = elements.size();
            IntBuffer vbo = IntBuffer.wrap( new int[ 2 * elSize ] );
            gl.glGenBuffers( 2 * elSize, vbo );
            vaoHandle = IntBuffer.wrap( new int[ elSize ] );
            gl.glGenVertexArrays( elSize, vaoHandle );
            for (int i = 0; i < elements.size(); i++) {

                Element o = elements.get( i );
                int vertexesSize = o.getFaces().size() * 9;
                FloatBuffer vertexes = model.getVertexesBuffer( i );
                FloatBuffer normals = model.getNormalsBuffer( i );
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 2 * i ) );
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, vertexesSize * 4, vertexes, GL2.GL_STATIC_DRAW );
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 2 * i + 1 ) );
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, vertexesSize * 4, normals, GL2.GL_STATIC_DRAW );
                gl.glBindVertexArray( vaoHandle.get( i ) );
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get( 2 * i ) );
                int vLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexPosition" );
                gl.glVertexAttribPointer( vLoc, 3, GL2.GL_FLOAT, false, 0, 0L );
                gl.glEnableVertexAttribArray( vLoc );
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vbo.get(  2 * i + 1 ) );
                int nLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexNormal" );
                gl.glVertexAttribPointer( nLoc, 3, GL2.GL_FLOAT, false, 0, 0L );
                gl.glEnableVertexAttribArray( nLoc );
            }

            //инициализация uniform-данных
            gl.glUniform4f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 0, 3f, 5f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.La" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Ld" ), 1f, 1f, 1f );
            gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Ls" ), 1f, 1f, 1f );

            //инициализация матриц
            //матрица модели-вида
            mvMatrix.translate( 0f, -2f, -7f );
            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ModelViewMatrix" ), 1, false,
                    FloatBuffer.wrap( mvMatrix.getMatrix() ) );

            //матрица проекции
            Matrix4 pMatrix = new Matrix4();
            gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ProjectionMatrix" ), 1, false,
                    FloatBuffer.wrap( pMatrix.getMatrix() ) );
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        //mvMatrix.rotate( rotX, 1f, 0f, 0f );
        mvMatrix.rotate( rotY, 0f, 1f, 0f );
        //mvMatrix.rotate( rotZ, 0f, 0f, 1f );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ModelViewMatrix" ),1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ));
        for (int i = 0; i < model.getElements().size(); i++) {

            Element o = model.getElements().get( i );
            FloatBuffer ka = o.getKa();
            FloatBuffer kd = o.getKd();
            FloatBuffer ks = o.getKs();
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 1, ka );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 1, kd );
            gl.glUniform3fv( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 1, ks );
            gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 21f );
            gl.glBindVertexArray( vaoHandle.get( i ) );
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
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "ProjectionMatrix" ), 1, false,
                FloatBuffer.wrap( pMatrix.getMatrix() ) );
    }
}
