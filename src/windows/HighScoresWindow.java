package windows;

import score.HighScore;
import score.HighScoreManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class HighScoresWindow extends JDialog
{
    private static final String WINDOW_TITLE = "High Scores";
    private final String EMPTY_NAME_VALUE = "-";

    private HighScoreManager highScoreMngr;
    private ButtonListener btnListener;

    private JButton okBtn, clearHighScoresBtn;
    private JLabel windowTitleLbl, rankTitleLbl, nameTitleLbl, scoreTitleLbl;
    private HighScoreRow[] highScoreRows;
    private JPanel highScoresPnl, buttonsPnl;

    private class ButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == okBtn)
            {
                dispose();
            }

            if (e.getSource() == clearHighScoresBtn)
            {
                int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the high scores? This action cannot be undone.", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
                if (result ==  JOptionPane.YES_OPTION)
                {
                    highScoreMngr.clearHighScoresLeaderboard();
                    updateHighScoreRows();

                    try
                    {
                        highScoreMngr.saveHighScores();
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null, "An error occured while trying to save the high score file (" + highScoreMngr.HIGH_SCORE_FILE_PATH + ").\n\nError Message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private class HighScoreRow
    {
        public JLabel rankLbl, nameLbl, scoreLbl;

        public HighScoreRow(Font parentFont, int rank, String name, int score)
        {
            final Font lblFont = new Font(parentFont.getFontName(), Font.PLAIN, parentFont.getSize());

            rankLbl = new JLabel(Integer.toString(rank));
            nameLbl = new JLabel(name);
            scoreLbl = new JLabel(Integer.toString(score));

            rankLbl.setFont(lblFont);
            nameLbl.setFont(lblFont);
            scoreLbl.setFont(lblFont);

            rankLbl.setHorizontalAlignment(JLabel.CENTER);
            nameLbl.setHorizontalAlignment(JLabel.CENTER);
            scoreLbl.setHorizontalAlignment(JLabel.CENTER);
        }
    }

    public HighScoresWindow(JFrame parentFrame, HighScoreManager highScoreMngr)
    {
        super(parentFrame, WINDOW_TITLE, true);

        this.highScoreMngr = highScoreMngr;

        setupWindow();
    }
    public void setupWindow()
    {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);

        setupWindowTitle();
        setupHighScorePnl(); 
        setupButtonsPnl();

        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void setupWindowTitle()
    {
        final Font titleFont = new Font(this.getFont().getFontName(), Font.BOLD, 24);

        windowTitleLbl = new JLabel("Top " + highScoreMngr.HIGH_SCORES_COUNT + " High Scores");
        windowTitleLbl.setFont(titleFont);
        windowTitleLbl.setBorder(new EmptyBorder(15, 15, 0, 15));

        this.add(windowTitleLbl, BorderLayout.NORTH);
    }

    private void setupHighScorePnl()
    {
        highScoresPnl = new JPanel();
        highScoresPnl.setLayout(new GridLayout(highScoreMngr.HIGH_SCORES_COUNT + 2, 4, 100, 10));
        highScoresPnl.setBorder(new EmptyBorder(15, 100, -15, 100));

        rankTitleLbl = new JLabel("Rank");
        nameTitleLbl = new JLabel("Name");
        scoreTitleLbl = new JLabel("Score");

        rankTitleLbl.setHorizontalAlignment(JLabel.CENTER);
        nameTitleLbl.setHorizontalAlignment(JLabel.CENTER);
        scoreTitleLbl.setHorizontalAlignment(JLabel.CENTER);

        highScoresPnl.add(rankTitleLbl);
        highScoresPnl.add(nameTitleLbl);
        highScoresPnl.add(scoreTitleLbl);

        updateHighScoreRows();

        this.add(highScoresPnl);
    }

    private void setupButtonsPnl()
    {
        btnListener = new ButtonListener();

        buttonsPnl = new JPanel();
        buttonsPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonsPnl.setBorder(new EmptyBorder(0, 15, 15, 15));

        okBtn = new JButton("Ok");
        clearHighScoresBtn = new JButton("Clear High Scores");

        okBtn.addActionListener(btnListener);
        clearHighScoresBtn.addActionListener(btnListener);

        buttonsPnl.add(okBtn);
        buttonsPnl.add(clearHighScoresBtn);

        this.add(buttonsPnl, BorderLayout.SOUTH);
    }

    private void updateHighScoreRows()
    {
        HighScore[] highScores = highScoreMngr.getHighScoresLeaderboard();

        if (highScoreRows == null)
        {
            highScoreRows = new HighScoreRow[highScoreMngr.HIGH_SCORES_COUNT];
        }

        for (int i = 0; i < highScoreRows.length; i++)
        {
            final String PLAYER_NAME = highScores[i].name.isEmpty() ? EMPTY_NAME_VALUE : highScores[i].name;

            if (highScoreRows[i] == null)
            {
                highScoreRows[i] = new HighScoreRow(this.getFont(),
                                                    i + 1,
                                                    PLAYER_NAME,
                                                    highScores[i].score);

                highScoresPnl.add(highScoreRows[i].rankLbl);
                highScoresPnl.add(highScoreRows[i].nameLbl);
                highScoresPnl.add(highScoreRows[i].scoreLbl);
            }
            else
            {
                highScoreRows[i].nameLbl.setText(PLAYER_NAME);
                highScoreRows[i].scoreLbl.setText(Integer.toString(highScores[i].score));
            }
        }
    }
}
