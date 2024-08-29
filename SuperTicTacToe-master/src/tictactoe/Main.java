package tictactoe;

import java.awt.Dimension;

import javax.swing.WindowConstants;
import tictactoe.MiniBoard;
import tictactoe.MainBoard;

public class Main
{
  public static GameGUI main;
  
  public static void main(String[] args)
  {
    main = new GameGUI();
    main.setResizable(false);
    main.pack();
    main.setVisible(true);
    main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }
}

