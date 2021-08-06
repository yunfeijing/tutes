/* Copyright (c)  2021, kvoli
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/*
  you can "echo" messages with the ridicule tO gEt tHe BeSt EfFecT
*/
public class EchoServer {
  public static final int port = 6379;

  public static void main(String[] args) {
    System.out.println("Main function...");
    new EchoServer().handle();
  }

  public static String ridicule(String in) {
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (char c : in.toCharArray()) {
      sb.append(random.nextBoolean() ? c : Character.toUpperCase(c));
    }
    return sb.toString();
  }

  public void handle() {
    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(port);
      System.out.println("Started server on port " + port);

      while (true) {

        // a "blocking" call which waits until a connection is requested
        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted connection from client");

        EchoConnection echoConnection = new EchoConnection(clientSocket);
        System.out.println("Made echo connection socket!");
        echoConnection.run();

      }
    } catch (IOException e) {
      // handle the exception
      e.printStackTrace();
    }
  }

  class EchoConnection {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connection_alive;

    public EchoConnection(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new PrintWriter(socket.getOutputStream());
    }

    public void close() {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void run() {
      connection_alive = true;
      while (connection_alive) {
        try {
          String s = reader.readLine();
          if (s == null) {
            connection_alive = false;
          } else {
            writer.println(ridicule(s));
            writer.flush();
          }
        } catch (IOException e) {
          connection_alive = false;
          e.printStackTrace();
        }
      }
      close();
    }
  }
}
