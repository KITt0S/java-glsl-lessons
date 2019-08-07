import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class CustomAdapter extends MouseAdapter implements KeyListener {

    private float mx, my;
    float angX, angY;



    @Override
    public void mousePressed(MouseEvent e) {

        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        angX += ( mx - ( float ) e.getX() ) / 300f;
        angY += ( my - ( float ) e.getY() ) / 100f;
        mx = e.getX();
        my = e.getY();
    }


    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
