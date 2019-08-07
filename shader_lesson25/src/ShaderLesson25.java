import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

/***
 * Применение фильтра размытия по Гауссу (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов - 2013. с.172)
 */

public class ShaderLesson25 {

    ShaderLesson25() {

        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLWindow window = GLWindow.create( capabilities );
        Field field = new Field();
        window.addGLEventListener( field );
        window.setSize( 400, 400 );
        window.setTitle( "Размытие по Гауссу" );
        window.setVisible( true );
        FPSAnimator animator = new FPSAnimator( window, 300, true );
        animator.start();
        window.addWindowListener( new WindowAdapter() {

            @Override
            public void windowDestroyNotify(WindowEvent e) {

                animator.stop();
                System.exit( 0 );
            }
        } );
    }

    public static void main(String[] args) {

        new ShaderLesson25();
    }
}
