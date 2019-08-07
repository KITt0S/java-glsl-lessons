import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 * Использование карт прозрачности для удаления пикселей
 * (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.131)
 */

public class ShaderLesson17 extends JFrame {

    ShaderLesson17() {

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

    public static void main(String[] args) {

        new ShaderLesson17();
    }
}
