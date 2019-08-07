import com.jogamp.nativewindow.util.Point;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class CustomAdapter extends MouseAdapter {

    private int x, y;
    protected float angX, angY;

    @Override
    public void mousePressed(MouseEvent e) {

        x = e.getX();
        y = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        angX += ( float ) ( e.getX() - x ) / 300f;
        angY += ( float )( e.getY() - y ) / 100f;
        x = e.getX();
        y = e.getY();
    }
}
