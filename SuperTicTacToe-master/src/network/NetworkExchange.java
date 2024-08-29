package network;

import java.io.Serializable;

public class NetworkExchange implements Serializable
{
  private boolean gameOver = false;
  private boolean player1;
  private int buttonIndex;
  private int parentIndex;
  public void setPlayer1(boolean player1_in)
  {
    player1 = player1_in;
  }
  public void setButtonIndex(int index)
  {
    buttonIndex = index;
  }
  public void setParentIndex(int index)
  {
    parentIndex = index;
  }
  public void setGameOver(boolean gameOver_in)
  {
    gameOver = gameOver_in;
  }
  public boolean isPlayer1()
  {
    return player1;
  }
  public boolean getGameOver()
  {
    return gameOver;
  }
  public int getButtonIndex()
  {
    return buttonIndex;
  }
  public int getParentIndex()
  {
    return parentIndex;
  }
}
