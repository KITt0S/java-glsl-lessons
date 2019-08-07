import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class CustomAdapter extends MouseAdapter {

    float mouseX, mouseY;
    float angX, angY;
    float dist = 2f;

    @Override
    public void mousePressed(MouseEvent e) {

        switch ( e.getButton() ) {

            case MouseEvent.BUTTON1: {

                mouseX = e.getX();
                mouseY = e.getY();
                break;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        switch( e.getButton() ) {

            case MouseEvent.BUTTON1: {

                angX += ( e.getX() - mouseX ) / 300f;
                angY += -( e.getY() - mouseY ) / 100f;
                mouseX = e.getX();
                mouseY = e.getY();
                break;
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

        dist += e.getRotation()[ 1 ] / 10f;
    }
}
