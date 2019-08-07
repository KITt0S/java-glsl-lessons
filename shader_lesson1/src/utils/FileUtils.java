package utils;

import java.io.*;

public class FileUtils {

    public static String readShader( File shaderFile ) {

        StringBuilder stringBuilder = new StringBuilder();
        try {

            BufferedReader bufferedReader = null;
            try {

                bufferedReader = new BufferedReader( new FileReader( shaderFile) );
                String line;
                while( ( line = bufferedReader.readLine() ) != null ) {

                    stringBuilder.append( line );
                    stringBuilder.append( '\n' );
                }
            } finally {

                if( bufferedReader != null ) {

                    bufferedReader.close();
                }
            }
        } catch ( IOException e ) {

            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
