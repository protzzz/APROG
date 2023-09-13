package game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SnakeGameContainer extends JPanel
{
    private final Color BG_COLOUR = new Color(30, 30, 30);
    private final Color FOOD_COLOUR = new Color(255, 44, 88);
    private final Color TEXT_COLOUR = new Color(255, 255, 255);
    private final Direction INITIAL_SNAKE_DIR = Direction.Right;
    private final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 24);
    private final boolean WALL_COLLISION = true;
    private final int FOOD_POINTS_WORTH = 15;
    private final int GAME_LOOP_SLEEP_MS = 75;
    private final int SNAKE_DIMENSIONS = 10;
    private final int SNAKE_START_X = 3, SNAKE_START_Y = 1;
    private final int CONTAINER_HEIGHT = SNAKE_DIMENSIONS * 50, CONTAINER_WIDTH = SNAKE_DIMENSIONS * 75;

    private ArrayList<SnakeGameContainerListener> eventListenersList = new ArrayList<SnakeGameContainerListener>();
    private Direction nextSnakeDirection = INITIAL_SNAKE_DIR, snakeDirection = nextSnakeDirection;
    private Point foodLocation;
    private Snake snake;
    private boolean gamePaused = false, gameStarted = false, gameOver = false, gameWon = false, killLoopThread = false;
    private int score = 0;

    public SnakeGameContainer()
    {
        super(true);

        this.setBackground(BG_COLOUR);

        setupSnakeAndFood();
    }

    public int getScore()
    {
        return score;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(CONTAINER_WIDTH, CONTAINER_HEIGHT);
    }

    public boolean isGameOver()
    {
        return gameOver;
    }

    public boolean isGamePaused()
    {
        return gamePaused;
    }

    public boolean isGameStarted()
    {
        return gameStarted;
    }

    public void setPauseState(boolean pause)
    {
        gamePaused = pause;
    }

    public void setSnakeDirection(Direction dir)
    {
        if (getOppositeDirection(dir).equals(snakeDirection) || gamePaused)
        {
            return;
        }

        nextSnakeDirection = dir;
    }

    public void addEventListener(SnakeGameContainerListener scoreListener)
    {
        eventListenersList.add(scoreListener);
    }

    public void startGame()
    {
        if (gameOver)
        {
            setupSnakeAndFood();
        }

        resetScore();
        resetVariables();

        gameStarted = true;

        new Thread(() ->
        {
            gameLoop();
        }).start();

        for (SnakeGameContainerListener listener : eventListenersList)
        {
            listener.onGameStarted();
        }
    }

    public void startNewGame()
    {
        killLoopThread = true;

        setupSnakeAndFood();
        resetScore();
        resetVariables();

        this.repaint();
    }

    /**
     * Resets some global scope variables to their default values.
     */
    private void resetVariables()
    {
        nextSnakeDirection = INITIAL_SNAKE_DIR;
        snakeDirection = nextSnakeDirection;
        gameOver = false;
        gamePaused = false;
        gameStarted = false;
        gameWon = false;
        killLoopThread = false;
    }

    private void setupSnakeAndFood()
    {
        // Create a new snake with a length of three
        snake = new Snake(new Point(SNAKE_START_X, SNAKE_START_Y), WALL_COLLISION, SNAKE_DIMENSIONS, CONTAINER_HEIGHT, CONTAINER_WIDTH);
        snake.addBodyPart(Direction.Left);
        snake.addBodyPart(Direction.Left);

        generateFood();
    }

    private void gameLoop()
    {
        while (!killLoopThread && !gameOver && !gameWon)
        {
            if (!gamePaused)
            {
                try
                {
                    CollisionType collisionTypeAfterMoving = snake.move(snakeDirection);
                    if (collisionTypeAfterMoving != CollisionType.None) // Either collided with a wall (if there are no walls) or one of its body parts
                    {
                        gameOver();

                        break;
                    }

                    handleFoodCollision();

                    snakeDirection = nextSnakeDirection;

                    Thread.sleep(GAME_LOOP_SLEEP_MS);
                }
                catch (InterruptedException ex)
                {
                    System.out.println("Exception thrown in game loop: " + ex.toString());
                }
            }

            this.repaint();
        }
    }

    private void handleFoodCollision()
    {
        if (snake.getBodyPartsList().get(0).equals(foodLocation))
        {
            addPointsToScore();

            snake.addBodyPart(snake.getTailLastLocation());

            generateFood();
        }
    }

    private void generateFood()
    {
        ArrayList<Point> map = getEmptyMapPoints();

        if (map.size() == 0)
        {
            winGame();
        }
        else
        {
            int randIndex = (int)(Math.random() * map.size());
            foodLocation = map.get(randIndex);
        }
    }

    private ArrayList<Point> getEmptyMapPoints()
    {
        ArrayList<Point> result = new ArrayList<Point>();

        Point mapPoint;
        for (int row = 0; row < CONTAINER_HEIGHT / SNAKE_DIMENSIONS; row++)
        {
            for (int col = 0; col < CONTAINER_WIDTH/ SNAKE_DIMENSIONS; col++)
            {
                mapPoint = new Point(col, row);

                if (!snake.getBodyPartsList().contains(mapPoint))
                {
                    result.add(mapPoint);
                }
            }
        }

        return result;
    }

    private Direction getOppositeDirection(Direction dir)
    {
        Direction oppDir;

        if (dir == Direction.Down)
        {
            oppDir = Direction.Up;
        }
        else if (dir == Direction.Right)
        {
            oppDir = Direction.Left;
        }
        else if (dir == Direction.Left)
        {
            oppDir = Direction.Right;
        }
        else
        {
            oppDir = Direction.Down;
        }

        return oppDir;
    }

    private void winGame()
    {
        gameStarted = false;
        gameOver = true;
        gameWon = true;

        for (SnakeGameContainerListener listener : eventListenersList)
        {
            listener.onGameWon();
        }
    }

    private void gameOver()
    {
        gameOver = true;
        gameStarted = false;

        for (SnakeGameContainerListener listener : eventListenersList)
        {
            listener.onGameOver();
        }
    }

    private void addPointsToScore()
    {
        score += FOOD_POINTS_WORTH;

        for (SnakeGameContainerListener listener : eventListenersList)
        {
            listener.onScoreUpdated();
        }
    }

    private void resetScore()
    {
        score = 0;

        for (SnakeGameContainerListener listener : eventListenersList)
        {
            listener.onScoreUpdated();
        }
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font)
    {
        FontMetrics metrics = g.getFontMetrics(font);

        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(FOOD_COLOUR);
        g2d.fillRect(foodLocation.x * SNAKE_DIMENSIONS, foodLocation.y * SNAKE_DIMENSIONS, SNAKE_DIMENSIONS, SNAKE_DIMENSIONS);

        g2d.setColor(Snake.SNAKE_COLOUR);
        for (int i = 0; i < snake.getBodyPartsList().size(); i++)
        {
            Point bodyPartLoc = snake.getBodyPartsList().get(i);

            g2d.fillRect(bodyPartLoc.x * SNAKE_DIMENSIONS, bodyPartLoc.y * SNAKE_DIMENSIONS, SNAKE_DIMENSIONS, SNAKE_DIMENSIONS);
        }

        g2d.setColor(TEXT_COLOUR);

        if (gameWon)
        {
            drawCenteredString(g2d, "You win!", this.getBounds(), TEXT_FONT);
        }
        else if (gameOver && !gameStarted)
        {
            drawCenteredString(g2d, "Game over! Press the Spacebar to start a new game!", this.getBounds(), TEXT_FONT);
        }
        else if (!gameStarted)
        {
            drawCenteredString(g2d, "Press the Spacebar to start the game!", this.getBounds(), TEXT_FONT);
        }
        else if (gamePaused)
        {
            drawCenteredString(g2d, "Game paused. Press P to unpause.", this.getBounds(), TEXT_FONT);
        }
    }
}
