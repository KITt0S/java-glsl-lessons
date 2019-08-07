import com.jogamp.newt.event.*;

public class CustomAdapter implements KeyListener, MouseListener {

    float angX1, angY1, angX2, angY2;
    private float mx2, my2;

    @Override
    public void keyPressed(KeyEvent e) {

        switch( e.getKeyCode() ) {

            case KeyEvent.VK_LEFT: {

                angX1 -= 0.01f;
                break;
            }

            case KeyEvent.VK_RIGHT: {

                angX1 += 0.01f;
                break;
            }

            case KeyEvent.VK_UP: {

                angY1 += 0.01f;
                break;
            }

            case KeyEvent.VK_DOWN: {

                angY1 -= 0.01f;
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        mx2 = e.getX();
        my2 = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        angX2 += ( mx2 - e.getX() ) / 300f;
        angY2 += ( my2 - e.getY() ) / 100f;
        mx2 = e.getX();
        my2 = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

    }
}
