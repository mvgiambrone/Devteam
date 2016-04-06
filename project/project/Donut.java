package project;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.sound.sampled.*;

public class Donut {
	JFrame window;

	int SCREEN_WIDTH, SCREEN_HEIGHT;
	int dimensions, offset;
	Choice mode_choice;
	JLabel score, donut_pic, game_over, title, difficult;
	AudioInputStream[] background_streams;
	JLabel[] pieces;
	Vector<Integer> moves;
	int current_move, current_display;
	boolean playing;
	BufferedImage donut;

	Color background_color;
	Clip background_music;
	int oldItem;
	Color[][] tile_colors;
	Clip effects;
	AudioInputStream good, bad, neutral;
	Clip timing;

	public static void main(String[] args)
	{
		new Donut();
	}

	public Donut()
	{
		// SETUP WINDOW
		window = new JFrame();
		SCREEN_WIDTH = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
		SCREEN_HEIGHT = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
		dimensions = SCREEN_HEIGHT;
		offset = (SCREEN_WIDTH - dimensions)/2;
		background_color = new Color(112, 183, 253);
		window.setBounds(offset, 0, dimensions, dimensions);
		window.getContentPane().setBackground(background_color);
		window.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (!playing)
					return;
				if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A')
				{
					processMove(0);
				}
				else if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W')
				{
					processMove(1);
				}
				else if (e.getKeyChar() == 'd' || e.getKeyChar() == 'd')
				{
					processMove(2);
				}
				else if (e.getKeyChar() == 's' || e.getKeyChar() == 's')
				{
					processMove(3);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				for (int i = 0; i < 4; i++)
					pieces[i].setBackground(tile_colors[i][0]);
			}});

		// TITLE
		title = new JLabel("Donut Simon Says", JLabel.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 64));
		title.setBounds(0,0,dimensions,150);
		title.setVisible(true);
		window.add(title);

		// DONUT
		try {
			donut = ImageIO.read(new File("donut.png"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// GAMEOVER
		game_over = new JLabel("Game Over", JLabel.CENTER);
		game_over.setFont(new Font("Helvetica", Font.BOLD, 64));
		game_over.setBackground(Color.RED);
		game_over.setBounds(0,dimensions/2-150, dimensions, 300);
		game_over.setOpaque(true);
		game_over.setVisible(false);
		window.add(game_over);

		// SETUP MOVES
		moves = new Vector<Integer>();

		// SOUND SETUP
		final String[] background_files = {"fantasy_town.wav", "respectfully_resigned.wav", "adventure_awaits.wav", "fantasy_boss.wav"};
		background_streams = new AudioInputStream[4];
		for (int i = 0; i < 4; i++)
		{
			setBackgroundMusic(i, background_files[i]);
		}
		try {
			background_music = AudioSystem.getClip();
			background_music.open(background_streams[0]);
			background_music.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
		try{
			effects = AudioSystem.getClip();
			timing = AudioSystem.getClip();
			timing.addLineListener(new LineListener() {

				@Override
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP)
					{
						for (int i = 0; i < 4; i++)
							pieces[i].setBackground(tile_colors[i][0]);
						current_display++;
						if (current_display < moves.size())
						{
							play(current_display);
						}

					}
				}});
			URL urlG = new File("cheer.wav").toURI().toURL();
			good = AudioSystem.getAudioInputStream(urlG);
			URL urlB = new File("damaged.wav").toURI().toURL();
			bad = AudioSystem.getAudioInputStream(urlB);
			URL urlN = new File("attack.wav").toURI().toURL();
			neutral = AudioSystem.getAudioInputStream(urlN);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		// DIFFICULTY LEVEL SETUP
		mode_choice = new Choice();
		mode_choice.add("Easy");
		mode_choice.add("Medium");
		mode_choice.add("Hard");
		mode_choice.add("Extra Hard");
		mode_choice.setFocusable(false);
		mode_choice.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int i = mode_choice.getSelectedIndex();
				if (i != oldItem)
				{
					if (background_music.isOpen())
					{
						background_music.close();
						setBackgroundMusic(oldItem, background_files[oldItem]);
					}
					try {
						background_music.open(background_streams[i]);
					} catch (LineUnavailableException | IOException e1) {
						e1.printStackTrace();
					}
					background_music.loop(Clip.LOOP_CONTINUOUSLY);
					oldItem = i;
					current_display = 0;
					current_move = 0;
					play(0);
				}
			}
		});
		mode_choice.setBounds(200, dimensions-150, 200, 100);
		window.add(mode_choice);

		difficult = new JLabel("Choose a Difficulty Level:", JLabel.CENTER);
		difficult.setFont(new Font("Helvetica", Font.PLAIN, 16));
		difficult.setBounds(0,dimensions-150, 200, 100);
		window.add(difficult);
		score = new JLabel("Score: 0", JLabel.CENTER);
		score.setFont(new Font("Helvetica", Font.PLAIN, 24));
		score.setBounds(dimensions-150,dimensions-150,100,100);
		window.add(score);

		// TILES SETUP
		tile_colors = new Color[4][2];
		tile_colors[0][0] = new Color(255, 45, 33);
		tile_colors[1][0] = new Color(110, 192, 56);
		tile_colors[2][0] = new Color(73, 155, 201);
		tile_colors[3][0] = new Color(241, 209, 48);
		tile_colors[0][1] = new Color(255, 95, 94);
		tile_colors[1][1] = new Color(156, 225, 89);
		tile_colors[2][1] = new Color(100, 179, 223);
		tile_colors[3][1] = new Color(255, 224, 97);

		pieces = new JLabel[4];
		pieces[0] = new JLabel("A", JLabel.CENTER);
		pieces[1] = new JLabel("W", JLabel.CENTER);
		pieces[2] = new JLabel("D", JLabel.CENTER);
		pieces[3] = new JLabel("S", JLabel.CENTER);
		int size = (dimensions-125)/4;
		pieces[0].setBounds(150, dimensions/2 - size/2, size, size);
		pieces[1].setBounds(dimensions/2 - size/2, 150, size, size);
		pieces[2].setBounds(dimensions - size - 150, dimensions/2 - size/2, size, size);
		pieces[3].setBounds(dimensions/2 - size/2, dimensions - size - 150, size, size);
		for (int i = 0; i < 4; i++)
		{
			pieces[i].setBackground(tile_colors[i][0]);
			pieces[i].setFont(new Font("Helvetica", Font.BOLD, 64));
			pieces[i].setVisible(true);
			pieces[i].setOpaque(true);
			window.add(pieces[i]);
		}
		// FINISH SETUP
		JLabel dummy = new JLabel(new ImageIcon(donut.getScaledInstance(dimensions - 300, dimensions - 300, 0)));
		dummy.setVisible(true);
		window.add(dummy);
		window.setVisible(true);
		for (int i = 0; i < 4; i++)
			pieces[i].setBackground(tile_colors[i][0]);
		// PLAY
		playing = true;
		moves.add((int)(Math.random()*4));
		current_move = current_display = 0;
		play(current_display);
	}

	private void play(int i)
	{
		int currI = moves.elementAt(i);
		pieces[currI].setBackground(tile_colors[currI][1]);
		neutral = setEffects(neutral, "attack.wav");
		try {
			URL url = new File("timing" + mode_choice.getSelectedIndex()+".wav").toURI().toURL();
			if (timing.isOpen())
				timing.close();
			timing.open(AudioSystem.getAudioInputStream(url));
			timing.start();
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	private void processMove(int code)
	{
		for (int i = 0; i < 4; i++)
			pieces[i].setBackground(tile_colors[i][0]);
		if (moves.elementAt(current_move) == code)
		{
			current_move++;
			pieces[code].setBackground(tile_colors[code][1]);
			if (current_move == moves.size())
			{
				good = setEffects(good, "cheer.wav");
				current_move = 0;
				score.setText("Score: " + moves.size());
				moves.add((int)(Math.random()*4));
				current_display = -1;
				try {
					URL url = new File("timing0.wav").toURI().toURL();
					if (timing.isOpen())
						timing.close();
					timing.open(AudioSystem.getAudioInputStream(url));
					timing.start();
				} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
			}
			else
			{
				neutral = setEffects(neutral, "attack.wav");
			}
		}
		else
		{
			bad = setEffects(bad, "damaged.wav");
			gameOver();
		}
	}

	private void gameOver()
	{
		playing = false;
		game_over.setVisible(true);
	}

	private AudioInputStream setEffects(AudioInputStream s, String file)
	{
		try {
			URL url = new File(file).toURI().toURL();
			s = AudioSystem.getAudioInputStream(url);
			if (effects.isOpen())
				effects.close();
			if (!effects.isOpen())
				effects.open(s);
			effects.start();
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		return s;
	}

	private void setBackgroundMusic(int i, String s)
	{
		try{
			URL url = new File(s).toURI().toURL();
			background_streams[i] = AudioSystem.getAudioInputStream(url);
		}
		catch(Exception e) {

		}
	}
}