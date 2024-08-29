package tictactoe;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;


public class Game_data 
{
	private ArrayDeque< String > moves;
	private ArrayDeque< String > undo_moves; 
	
	public Game_data()
	{
		moves = new ArrayDeque< String >();
		undo_moves = new ArrayDeque< String >();	
	}
	
	public void add( String move ) throws InvalidInputException
	{
		if( move.length() != 3 ||( move.charAt( 0 ) != 'x' && move.charAt( 0 ) != 'o' ) 
				|| !Character.isDigit(move.charAt(1)) || !Character.isDigit(move.charAt(2))  )
		{
			throw new InvalidInputException( "Invalid move given " + move );
		}
		undo_moves = new ArrayDeque< String >();	
		moves.addLast( move );
	}
	
	public String undo_move()
	{
		String last_move = moves.pollLast();
		if( last_move == null )
			return last_move; 
		undo_moves.addFirst( last_move );
		return last_move;
	}
	
	public ArrayDeque<String> get_moves()
	{
		return moves;
	}
	
	public ArrayDeque<String> get_undo_moves()
	{
		return undo_moves;
	}
	
	public String redo()
	{	
		System.out.println( "redo undo moves are :");
		for( String s : undo_moves )
			System.out.println( s ); 
		System.out.println();
		try 
		{
			String move = undo_moves.removeFirst();
			if( move == null )
			{
				return null; 
			}
			System.out.println( "undo move is " + move );
			moves.addLast( move );
			return move; 
		}
		catch(	NoSuchElementException e)
		{
			System.out.println( "in catch ");
			e.printStackTrace( System.out );
		}
		return null; 
	}
	
	public Boolean can_redo()
	{
		return !undo_moves.isEmpty();
	}
	
	public Boolean can_undo()
	{
		return !moves.isEmpty(); 
	}
	
}
