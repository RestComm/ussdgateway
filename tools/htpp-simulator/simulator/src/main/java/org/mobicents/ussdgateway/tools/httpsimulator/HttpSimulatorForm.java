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
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class HttpSimulatorForm {

    private ServerTesterForm testingForm;
    protected JFrame frmHtppSimulator;
    private static HttpSimulatorParameters initPar = null;
    private HttpSimulatorParameters par;

    private JButton btnConfigure;
    private JButton btnRun;

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    HttpSimulatorForm window = new HttpSimulatorForm();
                    window.frmHtppSimulator.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void setupLog4j() {
        BasicConfigurator.configure();
    }

    /**
     * Create the application.
     */
    public HttpSimulatorForm() {
        initialize();

        setupLog4j();

        // trying to read the ini-file
        HttpSimulatorParameters par = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("HtppSimulatorParameters.xml"));
            XMLDecoder d = new XMLDecoder(bis);
            initPar = (HttpSimulatorParameters) d.readObject();
            d.close();
        } catch (Exception e) {
            // we ignore exceptions
        }
        
        if (par == null) {
            this.par = new HttpSimulatorParameters();
//      } else {
//          this.par = par;
        }
    }
    private void initialize() {
        frmHtppSimulator = new JFrame();
        frmHtppSimulator.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (frmHtppSimulator.getDefaultCloseOperation() == JDialog.DO_NOTHING_ON_CLOSE) {
                    JOptionPane.showMessageDialog(getJFrame(), "Before exiting you must close a test window form");
                } else {
//                  if (hostImpl != null) {
//                      hostImpl.quit();
//                  }
                }
            }
        });
        frmHtppSimulator.setResizable(false);
        frmHtppSimulator.setTitle("HTPP Simulator");
        frmHtppSimulator.setBounds(100, 100, 510, 299);
        frmHtppSimulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        frmHtppSimulator.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        btnConfigure = new JButton("Configure");
        btnConfigure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                HttpParametersForm frame = new HttpParametersForm(getJFrame());
                frame.setData(par);
                frame.setVisible(true);

                HttpSimulatorParameters newPar = frame.getData();
                if (newPar != null) {
                    par = newPar;

                    try {
                        BufferedOutputStream bis = new BufferedOutputStream(new FileOutputStream("HtppSimulatorParameters.xml"));
                        XMLEncoder d = new XMLEncoder(bis);
                        d.writeObject(newPar);
                        d.close();
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed when saving the parameter file HtppSimulatorParameters.xml: " + ee.getMessage());
                    }
                }
            }
        });
        btnConfigure.setBounds(10, 25, 183, 23);
        panel.add(btnConfigure);
        
        btnRun = new JButton("Run test");
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                runTest();
            }
        });
        btnRun.setBounds(10, 59, 183, 23);
        panel.add(btnRun);
    }

    public JFrame getJFrame() {
        return this.frmHtppSimulator;
    }

    private void enableButtons(boolean enable) {
        this.btnConfigure.setEnabled(enable);
        this.btnRun.setEnabled(enable);
    }

    private void runTest() {
        ServerTesterForm dlg = new ServerTesterForm(getJFrame());
        this.enableButtons(false);
        frmHtppSimulator.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        testingForm = dlg;
        dlg.setData(this, this.par);
        dlg.setVisible(true);
    }

    public void testingFormClose() {
        testingForm = null;
        this.enableButtons(true);
        frmHtppSimulator.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    }

}
