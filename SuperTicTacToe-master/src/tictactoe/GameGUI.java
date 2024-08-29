package tictactoe;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;

import javax.swing.*;

import tictactoe.MiniBoard.Square_Button;

public class GameGUI extends JFrame
{
	JMenuBar menuBar;
	JMenu file;
	JMenuItem startGame;
	JMenuItem leaveGame;
	JMenuItem pauseGame;
	JMenuItem resetGame;

	MainBoard mainBoard;
	JPanel statsPanel;
	JLabel stats;
	JList<String> moves_list;
	DefaultListModel<String>  moves_model; 
	Game_data data;

	JButton undo, redo;
	boolean closePopup = false;
	
	public GameGUI()
	{
		setVisible(true);
		// Whatever actions we want the user to be able to take:
		startGame = new JMenuItem("Start Game");  
		leaveGame = new JMenuItem("Leave Game");
		pauseGame = new JMenuItem("Pause Game");
		resetGame = new JMenuItem("Reset Game");
		resetGame.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				mainBoard.resetBoard();
				data = new Game_data();
				moves_model.clear();
			}
		});


		file = new JMenu("File");
		file.add(startGame);
		file.add(pauseGame);
		file.add(leaveGame);
		file.add(resetGame);

		menuBar = new JMenuBar();
		menuBar.add(file);

		// This panel could show the stats of the game, 
		//depending on what we want..
		// It is currently set to a blue color just so we can 
		//see where it is in the
		// GUI.
		statsPanel = new JPanel();
		statsPanel.setLayout( new BoxLayout( statsPanel , BoxLayout.PAGE_AXIS ) );
		statsPanel.setPreferredSize(new Dimension(250,600));
		Color panelCol = new Color(0, 128, 255);
		statsPanel.setBackground(panelCol);

		data = new Game_data();

		JPanel top = new JPanel();
		undo = new JButton( "Undo" );
		undo.setEnabled( false );
		undo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if( !data.can_undo() )
					return;

				int num = mainBoard.AImove ? 2 : 1; 

				for( int i = 0 ; i < num ; ++i )
				{
					redo.setEnabled( true );
					String undo_s = data.undo_move(); 

					if( !data.can_undo() )
						undo.setEnabled( false );

					if( moves_model.size() != 0  )
						moves_model.remove( moves_model.size() - 1 );

					MainBoard.img = 'x' ==  undo_s.charAt( 0 ) ? 
					    MainBoard.xImage : MainBoard.oImage ;

					for( MiniBoard b : MainBoard.boards )
					{
						b.dissable_panel();
					}



					MiniBoard board = MainBoard.boards.get( 
					    Integer.parseInt( ""+undo_s.charAt( 1 ) ) );
					Square_Button temp =  board.buttons.get( 
					    Integer.parseInt( ""+undo_s.charAt( 2 ) ) );

					if( !board.is_active() )
					{
						board.resetMiniBoard(  mainBoard.button_listern );
						Iterator<String> it = data.get_moves().iterator();
						while( it.hasNext() )
						{
							String cur_move = it.next();
							if( Integer.parseInt( ""+cur_move.charAt( 1 ) ) ==  board.index )
							{
								Square_Button cur_button = board.buttons.get( 
								    Integer.parseInt( ""+cur_move.charAt( 2 ) ) );
								cur_button.set_fill( cur_move.charAt( 0 ) );
								cur_button.setDisabledIcon('x' ==  cur_move.charAt( 0 ) ? 
								    MainBoard.xImage : MainBoard.oImage );
								cur_button.setEnabled( false );
							}
						}  
					}

					temp.set_fill( 'n' );
					temp.setDisabledIcon( MiniBoard.disabledImage );


					if( data.get_moves().size() == 0  )
					{
						MainBoard.first_move = true; 
						MainBoard.img = MainBoard.xImage; 
						for( MiniBoard b : MainBoard.boards )
							b.enable_panel();
						return; 
					}

					board.enable_panel();
				}
			}		
		});
		top.add( undo );
		redo = new JButton( "Redo" );
		redo.setEnabled( false );
		redo.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{

				if( !data.can_redo() )
					return;

				int num = mainBoard.AImove ? 2 : 1; 

				for( int i = 0 ; i < num ; ++i )
				{
					String redo_s = data.redo();
					undo.setEnabled( true );

					if( !data.can_redo() )
						redo.setEnabled( false );

					MainBoard.img = 'x' ==  redo_s.charAt( 0 ) ? MainBoard.xImage : 
					  MainBoard.oImage ; 
					moves_model.addElement( moves_model.size()+": " + redo_s );

					MiniBoard board = MainBoard.boards.get( 
					    Integer.parseInt( ""+redo_s.charAt( 1 ) ) );
					Square_Button temp =  board.buttons.get( 
					    Integer.parseInt( ""+redo_s.charAt( 2 ) ) );

					temp.set_fill( redo_s.charAt( 0 ) );
					temp.setDisabledIcon(redo_s.charAt( 0 ) == 'x' ? 
					    MiniBoard.xImage : MiniBoard.oImage );
					temp.setEnabled( false );

					if(MainBoard.boards.get( temp.get_index() ).is_active() )
					{
						for( MiniBoard m : MainBoard.boards )
						{
							m.dissable_panel();
						}
					}
					else
					{
						for( MiniBoard m : MainBoard.boards )
						{
							m.enable_panel();
						}
					}
					MainBoard.boards.get( temp.get_index() ).enable_panel();

					if( board.CheckWinner( Integer.parseInt( ""+redo_s.charAt( 2 ) ) )
							&&  mainBoard.check_winner( Integer.parseInt(""+redo_s.charAt( 1 ) ) ) )
					{
						mainBoard.winner( redo_s.charAt( 0 ) );
					}
				}
			}
		});
		top.add( redo );

		statsPanel.add( top ); 

		stats = new JLabel() ;
		statsPanel.add( stats ); 
		//add a jlist with a scroable pannel 
		// give member variable of the itator 
		// 
		moves_model = new DefaultListModel<String>();
		moves_list = new JList<String>( moves_model ); 
		JScrollPane move_p = new JScrollPane( moves_list ); 
		//stats.add( moves_list );
		statsPanel.add( move_p );

		mainBoard = new MainBoard( stats, this );

		setLayout(new FlowLayout());
		setJMenuBar(menuBar);
		add(statsPanel);
		add(mainBoard);
		setStartScreen();
		pack();
	}

	public void setGlass(String glass_type)
	{

		final JPanel glass = new JPanel();
		JPanel inner = new JPanel();
		JPanel westPanel = new JPanel();
		westPanel.setOpaque(false);
		westPanel.setPreferredSize(new Dimension(250,650));
		JLabel winnerPic = new JLabel();
		assert(glass_type.equals("winner") || glass_type.equals("stalemate"));
		ImageIcon winPic = new ImageIcon(
		    getClass().getClassLoader().getResource(
		        "images/" + glass_type + ".jpg"));
		winnerPic.setIcon(winPic);
		inner.add(winnerPic);
		JPanel topBar = new JPanel();
		topBar.setPreferredSize(new Dimension(940,20));
		topBar.setOpaque(false);
		JButton dismiss = new JButton("Dismiss " + glass_type+ " Message");
		dismiss.setPreferredSize(new Dimension(200,70));
		westPanel.add(dismiss);
		dismiss.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				glass.setVisible(false);
			}
		});
		inner.setPreferredSize(new Dimension(660,660));
		glass.setLayout(new FlowLayout());
		glass.add(topBar);
		glass.add(westPanel);
		glass.add(inner);
		glass.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{}
		});

		setGlassPane(glass);
		glass.setVisible(true);
		glass.setOpaque(false);
	}

	public void setStartScreen()
	{
		final JPanel glass = new JPanel();
		glass.setLayout(new FlowLayout());
		glass.setBackground(new Color(226,237,135));
		JLabel pic = new JLabel();
		ImageIcon startIcon = new 
		    ImageIcon(getClass().getClassLoader().getResource("images/front.jpg"));
		pic.setIcon(startIcon);
		JLabel gameType = new JLabel("Which type of game would you like to play?");
		gameType.setFont(new Font("SansSerif", Font.BOLD, 36));
		glass.setPreferredSize(new Dimension(800,800));
		JButton local = new JButton("Local 2 Player");
		//pat modified action listener
		local.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				glass.setVisible(false);
			}
		});
		JButton comp = new JButton("Play the Computer");
		comp.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			  
				AIDialog AIPopup = new AIDialog(GameGUI.this);
				
				
	      WindowListener exitListener = new WindowAdapter()
	      {

	        @Override
	        public void windowClosing(WindowEvent e)
	        {
	          closePopup = true;
	        }
	      };
	      AIPopup.addWindowListener(exitListener);
	      AIPopup.setVisible(true);
				
				if(!closePopup)
				{
				  glass.setVisible(false);
				}
				//else 
			}
		});
		JButton network = new JButton("Play Online");
    // disabled because the server was taken down
    network.setEnabled(false);
		network.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				glass.setVisible(false);
				JOptionPane.showMessageDialog(
				    GameGUI.this, "Connecting to Opponent", 
				    "Connecting", JOptionPane.INFORMATION_MESSAGE);
				mainBoard.startOnlinePlay();
			}
		});
		glass.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{}
		});
		glass.add(gameType);
		glass.add(local);
		glass.add(comp);
		glass.add(network);
		glass.add(pic);
		setGlassPane(glass);
		glass.setVisible(true);
	}


	private class AIDialog extends JDialog
	{
		public AIDialog(JFrame mainframe)
		{
			//parent class ctor
			super(mainframe, "Select Computer Difficulty", true);
			//main layout same for each function
			this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


			//setup
			this.setMinimumSize(new Dimension(375, 100));
			this.setResizable(false);
			this.setLocation(300, 200);
		
			
			
			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

			topPanel.add(new JLabel("Please select Computer difficulty."));


			JButton randButton = new JButton("Easy");
			randButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainBoard.turnAIOn("EASY");
					setVisible(false);
					closePopup = false;
				}
			});

			JButton easyButton = new JButton("Medium");
			easyButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainBoard.turnAIOn("MEDIUM");
					setVisible(false);
					closePopup = false;
				}
			});

			JButton medButton = new JButton("Hard");
			medButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainBoard.turnAIOn("HARD");
					setVisible(false);
					closePopup = false;
				}
			});

			//cancel button implementation
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
				  
					setVisible(false);
					closePopup = true;
				}
			});
			//add items
			botPanel.add(randButton);
			botPanel.add(easyButton);
			botPanel.add(medButton);
			botPanel.add(cancelButton);

			//complete window
			this.add(topPanel);
			this.add(botPanel);
			return;
		}
	}
}
