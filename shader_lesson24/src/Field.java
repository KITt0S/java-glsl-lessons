import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field implements GLEventListener {

    private BlenderObj model = BlenderObjReader.read( "Model/Teapot_and_thorus/teapot_and_thorus.obj",
            "Model/Teapot_and_thorus/teapot_and_thorus.mtl", false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 2 ] );
    private IntBuffer fboHandle = IntBuffer.wrap( new int[ 1 ] );

    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 cameraMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();

    private int width, height;

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

        IntBuffer vbo = IntBuffer.wrap( new int[ 3 ] );
        gl.glGenBuffers( 3, vbo );
        gl.glGenVertexArrays( 2, vaoHandle );

        // первый объект вершин
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        FloatBuffer v = model.getAllVertexesBuffer();
        FloatBuffer n = model.getAllNormalsBuffer();
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

        //второй объект вершин
        gl.glBindVertexArray( vaoHandle.get( 1 ) );
        FloatBuffer v1 = FloatBuffer.wrap( new float[]{

                -1f, -1f,
                1f, 1f,
                -1f, 1f,
                -1f, -1f,
                1f, -1f,
                1f, 1f } );

        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, v1.capacity() * 4, v1, GL3.GL_STATIC_DRAW );
        gl.glVertexAttribPointer( vLoc, 2,  GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( vLoc );

        //материал
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.2f, 0.2f, 0.2f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.6f, 0.6f, 0.6f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess" ), 20f );

        //свет
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 10f, 10f );

        /*
        Подготовка объекта буффера
         */
        gl.glGenFramebuffers( 1, fboHandle );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );

        //объект текстуры, в которую будет производится запись изображения с экрана
        IntBuffer tbo = IntBuffer.wrap( new int[ 2 ] );
        gl.glGenTextures( 2, tbo );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
        gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, 1920, 1080 );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );

        //связывание буфера кадра с текстурой
        gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, tbo.get( 0 ), 0 );

        //буфер глубины
        IntBuffer depthBuf = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenRenderbuffers( 1, depthBuf );
        gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, depthBuf.get( 0 ) );
        gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, 1920, 1080 );

        //связывание буфера кадра с буфером глубины
        gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER, depthBuf.get( 0 ) );

        //цель для вывода результатов фрагментного шейдера
        IntBuffer drawBufs = IntBuffer.wrap( new int[]{ GL3.GL_COLOR_ATTACHMENT0 } );
        gl.glDrawBuffers( 1, drawBufs );

        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );

        //белая текстура
        gl.glActiveTexture( GL3.GL_TEXTURE1 );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 1 ) );
        gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, 1, 1 );
        IntBuffer whiteTex = IntBuffer.wrap( new int[]{ 100, 100, 255, 255 } );
        gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, 1, 1, GL3.GL_RGBA,
                GL3.GL_UNSIGNED_BYTE, whiteTex );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
        //gl.glViewport( 0, 0, width, height );
        gl.glClearColor( 0f, 0f, 0f, 1f );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );

        int txLoc = ShaderUtils.getUniLoc( gl, program, "RenderTex" );
        gl.glUniform1i( ShaderUtils.getUniLoc( gl, program, "Step" ), 0 );

        //матрицы для промежуточного буфера
        cameraMatrix.loadIdentity();
        cameraMatrix.translate( 0f, 2f, 10f );
        cameraMatrix.invert();
        Matrix4 mvMatrix = new Matrix4();
        mvMatrix.multMatrix( cameraMatrix );
        mvMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        Matrix4 mvpMatrix = new Matrix4();
        mvpMatrix.multMatrix( pMatrix );
        mvpMatrix.multMatrix( mvMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );

        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        int pSize = 0;
        for (int i = 0; i < model.getElementsSize(); i++) {

            pSize += model.getElement( i ).getFacesSize() * 3;
        }
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );

        //основной буффер кадра
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        //gl.glViewport( 0, 0, width, height );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        gl.glUniform1i( txLoc, 0 );

        gl.glUniform1i( ShaderUtils.getUniLoc( gl, program, "Step" ), 1 );

        //матрицы
        mvMatrix.loadIdentity();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        mvpMatrix.loadIdentity();
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvpMatrix.getMatrix() ) );

        gl.glBindVertexArray( vaoHandle.get( 1 ) );
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, 6 );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport( 0, 0, width, height );
        this.width = width;
        this.height = height;
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        pMatrix.loadIdentity();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
