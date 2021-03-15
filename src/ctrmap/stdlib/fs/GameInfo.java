package ctrmap.stdlib.fs;

public interface GameInfo {

	public abstract Game getGame();
	public abstract SubGame getSubGame();

	public boolean isXY();

	public boolean isOA();

	public boolean isOADemo();

	public enum Game {
		XY(true),
		ORAS(true),
		ORAS_DEMO(false);
		
		public final boolean isStandalone;
		
		private Game(boolean isStandalone){
			this.isStandalone = isStandalone;
		}
	}
	
	public enum SubGame{
		X,
		Y,
		ALPHA,
		OMEGA,
		DEMO
	}

	public static class DefaultGameManager implements GameInfo {

		private Game game;
		private SubGame subGame;

		public DefaultGameManager(Game g, SubGame sg) {
			game = g;
			subGame = sg;
		}

		@Override
		public GameInfo.Game getGame() {
			return game;
		}

		@Override
		public boolean isXY() {
			return game == Game.XY;
		}

		@Override
		public boolean isOA() {
			return !isXY();
		}

		@Override
		public boolean isOADemo() {
			return game == Game.ORAS_DEMO;
		}

		@Override
		public SubGame getSubGame() {
			return subGame;
		}
	}
}
