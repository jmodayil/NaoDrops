package web;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class PluggableGuiWebserver extends Thread {
	// shortened from http://fragments.turtlemeat.com/javawebserver.php

	private int port; // port we are going to listen to
	private WebserverPlugin wp;

	public PluggableGuiWebserver(int listen_port, WebserverPlugin wp) {
		port = listen_port;
		this.start();// this makes a new thread, as mentioned before
		this.wp = wp;
		// System.err.println("Webserver started");
	}

	private void s(String s2) {
		System.err.println("err:" + s2);
	}

	public void run() {
		ServerSocket serversocket = null;
		try {
			serversocket = new ServerSocket(port);
		} catch (Exception e) {
			System.err.println("\nFatal Error:" + e.getMessage());
			return;
		}
		while (true) {
			try {
				Socket connectionsocket = serversocket.accept();
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connectionsocket.getInputStream()));
				DataOutputStream output = new DataOutputStream(
						connectionsocket.getOutputStream());
				http_handler(input, output);
			} catch (Exception e) { // catch any errors, and print them
				System.err.println("\nError:" + e.getMessage());
			}
		}
	}

	private void http_handler(BufferedReader input, DataOutputStream output) {
		int method = 0; // 1 get, 2 head, 0 not supported
		String outstring = null;
		try {
			String tmp = input.readLine(); // read from the stream
			String tmp2 = new String(tmp);
			tmp.toUpperCase();
			if (tmp.startsWith("GET")) { // compare it is it GET
				method = 1;
			} // if we set it to method 1
			if (tmp.startsWith("HEAD")) { // same here is it HEAD
				method = 2;
			} // set method to 2
			if (method == 0) { // not supported
				try {
					output.writeBytes(construct_http_header(501, 0));
					output.close();
					return;
				} catch (Exception e3) { // if some error happened catch it
					s("error:" + e3.getMessage());
				} // and display error
			}
			// tmp contains "GET /index.html HTTP/1.0 ......."
			int start = 0;
			int end = 0;
			for (int a = 0; a < tmp2.length(); a++) {
				if (tmp2.charAt(a) == ' ' && start != 0) {
					end = a;
					break;
				}
				if (tmp2.charAt(a) == ' ' && start == 0) {
					start = a;
				}
			}
			// path = tmp2.substring(start + 2, end); // fill in the path
			outstring = wp.serveOnRequest(tmp2.substring(start + 2, end));

		} catch (Exception e) {
			s("error" + e.getMessage());
		} // catch any exception

		try {
			int type_is = 0;
			output.writeBytes(construct_http_header(200, 5));

			// if it was a HEAD request, we don't print any BODY
			if (method == 1) { // 1 is GET 2 is head and skips the body
				output.writeBytes(outstring);
			}
			output.close();
		}

		catch (Exception e) {
		}
	}

	private String construct_http_header(int return_code, int file_type) {
		String s = "HTTP/1.0 ";
		switch (return_code) {
		case 200:
			s = s + "200 OK";
			break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 403:
			s = s + "403 Forbidden";
			break;
		case 404:
			s = s + "404 Not Found";
			break;
		case 500:
			s = s + "500 Internal Server Error";
			break;
		case 501:
			s = s + "501 Not Implemented";
			break;
		}

		s = s + "\r\n"; // other header fields,
		s = s + "Connection: close\r\n"; // we can't handle persistent
											// connections
		s = s + "Server: SimpleHTTPtutorial v0\r\n"; // server name

		// Construct the right Content-Type for the header.
		// This is so the browser knows what to do with the
		// file, you may know the browser dosen't look on the file
		// extension, it is the servers job to let the browser know
		// what kind of file is being transmitted. You may have experienced
		// if the server is miss configured it may result in
		// pictures displayed as text!
		switch (file_type) {
		// plenty of types for you to fill in
		case 0:
			break;
		case 1:
			s = s + "Content-Type: image/jpeg\r\n";
			break;
		case 2:
			s = s + "Content-Type: image/gif\r\n";
		case 3:
			s = s + "Content-Type: application/x-zip-compressed\r\n";
		default:
			s = s + "Content-Type: text/html\r\n";
			break;
		}

		// //so on and so on......
		s = s + "\r\n"; // this marks the end of the httpheader
		// and the start of the body
		// ok return our newly created header!
		return s;
	}

}
