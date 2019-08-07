import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/***
 * Двухстороннее освещение чайника (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов, с. 83)
 * Работа с режимом плоского затенения - добавление аттрибута flat к выходным параметрам вершинного шейдера и входным
 * параметрам фрагементного шейдера (Дэвид Вольф - OpenGL4. Язык шейдеров. Книга рецептов, с. 87)
 */

public class ShaderLesson7 extends JFrame {

    private ShaderLesson7() {

        GLProfile profile = GLProfile.get( GLProfile.GL2 );
        GLCapabilities capabilities = new GLCapabilities( profile );
        GLCanvas canvas = new GLCanvas( capabilities );
        canvas.addGLEventListener( new Field() );
        canvas.setSize( 400, 400 );
        canvas.setFocusable( true );
        getContentPane().add( canvas );
        setSize( getContentPane().getPreferredSize() );
        setTitle( "Двухстороннее освещение и плоское затенение" );
        setVisible( true );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        FPSAnimator animator = new FPSAnimator( canvas, 300, true );
        animator.start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater( ShaderLesson7::new );
    }
}
