package network;

import static java.lang.System.out;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSideSocket
{
  private String ipAddress;
  private int portNum;
  private Socket socket;
  private ObjectOutputStream outObject;
  private ObjectInputStream inObject;
  public ClientSideSocket(String inIPAddress, int inPortNum)
  {
    ipAddress = inIPAddress;
    portNum = inPortNum;
  }
  public void connectToServer()
  {
    try
    {
      out.println("Connecting to server...");
      socket = new Socket(ipAddress, portNum);
      outObject = new ObjectOutputStream(socket.getOutputStream());
      inObject = new ObjectInputStream(socket.getInputStream());
      out.println("Connection made!");
    }
    catch (IOException ioe)
    {
      out.println("ERROR: Caught exception starting client");
      System.exit(7);
    }
  }
  public boolean sendToServer(NetworkExchange gameData) throws IOException
  {
    boolean success = false;
    try
    {
      outObject.writeObject(gameData);
  //    outData.writeByte(0); //send 0 to signal the end of the string
      success = true;
    }
    catch (IOException e)
    {
      System.out.println("Caught IOException Writing To Socket Stream!");
      throw(new IOException());
    }
    return (success);
  }
  public NetworkExchange recvObject() throws IOException
  {
    NetworkExchange receivedGameData = null;
    try
    {
      receivedGameData = (NetworkExchange)inObject.readObject();
    }
    catch (IOException ioe)
    {
      out.println("ERROR: receiving string from socket");
      throw(new IOException());
    }
    catch( ClassNotFoundException e )
    {
      out.println("ERROR: Class not found in above casting");
      e.printStackTrace();
    }
    return (receivedGameData);
  }
}
