package rltoys.experiments.scheduling.network.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SyncSocket {
  private final Socket socket;
  private final InputStream in;
  private final OutputStream out;

  public SyncSocket(Socket socket) {
    this.socket = socket;
    in = inputStream();
    out = outputStream();
  }

  private InputStream inputStream() {
    InputStream in = null;
    try {
      in = socket.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      close();
    }
    return in;
  }

  private OutputStream outputStream() {
    OutputStream out = null;
    try {
      out = socket.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
      close();
    }
    return out;
  }

  private MessageBinary transaction(Message message) {
    MessageBinary messageBinary;
    synchronized (out) {
      synchronized (in) {
        write(message);
        messageBinary = read();
      }
    }
    return messageBinary;
  }

  public MessageBinary read() {
    MessageBinary messageBinary = new MessageBinary();
    if (isClosed())
      return messageBinary;
    synchronized (in) {
      try {
        messageBinary.read(in);
      } catch (IOException e) {
        Messages.displayError(e);
        close();
      }
    }
    Messages.debug(this.toString() + " reads " + messageBinary.type().toString());
    return messageBinary;
  }

  synchronized public void write(Message message) {
    Messages.debug(this.toString() + " writes " + message.type().toString());
    if (isClosed())
      return;
    synchronized (out) {
      try {
        message.write(out);
      } catch (IOException e) {
        Messages.displayError(e);
        close();
      }
    }
  }

  public MessageClassData classTransaction(String className) {
    Messages.println("Downloading code for " + className);
    MessageBinary message = transaction(new MessageRequestClass(className));
    MessageClassData messageClassData = (MessageClassData) Messages.cast(message, null);
    return messageClassData;
  }

  public MessageJob jobTransaction(ClassLoader classLoader, boolean blocking) {
    MessageBinary message = transaction(new MessageRequestJob(blocking));
    MessageJob messageJobTodo = (MessageJob) Messages.cast(message, classLoader);
    return messageJobTodo;
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
      Messages.displayError(e);
    }
  }

  public boolean isClosed() {
    return socket.isClosed();
  }
}
