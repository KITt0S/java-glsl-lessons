import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 * Наложение нескольких текстур (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.128)
 */

public class ShaderLesson16 extends JFrame {

    private ShaderLesson16() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        canvas.addGLEventListener( new Field() );
        canvas.setSize( 400, 400 );
        canvas.setFocusable( true );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setVisible( true );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        setTitle( "Две текстуры" );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main( String[] args ) {

        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {

                new ShaderLesson16();
            }
        });
    }
}
