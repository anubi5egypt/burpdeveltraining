/*
 * SiteLogger - Log sitemap and findings to database
 *
 * Copyright (c) 2017 Luca Carettoni - Doyensec LLC.
 */
package com.doyensec.sitelogger;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IScanIssue;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class SiteLoggerPanel extends javax.swing.JPanel {

    private final IBurpExtenderCallbacks callbacks;
    private final IExtensionHelpers helpers;

    public SiteLoggerPanel(IBurpExtenderCallbacks callbacks, IExtensionHelpers helpers) {
        initComponents();
        this.callbacks = callbacks;
        this.helpers = helpers;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        website = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        mongohost = new javax.swing.JTextField();
        mongoport = new javax.swing.JTextField();
        logButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        jLabel1.setText("Website:");

        jLabel2.setText("MongoDB Host: ");

        mongohost.setText("127.0.0.1");

        mongoport.setText("27017");

        logButton.setText("Log to Database");
        logButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("MongoDB Port: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mongoport, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(mongohost, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(website)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(logButton, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(818, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(website, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(mongohost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(mongoport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(logButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(115, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void logButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logButtonActionPerformed

        PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
        PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);

        try {
            //Connect to the database and create the collections
            MongoClient mongo = new MongoClient(mongohost.getText(), Integer.parseInt(mongoport.getText()));
            DB db = mongo.getDB("sitelogger");
            URL siteUrl = new URL(website.getText());
            DBCollection tableSite = db.getCollection(siteUrl.getHost().replaceAll("\\.", "_") + "_site");
            DBCollection tableVuln = db.getCollection(siteUrl.getHost().replaceAll("\\.", "_") + "_vuln");

            //Retrieve SiteMap HTTP Requests and Responses and save to the database
            IHttpRequestResponse[] allReqRes = callbacks.getSiteMap(website.getText());
            for (int rc = 0; rc < allReqRes.length; rc++) {
                BasicDBObject document = new BasicDBObject();
                document.put("host", allReqRes[rc].getHost());
                document.put("port", allReqRes[rc].getPort());
                document.put("protocol", allReqRes[rc].getProtocol());
                document.put("URL", allReqRes[rc].getUrl().toString());
                document.put("status_code", allReqRes[rc].getStatusCode());
                if (allReqRes[rc].getRequest() != null) {
                    document.put("request", helpers.base64Encode(allReqRes[rc].getRequest()));
                }
                if (allReqRes[rc].getResponse() != null) {
                    document.put("response", helpers.base64Encode(allReqRes[rc].getResponse()));
                }
                tableSite.insert(document);
            }

            //Retrieve Scan findings and save to the database
            IScanIssue[] allVulns = callbacks.getScanIssues(website.getText());
            for (int vc = 0; vc < allVulns.length; vc++) {
                BasicDBObject document = new BasicDBObject();
                document.put("type", allVulns[vc].getIssueType());
                document.put("name", allVulns[vc].getIssueName());
                document.put("detail", allVulns[vc].getIssueDetail());
                document.put("severity", allVulns[vc].getSeverity());
                document.put("confidence", allVulns[vc].getConfidence());
                document.put("host", allVulns[vc].getHost());
                document.put("port", allVulns[vc].getPort());
                document.put("protocol", allVulns[vc].getProtocol());
                document.put("URL", allVulns[vc].getUrl().toString());
                if (allVulns[vc].getHttpMessages().length > 1) {
                    if (allVulns[vc].getHttpMessages()[0].getRequest() != null) {
                        document.put("request", helpers.base64Encode(allVulns[vc].getHttpMessages()[0].getRequest()));
                    }
                    if (allVulns[vc].getHttpMessages()[0].getResponse() != null) {
                        document.put("response", helpers.base64Encode(allVulns[vc].getHttpMessages()[0].getResponse()));
                    }
                }
                tableVuln.insert(document);
            }

            callbacks.issueAlert("Data Saved!");

        } catch (UnknownHostException ex) {
            
            stderr.println("Mongo DB Connection Error:" + ex.toString());
            
        } catch (MalformedURLException ex) {
            
            stderr.println("Malformed URL:" + ex.toString());
            
        }
    }//GEN-LAST:event_logButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton logButton;
    private javax.swing.JTextField mongohost;
    private javax.swing.JTextField mongoport;
    private javax.swing.JTextField website;
    // End of variables declaration//GEN-END:variables
}
