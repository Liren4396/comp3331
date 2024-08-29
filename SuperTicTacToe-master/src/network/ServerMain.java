package network;

import java.io.IOException;
import java.util.ArrayList;

public class ServerMain
{
  private static int portNum = 45000;
  private static int maxGameLimit = 3;
  public static void main(String[] args)
  {
    ArrayList<ServerSideSocket> serverSockArr = 
        new ArrayList<ServerSideSocket>();
    while(true)
    {
      if(serverSockArr.size() < maxGameLimit)
      {
        ServerSideSocket serverSocket = new ServerSideSocket(portNum);
        serverSocket.connectToClient(true);
        serverSocket.connectToClient(false);
        Thread thread = new Thread(serverSocket);
        thread.start();
        serverSockArr.add(serverSocket);
      }
      for(int x = 0; x < serverSockArr.size(); x++)
      {
        if(serverSockArr.get(x).isFinished())
        {
          serverSockArr.remove(x);
        }
      }
    }
    
  }
}
