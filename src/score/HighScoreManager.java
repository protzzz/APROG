package score;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class HighScoreManager
{
    public final String HIGH_SCORE_FILE_PATH = "data\\highscores.dat";
    public final int HIGH_SCORES_COUNT = 5;
    public final int MIN_NAME_LENGTH = 1, MAX_NAME_LENGTH = 30;

    private final String DATA_DELIMITER = "\\|"; // NOTE: The pipe symbol is a metacharacter in regex so we must escape it by using two backwards slashes
    private final String COMMENT_PREFIX = "--";

    private HighScore[] highScoresLeaderboard;

    public HighScoreManager()
    {
        clearHighScoresLeaderboard();
    }

    public void clearHighScoresLeaderboard()
    {
        highScoresLeaderboard = new HighScore[HIGH_SCORES_COUNT];

        for (int i = 0; i < HIGH_SCORES_COUNT; i++)
        {
            highScoresLeaderboard[i] = new HighScore("", 0);
        }
    }

    public String getDataDelimiter()
    {
        return DATA_DELIMITER.replace("\\", "");
    }

    public final HighScore[] getHighScoresLeaderboard()
    {
        return highScoresLeaderboard;
    }

    private String getFileHeaderComment()
    {
        return COMMENT_PREFIX + " This file stores the high scores for the Snake Game by Darian Benam.\n" + COMMENT_PREFIX + " MODIFYING THIS FILE CAN RESULT IN DATA CORRUPTION / UNEXPECTED PROGRAM BEHAVIOUR.\n\n";
    }

    public void loadHighScores() throws Exception
    {
        File highScoreFile = new File(HIGH_SCORE_FILE_PATH);
        Scanner fileReader = new Scanner(highScoreFile);

        int totalLinesRead = 0;
        String highScoreLine;
        String[] lineTokens;
        
        while (fileReader.hasNextLine() && totalLinesRead < HIGH_SCORES_COUNT)
        {
            highScoreLine = fileReader.nextLine();

            if (highScoreLine.isEmpty()
                || highScoreLine.length() >= COMMENT_PREFIX.length()
                && highScoreLine.substring(0, COMMENT_PREFIX.length()).equals(COMMENT_PREFIX))
            {
                continue;
            }

            lineTokens = highScoreLine.split(DATA_DELIMITER);

            highScoresLeaderboard[totalLinesRead].name = lineTokens[0];
            highScoresLeaderboard[totalLinesRead].score = Integer.parseInt(lineTokens[1]);

            totalLinesRead++;
        }

        fileReader.close();
    }

    public void saveHighScores() throws IOException
    {
        File highScoreFile = new File(HIGH_SCORE_FILE_PATH);
        FileWriter fileWriter = new FileWriter(highScoreFile);

        fileWriter.write(getFileHeaderComment());

        for (int i = 0; i < highScoresLeaderboard.length; i++)
        {
            fileWriter.write(highScoresLeaderboard[i].name + getDataDelimiter() + highScoresLeaderboard[i].score + (i == highScoresLeaderboard.length - 1 ? "" : "\n"));
        }

        fileWriter.close();
    }

    public int getHighScoreRank(int score)
    {
        for (int i = 0; i < highScoresLeaderboard.length; i++)
        {
            if (score >= highScoresLeaderboard[i].score)
            {
                return i + 1;
            }
        }

        return -1;
    }

    public boolean isValidName(String name)
    {
        return name != null && !name.isEmpty() && name.length() >= MIN_NAME_LENGTH && name.length() <= MAX_NAME_LENGTH && !name.contains(getDataDelimiter());
    }

    public void updateHighScore(int rank, String name, int score)
    {
        final int RANK_INDEX = rank - 1;

        if (RANK_INDEX < 0 || RANK_INDEX > HIGH_SCORES_COUNT)
        {
            throw new RuntimeException("Rank out of bounds.");
        }

        for (int i = highScoresLeaderboard.length - 1; i > RANK_INDEX; i--)
        {
            highScoresLeaderboard[i].name = highScoresLeaderboard[i - 1].name;
            highScoresLeaderboard[i].score = highScoresLeaderboard[i - 1].score;
        }

        highScoresLeaderboard[RANK_INDEX].name = name;
        highScoresLeaderboard[RANK_INDEX].score = score;
    }
}