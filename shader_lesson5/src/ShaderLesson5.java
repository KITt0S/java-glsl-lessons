import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;


/***
 * Рисование шаблона стены с помощью шейдеров ( Add. Wesley OpenGL.Shading.Language, 2009. p. 183-196)
 */

public class ShaderLesson5 extends JFrame {

    ShaderLesson5() {

        GLProfile profile = GLProfile.get( GLProfile.GL2 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        canvas.addGLEventListener( new Field() );
        canvas.setFocusable( true );
        canvas.setSize( 400, 400 );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setTitle( "Another brick in the wall" );
        setVisible( true );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new ShaderLesson5();
            }
        });
    }
}
