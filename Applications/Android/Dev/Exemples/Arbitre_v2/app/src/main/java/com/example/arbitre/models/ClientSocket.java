package com.example.arbitre.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

// Singleton
public class ClientSocket{
	
	private static ClientSocket cs;
	private Socket socket;
	
	private ClientSocket()
	{
		socket = new Socket();
	}
	
	public static ClientSocket GetInstance(){
		if(cs == null)
			cs = new ClientSocket();
		
		return cs;
	}	
	
	public String Execute(String message)
	{
		String res = null;
        NetworkTask nt = new NetworkTask();
        nt.setNsocket(socket);
		nt.execute(message);
		
		while(res == null)
		{
			res = nt.getMessage();
		}

		return res;
	}

	public void Close(){
		try {
			socket.close();
            cs = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ACCESSORS
	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
      