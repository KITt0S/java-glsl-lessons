import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/***
 * Сглаживание множественной выборкой Multisample Anti-Aliasing, MSAA
 * (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.192)
 */

public class ShaderLesson27 {

    ShaderLesson27() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        capabilities.setSampleBuffers( true );
        capabilities.setNumSamples( 8 );
        GLWindow window = GLWindow.create( capabilities );
        window.addGLEventListener( new Field() );
        window.setSize( 400, 400 );
        window.setVisible( true );
        window.setTitle( "Сглаживание множественной выборкой" );
        FPSAnimator animator = new FPSAnimator( window, 300, true );
        animator.start();
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowDestroyNotify(WindowEvent e) {

                animator.stop();
                System.exit( 0 );
            }
        });
    }

    public static void main(String[] args) {

        new ShaderLesson27();
    }
}
