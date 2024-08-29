package tictactoe;

import static java.lang.Math.*;

import java.util.Random;

import javax.swing.JDialog;

public class ArtificialIntelligence
{
  private MainBoard mainBoard;
	private String difficulty;	//one of "EASY, MEDIUM, or HARD"
	private int thinkingDelay;	//"thinking" speed in milliseconds
	
	private char side = 'o'; //always
	private char opside = 'x'; //first player (human)
	
	public ArtificialIntelligence(MainBoard mainBoard_In, String difficulty_In)
	{
	  mainBoard = mainBoard_In;
	  difficulty = difficulty_In;
	  
	  if(difficulty_In == "EASY")
    {
      thinkingDelay = 500;
    }
	  else if(difficulty_In == "MEDIUM")
    {
      thinkingDelay = 1500;
    }
	  else if(difficulty_In == "HARD")
    {
      thinkingDelay = 2000;
    }
	  else thinkingDelay = 500;
	}

	public void makeMove(int miniIndex, GameGUI parent)//MiniBoard mini)
	{
	  //wait for a bit before making move
    try
    {
      JDialog think = new JDialog(parent, "     Thinking...");
      think.setSize(200, 0);
      think.setResizable(false);
      think.setVisible(true);
      think.setLocationRelativeTo(null);
      Thread.sleep(thinkingDelay);    //thinking...
      think.dispose();
    }
    catch(InterruptedException iE)
    {
     //do nothing
    }

    int move = 0; //default
    
    if( !(mainBoard.boards.get( miniIndex ).is_active())  )
    {
      Random randBoard = new Random();
      do
      {
        miniIndex = abs( (randBoard.nextInt() % 9) );
      }
      while( !(mainBoard.boards.get(miniIndex).is_active()) );
    }
    
    Random randMove = new Random();
    do
    {
      if(difficulty == "MEDIUM")
      {
        move = findWinSquare(miniIndex);
        if(move >= 0){}
        else move = abs( (randMove.nextInt() % 9) );
      }
      
      else if(difficulty == "HARD")
      {
        move = findWinSquare(miniIndex);
        if(move == -1)
        {
          move = playDefense(miniIndex);
        }
        if(move >= 0){}
        else move = abs( (randMove.nextInt() % 9) );
      }
      else  move = abs( (randMove.nextInt() % 9) );
    }
    while(mainBoard.boards.get( miniIndex ).buttons.get( move ).get_fill() != 
        'n');
        
    mainBoard.boards.get( miniIndex ).buttons.get( move ).doClick(); 
	  
	  return;
	}
	
	private int findWinSquare(int miniIndex)
	{
	  int square = -1;
	  //top row
	  if((mainBoard.boards.get( miniIndex ).buttons.get(0).get_fill() == side)
	      && (mainBoard.boards.get( 
	          miniIndex ).buttons.get(1).get_fill() == side)
	      && (mainBoard.boards.get( 
	          miniIndex ).buttons.get(2).get_fill() == 'n'))
	  {
	    square = 2;
	  }
	  else if((mainBoard.boards.get( 
	      miniIndex ).buttons.get(0).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(1).get_fill() == 'n'))
    {
      square = 1;
    }
	  else if((mainBoard.boards.get( 
	      miniIndex ).buttons.get(1).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
	  
	  //mid row
	  else if((mainBoard.boards.get( 
	      miniIndex ).buttons.get(3).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == 'n'))
    {
      square = 5;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(3).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == 'n'))
    {
      square = 3;
    }
	  
	  //bot row
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == 'n'))
    {
      square = 7;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(7).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
	  
	  //left col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == 'n'))
    {
      square = 3;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(3).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
	  
	  //mid col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(1).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == 'n'))
    {
      square = 7;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(1).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(1).get_fill() == 'n'))
    {
      square = 1;
    }
	  
	  //right col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == 'n'))
    {
      square = 5;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(5).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == 'n'))
    {
      square = 2;
    }
	  
	  //0,4,8 diag
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
	  
	  //2,4,6 diag
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == side)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == 'n'))
    {
      square = 2;
    }
	  
	  return square;
	}
	
	private int playDefense(int miniIndex)
	{
    int square = -1;
    //top row
    if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(1).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == 'n'))
    {
      square = 2;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(1).get_fill() == 'n'))
    {
      square = 1;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(1).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
    
    //mid row
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(3).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == 'n'))
    {
      square = 5;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(3).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == 'n'))
    {
      square = 3;
    }
    
    //bot row
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == 'n'))
    {
      square = 7;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(7).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
    
    //left col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(3).get_fill() == 'n'))
    {
      square = 3;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(3).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
    
    //mid col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(1).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == 'n'))
    {
      square = 7;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(1).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(7).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(1).get_fill() == 'n'))
    {
      square = 1;
    }
    
    //right col
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(5).get_fill() == 'n'))
    {
      square = 5;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(5).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == 'n'))
    {
      square = 2;
    }
    
    //0,4,8 diag
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == 'n'))
    {
      square = 8;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(0).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(8).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(0).get_fill() == 'n'))
    {
      square = 0;
    }
    
    //2,4,6 diag
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == 'n'))
    {
      square = 6;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(2).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(4).get_fill() == 'n'))
    {
      square = 4;
    }
    else if((mainBoard.boards.get( 
        miniIndex ).buttons.get(4).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(6).get_fill() == opside)
        && (mainBoard.boards.get( 
            miniIndex ).buttons.get(2).get_fill() == 'n'))
    {
      square = 2;
    }
    
    return square;
  }
}
