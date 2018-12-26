/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.awt.Color;
import java.awt.Point;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author S
 */
public class ClientManager {

    private static final int PORT_FOR_SINGLE_INSTANCE = 32573;
    private static ServerSocket serverSocket_to_reserve_single_instance;
    //
    public static final String CONNECTED = "Connected";
    public static final String UNCONNECTED = "Connect";
    private String client_LAN_name;
    Socket socket;
    private static final int PORT = 48317;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    private volatile boolean enabilityRecieving = true;

    private ArrayList<CurveData> curvesList;
    Painter paint;
    Thread threadConnection;
    Thread recieveThread;

    public ClientManager() {
        set_fullAddressName();
        paint = new Painter();
        paint.createPainterGUI(paint);
        paint.clientManager = this;
        threadConnection = new Thread(connectToServer);
    }
    
    public String getClientName()
    {
        return "Client board: " + this.client_LAN_name;
    }

    private void set_fullAddressName() {
        try {
            InetAddress clientAddress = null;
            client_LAN_name = clientAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            System.out.println("Error > set_and_get_fullAddressName >");
            e.printStackTrace();
        }
    }

    public void startConnection() {
        try {
            threadConnection.start();
        } catch (java.lang.IllegalThreadStateException e) {
            handleExceptionOnConnection();
            System.out.println("IllegalThreadStateException catched");
            e.printStackTrace();
        }
    }

    private void handleExceptionOnConnection() {
        allowReceivingThread(false);
        closeStreams();
        threadConnection = new Thread(connectToServer);
        paint.setConnectionState(UNCONNECTED);
        paint.setConnectionStarterEnabled(true);
        paint.set_tf_IP_Enabled(true);
    }

    Runnable connectToServer = new Runnable() {
        @Override
        public void run() {
            {
                try {//paint.getIPAddress()
                    socket = new Socket(paint.getIPAddress(), PORT);
                    openStreams();
                } catch (java.net.ConnectException d) {
                    System.out.println("ConnectException catch");
                    handleExceptionOnConnection();
                } catch (IOException ex) {
                    handleExceptionOnConnection();
                    Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    private void openStreams() throws IOException {
        if (socket != null) {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            //
            setOperations();
        }
    }

    private void setOperations() {
        start_new_recieving_thread();
        sendToServer_myClientAddress();
    }

    private void start_new_recieving_thread() {
        recieveThread = new Thread(receiveFromServer);
        recieveThread.start();
    }

    private void sendToServer_myClientAddress() {
        writeObject("address" + client_LAN_name);
    }

    private String[] extract_clientAddress_segments(String fullAddress) {

        String[] address_segments = new String[2];
        int i;
        String str = fullAddress;
        for (i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '/') {
                break;
            }
        }

        String name = str.substring(0, i);
        String IP = str.substring(i + 1);

        address_segments[0] = name;
        address_segments[1] = IP;

        return address_segments;
    }

    Runnable receiveFromServer = new Runnable() {
        @Override
        public void run() {
            while (enabilityRecieving) {
                receiveFromServer();
            }
            closeStreams();
            recieveThread = null;
        }
    };

    public void allowReceivingThread(boolean enable) {
        enabilityRecieving = enable;
    }

    private void receiveFromServer() {
        if (socket != null && ois != null) {
            try {
                Object obj = ois.readObject();

                if (obj instanceof Point) {
                    paint.addPoint_onMouseDragged((Point) obj);
                } else if (obj instanceof CurveData) {
                    paint.setDrawDataList_onMousePressed((CurveData) obj);
                } else if (obj instanceof Color) {
                    paint.setBackground((Color) obj);
                } else if (obj instanceof PointData) {
                    //happen on mouse relased on server
                    //occures if no dragging; but press then release.
                    paint.setPointData((PointData) obj);
                } else if (obj instanceof ArrayList<?>) {
                    if (((ArrayList<?>) obj).get(0) instanceof CurveData) {
                        paint.set_initial_list_drawData((ArrayList<Object>) obj);
                    }
                } else if (obj instanceof DrawData_forRedo) {
                    paint.set_initial_list_drawData_redo((ArrayList<Object>) ((DrawData_forRedo) obj).getCurvesList_redo());
                } else if (obj instanceof String) {
                    String command = (String) obj;
                    switch (command) {
                        case "mouseReleased":
                            paint.mouseReleasedEvent();
                            break;
                        case "undo":
                            paint.undoPaint();
                            break;
                        case "redo":
                            paint.redoPaint();
                            break;
                        case "newAll":
                            paint.newPage("newAll");
                            break;
                        case "newDrawList":
                            paint.newPage("newDrawList");
                            break;
                        case "newRedoList":
                            paint.newPage("newRedoList");
                            break;
                        case "welcome":
                            paint.setConnectionState(CONNECTED);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {

                handleExceptionOnConnection();
                System.out.println("java.net.SocketException: Connection reset");
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void notifyServer_clientExit() {
        writeObject("ClientClose");
        allowReceivingThread(false);
    }

    public void notifyServer_client_upfront_down(boolean state) {
        writeObject("isUpFront" + state);
    }

    // Serves both Active and Focus state.
    public void notifyServer_client_active_state(boolean state) {
        writeObject("isActive" + state);
    }

    public void notifyServer_client_screen_state(boolean state) {
        writeObject("isFullscreen" + state);
    }

    private void writeObject(Object obj) {
        if (socket != null && oos != null) {
            try {
                oos.reset();
                oos.writeObject(obj);
                oos.flush();
            } catch (IOException ex) {
                System.out.println("writeObject clientExited");
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void closeStreams() {
        try {
            if (ois != null) {

                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientManager.checkIfRunning();
                ClientManager c = new ClientManager();
            }
        });
    }

    private static void checkIfRunning() {
        try {
            //Bind to localhost adapter with a zero connection queue 
            serverSocket_to_reserve_single_instance = new ServerSocket(PORT_FOR_SINGLE_INSTANCE, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        } catch (BindException e) {
            JOptionPane.showMessageDialog(null, "An instance of this application is already running", "Only single instance allowed", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Some unknown error occured.\nPlease try later", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
