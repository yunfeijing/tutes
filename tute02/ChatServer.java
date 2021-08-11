/*
 * Copyright (c)  2021, kvoli
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

package skeletons;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
  public static final int port = 6379;
  private boolean alive;
  private List<ChatConnection> connectionList = new ArrayList<>();

  public static void main(String[] args) {
    new ChatServer().handle();
  }

  private void enter(ChatConnection connection) {
    broadCast(String.format("%d has just joined the chat", connection.socket.getPort()), null);
    connectionList.add(connection);
  }

  private void leave(ChatConnection connection) {
    broadCast(String.format("%d has just left the chat", connection.socket.getPort()), connection);
    connectionList.remove(connection);
  }

  private synchronized void broadCast(String message, ChatConnection ignored) {
    for (ChatConnection c : connectionList) {
      if (ignored == null || !ignored.equals(c))
        c.sendMessage(message);
    }
  }

  public void handle() {
    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(port);
      System.out.printf("Listening on port %d\n", port);
      alive = true;

      while (alive) {
        Socket newSocket = serverSocket.accept();
        ChatConnection conn = new ChatConnection(newSocket);
        conn.start();
        join(conn);
      }

    } catch (IOException e) {
      System.out.println("Exception occured creating ServerSocket: ", e.getMessage());
      alive = false;
      e.printStackTrace();
    }
  }

  class ChatConnection extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connection_alive = false;

    public ChatConnection(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
      connection_alive = true;
      while (connection_alive) {
        try {
          String in = reader.readLine();
          // broadcast
          if (in != null) {
            broadCast(String.format("%d: %s\n", socket.getPort(), in), this);
          } else {
            connection_alive = false;
          }
        } catch (IOException e) {
          connection_alive = false;
          e.printStackTrace();
        }
      }
      close();
    }

    public void close() {
      try {
        leave(this);
        reader.close();
        writer.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void sendMessage(String message) {
      writer.print(message);
      writer.flush();
    }
  }
}