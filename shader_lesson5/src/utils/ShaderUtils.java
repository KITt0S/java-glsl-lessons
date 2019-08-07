package utils;

import com.jogamp.opengl.GL2;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ShaderUtils {

    public static int createProgram( GL2 gl, int vsId, int fsId ) {

        final int programId = gl.glCreateProgram();
        if( programId == 0 ) {

            return 0;
        }
        gl.glAttachShader( programId, vsId );
        gl.glAttachShader( programId, fsId );
        gl.glLinkProgram( programId );
        final int[] linkStatus = new int[ 1 ];
        gl.glGetProgramiv( programId, GL2.GL_LINK_STATUS, linkStatus, 0 );
        if( linkStatus[ 0 ] == 0 ) {

            gl.glDeleteProgram( programId );
            return 0;
        }
        return programId;
    }

    public static int createShader( GL2 gl, int type, File shaderFile ) {

        String shaderText = FileUtils.readShader( shaderFile );
        return ShaderUtils.createShader( gl, type, shaderText );
    }

    public static int createShader(GL2 gl, int type, String shaderText ) {

        final int shaderId = gl.glCreateShader( type );
        if( shaderId == 0 ) {

            return 0;
        }
        String[] str = new String[]{ shaderText };
        gl.glShaderSource( shaderId, 1, str, null );
        gl.glCompileShader( shaderId );
        getShaderInfo( gl, shaderId );
        final int[] compileStatus = new int[ 1 ];
        gl.glGetShaderiv( shaderId, GL2.GL_COMPILE_STATUS, compileStatus, 0 );
        if( compileStatus[ 0 ] == 0 ) {

            gl.glDeleteShader( shaderId );
            return 0;
        }
        return shaderId;
    }

    public static void getShaderInfo( GL2 gl, int shaderId ) {

        IntBuffer intBuffer = IntBuffer.wrap( new int[ 1 ] );
        gl.glGetShaderiv( shaderId, GL2.GL_INFO_LOG_LENGTH, intBuffer );
        int infoLength = intBuffer.get( 0 );
        if( infoLength > 0 ) {

            ByteBuffer byteBuffer = ByteBuffer.allocate( infoLength );
            gl.glGetShaderInfoLog( shaderId, infoLength, intBuffer, byteBuffer );
            for ( byte b :
                 byteBuffer.array() ) {

                System.err.print( ( char )b );
            }
            System.err.println();
        }
    }

    public static int getUniLoc( GL2 gl, int progId, String name ) {

        int loc = gl.glGetUniformLocation( progId, name );
        if( loc == -1 ) {

            System.out.println( String.format( "Uniform-переменная \"%s\" не существует\n", name ) );
        }
        return loc;
    }

    public static int getVaryingLoc( GL2 gl, int progId, String name ) {

        int loc = gl.glGetAttribLocation( progId, name );
        if( loc == -1 ) {

            System.out.println( String.format( "Переменная \"%s\" не существует\n", name ) );
        }
        return loc;
    }
}
