import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 *  Рисование и освещение тора (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов, с. 73)
 */

public class ShaderLesson6 extends JFrame {

    ShaderLesson6() {

        GLProfile profile = GLProfile.get( GLProfile.GL2 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        canvas.addGLEventListener( new Field() );
        canvas.setFocusable( true );
        canvas.setSize( 400, 400 );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setTitle( "Ambient-Diffuse-Specular model" );
        setVisible( true );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new ShaderLesson6();
            }
        });
    }
}
