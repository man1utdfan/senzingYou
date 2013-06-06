package control;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import model.Camera;
import model.Drive;
import model.Game;
import model.Song;
import model.entities.Entity;
import model.levels.Level;
import model.levels.cave.CaveLevel;
import model.levels.desert.DesertLevel;
import model.levels.rainforest.RainforestLevel;
import model.levels.sky.SkyLevel;
import model.levels.underwater.UnderwaterLevel;

public class GameController implements ActionListener
{
	private Game game;
	private Drive drive;
	private int activeStage;
	private final int UPDATES_PER_SECOND = 30;

	public GameController(Game g)
	{
		this.game = g;
		

		// Initialize the hardware in a seperate thread because it takes a while...
		// while...
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				game.setLoading(true);
				Hardware.getInstance();
				game.setLoading(false);
			}

		})).start();

		(new Timer(1000 / UPDATES_PER_SECOND, this)).start();
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (game.getSong() != null)
					{
						if (!drive.isConnected())
						{
							game.getSong().stop();
							game.setSong(null);
							game.setLevel(null);
						}
					}

					else
					{
						List<Drive> justConnected = game.getJustConnectedDrives();
	
						if (justConnected.size() > 0)
						{
							game.setLoading(true);
							
							// Put all the songs into a list
							List<File> audioFiles = new ArrayList<File>();
		
							for (Drive d : justConnected)
							{
								audioFiles.addAll(d.getAudioFiles());
							}
		
							// Pick one
							if (audioFiles.size() > 0)
							{
								File file = audioFiles.get((new Random())
										.nextInt(audioFiles.size()));
		
								try
								{
									drive = justConnected.get(0);
									game.setSong(new Song(file));
									game.getSong().play();
									activeStage = -1;
								} catch (Exception ex)
								{
									ex.printStackTrace();
								}
							}
							
							game.setLoading(false);
						}
					}

					try
					{
						Thread.sleep(200);
					} catch (InterruptedException e)
					{
					}
				}
			}
		})).start();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (game.getSong() != null)
		{
			double lengthOfStage = game.getSong().getLength() / 5;
			int currentStage = (int) Math.floor(game.getSong().getTime() / lengthOfStage);

			if (currentStage != activeStage)
			{
				game.getEntities().clear();

				switch (currentStage)
				{
				case 0:
					game.setLevel(new UnderwaterLevel(game));
					break;

				case 1:
					try
					{
						Robot robot = new Robot();
						BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
						ImageIO.write(screenShot, "PNG", new File(drive.getPath()+"screenShot_"+System.currentTimeMillis()+".png"));
						game.setScreenCapture(screenShot); //We use this for the Highscore.
						
					}
					catch(Exception exc)
					{
						
					}
					
					game.setLevel(new RainforestLevel(game));
					break;

				case 2:
					game.setLevel(new CaveLevel(game));
					break;

				case 3:
					game.setLevel(new DesertLevel(game));
					break;

				case 4:
					game.setLevel(new SkyLevel(game));
					break;
				}

				activeStage = currentStage;

			}
		}

		Level level = game.getLevel();

		if (level != null)
		{
			level.update(1000 / UPDATES_PER_SECOND);
		} else
		{
			game.getEntities().clear();
		}

		for (Iterator<Entity> it = game.getEntities().iterator(); it.hasNext();)
		{
			Entity entity = it.next();

			if ((entity.getBounds().getMaxX() < -100 || entity.getBounds().getMaxY() < -100) || (entity.getBounds().getMinX() > (Camera.VIEW_WIDTH + 100) || entity.getBounds().getMinY() > (Camera.VIEW_HEIGHT + 100)))
			{
				it.remove();
			}
		}
	}
}
