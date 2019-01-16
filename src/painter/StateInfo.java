/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.io.Serializable;

/**
 *
 * @author Saleh
 */
public class StateInfo implements Serializable {

    private static final long serialVersionUID = 83904320664393L;

    private String type;
    private boolean state;
    private String info;

    public StateInfo() {

    }

    public StateInfo(String type, boolean state) {
        this.type = type;
        this.state = state;
    }
    public StateInfo(String type, boolean state, String message) {
        this.type = type;
        this.state = state;
        this.info = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
