/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 *
 * @author S
 */
public class PainterWSL extends WindowAdapter {

    Painter painter;
    ArrayList<Boolean> active_focus_state;
    int counter;

    // Whate to send
    boolean active;
    boolean upfront;
    boolean fullScreen;
    //
    boolean fullScreenOld;

    private ClientManager clientManager;

    PainterWSL(Painter painter) {
        counter = 0;
        active_focus_state = new ArrayList();
        //
        active = true;
        upfront = true;
        fullScreen = false;
        //
        fullScreenOld = false;
        //
        this.painter = painter;
        clientManager = painter.getClientManager();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        clientManager.notifyServer_clientExit();
    }

    @Override
    public void windowIconified(WindowEvent e) {
        upfront = false;
        clientManager.notifyServer_client_upfront_down(upfront);
        //send upfront
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        upfront = true;
        clientManager.notifyServer_client_upfront_down(upfront);

        //send upfront
    }

    @Override
    public void windowActivated(WindowEvent e) {
        say_combined_focus_active(true);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        say_combined_focus_active(false);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        say_combined_focus_active(true);
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        say_combined_focus_active(false);
    }

    private void say_combined_focus_active(boolean state) {

        if (active_focus_state.size() < 2) {
            active_focus_state.add(state);
        }

        boolean calculatedState = false;
        if (active_focus_state.size() == 2) {
            boolean b1 = active_focus_state.get(0);
            boolean b2 = active_focus_state.get(1);
            calculatedState = b1 || b2;
            active_focus_state.clear();
        }

        ++counter;
        if (counter == 2) {
            counter = 0;

            active = calculatedState;
            clientManager.notifyServer_client_active_state(active);
            //send active state
        }
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        
        if (upfront) {
            
            boolean state = getState(e.getNewState());

            if (state != fullScreenOld) {
                fullScreenOld = state;
                clientManager.notifyServer_client_screen_state(state);
            }
        }
    }

    /**
     * true: fullscreen false: Non-fullscreen
     *
     * @param state
     * @return
     */
    private boolean getState(int state) {

        if (state == Frame.NORMAL) {
            fullScreen = false;
        } else if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            fullScreen = true;
        }

        return fullScreen;
    }
}
