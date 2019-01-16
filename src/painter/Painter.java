/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Painter extends JPanel {

    private ClientManager clientManager; // Set at ClientManager creation
    int height;
    int width;

    JFrame f;

    private ArrayList<Object> drawObjects_redo;
    private ArrayList<Object> drawObjects;
    private CurveData currentCurve;
    private JButton btn_connect_toServer;
    private JTextField tf_IP_address;
    private final String str_IP_default;
    private final String str_IP_pattern;
    private final Color color_right_address;
    private final Color color_wrong_address;
    private final Color color_connected;

    public Painter() {

        height = 600;
        width = 600;

        str_IP_default = "192.168.1.100";
        str_IP_pattern = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        color_right_address = new Color(226, 252, 237);
        color_wrong_address = new Color(254, 225, 214);
        color_connected = new Color(200, 224, 197);
        drawObjects_redo = new ArrayList<>();
        drawObjects = new ArrayList<>();
        // currentColor = Color.BLACK;
        setBackground(Color.white);
    }

    public void setDrawDataList_onMousePressed(CurveData curveData) {
        currentCurve = curveData;
        this.drawObjects.add(currentCurve);
    }

    public void setPointData(PointData point) {
        this.drawObjects.remove(currentCurve);
        currentCurve = null;
        this.drawObjects.add(point);
        repaint();
    }

    public void addPoint_onMouseDragged(Point point) {
        currentCurve.getPointsList().add(point);
        this.repaint();
    }

    public void set_initial_list_drawData(ArrayList<Object> drawData) {
        this.drawObjects = drawData;
        this.repaint();
    }

    public void set_initial_list_drawData_redo(ArrayList<Object> drawData_redo) {
        this.drawObjects_redo = drawData_redo;
    }

    /**
     * do event of mouse released
     */
    public void mouseReleasedEvent() {

        if (currentCurve.getPointsList().size() < 1) {
            drawObjects.remove(currentCurve);
        }
        currentCurve = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        int length = drawObjects.size();
        for (int i = 0; i < length; i++) {
            Object drawObj = drawObjects.get(i);
            if (drawObj instanceof CurveData) {
                drawCurve(g2, (CurveData) drawObj);
            } else if (drawObj instanceof PointData) {
                drawPoint(g2, (PointData) drawObj);
            }
        }
        g2.dispose();
    }

    private void drawCurve(Graphics2D g2, CurveData curve) {

        if (curve.isEraser()) {
            g2.setColor(getBackground());
        } else {
            g2.setColor(curve.getColor());
        }

        g2.setStroke(getStroke(curve.getStroke()));

        for (int i = 1; i < curve.getPointsList().size(); i++) {
            int x1 = curve.getPointsList().get(i - 1).x;
            int y1 = curve.getPointsList().get(i - 1).y;
            int x2 = curve.getPointsList().get(i).x;
            int y2 = curve.getPointsList().get(i).y;

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawPoint(Graphics2D g2, PointData point) {

        if (point.isEraser()) {
            g2.setColor(getBackground());
        } else {
            g2.setColor(point.getColor());
        }

        int diameter = (int) point.getDiameter() + 3;
        int x = point.getPoint().x - diameter / 2;
        int y = point.getPoint().y - diameter / 2;

        g2.fillOval(x, y, diameter, diameter);

    }

    private Stroke getStroke(float stroke) {
        return new BasicStroke(stroke, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
    }

    public void undoPaint() {
        if (drawObjects.size() > 0) {
            drawObjects_redo.add(drawObjects.remove(drawObjects.size() - 1));
            repaint();  // Redraw without the curve that has been removed.
        }
    }

    public void redoPaint() {
        if (drawObjects_redo.size() > 0) {
            drawObjects.add(drawObjects_redo.remove(drawObjects_redo.size() - 1));
            repaint();  // Redraw without the curve that has been removed.
        }
    }

    public void newPage(String newType) {
        switch (newType) {
            case "newAll":
                drawObjects.clear();
                drawObjects_redo.clear();
                repaint();
                break;
            case "newDrawList":
                drawObjects.clear();
                repaint();
                break;
            case "newRedoList":
                drawObjects_redo.clear();
                break;
        }
    }

    private JMenuBar createJMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        btn_connect_toServer = new JButton("Connect");
        btn_connect_toServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setConnectionStarterEnabled(false);
                set_tf_IP_Enabled(false);
                clientManager.allowReceivingThread(true);
                clientManager.startConnection();
            }
        });
        menuBar.add(btn_connect_toServer);

        tf_IP_address = new JTextField(str_IP_default);
        tf_IP_address.setBackground(color_right_address);
        Font font = tf_IP_address.getFont();
        font = new Font(font.getFamily(), Font.BOLD, font.getSize() + 2);
        tf_IP_address.setFont(font);

        tf_IP_address.getDocument().addDocumentListener((new DocumentRegex()));

        menuBar.add(tf_IP_address);

        /* Return the menu bar that has been constructed. */
        return menuBar;
    }

    public String getIPAddress() {
        return tf_IP_address.getText();
    }

    class DocumentRegex implements DocumentListener {

        String oldText = tf_IP_address.getText();

        private void doWork() {
            if (tf_IP_address.getText().matches(str_IP_pattern)) {
                tf_IP_address.setBackground(color_right_address);
                btn_connect_toServer.setEnabled(true);
            } else {
                tf_IP_address.setBackground(color_wrong_address);
                btn_connect_toServer.setEnabled(false);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            doWork();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            doWork();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

    }

    public void setConnectionStarterEnabled(boolean enabled) {
        btn_connect_toServer.setEnabled(enabled);
    }

    public void set_tf_IP_Enabled(boolean enabled) {
        tf_IP_address.setEnabled(enabled);
    }

    public void setConnectionState(String state) {
        if (state.equals(ClientManager.CONNECTED)) {
            btn_connect_toServer.setBackground(color_connected);
            btn_connect_toServer.setText(state);
        } else if (state.equals(ClientManager.UNCONNECTED)) {
            btn_connect_toServer.setBackground(null);
            btn_connect_toServer.setText(state);
        }
    }

    public ClientManager getClientManager() {
        return this.clientManager;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }


    public void createPainterGUI(Painter painter) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                f = new JFrame();
                Toolkit tk = Toolkit.getDefaultToolkit();
                int xSize = ((int) tk.getScreenSize().getWidth() - 250);
                int ySize = ((int) tk.getScreenSize().getHeight() - 200);
                f.setSize(xSize, ySize);
                f.setLocationRelativeTo(null);

                f.setTitle(clientManager.getClientName());
                f.setJMenuBar(Painter.this.createJMenuBar());

                PainterWSL pwl = new PainterWSL(Painter.this);

                f.addWindowListener(pwl);
                f.addWindowFocusListener(pwl);
                f.addWindowStateListener(pwl);

                f.getContentPane().add(painter);

                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                f.setVisible(true);
            }
        }
        );
    }
}
