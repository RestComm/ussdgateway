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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class HttpParametersForm extends JDialog {

    private HttpSimulatorParameters data;
    private JTextField tbListeningPort;
    private JTextField tbCallingHost;
    private JTextField tbCallingPort;
    private JTextField tbUrl;

    public HttpParametersForm(JFrame owner) {
        super(owner, true);

        setTitle("HTTP general parameters");
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 620, 227);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        JLabel lblListeningPort = new JLabel("Listening port");
        lblListeningPort.setBounds(10, 14, 401, 14);
        panel.add(lblListeningPort);

        tbListeningPort = new JTextField();
        tbListeningPort.setColumns(10);
        tbListeningPort.setBounds(424, 11, 180, 20);
        panel.add(tbListeningPort);
        
        JButton btOK = new JButton("OK");
        btOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                doOK();
            }
        });
        btOK.setBounds(327, 165, 136, 23);
        panel.add(btOK);
        
        JButton btCancel = new JButton("Cancel");
        btCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });
        btCancel.setBounds(468, 165, 136, 23);
        panel.add(btCancel);
        
        JLabel lblCallingHost = new JLabel("Calling host");
        lblCallingHost.setBounds(10, 42, 401, 14);
        panel.add(lblCallingHost);
        
        tbCallingHost = new JTextField();
        tbCallingHost.setColumns(10);
        tbCallingHost.setBounds(424, 39, 180, 20);
        panel.add(tbCallingHost);
        
        JLabel lblCallingPort = new JLabel("Calling port");
        lblCallingPort.setBounds(10, 70, 401, 14);
        panel.add(lblCallingPort);
        
        tbCallingPort = new JTextField();
        tbCallingPort.setColumns(10);
        tbCallingPort.setBounds(424, 67, 180, 20);
        panel.add(tbCallingPort);
        
        JLabel lblUrl = new JLabel("URL");
        lblUrl.setBounds(10, 98, 401, 14);
        panel.add(lblUrl);
        
        tbUrl = new JTextField();
        tbUrl.setColumns(10);
        tbUrl.setBounds(424, 95, 180, 20);
        panel.add(tbUrl);
    }

    public void setData(HttpSimulatorParameters data) {
        this.data = data;

        this.tbListeningPort.setText(((Integer) data.getListeningPort()).toString());
        this.tbCallingHost.setText(data.getCallingHost());
        this.tbCallingPort.setText(((Integer) data.getCallingPort()).toString());
        this.tbUrl.setText(data.getUrl());
    }

    public HttpSimulatorParameters getData() {
        return this.data;
    }

    private void doOK() {
        int intVal = 0;
        try {
            intVal = Integer.parseInt(this.tbListeningPort.getText());
            this.data.setListeningPort(intVal);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing ListeningPort value: " + e.toString());
            return;
        }
        this.data.setCallingHost(this.tbCallingHost.getText());
        try {
            intVal = Integer.parseInt(this.tbCallingPort.getText());
            this.data.setCallingPort(intVal);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing CallingPort value: " + e.toString());
            return;
        }
        this.data.setUrl(this.tbUrl.getText());

        this.dispose();
    }

    private void doCancel() {
        this.data = null;
        this.dispose();
    }
}
