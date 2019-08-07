import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;

/***
 * Имитация преломления с помощью кубической текстуры (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.147)
 */

public class ShaderLesson20 extends JFrame {

    ShaderLesson20() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLWindow window = GLWindow.create( capabilities );
        NewtCanvasAWT canvas = new NewtCanvasAWT( window );
        Field field = new Field();
        window.addGLEventListener( field );
        window.addMouseListener( field );
        window.setDefaultCloseOperation( WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE );
        canvas.setSize( 400, 400 );
        canvas.setFocusable( true );
        getContentPane().add( canvas );
        setSize( 400, 400 );
        setVisible( true );
        setTitle( "Эффект преломления" );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        GLAnimatorControl animator = new Animator( window );
        animator.start();
    }

    public static void main( String[] args ) {

        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {

                new ShaderLesson20();
            }
        });
    }
}
