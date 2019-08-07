import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 * Имитация отражения с помощью кубической текстуры (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.140)
 */

public class ShaderLesson19 extends JFrame {

    ShaderLesson19() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        Field field = new Field();
        canvas.addGLEventListener( field );
        canvas.addMouseMotionListener( field );
        canvas.addMouseListener( field );
        canvas.setSize( 400, 400 );
        canvas.setFocusable( true );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setTitle( "Применение кубической текстуры" );
        setVisible( true );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new ShaderLesson19();
            }
        });
    }
}
