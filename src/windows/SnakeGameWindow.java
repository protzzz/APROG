package windows;

import adapter.MenuAdapter;
import game.Direction;
import game.SnakeGameContainer;
import game.SnakeGameContainerListener;
import score.HighScoreManager;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class SnakeGameWindow extends JFrame implements SnakeGameContainerListener
{
    private static final String WINDOW_TITLE = "Snake";
    private final Color BG_COLOUR = new Color(0, 0, 0);
    private final int WINDOW_HEIGHT = 400, WINDOW_WIDTH = 400;

    private WindowKeyListener keyListener;
    private MainMenuListener menuListener;

    private JMenuBar menuBar;
    private JMenu fileMenu, helpMenu;
    private JMenuItem newGameMenuItem, highScoresMenuItem, closeMenuItem,aboutMenuItem;

    private SnakeGameContainer snakeGame;
    private HighScoreManager highScoreMngr;

    private class MainMenuListener extends MenuAdapter implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == newGameMenuItem)
            {
                int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to start a new game?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (dialogResult == JOptionPane.YES_OPTION)
                {
                    snakeGame.startNewGame();
                }
            }

            if (e.getSource() == highScoresMenuItem)
            {
                showHighScoreWindow();
            }

            if (e.getSource() == closeMenuItem)
            {
                System.exit(0);
            }

            if (e.getSource() == aboutMenuItem)
            {
                JOptionPane.showMessageDialog(null, "Snake\nBy: protbo00@upol.cz", "About", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        @Override
        public void menuSelected(MenuEvent e)
        {
            if (snakeGame.isGameStarted())
            {
                snakeGame.setPauseState(true);
            }
        }
    }

    private class WindowKeyListener extends KeyAdapter
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    snakeGame.setSnakeDirection(Direction.Up);
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    snakeGame.setSnakeDirection(Direction.Down);
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    snakeGame.setSnakeDirection(Direction.Left);
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    snakeGame.setSnakeDirection(Direction.Right);
                    break;
                case KeyEvent.VK_P:
                    if (snakeGame.isGameStarted())
                    {
                        snakeGame.setPauseState(!snakeGame.isGamePaused());
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (!snakeGame.isGameStarted() || snakeGame.isGameOver())
                    {
                        snakeGame.startGame();
                    }
                    break;
            }
        }
    }

    public SnakeGameWindow()
    {
        super();

        setupWindow();
    }

    private void setupWindow()
    {
        this.setBackground(BG_COLOUR);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setResizable(false);

        initListeners();
        initHighScoreManager();
        setupMenuBar();
        setupSnakeGameContainer();
        updateTitleWithScore();

        this.addKeyListener(keyListener);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initListeners()
    {
        keyListener = new WindowKeyListener();
        menuListener = new MainMenuListener();
    }

    private void initHighScoreManager()
    {
        highScoreMngr = new HighScoreManager();

        try
        {
            highScoreMngr.loadHighScores();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "An error occured while trying to load the high score file (" + highScoreMngr.HIGH_SCORE_FILE_PATH + ").\n\nError Message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupMenuBar()
    {
        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        newGameMenuItem = new JMenuItem("New Game");
        highScoresMenuItem = new JMenuItem("High Scores");
        closeMenuItem = new JMenuItem("Close");
        fileMenu.add(newGameMenuItem);
        fileMenu.add(highScoresMenuItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(closeMenuItem);

        helpMenu = new JMenu("Help");
        aboutMenuItem = new JMenuItem("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        fileMenu.addMenuListener(menuListener);
        helpMenu.addMenuListener(menuListener);

        newGameMenuItem.addActionListener(menuListener);
        highScoresMenuItem.addActionListener(menuListener);
        closeMenuItem.addActionListener(menuListener);
        aboutMenuItem.addActionListener(menuListener);

        this.setJMenuBar(menuBar);
    }

    private void setupSnakeGameContainer()
    {
        JPanel gameContainerPanel = new JPanel();
        gameContainerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gameContainerPanel.setBackground(BG_COLOUR);

        snakeGame = new SnakeGameContainer();
        gameContainerPanel.add(snakeGame);

        snakeGame.addEventListener(this);

        this.add(gameContainerPanel);
    }

    private void handleNewHighScore()
    {
        if (snakeGame.getScore() > 0)
        {
            int rank = highScoreMngr.getHighScoreRank(snakeGame.getScore());

            if (rank != -1)
            {
                String name;
                boolean cancelled = false;
                boolean invalidName = false;

                while (true)
                {
                    name = JOptionPane.showInputDialog(null, "You achieved a high score! Enter your name to be displayed on the high score board:", "Congratulations", JOptionPane.INFORMATION_MESSAGE);

                    if (name == null)
                    {
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to cancel? Your high score will not be saved.", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                        if (result == JOptionPane.YES_OPTION)
                        {
                            cancelled = true;

                            break;
                        }
                    }
                    else
                    {
                        if (highScoreMngr.isValidName(name))
                        {
                            break;
                        }
                        else
                        {
                            invalidName = true;
                        }
                    }

                    if (invalidName)
                    {
                        JOptionPane.showMessageDialog(null, "The name you entered is invalid. A valid name cannot contain \"" + highScoreMngr.getDataDelimiter() + "\" and also it must be between " + highScoreMngr.MIN_NAME_LENGTH + " and " + highScoreMngr.MAX_NAME_LENGTH + " characters in length.", "Error", JOptionPane.ERROR_MESSAGE);

                        invalidName = false;
                    }
                }

                if (!cancelled)
                {
                    highScoreMngr.updateHighScore(rank, name, snakeGame.getScore());

                    try
                    {
                        highScoreMngr.saveHighScores();

                        showHighScoreWindow();
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null, "An error occured while trying to save the high score file (" + highScoreMngr.HIGH_SCORE_FILE_PATH + ").\n\nError Message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void showHighScoreWindow()
    {
        HighScoresWindow hsWindow = new HighScoresWindow(this, highScoreMngr);
        hsWindow.setVisible(true);
    }

    private void updateTitleWithScore()
    {
        this.setTitle(WINDOW_TITLE + " | Score: " + snakeGame.getScore());
    }

    @Override
    public void onGameStarted()
    {
        updateTitleWithScore();
    }

    @Override
    public void onGameOver()
    {
        this.setTitle(WINDOW_TITLE + " | Game Over! Final Score: " + snakeGame.getScore());

        handleNewHighScore();
    }

    @Override
    public void onGameWon()
    {
        this.setTitle(WINDOW_TITLE + " | You win! Final Score: " + snakeGame.getScore());
    }

    @Override
    public void onScoreUpdated()
    {
        updateTitleWithScore();
    }
}
