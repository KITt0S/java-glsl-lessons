import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Matrix4;
import utils.ShaderUtils;

import java.awt.*;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Field extends CustomAdapter implements GLEventListener {

    private BlenderObj teapot = BlenderObjReader.read( "Model/Teapot/teapot.obj",
            "Model/Teapot/teapot.mtl", false );
    private BlenderObj cube = BlenderObjReader.read( "Model/Cube/cube.obj", "Model/Cube/cube.mtl",
            false );
    private int program;
    private IntBuffer vaoHandle = IntBuffer.wrap( new int[ 2 ] );
    private IntBuffer fboHandle = IntBuffer.wrap( new int[ 1 ] ); //дескриптор FBO
    private Matrix4 modelMatrix = new Matrix4();
    private Matrix4 cameraMatrix = new Matrix4();
    private Matrix4 pMatrix = new Matrix4();
    private Dimension screenSize = new Dimension();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glEnable( GL3.GL_DEPTH_TEST );
        gl.glDepthFunc( GL3.GL_LEQUAL );
        int vs = ShaderUtils.createShader( gl, GL3.GL_VERTEX_SHADER, new File( "Shaders/Vertex shader.glsl" ) );
        int fs = ShaderUtils.createShader( gl, GL3.GL_FRAGMENT_SHADER, new File( "Shaders/Fragment shader.glsl" ) );
        program = ShaderUtils.createProgram( gl, vs, fs );
        gl.glUseProgram( program );
        setShaderData( gl );
    }

    private void setShaderData( GL3 gl ) {

        /* Быстро-меняющиеся переменнные*/
        IntBuffer vbo = IntBuffer.wrap( new int[ 5 ] );
        gl.glGenBuffers( 5, vbo );
        gl.glGenVertexArrays( 2, vaoHandle );

        //объект буфера вершин и нормалей чайника
        gl.glBindVertexArray( vaoHandle.get( 0 ) );
        FloatBuffer v = teapot.getAllVertexesBuffer();
        FloatBuffer n = teapot.getAllNormalsBuffer();
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

        //объект буфера вершин, нормалей и текстурных координат куба
        gl.glBindVertexArray( vaoHandle.get( 1 ) );
        v = cube.getAllVertexesBuffer();
        n = cube.getAllNormalsBuffer();
        FloatBuffer t = cube.getAllTexCoordsBuffer();
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 2 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, v.capacity() * 4, v, GL3.GL_STATIC_DRAW );
        gl.glVertexAttribPointer( vLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( vLoc );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 3 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, n.capacity() * 4, n, GL3.GL_STATIC_DRAW );
        gl.glVertexAttribPointer( nLoc, 3, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( nLoc );
        gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vbo.get( 4 ) );
        gl.glBufferData( GL3.GL_ARRAY_BUFFER, t.capacity() * 4, t, GL3.GL_STATIC_DRAW );
        int tLoc = ShaderUtils.getVaryingLoc( gl, program, "VertexTexCoord" );
        gl.glVertexAttribPointer( tLoc, 2, GL3.GL_FLOAT, false, 0, 0L );
        gl.glEnableVertexAttribArray( tLoc );

        /*Отображение в текстуру
        1. Подготовка объекта буффера*/
        //текстура
        gl.glGenFramebuffers( 1, fboHandle );
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );

        //объект текстуры
        IntBuffer tbo = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenTextures( 1, tbo );
        gl.glActiveTexture( GL3.GL_TEXTURE0 );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, tbo.get( 0 ) );
        gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, 512, 512 );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );

        //связать текстуру с объектом буфера кадра
        gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, tbo.get( 0 ), 0 );

        //создать буффер глубины
        IntBuffer depthBuf = IntBuffer.wrap( new int[ 1 ] );
        gl.glGenRenderbuffers( 1, depthBuf );
        gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, depthBuf.get( 0 ) );
        gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, 512, 512 );

        //Связать буфер глубины с объектом буффера кадра
        gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER,
                depthBuf.get( 0 ) );

        //установить цель для вывода результатов фрагментного шейдера
        IntBuffer drawBufs = IntBuffer.wrap( new int[]{ GL3.GL_COLOR_ATTACHMENT0 } );
        gl.glDrawBuffers( 1, drawBufs );

        //отвязать буфер кадра и вернуть буфер кадра по умолчанию
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );

        /*2. Создание простой текстуры 1x1*/
        //однопиксельная белая текстура
        IntBuffer whiteTexHandle = IntBuffer.wrap( new int[ 1 ] );
        IntBuffer whiteTex = IntBuffer.wrap( new int[]{ 255, 255, 255, 255 } );
        gl.glActiveTexture( GL3.GL_TEXTURE1 );
        gl.glGenTextures( 1, whiteTexHandle );
        gl.glBindTexture( GL3.GL_TEXTURE_2D, whiteTexHandle.get( 0 ) );
        gl.glTexStorage2D( GL3.GL_TEXTURE_2D, 1, GL3.GL_RGBA8, 1, 1 );
        gl.glTexSubImage2D( GL3.GL_TEXTURE_2D, 0, 0, 0, 1, 1, GL3.GL_RGBA,
                GL3.GL_UNSIGNED_BYTE, whiteTex );
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();
        //связать с буфером кадра для текстуры
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, fboHandle.get( 0 ) );
        gl.glViewport( 0, 0, 512, 512 );
        gl.glClearColor( 0.5f, 0.5f, 0.5f, 1f );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );

        //использовать белую текстуру
        int loc = ShaderUtils.getUniLoc( gl, program, "Texture" );
        gl.glUniform1i( loc, 1 );

        //подготовить матрицу проекции и вида для сцены, отображаемой в текстуру
        modelMatrix.translate( 0f, -1.5f, 0f );
        cameraMatrix.loadIdentity();
        cameraMatrix.rotate( angX1, 0f, 1f, 0f );
        cameraMatrix.rotate( angY1, 1f, 0f, 0f );
        cameraMatrix.translate( 0f, 0f, 7f );
        cameraMatrix.invert();
        Matrix4 mvMatrix = new Matrix4();
        mvMatrix.multMatrix( cameraMatrix );
        mvMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        Matrix4 mvp = new Matrix4();
        mvp.multMatrix( pMatrix );
        mvp.multMatrix( cameraMatrix );
        mvp.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvp.getMatrix() ) );

        gl.glBindVertexArray( vaoHandle.get( 0 ) );

        //параметры освещения чайника
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 5f, 5f, 0f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //параметры материала чайника
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess"), 20f );
        int pSize = 0;
        for (int i = 0; i < teapot.getElements().size(); i++) {

            pSize += teapot.getElements().get( i ).getFaces().size() * 3;
        }
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );

        //отвязать FBO текстуры (вернуться к буферу кадра по умолчанию) и использовать сгенерированную текстуру
        gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 );
        gl.glViewport( 0, 0, ( int )screenSize.getWidth(), ( int )screenSize.getHeight() );
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT );
        gl.glUniform1i( loc, 0 );

        //подготовить матрицу проекции и вида для главной сцены
        modelMatrix.loadIdentity();
        cameraMatrix.loadIdentity();
        cameraMatrix.rotate( angX2, 0f, 1f, 0f );
        cameraMatrix.rotate( angY2, 1f, 0f, 0f );
        cameraMatrix.translate( 0f, 0f, 4f );
        cameraMatrix.invert();
        mvMatrix.loadIdentity();
        mvMatrix.multMatrix( cameraMatrix );
        mvMatrix.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVMatrix"), 1, false,
                FloatBuffer.wrap( mvMatrix.getMatrix() ) );
        mvp.loadIdentity();
        mvp.multMatrix( pMatrix );
        mvp.multMatrix( cameraMatrix);
        mvp.multMatrix( modelMatrix );
        gl.glUniformMatrix4fv( ShaderUtils.getUniLoc( gl, program, "MVP" ), 1, false,
                FloatBuffer.wrap( mvp.getMatrix() ) );

        gl.glBindVertexArray( vaoHandle.get( 1 ) );

        //параметры освещения куба
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Position" ), 10f, 0f, 10f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Light.Intensity" ), 1f, 1f, 1f );

        //параметры материала куба
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ka" ), 0.2f, 0.2f, 0.2f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Kd" ), 0.64f, 0.64f, 0.64f );
        gl.glUniform3f( ShaderUtils.getUniLoc( gl, program, "Material.Ks" ), 0.5f, 0.5f, 0.5f );
        gl.glUniform1f( ShaderUtils.getUniLoc( gl, program, "Material.Shininess"), 20f );

        pSize = 0;
        for (int i = 0; i < cube.getElements().size(); i++) {

            pSize += cube.getElements().get( i ).getFaces().size() * 3;
        }
        gl.glDrawArrays( GL3.GL_TRIANGLES, 0, pSize );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();
        screenSize.setSize( width, height );
        gl.glViewport( 0, 0, width, height );
        if( height == 0 ) {

            height = 1;
        }
        float h = ( float ) width / ( float ) height;
        pMatrix.loadIdentity();
        pMatrix.makePerspective( 45f, h, 0.1f, 100f );
    }
}
