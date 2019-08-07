import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class CustomAdapter extends MouseAdapter {

    private float mx, my;
    float angX, angY;

    @Override
    public void mousePressed(MouseEvent e) {

        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        angX += ( e.getX() - mx ) / 300f;
        angY += ( e.getY() - my ) / 100f;
        mx = e.getX();
        my = e.getY();
    }
}
