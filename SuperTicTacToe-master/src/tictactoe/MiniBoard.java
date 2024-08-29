package tictactoe;

import java.awt.*;

import javax.imageio.ImageIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class MiniBoard extends JPanel
{
  // holds all the buttons 
  ArrayList< Square_Button > buttons = new ArrayList< Square_Button >() ;
  
  // Image that will be toggled to X's or O's depending on the player:
  static ImageIcon img;
  static ImageIcon xImage;
  static ImageIcon oImage;
  static ImageIcon xLarge;
  static ImageIcon oLarge;
  
  String xImagePath;
  String oImagePath;
  static ImageIcon blankImage;
  static ImageIcon disabledImage;
  static boolean xWins;
  static BufferedImage bkImage;
  int index; 
  char winner_label = 'n';
  
  // glass pane
  //JPanel glass;
  
  public class Square_Button extends JButton {
	 
	private int index; 
	private int parent; 
	//change to private 
	private char fill = 'n';
	
	  public Square_Button( int _index , int _parent )
	  {
		  super(); 
		  index = _index; 
		  parent = _parent; 
	  }
	  int get_index(){
		  return index; 
	  }
	  int get_parent()
	  {
		  return parent; 
	  }
	  void set_fill( char in )
	  {
		  if( in == 'x' || in == 'o'  || in == 'n' )
			  fill = in; 
	  }
	  char get_fill()
	  {
		  return fill; 
	  }
	  
	  public void resetButton()
	  {
	    this.set_fill('n');
	    this.setEnabled(true);
	    this.setIcon(blankImage); // this also happens in the constructor.... don't need to do it twice?
	    this.setDisabledIcon(disabledImage);

	  }
  }
  
  public void resetMiniBoard(ActionListener button_listener)
  {
      this.removeAll();
      buttons.clear();
      setPreferredSize(new Dimension(210, 210));
     
      // Set a 3 by 3 gridlayout for each Mini board:
      setLayout(new GridLayout( 3, 3, 1, 1 ) );
      
      // Initialize buttons:
      for( int i = 0 ; i < 9 ; ++i )
      {
        buttons.add( new Square_Button( i , index  ) ) ; 
        ButtonInitializer( buttons.get( i ) , button_listener );
        add( buttons.get( i ) );
      }
      winner_label = 'n';
      this.updateUI();
  }
  
  public MiniBoard( ActionListener button_listener , int _index )
  {
    index = _index;   
    
    /**Color black = new Color(0, 0, 0);   
    setBackground(black);*/
    setLayout(new GridLayout(3,3, 2, 2));   
    setPreferredSize(new Dimension(210, 210));
   
    // Initialize Image:
    xImagePath = new String("images/X_Image.jpg");
    img = new ImageIcon();
    xImage = new ImageIcon(getClass().getClassLoader().getResource(xImagePath));
    
    oImage = new ImageIcon(getClass().getClassLoader().getResource("images/O_image.jpg"));
    img = xImage;
    blankImage = new ImageIcon(getClass().getClassLoader().getResource("images/blank.jpg"));
    disabledImage = new ImageIcon(getClass().getClassLoader().getResource("images/disabled.jpg"));
    
    xLarge = new ImageIcon(getClass().getClassLoader().getResource("images/X_large.png") );
    oLarge = new ImageIcon(getClass().getClassLoader().getResource("images/O_large.png") );
    
    // Set a 3 by 3 gridlayout for each Mini board:
    setLayout(new GridLayout( 3, 3, 1, 1 ) );
    
    // Initialize buttons:
    for( int i = 0 ; i < 9 ; ++i )
    {
      buttons.add( new Square_Button( i , index  ) ) ; 
      ButtonInitializer( buttons.get( i ) , button_listener );
      add( buttons.get( i ) );
    }
    
    
    /**this.setVisible(true);
    //Set tic-tac-toe gridlines on board (this currently is not successful)
    
    if(this.getGraphics() == null)
      System.out.println("HAHAH ITS NULL");
    
    Graphics graph = this.getGraphics();
    graph.drawLine(70, 0, 70, 210);
    graph.drawLine(140, 0, 140, 210);
    graph.drawLine(0, 70, 210, 70);
    graph.drawLine(0, 140, 210, 140);*/   
  }
  
  public void dissable_panel()
  {
	  if( !is_active() )
		  return; 
	  
	  for (Component c : this.getComponents()) 
	  {
		  c.setEnabled( false );
	  }
  }
  
  public boolean enable_panel()
  {

	  for (Component c : this.getComponents()) 
	  {
		  if( c instanceof Square_Button )
		  {
			  Square_Button button = ( Square_Button ) c ;
			  if ( button.get_fill() == 'n' )
			  {
				  button.setEnabled( true );
			  }
		  }
	  }
	  return true; 
  }
  
  public void ButtonInitializer( Square_Button b ,  ActionListener button_listener)
  {
    b.setIcon(blankImage);
    b.setDisabledIcon(disabledImage);
    b.addActionListener( button_listener );
    
  }
  
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    g.drawImage(bkImage, 0, 0, getWidth(), getHeight(), this);
  }
  

  
  
  public boolean CheckWinner( int in )
  {	
	  int row = in / 3; 
	  int col = in % 3;
	   
	  //System.out.println( "row: " + row + " col: " + col );
	  
	  char move = buttons.get( in ).get_fill();
	  System.out.println( move ); 
	  if( move == 'n' )
		  return false; 
	  
	  //need to change this a better name 
	  int i ; 
	  
	  //check horizontal 
	  for ( i = 0 ; i < 3 ; ++i)
	  {
		  if( buttons.get( i + row * 3 ).get_fill() != move )
		  {
			  break; 
		  }
	  }
	  
	  if ( i == 3 ) {
		  winner( move ); 
		  return true;
	  }
	  
	  //check vertical 
	  for( i = 0 ; i < 9 ; i += 3 )
	  {
		  if( buttons.get( i + col ).get_fill() != move )
		  {
			  break; 
		  }
	  }
	  
	  if ( i >= 9 )
	  {
		  winner( move ); 
		  return true;
	  }
	  
	  //check diag 0, 4 , 8 
	  if ( row == col )
	  {
		  for( i = 0 ; i < 9 ; i += 4 )
		  {
			  if( buttons.get( i ).get_fill() != move )
			  {
				  break; 
			  }
		  }
	  }
	  
	  if ( i >= 9 ){
		  winner( move ); 
		  return true;
	  }
	  
	  //check diag  2, 4, 6 
	  for ( i = 2 ; i < 8 ; i += 2 )
	  {
		  if( buttons.get( i ).get_fill() != move )
		  {
			  break; 
		  }  
	  }
	  
	  if ( i >= 8 )
	  {
		winner( move );
		return true; 
	  }
	  
	  return false; 
  }
  
  private void winner( char in )
  {	
	  
	  this.removeAll();
	  winner_label = in ; 
	  setLayout( new FlowLayout() );
	  JLabel lable = new JLabel( in == 'x' ? xLarge : oLarge );
	  //lable.setMinimumSize( this.getSize() );
	  add( lable );
	  this.updateUI();
	  //do stuff when there is a winner 
  }
  
  public boolean is_active()
  {
    if(winner_label == 'n')
    {
      for(Square_Button but: buttons)
    	{
    	  if(but.get_fill() == 'n')
    	    return true;
    	}
    	return false;
    }
    else
      return false;
  }
  
  public char get_winner()
  {
     return winner_label; 
  }
  
  public boolean getWinner()
  {
    return xWins;
  }

}

