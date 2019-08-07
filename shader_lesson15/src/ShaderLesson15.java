import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 * Наложение двумерной текстуры (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.123)
 */

public class ShaderLesson15 extends JFrame {

    ShaderLesson15() {

        GLProfile profile = GLProfile.get( GLProfile.GL2 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        canvas.addGLEventListener( new Field() );
        canvas.setSize( 400, 400 );
        canvas.setFocusable( true );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setTitle( "Наложение двумерной текстуры" );
        setVisible( true );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main( String[] args ) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new ShaderLesson15();
            }
        });
    }
}
