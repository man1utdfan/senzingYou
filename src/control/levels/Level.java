package control.levels;

import java.awt.geom.Rectangle2D;
import java.util.List;

import model.Game;
import model.entities.Entity;
import model.entities.HostileEntity;

public class Level
{
	protected Game game;

	public Level(Game game)
	{
		super();
		this.game = game;
	}
	
	public void update(double time)
	{
		for(Entity entity : game.getEntities())
		{
			entity.update(time);
		}
	}
}
