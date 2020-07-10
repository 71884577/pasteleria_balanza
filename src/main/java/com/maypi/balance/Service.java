/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maypi.balance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author rcordova
 */
public class Service extends Thread {
    
    String url;
    Map<String,Object> params;
    String token;
    String method;
    JFrame frame;
    
    public Service(JFrame frame, String url, Map<String, Object> params, String token, String method) {
        this.url = url;
        this.params = params;
        this.token = token;
        this.method = method;
        this.frame = frame;
        
    }

    Service() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run(){ 

        try {
            
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Reader in;
            StringBuilder sb;
            
            switch(method){
                
                case "GET": 
                    
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Authorization", "bearer " + token);
                    int responseCode = conn.getResponseCode();

                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

                    sb = new StringBuilder();

                    for(int c; (c = in.read()) >= 0;){
                        sb.append((char)c);
                    }

                    this.response(sb.toString(),this.url, this.method);
                    
                    break;
                case "POST":
                    
                    StringBuilder postData = new StringBuilder();
                    for(Map.Entry<String,Object> param: params.entrySet()){
                        if(postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append("=");
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    conn.setRequestProperty("Authorization", "bearer " + token);
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postDataBytes);

                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

                    sb = new StringBuilder();

                    for(int c; (c = in.read()) >= 0;){
                        sb.append((char)c);
                    }

                    this.response(sb.toString(),this.url, this.method);

                    break;
            }
        } catch (Exception e) {
            Logger.getLogger(Reading.class.getName()).log(Level.SEVERE, null, e);
        }
  
    }
    
    private void response(String data, String url, String method){
        
        switch(frame.getClass().getName()){
            
            case "com.maypi.balance.JFrameLogin": 
                ((JFrameLogin)frame).response(data);
                break;
                
            case "com.maypi.balance.JFrameBalance": 
                ((JFrameBalance)frame).responseService(data, url, method);
                break;
                
        }
    }
     
}
