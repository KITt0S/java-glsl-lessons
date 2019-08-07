import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

/***
 * Отложенное освещение и затенение (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.197)
 */

public class ShaderLesson28 {

    ShaderLesson28() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLWindow window = GLWindow.create( capabilities );
        Field myField = new Field();
        window.addGLEventListener( myField );
        window.setSize( 1024, 768 );
        window.setTitle( "Отложенное освещение" );
        window.setVisible( true );
        FPSAnimator animator = new FPSAnimator( window, 300, true );
        animator.start();
        window.addWindowListener( new WindowAdapter() {

            @Override
            public void windowDestroyNotify(WindowEvent e) {

                animator.stop();
                System.exit( 0 );
            }
        });
    }

    public static void main( String[] args ) {

        new ShaderLesson28();
    }
}
