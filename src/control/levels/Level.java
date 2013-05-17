package control.levels;

import model.Game;
import model.entities.Entity;

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
