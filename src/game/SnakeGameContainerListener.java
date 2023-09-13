package game;

public interface SnakeGameContainerListener {
    void onGameOver();

    void onGameStarted();

    void onGameWon();

    void onScoreUpdated();
}
