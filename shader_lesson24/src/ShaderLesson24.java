import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

/***
 * Применение фильтра выделения границ (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.166)
 */

public class ShaderLesson24 {

    ShaderLesson24() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLWindow window = GLWindow.create( capabilities );
        window.addGLEventListener( new Field() );
        window.setSize( 400, 400 );
        window.setVisible( true );
        window.setTitle( "Выделение границ" );
        FPSAnimator animator = new FPSAnimator( window, 300, true );
        animator.start();
        window.addWindowListener( new WindowAdapter() {
            @Override
            public void windowDestroyNotify( WindowEvent e ) {

                animator.stop();
                System.exit( 0 );
            }
        });
    }

    public static void main(String[] args) {

        new ShaderLesson24();
    }
}
