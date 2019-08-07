import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.Display;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import jogamp.newt.PointerIconImpl;

import javax.swing.*;

/***
 * Наложение проецируемой текстуры (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.152)
 */

public class ShaderLesson21 {

    ShaderLesson21() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLWindow window = GLWindow.create( capabilities );
        Field field = new Field();
        window.addGLEventListener( field );
        window.addMouseListener( field );
        window.setSize( 400, 400 );
        window.setTitle( "Проецирование текстуры" );
        window.setVisible( true );
        FPSAnimator animator = new FPSAnimator( window, 300, true );
        animator.start();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent e) {

                if( animator.isStarted() ) {

                    animator.stop();
                }
                System.exit( 0 );
            }
        });
    }

    public static void main(String[] args) {

        new ShaderLesson21();
    }
}
