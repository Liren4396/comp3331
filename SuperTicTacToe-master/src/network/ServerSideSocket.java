package network;

import static java.lang.System.out;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSideSocket implements Runnable
{
  private Socket player1Sock;
  private Socket player2Sock;
  private ObjectOutputStream outObjectP1;
  private ObjectInputStream inObjectP1;
  private ObjectOutputStream outObjectP2;
  private ObjectInputStream inObjectP2;
  private static ServerSocket serverSocket;
  private NetworkExchange dataPlayer1;
  private NetworkExchange dataPlayer2;
  private boolean finished = false; 
  
  public ServerSideSocket(int inPortNum)
  {
    if(serverSocket == null)
    {
      try
      {
        serverSocket = new ServerSocket(inPortNum);
      }
      catch (IOException ioe)
      {
        out.println("ERROR: Caught exception initializing server");
        System.exit(7);
      }
    }
  }
  public void connectToClient(boolean player1)
  {
    try
    {
      out.println("Waiting for client to connect...");
      if(player1)
      {
        player1Sock = serverSocket.accept();
        outObjectP1 = new ObjectOutputStream(player1Sock.getOutputStream());
        inObjectP1 = new ObjectInputStream(player1Sock.getInputStream());
      }
      else
      {
        player2Sock = serverSocket.accept();
        outObjectP2 = new ObjectOutputStream(player2Sock.getOutputStream());
        inObjectP2 = new ObjectInputStream(player2Sock.getInputStream());
      }
      out.println("Client connection accepted");
    }
    catch (IOException ioe)
    {
      out.println("ERROR: Caught exception starting server");
      System.exit(7);
    }
  }
  public boolean sendToClient(
      NetworkExchange gameData, boolean player1) throws IOException
  {
    boolean success = false;
    try
    {
      if(player1)
      {
        outObjectP1.writeObject(gameData);
      }
      else
      {
        outObjectP2.writeObject(gameData);
      }
      success = true;
    }
    catch (IOException e)
    {
      System.out.println("Caught IOException Writing To Socket Stream!");
      player1Sock.close();
      player2Sock.close();
      finished = true;
      throw(new IOException());
    }
    return (success);
  }
  public NetworkExchange recvObject(boolean player1) throws IOException
  {
    NetworkExchange receivedGameData = null;
    try
    {
      if(player1)
      {
        receivedGameData = (NetworkExchange)inObjectP1.readObject();
      }
      else
      {
        receivedGameData = (NetworkExchange)inObjectP2.readObject();
      }
    }
    catch (IOException ioe)
    {
      out.println("ERROR: receiving string from socket");
      player1Sock.close();
      player2Sock.close();
      finished = true;
      throw(new IOException());
    }
    catch( ClassNotFoundException e )
    {
      out.println("ERROR: Class not found in above casting");
      e.printStackTrace();
    }
    return (receivedGameData);
  }
  public boolean isFinished()
  {
    return finished;
  }
  public void run()
  {
    try
    {
      dataPlayer1 = new NetworkExchange();
      dataPlayer1.setPlayer1(true);
      dataPlayer2 = new NetworkExchange();
      dataPlayer2.setPlayer1(false);
      sendToClient(dataPlayer1, true);
      sendToClient(dataPlayer2, false);
      while(true)
      {
        dataPlayer1 = recvObject(true);
        System.out.println("received from p1");
        sendToClient(dataPlayer1, false);
        System.out.println("sent to p2");
        if(dataPlayer1.getGameOver())
        {
          finished = true;
          break;
        }
        dataPlayer2 = recvObject(false);
        System.out.println("received from p2");
        sendToClient(dataPlayer2, true);
        if(dataPlayer2.getGameOver())
        {
          finished = true;
          break;
        }
        System.out.println("sent to p1");
      }
    }
    catch(IOException e)
    {
      System.out.println("Client disconnected");
      finished = true;
    }
    
  }
}

