/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.ussdgateway.tools.httpsimulator;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class ServerTesterForm extends JDialog {

    private HttpSimulatorForm mainForm;
    private HttpSimulatorParameters param;
    private JButton btStart;
    private JButton btStop;
    private JTextArea tbLog;
    private JTextArea tbResponse;

    private ServerSocket socket;
    private boolean started = false;
    private JButton btSend;
    private JButton btClearLog;

    private Charset utf8;
    private int responseNum;
    private String cookie = null;

    public ServerTesterForm(JFrame owner) {
        super(owner, true);
        setModal(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (getDefaultCloseOperation() == JDialog.DO_NOTHING_ON_CLOSE) {
                    JOptionPane.showMessageDialog(getJDialog(), "Before exiting you must Stop the testing process");
                } else {
                    closingWindow();
                }
            }
        });
        setBounds(100, 100, 772, 677);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        btStart = new JButton("Start a listerning");
        btStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        btStart.setBounds(10, 309, 141, 23);
        panel.add(btStart);
        
        btStop = new JButton("Stop a listerning");
        btStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        btStop.setEnabled(false);
        btStop.setBounds(158, 309, 122, 23);
        panel.add(btStop);
        
        JLabel lblResponseContext = new JLabel("Request / Response context");
        lblResponseContext.setBounds(10, 349, 448, 14);
        panel.add(lblResponseContext);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 11, 736, 292);
        panel.add(scrollPane);
        
        tbLog = new JTextArea();
        scrollPane.setViewportView(tbLog);
        tbLog.setEditable(false);
        
        btSend = new JButton("Send request");
        btSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
        btSend.setBounds(287, 309, 141, 23);
        panel.add(btSend);
        
        btClearLog = new JButton("Clear Log");
        btClearLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                clearLog();
            }
        });
        btClearLog.setBounds(438, 309, 141, 23);
        panel.add(btClearLog);
        
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(10, 374, 736, 254);
        panel.add(scrollPane_1);
        
        tbResponse = new JTextArea();
        scrollPane_1.setViewportView(tbResponse);

        utf8 = Charset.forName("utf-8");
    }

    private JDialog getJDialog() {
        return this;
    }

    private void closingWindow() {
        this.mainForm.testingFormClose();
    }

    public void setData(HttpSimulatorForm mainForm, HttpSimulatorParameters param) {
        this.param = param;
        this.mainForm = mainForm;
    }

    private void send() {
        try {
            InetAddress ipAddress = InetAddress.getByName(this.param.getCallingHost());
            Socket socket = new Socket(ipAddress, this.param.getCallingPort());

            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            String s1 = getResponseText();
            byte[] bufx = s1.getBytes(utf8);
            StringBuilder sb = new StringBuilder();
            sb.append("POST " + this.param.getUrl() + " HTTP/1.1\n");
            sb.append("Content-Length: ");
            sb.append(bufx.length);
            sb.append("\n");
            sb.append("Content-Type: text/xml\n");
            sb.append("Content-Encoding: utf-8\n");
            sb.append("Host: ");
            sb.append(this.param.getCallingHost());
            sb.append(":");
            sb.append(this.param.getCallingPort());
            sb.append("\n");
            if (cookie != null) {
                sb.append("Cookie: JSESSIONID=");
                sb.append(cookie);
                sb.append("\n");
            }
            sb.append("Connection: Keep-Alive\n\n");
            sb.append(s1);

            byte[] buf = sb.toString().getBytes(utf8);
            sout.write(buf);
            sout.flush();

            tbLog.setText(tbLog.getText() + "\n----------------------------------------------" + new Date() + "\n");
            tbLog.setText(tbLog.getText() + sb.toString());

            tbLog.setText(tbLog.getText() + "\n===================\n");

            StringBuilder resp = new StringBuilder();
            int respRecieved = 0;
            for (int i0 = 0; i0 < 1200; i0++) {
                Thread.sleep(100);
                if (sin.available() > 0) {
                    int i1 = sin.read(buf);
                    byte[] buf2 = new byte[i1];
                    System.arraycopy(buf, 0, buf2, 0, i1);
                    String s = new String(buf2, utf8);
                    resp.append(s);
                    tbLog.setText(tbLog.getText() + s);
                    respRecieved = 1;
                } else {
                    if (respRecieved != 0)
                        break;
                }
            }
            tbLog.setText(tbLog.getText() + "\n----------------------------------------------" + new Date() + "\n");

            String[] ss = resp.toString().split("\r\n");
            for (String sa : ss) {
                if (sa.startsWith("Set-Cookie: JSESSIONID=")) {
                    String saa = sa.substring(23);
                    String[] ssa = saa.split(";");
                    cookie = ssa[0];
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            socket = new ServerSocket(param.getListeningPort());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        enableStart(false);

        SockListener cont = new SockListener();
        Thread t = new Thread(cont);
        t.start();
    }

    public void stop() {
        this.doStop();
    }

    public void doStop() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        enableStart(true);

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            socket = null;
        }
    }

    public void enableStart(boolean enabled) {
        this.btStart.setEnabled(enabled);
        this.btStop.setEnabled(!enabled);
        this.started = !enabled;
    }

    private String getResponseText() {
        String[] ss = getResponseArray();
        if (responseNum > ss.length - 1)
            responseNum = ss.length - 1;
        String res = ss[responseNum];
        responseNum++;
        return res;
    }

    private String[] getResponseArray() {
        String s = tbResponse.getText();
        String[] ss1 = s.split("\r\n\r\n");
        String[] ss2 = s.split("\n\n");
        if (ss1.length > ss2.length)
            return ss1;
        else
            return ss2;
    }

    private void clearLog() {
        tbLog.setText("");
        responseNum = 0;
        cookie = null;
    }

    private class SockListener implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Socket sk = socket.accept();
                    InputStream sin = sk.getInputStream();
                    byte[] buf = new byte[10000];
                    int i1 = sin.read(buf);
                    byte[] buf2 = new byte[i1];
                    System.arraycopy(buf, 0, buf2, 0, i1);
                    String s = new String(buf2);
                    tbLog.setText(tbLog.getText() + "\n----------------------------------------------\n" + s);

                    for (int i0 = 0; i0 < 10; i0++) {
                        Thread.sleep(100);
                        if (sin.available() > 0) {
                            i1 = sin.read(buf);
                            buf2 = new byte[i1];
                            System.arraycopy(buf, 0, buf2, 0, i1);
                            s = new String(buf2, utf8);
                            tbLog.setText(tbLog.getText() + s);
                        }
                    }

                    String s1 = getResponseText();
                    byte[] bufx = s1.getBytes(utf8);
                    StringBuilder sb = new StringBuilder();
                    sb.append("HTTP/1.1 200 OK\n");
                    sb.append("Content-Length: ");
                    sb.append(bufx.length);
                    sb.append("\n");
                    sb.append("\n");
                    sb.append(s1);

                    OutputStream son = sk.getOutputStream();
                    byte[] buf5 = sb.toString().getBytes(utf8);
                    son.write(buf5);
                    sk.close();

                    tbLog.setText(tbLog.getText() + "\n==========\n" + sb.toString());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (!started)
                    break;
            }

            socket = null;
        }
    }
}

