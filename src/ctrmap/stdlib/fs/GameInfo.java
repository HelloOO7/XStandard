package ctrmap.stdlib.fs;

public abstract class GameInfo {

	public abstract Game getGame();
	public abstract SubGame getSubGame();

	public boolean isXY(){
		return getGame() == Game.XY;
	}

	public boolean isOA(){
		return getGame() == Game.ORAS || getGame() == Game.ORAS_DEMO;
	}

	public boolean isOADemo(){
		return getGame() == Game.ORAS_DEMO;
	}
	
	public boolean isGenV(){
		return getGame() == Game.BW || getGame() == Game.BW2;
	}
	
	public boolean isBW2(){
		return getGame() == Game.BW2;
	}

	public enum Game {
		XY(true),
		ORAS(true),
		ORAS_DEMO(false),
		
		BW(true),
		BW2(true);
		
		public final boolean isStandalone;
		
		private Game(boolean isStandalone){
			this.isStandalone = isStandalone;
		}
	}
	
	public enum SubGame{
		X(Game.XY),
		Y(Game.XY),
		ALPHA(Game.ORAS),
		OMEGA(Game.ORAS),
		DEMO(Game.ORAS_DEMO),
		
		B(Game.BW),
		W(Game.BW),
		B2(Game.BW2),
		W2(Game.BW2);
		
		public final Game game;
		
		private SubGame(Game game){
			this.game = game;
		}
	}

	public static class DefaultGameManager extends GameInfo {

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
		public SubGame getSubGame() {
			return subGame;
		}
	}
}
