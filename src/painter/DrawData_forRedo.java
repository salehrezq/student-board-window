/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author S
 */
public class DrawData_forRedo implements Serializable {

    private static final long serialVersionUID = 33702304602081L;

    private ArrayList<Object> drawDataList_redo;

    public ArrayList<Object> getCurvesList_redo() {
        return drawDataList_redo;
    }

}
